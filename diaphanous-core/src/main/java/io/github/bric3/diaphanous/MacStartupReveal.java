/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.PaintEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Shows an AWT window while minimizing the initial white-frame flash on macOS.
 * <p>
 * Why this utility exists:
 * <p>
 * During startup, a decorated Swing/AWT window can briefly present a default compositor frame
 * before native styling/backdrop setup is fully visible. This helper centralizes the startup
 * reveal sequence so callers do not duplicate fragile ordering code.
 * <p>
 * Startup sequence:
 * <ol>
 *   <li>Ensure peer creation via {@link Window#addNotify()} when needed.</li>
 *   <li>Hide the native window with alpha 0.</li>
 *   <li>Show the window.</li>
 *   <li>Restore alpha to 1 on first paint (with event-loop fallback).</li>
 * </ol>
 * <p>
 * Used platform artifacts:
 * <ul>
 *   <li>AWT window lifecycle: {@link Window#addNotify()}</li>
 *   <li>AWT visibility and fallback opacity: {@link Window#setVisible(boolean)}, {@link Window#setOpacity(float)}</li>
 *   <li>AWT event queue scheduling: {@link EventQueue#invokeLater(Runnable)}</li>
 *   <li>AWT paint event observation: {@link Toolkit#addAWTEventListener(AWTEventListener, long)}</li>
 *   <li>macOS native alpha bridge: {@link MacWindowBackdrop#setWindowAlpha(Window, double)}</li>
 * </ul>
 * <p>
 * References:
 * <ul>
 *   <li><a href="https://docs.oracle.com/en/java/javase/25/docs/api/java.desktop/java/awt/Window.html">JDK Window API</a></li>
 *   <li><a href="https://docs.oracle.com/en/java/javase/25/docs/api/java.desktop/java/awt/EventQueue.html">JDK EventQueue API</a></li>
 *   <li><a href="https://developer.apple.com/documentation/appkit/nswindow/1419167-alphavalue">Apple NSWindow.alphaValue</a></li>
 * </ul>
 */
public final class MacStartupReveal {
    private static final long MIN_HIDDEN_MILLIS = Long.getLong("diaphanous.startupReveal.minHiddenMillis", 120L);
    private static final long FORCE_REVEAL_MILLIS = Long.getLong("diaphanous.startupReveal.forceRevealMillis", 650L);

    private MacStartupReveal() {
    }

    /**
     * Shows the window and reveals it on the next AWT event-loop turn.
     *
     * @param window target window
     */
    public static void show(Window window) {
        if (!window.isDisplayable()) {
            window.addNotify();
        }

        boolean revealWithOpacity = false;
        try {
            if (MacWindowBackdrop.isSupported()) {
                MacWindowBackdrop.setWindowAlpha(window, 0.0);
                revealWithOpacity = true;
            } else {
                window.setOpacity(0.0f);
                revealWithOpacity = true;
            }
        } catch (IllegalComponentStateException | UnsupportedOperationException ignored) {
            revealWithOpacity = false;
        }

        window.setVisible(true);
        if (!revealWithOpacity) {
            return;
        }

        revealOnFirstPaint(window);
    }

    private static void revealOnFirstPaint(Window window) {
        AtomicBoolean revealed = new AtomicBoolean(false);
        AtomicBoolean painted = new AtomicBoolean(false);
        AtomicLong shownAtMillis = new AtomicLong(System.currentTimeMillis());
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Timer timer = new Timer("diaphanous-startup-reveal", true);

        AWTEventListener[] holder = new AWTEventListener[1];
        holder[0] = event -> {
            if (!(event instanceof PaintEvent)) {
                return;
            }
            Object source = event.getSource();
            if (!(source instanceof Component component)) {
                return;
            }
            if (windowOf(component) != window) {
                return;
            }
            painted.set(true);
            tryReveal(window, toolkit, holder[0], timer, revealed, painted, shownAtMillis.get());
        };

        toolkit.addAWTEventListener(holder[0], AWTEvent.PAINT_EVENT_MASK);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(
                    () -> tryReveal(window, toolkit, holder[0], timer, revealed, painted, shownAtMillis.get())
                );
            }
        }, 16L, 16L);
    }

    private static Window windowOf(Component component) {
        Component cursor = component;
        while (cursor != null) {
            if (cursor instanceof Window window) {
                return window;
            }
            Container parent = cursor.getParent();
            cursor = parent;
        }
        return null;
    }

    private static void reveal(Window window) {
        try {
            if (MacWindowBackdrop.isSupported()) {
                MacWindowBackdrop.setWindowAlpha(window, 1.0);
            } else {
                window.setOpacity(1.0f);
            }
        } catch (RuntimeException ignored) {
            // Ignore reveal errors; the window is already visible.
        }
    }

    private static void tryReveal(
        Window window,
        Toolkit toolkit,
        AWTEventListener listener,
        Timer timer,
        AtomicBoolean revealed,
        AtomicBoolean painted,
        long shownAtMillis
    ) {
        if (revealed.get()) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean afterMinHidden = now - shownAtMillis >= MIN_HIDDEN_MILLIS;
        boolean forceReveal = now - shownAtMillis >= FORCE_REVEAL_MILLIS;
        if ((!painted.get() || !afterMinHidden) && !forceReveal) {
            return;
        }
        if (revealed.compareAndSet(false, true)) {
            toolkit.removeAWTEventListener(listener);
            timer.cancel();
            reveal(window);
        }
    }
}
