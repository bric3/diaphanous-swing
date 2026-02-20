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

import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import java.awt.Window;

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
 *   <li>Restore alpha to 1 on the next AWT event-loop turn.</li>
 * </ol>
 * <p>
 * Used platform artifacts:
 * <ul>
 *   <li>AWT window lifecycle: {@link Window#addNotify()}</li>
 *   <li>AWT visibility and fallback opacity: {@link Window#setVisible(boolean)}, {@link Window#setOpacity(float)}</li>
 *   <li>AWT event queue scheduling: {@link EventQueue#invokeLater(Runnable)}</li>
 *   <li>macOS native alpha bridge: {@link MacWindowStyler#setWindowAlpha(Window, double)}</li>
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
            if (MacWindowStyler.isSupported()) {
                MacWindowStyler.setWindowAlpha(window, 0.0);
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

        EventQueue.invokeLater(() -> {
            if (MacWindowStyler.isSupported()) {
                MacWindowStyler.setWindowAlpha(window, 1.0);
            } else {
                window.setOpacity(1.0f);
            }
        });
    }
}
