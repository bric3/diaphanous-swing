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

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;

/**
 * Transparent root pane that can erase Java-side background fill when the native
 * macOS backdrop pipeline is enabled.
 *
 * This helps avoid painting an opaque Swing background over the native effect.
 */
public final class RootErasingContentPane extends JPanel {

    /**
     * Creates a root pane with the specified layout.
     *
     * @param layout the layout manager to use
     */
    public RootErasingContentPane(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!BackdropSupport.clearBackgroundIfEnabled(g, this)) {
            super.paintComponent(g);
        }
    }
}
