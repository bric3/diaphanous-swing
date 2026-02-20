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
import io.github.bric3.diaphanous.MacToolbarStyle
import io.github.bric3.diaphanous.MacWindowStyle
import io.github.bric3.diaphanous.MacWindowAppearance
import io.github.bric3.diaphanous.MacWindowStyler
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.GeneralPath
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingUtilities
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JButton
import javax.swing.JColorChooser
import javax.swing.BorderFactory
import javax.swing.JComponent
import kotlin.random.Random

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
        val mode = parseMode(args)
        SwingUtilities.invokeLater { showWindow(mode) }
    }

    private fun parseMode(args: Array<String>): WindowMode {
        return when (args.firstOrNull()?.lowercase()) {
            "undecorated", "--undecorated" -> WindowMode.UNDECORATED
            "decorated", "--decorated", null -> WindowMode.DECORATED
            else -> WindowMode.DECORATED
        }
    }

    private fun showWindow(mode: WindowMode) {
        val frame = JFrame("Diaphanous Swing Demo")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val undecorated = mode == WindowMode.UNDECORATED
        frame.isUndecorated = undecorated
        frame.setSize(980, 640)
        frame.setLocationRelativeTo(null)
        if (undecorated) {
            frame.background = Color(0, 0, 0, 0)
        }
        frame.rootPane.isOpaque = false
        frame.layeredPane.isOpaque = false

        val title = JLabel("Native macOS experimental backdrop for this Swing window", JLabel.CENTER)
        title.font = Font("SF Pro Text", Font.BOLD, 22)

        val topSeriesPanel = RandomTimeseriesPanel()

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
        if (undecorated) {
            topSeriesPanel.addMouseListener(dragListener)
            topSeriesPanel.addMouseMotionListener(dragListener)
        }

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
        val undecoratedInfo = JLabel(
            "Undecorated mode: alpha/blur controls apply experimental NSVisualEffectView backdrop",
            JLabel.LEFT
        )
        undecoratedInfo.foreground = Color(220, 220, 220, 210)

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

        fun decoratedStyle(): MacVibrancyStyle = MacVibrancyStyle.builder()
            .material(MacVibrancyMaterial.UNDER_WINDOW_BACKGROUND)
            .backdropAlpha(1.0)
            .build()

        val centerPanel = JPanel(BorderLayout(0, 0))
        centerPanel.isOpaque = false
        centerPanel.add(title, BorderLayout.NORTH)
        val mainContentPanel = JPanel(BorderLayout(0, 16))
        mainContentPanel.isOpaque = false

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

        val stylePanel = JPanel(GridBagLayout())
        stylePanel.isOpaque = false
        val styleGbc = GridBagConstraints()
        styleGbc.insets = Insets(4, 8, 4, 8)
        styleGbc.fill = GridBagConstraints.HORIZONTAL

        val transparentTitleBarCheck = JCheckBox("Transparent title bar", true)
        transparentTitleBarCheck.name = "transparentTitleBarCheck"
        transparentTitleBarCheck.isOpaque = false
        val fullSizeContentCheck = JCheckBox("Full-size content view", true)
        fullSizeContentCheck.name = "fullSizeContentCheck"
        fullSizeContentCheck.isOpaque = false
        val titleVisibleCheck = JCheckBox("Title visible", false)
        titleVisibleCheck.name = "titleVisibleCheck"
        titleVisibleCheck.isOpaque = false
        val toolbarStyleCombo = JComboBox(MacToolbarStyle.entries.toTypedArray())
        toolbarStyleCombo.name = "toolbarStyleCombo"
        toolbarStyleCombo.selectedItem = MacToolbarStyle.UNIFIED_COMPACT
        val appearanceCombo = JComboBox(MacWindowAppearance.entries.toTypedArray())
        appearanceCombo.name = "appearanceCombo"
        appearanceCombo.selectedItem = MacWindowAppearance.SYSTEM

        fun applyWindowStyleFromControls() {
            val style = MacWindowStyle.builder()
                .transparentTitleBar(transparentTitleBarCheck.isSelected)
                .fullSizeContentView(fullSizeContentCheck.isSelected)
                .titleVisible(titleVisibleCheck.isSelected)
                .toolbarStyle(toolbarStyleCombo.selectedItem as MacToolbarStyle)
                .build()
            MacWindowStyler.apply(frame, style)
            if (!undecorated) {
                MacWindowStyler.applyVibrancy(frame, decoratedStyle())
            }
        }
        transparentTitleBarCheck.addActionListener { applyWindowStyleFromControls() }
        fullSizeContentCheck.addActionListener { applyWindowStyleFromControls() }
        titleVisibleCheck.addActionListener { applyWindowStyleFromControls() }
        toolbarStyleCombo.addActionListener { applyWindowStyleFromControls() }
        appearanceCombo.addActionListener {
            MacWindowStyler.applyAppearance(frame, appearanceCombo.selectedItem as MacWindowAppearance)
        }

        alphaSlider.addChangeListener {
            val alpha = alphaSlider.value / 100.0
            alphaValue.text = "%.2f".format(alpha)
            if (undecorated) {
                MacWindowStyler.applyVibrancy(frame, currentStyle())
            }
        }
        blurSlider.addChangeListener {
            blurValue.text = blurSlider.value.toString()
            if (undecorated) {
                MacWindowStyler.applyVibrancy(frame, currentStyle())
            }
        }

        styleGbc.gridy = 0
        styleGbc.gridx = 0
        styleGbc.gridwidth = 2
        stylePanel.add(JLabel("Window style settings"), styleGbc)

        styleGbc.gridy = 1
        styleGbc.gridx = 0
        styleGbc.gridwidth = 2
        stylePanel.add(transparentTitleBarCheck, styleGbc)

        styleGbc.gridy = 2
        stylePanel.add(fullSizeContentCheck, styleGbc)

        styleGbc.gridy = 3
        stylePanel.add(titleVisibleCheck, styleGbc)

        styleGbc.gridy = 4
        styleGbc.gridx = 0
        styleGbc.gridwidth = 1
        stylePanel.add(JLabel("Toolbar style"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(toolbarStyleCombo, styleGbc)

        styleGbc.gridy = 5
        styleGbc.gridx = 0
        stylePanel.add(JLabel("Appearance"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(appearanceCombo, styleGbc)

        val colorPanel = JPanel(GridBagLayout())
        colorPanel.isOpaque = false
        val colorGbc = GridBagConstraints()
        colorGbc.insets = Insets(4, 8, 4, 8)
        colorGbc.fill = GridBagConstraints.HORIZONTAL
        colorGbc.gridx = 0
        colorGbc.gridy = 0
        colorGbc.gridwidth = 2
        colorPanel.add(JLabel("Color settings"), colorGbc)

        fun addColorControl(
            row: Int,
            label: String,
            current: () -> Color,
            apply: (Color) -> Unit
        ) {
            val pickButton = JButton("Pick")
            pickButton.addActionListener {
                val selected = JColorChooser.showDialog(frame, "Choose $label", current()) ?: return@addActionListener
                apply(selected)
            }
            colorGbc.gridy = row
            colorGbc.gridx = 0
            colorGbc.gridwidth = 1
            colorPanel.add(JLabel(label), colorGbc)
            colorGbc.gridx = 1
            colorPanel.add(pickButton, colorGbc)
        }

        addColorControl(
            row = 1,
            label = "Timeseries line",
            current = { topSeriesPanel.lineColor() }
        ) { color -> topSeriesPanel.setLineColor(color) }
        addColorControl(
            row = 2,
            label = "Timeseries fill",
            current = { topSeriesPanel.areaColor() }
        ) { color -> topSeriesPanel.setAreaColor(color) }

        if (undecorated) {
            val blurPanel = JPanel(BorderLayout(0, 8))
            blurPanel.isOpaque = false
            blurPanel.add(undecoratedInfo, BorderLayout.NORTH)
            blurPanel.add(controlsPanel, BorderLayout.CENTER)
            mainContentPanel.add(blurPanel, BorderLayout.EAST)
        } else {
            mainContentPanel.add(stylePanel, BorderLayout.EAST)
        }
        mainContentPanel.add(colorPanel, BorderLayout.WEST)
        centerPanel.add(mainContentPanel, BorderLayout.CENTER)

        val panel = JPanel(BorderLayout(16, 16))
        panel.isOpaque = false
        panel.background = Color(0, 0, 0, 0)
        val centered = JPanel(GridBagLayout())
        centered.isOpaque = false
        centered.add(centerPanel)
        panel.add(topSeriesPanel, BorderLayout.NORTH)
        panel.add(centered, BorderLayout.CENTER)

        frame.contentPane = panel
        frame.isVisible = true
        if (java.lang.Boolean.getBoolean("diaphanous.dump.swing")) {
            dumpComponentTree(frame.rootPane, 0)
        }
        MacWindowStyler.applyAppearance(frame, appearanceCombo.selectedItem as MacWindowAppearance)
        if (undecorated) {
            MacWindowStyler.applyVibrancy(frame, currentStyle())
        } else {
            applyWindowStyleFromControls()
        }
    }

    private enum class WindowMode {
        DECORATED,
        UNDECORATED
    }

    private fun dumpComponentTree(component: java.awt.Component, depth: Int) {
        val indent = "  ".repeat(depth)
        val opaque = if (component is JComponent) component.isOpaque else false
        val background = component.background
        val namePart = if (component.name != null) " name=${component.name}" else ""
        println(
            "$indent- ${component.javaClass.simpleName}$namePart visible=${component.isVisible} " +
                "opaque=$opaque bg=rgba(${background.red},${background.green},${background.blue},${background.alpha})"
        )
        if (component is java.awt.Container) {
            component.components.forEach { child -> dumpComponentTree(child, depth + 1) }
        }
    }

    private class RandomTimeseriesPanel : JPanel() {
        private val values: DoubleArray = DoubleArray(180) { Random.nextDouble(0.0, 1.0) }
        private var areaColor = Color(170, 110, 255, 100)
        private var lineColor = Color(139, 61, 255)
        private var barBackgroundColor = Color(35, 20, 50, 190)
        private var barBorderColor = Color(150, 110, 220, 170)

        init {
            isOpaque = false
            preferredSize = java.awt.Dimension(1, 72)
            minimumSize = java.awt.Dimension(1, 56)
            border = BorderFactory.createMatteBorder(0, 0, 1, 0, barBorderColor)
        }

        fun setLineColor(color: Color) {
            lineColor = color
            repaint()
        }

        fun setAreaColor(color: Color) {
            areaColor = color
            repaint()
        }

        fun setBarBackgroundColor(color: Color) {
            barBackgroundColor = color
            repaint()
        }

        fun setBarBorderColor(color: Color) {
            barBorderColor = color
            border = BorderFactory.createMatteBorder(0, 0, 1, 0, color)
            repaint()
        }

        fun lineColor(): Color = lineColor
        fun areaColor(): Color = areaColor
        fun barBackgroundColor(): Color = barBackgroundColor
        fun barBorderColor(): Color = barBorderColor
        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = barBackgroundColor
            g2.fillRect(0, 0, width, height)

            val w = width.toDouble().coerceAtLeast(1.0)
            val h = height.toDouble().coerceAtLeast(1.0)
            val stepX = if (values.size > 1) w / (values.size - 1) else w

            val area = GeneralPath()
            area.moveTo(0.0, 0.0)
            for (i in values.indices) {
                val x = i * stepX
                val y = values[i] * h
                area.lineTo(x, y)
            }
            area.lineTo(w, 0.0)
            area.closePath()

            val line = GeneralPath()
            for (i in values.indices) {
                val x = i * stepX
                val y = values[i] * h
                if (i == 0) {
                    line.moveTo(x, y)
                } else {
                    line.lineTo(x, y)
                }
            }

            g2.color = areaColor
            g2.fill(area)
            g2.color = lineColor
            g2.draw(line)
            g2.dispose()
        }
    }
}
