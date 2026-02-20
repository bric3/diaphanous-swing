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

import io.github.bric3.diaphanous.MacVibrancyMaterial
import io.github.bric3.diaphanous.MacVibrancyStyle
import io.github.bric3.diaphanous.MacWindowStyler
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingUtilities

/**
 * Manual demo for toggling native macOS style attributes on a Swing frame.
 */
object DemoApp {
    private const val DEFAULT_BACKDROP_ALPHA = 0.55
    private const val DEFAULT_BLUR_STRENGTH = 55

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
        frame.isUndecorated = true
        frame.setSize(980, 640)
        frame.setLocationRelativeTo(null)
        frame.background = Color(0, 0, 0, 0)
        frame.rootPane.isOpaque = false
        frame.layeredPane.isOpaque = false

        val title = JLabel("Native macOS experimental backdrop for this Swing window", JLabel.CENTER)
        title.font = Font("SF Pro Text", Font.BOLD, 22)

        val details = JLabel(
            """
            <html>
            <div style='font-size: 15px; text-align: left;'>
              This demo starts with an experimental native backdrop layer enabled:
              <ul>
                <li>install an NSVisualEffectView backdrop behind content</li>
                <li>attempt native backdrop rendering (currently experimental)</li>
                <li>allow live alpha and blur-strength tuning via sliders</li>
                <li>keep window in undecorated transparent mode</li>
              </ul>
            </div>
            </html>
            """.trimIndent()
        )

        val dragHandle = JLabel("Drag here to move window", JLabel.CENTER)
        dragHandle.foreground = Color(230, 230, 230, 190)
        dragHandle.font = Font("SF Pro Text", Font.PLAIN, 12)

        val dragListener = object : MouseAdapter() {
            private var clickPoint: Point? = null

            override fun mousePressed(e: MouseEvent) {
                clickPoint = e.point
            }

            override fun mouseDragged(e: MouseEvent) {
                val point = clickPoint ?: return
                val location = frame.location
                frame.setLocation(
                    location.x + e.x - point.x,
                    location.y + e.y - point.y
                )
            }
        }
        dragHandle.addMouseListener(dragListener)
        dragHandle.addMouseMotionListener(dragListener)

        val alphaValue = JLabel("0.55")
        alphaValue.foreground = Color(230, 230, 230, 190)
        val alphaLabel = JLabel("Backdrop alpha")
        val alphaSlider = JSlider(0, 100, (DEFAULT_BACKDROP_ALPHA * 100.0).toInt())
        alphaSlider.isOpaque = false
        val blurValue = JLabel("$DEFAULT_BLUR_STRENGTH")
        blurValue.foreground = Color(230, 230, 230, 190)
        val blurLabel = JLabel("Blur strength")
        val blurSlider = JSlider(0, 100, DEFAULT_BLUR_STRENGTH)
        blurSlider.isOpaque = false

        fun materialForBlurStrength(value: Int): MacVibrancyMaterial = when {
            value < 20 -> MacVibrancyMaterial.CONTENT_BACKGROUND
            value < 40 -> MacVibrancyMaterial.WINDOW_BACKGROUND
            value < 60 -> MacVibrancyMaterial.SIDEBAR
            value < 80 -> MacVibrancyMaterial.MENU
            else -> MacVibrancyMaterial.HUD_WINDOW
        }

        fun currentStyle(): MacVibrancyStyle = MacVibrancyStyle.builder()
            .material(materialForBlurStrength(blurSlider.value))
            .backdropAlpha(alphaSlider.value / 100.0)
            .build()

        alphaSlider.addChangeListener {
            val alpha = alphaSlider.value / 100.0
            alphaValue.text = "%.2f".format(alpha)
            MacWindowStyler.applyVibrancy(frame, currentStyle())
        }
        blurSlider.addChangeListener {
            blurValue.text = blurSlider.value.toString()
            MacWindowStyler.applyVibrancy(frame, currentStyle())
        }

        val centerPanel = JPanel(BorderLayout(0, 12))
        centerPanel.isOpaque = false
        centerPanel.add(title, BorderLayout.NORTH)
        val mainContentPanel = JPanel(BorderLayout(0, 16))
        mainContentPanel.isOpaque = false
        mainContentPanel.add(details, BorderLayout.CENTER)

        val controlsPanel = JPanel(GridBagLayout())
        controlsPanel.isOpaque = false
        val gbc = GridBagConstraints()
        gbc.insets = Insets(4, 8, 4, 8)
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.gridy = 0
        gbc.gridx = 0
        gbc.gridwidth = 1
        controlsPanel.add(alphaLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(alphaValue, gbc)

        gbc.gridy = 1
        gbc.gridx = 0
        gbc.weightx = 1.0
        gbc.gridwidth = 2
        controlsPanel.add(alphaSlider, gbc)

        gbc.gridy = 2
        gbc.gridx = 0
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        controlsPanel.add(blurLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(blurValue, gbc)

        gbc.gridy = 3
        gbc.gridx = 0
        gbc.weightx = 1.0
        gbc.gridwidth = 2
        controlsPanel.add(blurSlider, gbc)

        mainContentPanel.add(controlsPanel, BorderLayout.SOUTH)
        centerPanel.add(mainContentPanel, BorderLayout.CENTER)

        val panel = JPanel(BorderLayout(16, 16))
        panel.isOpaque = false
        panel.background = Color(0, 0, 0, 0)
        val centered = JPanel(GridBagLayout())
        centered.isOpaque = false
        centered.add(centerPanel)
        panel.add(dragHandle, BorderLayout.NORTH)
        panel.add(centered, BorderLayout.CENTER)

        frame.contentPane = panel
        frame.isVisible = true
        MacWindowStyler.applyVibrancy(frame, currentStyle())
    }
}
