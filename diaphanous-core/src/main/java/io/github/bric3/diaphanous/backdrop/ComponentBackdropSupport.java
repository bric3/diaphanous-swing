/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.backdrop;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Swing-side support for native backdrop rendering in decorated Swing/AWT windows.
 * <p>
 * This helper enables an explicit clear/erase paint pass on selected Swing containers so
 * the native backdrop can remain visible where intended.
 * <p>
 * Current behavior is implemented for the macOS backend.
 */
public final class ComponentBackdropSupport {
    private static final String BACKDROP_ERASE_ENABLED_KEY = "diaphanous.backdropEraseEnabled";

    private ComponentBackdropSupport() {
    }

    static void setEraseEnabled(Window window, boolean enabled) {
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
