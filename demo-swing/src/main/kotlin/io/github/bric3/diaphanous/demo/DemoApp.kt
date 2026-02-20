/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.demo

import io.github.bric3.diaphanous.MacToolbarStyle
import io.github.bric3.diaphanous.MacWindowStyle
import io.github.bric3.diaphanous.MacWindowStyler
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridBagLayout
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * Manual demo for toggling native macOS style attributes on a Swing frame.
 */
object DemoApp {
    private const val APPLY_BUTTON_TEXT = "Apply macOS style"
    private const val REVERT_BUTTON_TEXT = "Revert macOS style"

    private val styledWindowStyle: MacWindowStyle = MacWindowStyle.builder()
        .transparentTitleBar(true)
        .fullSizeContentView(true)
        .titleVisible(false)
        .toolbarStyle(MacToolbarStyle.UNIFIED_COMPACT)
        .build()

    private val standardWindowStyle: MacWindowStyle = MacWindowStyle.builder()
        .transparentTitleBar(false)
        .fullSizeContentView(false)
        .titleVisible(true)
        .toolbarStyle(MacToolbarStyle.AUTOMATIC)
        .build()

    /**
     * Starts the demo UI.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater(::showWindow)
    }

    private fun showWindow() {
        val frame = JFrame("Diaphanous Swing Demo")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(980, 640)
        frame.setLocationRelativeTo(null)

        val title = JLabel("Native macOS title bar restyling for this Swing window", JLabel.CENTER)
        title.font = Font("SF Pro Text", Font.BOLD, 22)

        val details = JLabel(
            """
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
            """.trimIndent()
        )

        val applyStyleButton = JButton(APPLY_BUTTON_TEXT)
        val styledApplied = AtomicBoolean(false)

        applyStyleButton.addActionListener {
            val style = if (styledApplied.get()) standardWindowStyle else styledWindowStyle
            MacWindowStyler.apply(frame, style)
            val nowStyled = !styledApplied.get()
            styledApplied.set(nowStyled)
            applyStyleButton.text = if (nowStyled) REVERT_BUTTON_TEXT else APPLY_BUTTON_TEXT
        }

        val centerPanel = JPanel(BorderLayout(0, 12))
        centerPanel.add(title, BorderLayout.NORTH)
        centerPanel.add(details, BorderLayout.CENTER)

        val panel = JPanel(BorderLayout(16, 16))
        val centered = JPanel(GridBagLayout())
        centered.add(centerPanel)
        panel.add(centered, BorderLayout.CENTER)
        panel.add(applyStyleButton, BorderLayout.SOUTH)

        frame.contentPane = panel
        frame.isVisible = true
    }
}
