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

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Swing-side support for macOS backdrop rendering in decorated Swing/AWT windows.
 * <p>
 * Raison d'etre: on modern macOS JDKs, AWT content is presented through a Metal-backed host
 * ({@code MTLLayer}) and Java2D output is blitted into that layer. Even when an
 * {@code NSVisualEffectView} exists behind AWT content, normal Java background painting can still
 * cover the full surface and hide vibrancy.
 * <p>
 * This helper enables an explicit clear/erase paint pass on selected Swing containers so the native
 * backdrop can remain visible where intended.
 */
public final class MacBackdropSupport {
    private static final String BACKDROP_ERASE_ENABLED_KEY = "diaphanous.backdropEraseEnabled";

    private MacBackdropSupport() {
    }

    /**
     * Enables or disables backdrop erase on the window root pane based on platform, decoration and appearance.
     *
     * @param window the target window
     * @param appearance the selected macOS appearance
     */
    public static void configure(Window window, MacWindowAppearance appearance) {
        boolean enabled = shouldEnable(window, appearance);
        configure(window, enabled);
    }

    /**
     * Enables or disables backdrop erase on the window root pane.
     *
     * @param window the target window
     * @param enabled whether erase is enabled
     */
    public static void configure(Window window, boolean enabled) {
        if (!(window instanceof RootPaneContainer container)) {
            return;
        }
        JRootPane rootPane = container.getRootPane();
        if (rootPane == null) {
            return;
        }
        rootPane.putClientProperty(BACKDROP_ERASE_ENABLED_KEY, enabled);
        rootPane.repaint();
    }

    /**
     * Returns {@code true} when backdrop erase should be enabled for the given window and appearance.
     *
     * @param window the target window
     * @param appearance selected appearance
     * @return {@code true} if backdrop erase should be active
     *
     * <p>Current policy:
     * enabled for decorated macOS windows using {@code SYSTEM}/{@code VIBRANT_*} appearance,
     * disabled otherwise.
     */
    public static boolean shouldEnable(Window window, MacWindowAppearance appearance) {
        if (!MacWindowStyler.isSupported() || window == null || appearance == null) {
            return false;
        }
        if (window instanceof java.awt.Frame frame && frame.isUndecorated()) {
            return false;
        }
        return appearance == MacWindowAppearance.SYSTEM
            || appearance == MacWindowAppearance.VIBRANT_LIGHT
            || appearance == MacWindowAppearance.VIBRANT_DARK;
    }

    /**
     * Returns whether backdrop erase is enabled for the specified component's root pane.
     *
     * @param component a Swing component
     * @return {@code true} when erase is enabled on the component root
     */
    public static boolean isEnabledFor(JComponent component) {
        if (component == null) {
            return false;
        }
        JRootPane root = SwingUtilities.getRootPane(component);
        if (root == null) {
            return false;
        }
        return Boolean.TRUE.equals(root.getClientProperty(BACKDROP_ERASE_ENABLED_KEY));
    }

    /**
     * Clears the component paint background when backdrop erase is enabled.
     *
     * @param g current graphics
     * @param component current component
     * @return {@code true} if a clear pass was applied
     *
     * <p>This uses {@link AlphaComposite#Clear} to remove already-rendered pixels in component bounds.
     */
    public static boolean clearBackgroundIfEnabled(Graphics g, JComponent component) {
        if (!isEnabledFor(component)) {
            return false;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, component.getWidth(), component.getHeight());
        g2.dispose();
        return true;
    }
}
