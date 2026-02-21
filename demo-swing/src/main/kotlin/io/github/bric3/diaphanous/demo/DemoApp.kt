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
import io.github.bric3.diaphanous.MacVibrancyBlendingMode
import io.github.bric3.diaphanous.MacVibrancyState
import io.github.bric3.diaphanous.MacVibrancyStyle
import io.github.bric3.diaphanous.MacToolbarStyle
import io.github.bric3.diaphanous.MacWindowStyle
import io.github.bric3.diaphanous.MacWindowAppearance
import io.github.bric3.diaphanous.MacWindowDecorations
import io.github.bric3.diaphanous.MacWindowBackdrop
import io.github.bric3.diaphanous.MacStartupReveal
import io.github.bric3.diaphanous.MacBackdropSupport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.LayoutManager
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
        configureApplicationAppearancePolicy()
        val options = parseOptions(args)
        SwingUtilities.invokeLater { showWindow(options) }
    }

    private fun configureApplicationAppearancePolicy() {
        val isMac = System.getProperty("os.name", "").contains("Mac", ignoreCase = true)
        if (!isMac) {
            return
        }
        val key = "apple.awt.application.appearance"
        if (System.getProperty(key).isNullOrBlank()) {
            System.setProperty(key, "system")
        }
    }

    private fun parseOptions(args: Array<String>): LaunchOptions {
        var appearance = MacWindowAppearance.SYSTEM

        for (arg in args) {
            when {
                arg.startsWith("--appearance=", ignoreCase = true) -> {
                    val raw = arg.substringAfter('=')
                    appearance = parseAppearance(raw) ?: appearance
                }
            }
        }
        return LaunchOptions(appearance)
    }

    private fun parseAppearance(raw: String): MacWindowAppearance? {
        val normalized = raw.trim().replace('-', '_').uppercase()
        return MacWindowAppearance.entries.firstOrNull { it.name == normalized }
    }

    private fun showWindow(options: LaunchOptions) {
        val frame = JFrame("Diaphanous Swing Demo")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(980, 640)
        frame.setLocationRelativeTo(null)
        frame.rootPane.isOpaque = false
        frame.layeredPane.isOpaque = false

        val title = JLabel("Native macOS experimental backdrop for this Swing window", JLabel.CENTER)
        title.font = Font("SF Pro Text", Font.BOLD, 22)

        val topSeriesPanel = RandomTimeseriesPanel()

        val nativeDefaultAlpha = MacWindowBackdrop.defaultAlpha()
        val initialAlpha = if (nativeDefaultAlpha in 0.0..1.0) nativeDefaultAlpha else DEFAULT_BACKDROP_ALPHA
        fun blurStrengthForMaterial(material: MacVibrancyMaterial): Int = when (material) {
            MacVibrancyMaterial.CONTENT_BACKGROUND -> 10
            MacVibrancyMaterial.WINDOW_BACKGROUND -> 30
            MacVibrancyMaterial.SIDEBAR -> 50
            MacVibrancyMaterial.MENU -> 70
            MacVibrancyMaterial.HUD_WINDOW -> 90
            else -> DEFAULT_BLUR_STRENGTH
        }
        val initialMaterial = MacWindowBackdrop.defaultMaterial()
            .orElse(MacVibrancyMaterial.UNDER_WINDOW_BACKGROUND)
        val initialBlurStrength = MacWindowBackdrop.defaultMaterial()
            .map(::blurStrengthForMaterial)
            .orElse(DEFAULT_BLUR_STRENGTH)
        val alphaValue = JLabel("%.2f".format(initialAlpha))
        alphaValue.foreground = Color(230, 230, 230, 190)
        val alphaLabel = JLabel("Backdrop alpha")
        val alphaSlider = JSlider(0, 100, (initialAlpha * 100.0).toInt())
        alphaSlider.name = "alphaSlider"
        alphaSlider.isOpaque = false
        val blurValue = JLabel("$initialBlurStrength")
        blurValue.foreground = Color(230, 230, 230, 190)
        val blurLabel = JLabel("Blur strength")
        val blurSlider = JSlider(0, 100, initialBlurStrength)
        blurSlider.name = "blurSlider"
        blurSlider.isOpaque = false
        val materialLabel = JLabel("Material")
        val materialCombo = JComboBox(MacVibrancyMaterial.entries.toTypedArray())
        materialCombo.name = "materialCombo"
        materialCombo.selectedItem = initialMaterial
        val blendingLabel = JLabel("Blending mode")
        val blendingCombo = JComboBox(MacVibrancyBlendingMode.entries.toTypedArray())
        blendingCombo.name = "blendingModeCombo"
        blendingCombo.selectedItem = MacVibrancyBlendingMode.BEHIND_WINDOW
        val stateLabel = JLabel("State")
        val stateCombo = JComboBox(MacVibrancyState.entries.toTypedArray())
        stateCombo.name = "vibrancyStateCombo"
        stateCombo.selectedItem = MacVibrancyState.FOLLOWS_WINDOW_ACTIVE_STATE
        val emphasizedCheck = JCheckBox("Emphasized")
        emphasizedCheck.name = "emphasizedCheck"
        emphasizedCheck.isOpaque = false
        val undecoratedInfo = JLabel(
            "<html><div style='width:260px'>Undecorated mode: alpha/blur controls apply experimental NSVisualEffectView backdrop.</div></html>",
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
            .material(materialCombo.selectedItem as MacVibrancyMaterial)
            .blendingMode(blendingCombo.selectedItem as MacVibrancyBlendingMode)
            .state(stateCombo.selectedItem as MacVibrancyState)
            .emphasized(emphasizedCheck.isSelected)
            .backdropAlpha(alphaSlider.value / 100.0)
            .build()

        fun transparentPanel(layout: LayoutManager): JPanel = JPanel(layout).apply {
            isOpaque = false
            background = Color(0, 0, 0, 0)
        }

        val centerPanel = transparentPanel(BorderLayout(0, 0))
        centerPanel.add(title, BorderLayout.NORTH)
        val mainContentPanel = transparentPanel(BorderLayout(0, 16))

        val controlsPanel = transparentPanel(GridBagLayout())
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

        gbc.gridy = 4
        gbc.gridx = 0
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        controlsPanel.add(materialLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(materialCombo, gbc)

        gbc.gridy = 5
        gbc.gridx = 0
        controlsPanel.add(blendingLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(blendingCombo, gbc)

        gbc.gridy = 6
        gbc.gridx = 0
        controlsPanel.add(stateLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(stateCombo, gbc)

        gbc.gridy = 7
        gbc.gridx = 0
        gbc.gridwidth = 2
        controlsPanel.add(emphasizedCheck, gbc)

        val stylePanel = transparentPanel(GridBagLayout())
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
        appearanceCombo.selectedItem = options.appearance

        fun applyWindowStyleFromControls() {
            val style = MacWindowStyle.builder()
                .transparentTitleBar(transparentTitleBarCheck.isSelected)
                .fullSizeContentView(fullSizeContentCheck.isSelected)
                .titleVisible(titleVisibleCheck.isSelected)
                .toolbarStyle(toolbarStyleCombo.selectedItem as MacToolbarStyle)
                .build()
            MacWindowDecorations.applyStyle(frame, style)
        }
        transparentTitleBarCheck.addActionListener { applyWindowStyleFromControls() }
        fullSizeContentCheck.addActionListener { applyWindowStyleFromControls() }
        titleVisibleCheck.addActionListener { applyWindowStyleFromControls() }
        toolbarStyleCombo.addActionListener { applyWindowStyleFromControls() }
        appearanceCombo.addActionListener {
            val appearance = appearanceCombo.selectedItem as MacWindowAppearance
            MacWindowDecorations.applyAppearance(frame, appearance)
            MacBackdropSupport.configure(frame, appearance)
        }

        alphaSlider.addChangeListener {
            val alpha = alphaSlider.value / 100.0
            alphaValue.text = "%.2f".format(alpha)
            MacWindowBackdrop.apply(frame, currentStyle())
        }
        blurSlider.addChangeListener {
            blurValue.text = blurSlider.value.toString()
            materialCombo.selectedItem = materialForBlurStrength(blurSlider.value)
            MacWindowBackdrop.apply(frame, currentStyle())
        }
        materialCombo.addActionListener {
            val material = materialCombo.selectedItem as MacVibrancyMaterial
            val strength = blurStrengthForMaterial(material)
            if (blurSlider.value != strength) {
                blurSlider.value = strength
            }
            MacWindowBackdrop.apply(frame, currentStyle())
        }
        blendingCombo.addActionListener { MacWindowBackdrop.apply(frame, currentStyle()) }
        stateCombo.addActionListener { MacWindowBackdrop.apply(frame, currentStyle()) }
        emphasizedCheck.addActionListener { MacWindowBackdrop.apply(frame, currentStyle()) }

        var styleRow = 0
        styleGbc.gridy = styleRow++
        styleGbc.gridx = 0
        styleGbc.gridwidth = 2
        stylePanel.add(JLabel("Window style settings"), styleGbc)

        styleGbc.gridy = styleRow++
        styleGbc.gridx = 0
        styleGbc.gridwidth = 2
        stylePanel.add(transparentTitleBarCheck, styleGbc)

        styleGbc.gridy = styleRow++
        stylePanel.add(fullSizeContentCheck, styleGbc)

        styleGbc.gridy = styleRow++
        stylePanel.add(titleVisibleCheck, styleGbc)

        styleGbc.gridy = styleRow++
        styleGbc.gridx = 0
        styleGbc.gridwidth = 1
        stylePanel.add(JLabel("Toolbar style"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(toolbarStyleCombo, styleGbc)

        styleGbc.gridy = styleRow
        styleGbc.gridx = 0
        styleGbc.gridwidth = 1
        stylePanel.add(JLabel("Appearance"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(appearanceCombo, styleGbc)

        val colorPanel = transparentPanel(GridBagLayout())
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
            pickButton.isOpaque = false
            pickButton.background = Color(0, 0, 0, 0)
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

        val settingsPanel = transparentPanel(BorderLayout(0, 8))
        settingsPanel.add(stylePanel, BorderLayout.NORTH)
        settingsPanel.add(controlsPanel, BorderLayout.CENTER)
        settingsPanel.preferredSize = Dimension(320, settingsPanel.preferredSize.height)
        mainContentPanel.add(settingsPanel, BorderLayout.EAST)
        mainContentPanel.add(colorPanel, BorderLayout.WEST)
        centerPanel.add(mainContentPanel, BorderLayout.CENTER)

        val rootContentPane = RootErasingContentPane(BorderLayout(16, 16))
        val centered = transparentPanel(GridBagLayout())
        centered.add(centerPanel)
        rootContentPane.add(topSeriesPanel, BorderLayout.NORTH)
        rootContentPane.add(centered, BorderLayout.CENTER)

        frame.contentPane = rootContentPane
        val initialAppearance = appearanceCombo.selectedItem as MacWindowAppearance
        MacWindowDecorations.applyAppearance(frame, initialAppearance)
        MacBackdropSupport.configure(frame, initialAppearance)

        applyWindowStyleFromControls()

        MacWindowBackdrop.apply(frame, currentStyle())
        MacStartupReveal.show(frame)
        if (java.lang.Boolean.getBoolean("diaphanous.dump.swing")) {
            dumpComponentTree(frame.rootPane, 0)
        }
    }

    private data class LaunchOptions(
        val appearance: MacWindowAppearance
    )

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

    private class RootErasingContentPane(
        layout: LayoutManager
    ) : JPanel(layout) {
        init {
            isOpaque = false
            background = Color(0, 0, 0, 0)
        }

        override fun paintComponent(g: Graphics) {
            if (!MacBackdropSupport.clearBackgroundIfEnabled(g, this)) {
                super.paintComponent(g)
            }
        }
    }
}
