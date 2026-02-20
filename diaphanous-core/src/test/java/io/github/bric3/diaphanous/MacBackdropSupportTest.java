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

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacBackdropSupportTest {
    @Test
    void isEnabledForReadsRootPaneClientProperty() {
        JPanel panel = new JPanel();
        JRootPane rootPane = new JRootPane();
        rootPane.setContentPane(panel);

        assertFalse(MacBackdropSupport.isEnabledFor(panel));

        rootPane.putClientProperty("diaphanous.backdropEraseEnabled", Boolean.TRUE);
        assertTrue(MacBackdropSupport.isEnabledFor(panel));
    }

    @Test
    void clearBackgroundIfEnabledClearsPixels() {
        JPanel panel = new JPanel();
        panel.setSize(8, 8);
        JRootPane rootPane = new JRootPane();
        rootPane.setContentPane(panel);
        rootPane.putClientProperty("diaphanous.backdropEraseEnabled", Boolean.TRUE);

        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(255, 0, 0, 255));
        g2.fillRect(0, 0, 8, 8);

        boolean applied = MacBackdropSupport.clearBackgroundIfEnabled(g2, panel);
        g2.dispose();

        assertTrue(applied);
        int alpha = (image.getRGB(0, 0) >>> 24) & 0xFF;
        assertTrue(alpha == 0, "Expected cleared alpha, got " + alpha);
    }
}

