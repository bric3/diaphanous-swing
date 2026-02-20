/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.demo;

import io.github.bric3.diaphanous.MacToolbarStyle;
import io.github.bric3.diaphanous.MacWindowStyle;
import io.github.bric3.diaphanous.MacWindowStyler;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manual demo for toggling native macOS style attributes on a Swing frame.
 */
public final class DemoApp {
    private static final String APPLY_BUTTON_TEXT = "Apply macOS style";
    private static final String REVERT_BUTTON_TEXT = "Revert macOS style";
    private static final MacWindowStyle STYLED_WINDOW_STYLE = MacWindowStyle.builder()
        .transparentTitleBar(true)
        .fullSizeContentView(true)
        .titleVisible(false)
        .toolbarStyle(MacToolbarStyle.UNIFIED_COMPACT)
        .build();
    private static final MacWindowStyle STANDARD_WINDOW_STYLE = MacWindowStyle.builder()
        .transparentTitleBar(false)
        .fullSizeContentView(false)
        .titleVisible(true)
        .toolbarStyle(MacToolbarStyle.AUTOMATIC)
        .build();

    private DemoApp() {
    }

    /**
     * Starts the demo UI.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DemoApp::showWindow);
    }

    private static void showWindow() {
        JFrame frame = new JFrame("Diaphanous Swing Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 640);
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("Native macOS title bar restyling for this Swing window", JLabel.CENTER);
        title.setFont(new Font("SF Pro Text", Font.BOLD, 22));

        JLabel details = new JLabel("""
            <html>
            <div style='font-size: 15px; text-align: left;'>
              Clicking <b>Apply macOS style</b> will:
              <ul>
                <li>make the title bar transparent</li>
                <li>extend content into the title bar area (full-size content view)</li>
                <li>hide the text title</li>
                <li>switch to unified compact toolbar style</li>
                <li>click again to revert to a standard title bar</li>
              </ul>
            </div>
            </html>
            """);

        JButton applyStyleButton = new JButton(APPLY_BUTTON_TEXT);
        AtomicBoolean styledApplied = new AtomicBoolean(false);

        applyStyleButton.addActionListener(e -> {
            MacWindowStyle style = styledApplied.get() ? STANDARD_WINDOW_STYLE : STYLED_WINDOW_STYLE;
            MacWindowStyler.apply(frame, style);
            boolean nowStyled = !styledApplied.get();
            styledApplied.set(nowStyled);
            applyStyleButton.setText(nowStyled ? REVERT_BUTTON_TEXT : APPLY_BUTTON_TEXT);
        });

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.add(title, BorderLayout.NORTH);
        centerPanel.add(details, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(16, 16));
        JPanel centered = new JPanel(new GridBagLayout());
        centered.add(centerPanel);
        panel.add(centered, BorderLayout.CENTER);
        panel.add(applyStyleButton, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }
}
