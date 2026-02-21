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
import kotlin.div
import kotlin.random.Random
import kotlin.toString

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
        val frame = JFrame("Diaphanous Swing Demo").apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(980, 640)
            setLocationRelativeTo(null)
            rootPane.isOpaque = false
            layeredPane.isOpaque = false
        }

        val title = JLabel("Native macOS experimental backdrop for this Swing window", JLabel.CENTER).apply {
            font = Font("SF Pro Text", Font.BOLD, 22)
        }

        val topSeriesPanel = RandomTimeseriesPanel()

        val centerPanel = transparentPanel(BorderLayout(0, 0))
        centerPanel.add(title, BorderLayout.NORTH)
        val mainContentPanel = transparentPanel(BorderLayout(0, 16))

        val windowBackdropControls = WindowBackdropControls() {
            MacWindowBackdrop.apply(frame, it)
        }

        val stylePanel = transparentPanel(GridBagLayout())
        val styleGbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            fill = GridBagConstraints.HORIZONTAL
        }

        val transparentTitleBarCheck = JCheckBox("Transparent title bar", true).apply {
            name = "transparentTitleBarCheck"
            isOpaque = false
        }
        val fullSizeContentCheck = JCheckBox("Full-size content view", true).apply {
            name = "fullSizeContentCheck"
            isOpaque = false
        }
        val titleVisibleCheck = JCheckBox("Title visible", false).apply {
            name = "titleVisibleCheck"
            isOpaque = false
        }
        val toolbarStyleCombo = JComboBox(MacToolbarStyle.entries.toTypedArray()).apply {
            name = "toolbarStyleCombo"
            selectedItem = MacToolbarStyle.UNIFIED_COMPACT
        }
        val appearanceCombo = JComboBox(MacWindowAppearance.entries.toTypedArray()).apply {
            name = "appearanceCombo"
            selectedItem = options.appearance
        }

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

        var styleRow = 0
        styleGbc.apply {
            gridy = styleRow++
            gridx = 0
            gridwidth = 2
        }
        stylePanel.add(JLabel("Window style settings"), styleGbc)

        styleGbc.apply {
            gridy = styleRow++
            gridx = 0
            gridwidth = 2
        }
        stylePanel.add(transparentTitleBarCheck, styleGbc)

        styleGbc.gridy = styleRow++
        stylePanel.add(fullSizeContentCheck, styleGbc)

        styleGbc.gridy = styleRow++
        stylePanel.add(titleVisibleCheck, styleGbc)

        styleGbc.apply {
            gridy = styleRow++
            gridx = 0
            gridwidth = 1
        }
        stylePanel.add(JLabel("Toolbar style"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(toolbarStyleCombo, styleGbc)

        styleGbc.apply {
            gridy = styleRow
            gridx = 0
            gridwidth = 1
        }
        stylePanel.add(JLabel("Appearance"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(appearanceCombo, styleGbc)

        val colorPanel = transparentPanel(GridBagLayout())
        val colorGbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0
            gridy = 0
            gridwidth = 2
        }
        colorPanel.add(JLabel("Color settings"), colorGbc)

        fun addColorControl(
            row: Int,
            label: String,
            current: () -> Color,
            apply: (Color) -> Unit
        ) {
            val pickButton = JButton("Pick").apply {
                isOpaque = false
                background = Color(0, 0, 0, 0)
                addActionListener {
                    val selected = JColorChooser.showDialog(frame, "Choose $label", current()) ?: return@addActionListener
                    apply(selected)
                }
            }
            colorGbc.apply {
                gridy = row
                gridx = 0
                gridwidth = 1
            }
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
        settingsPanel.add(windowBackdropControls.component, BorderLayout.CENTER)
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

        MacWindowBackdrop.apply(frame, windowBackdropControls.currentStyle())
        MacStartupReveal.show(frame)
        if (java.lang.Boolean.getBoolean("diaphanous.dump.swing")) {
            dumpComponentTree(frame.rootPane, 0)
        }
    }

    fun transparentPanel(layout: LayoutManager): JPanel = JPanel(layout).apply {
        isOpaque = false
        background = Color(0, 0, 0, 0)
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

    private class WindowBackdropControls(
        private val onChange: (style: MacVibrancyStyle) -> Unit
    ) : JPanel() {
        init {
            isOpaque = false
        }
        private val nativeDefaultAlpha = MacWindowBackdrop.defaultAlpha()
        private val initialAlpha = if (nativeDefaultAlpha in 0.0..1.0) nativeDefaultAlpha else DEFAULT_BACKDROP_ALPHA
        private val initialMaterial = MacWindowBackdrop.defaultMaterial()
            .orElse(MacVibrancyMaterial.UNDER_WINDOW_BACKGROUND)
        private val initialBlurStrength = MacWindowBackdrop.defaultMaterial()
            .map(::blurStrengthForMaterial)
            .orElse(DEFAULT_BLUR_STRENGTH)

        private val alphaValue = JLabel("%.2f".format(initialAlpha)).apply {
            foreground = Color(230, 230, 230, 190)
        }

        private val alphaSlider = JSlider(0, 100, (initialAlpha * 100.0).toInt()).apply {
            name = "alphaSlider"
            isOpaque = false
            addChangeListener {
                val alpha = value / 100.0
                alphaValue.text = "%.2f".format(alpha)
                onChange(currentStyle())
            }
        }

        private val blurValue = JLabel("$initialBlurStrength").apply {
            foreground = Color(230, 230, 230, 190)
        }

        private val blurSlider = JSlider(0, 100, initialBlurStrength).apply {
            name = "blurSlider"
            isOpaque = false
            addChangeListener {
                blurValue.text = value.toString()
                materialCombo.selectedItem = materialForBlurStrength(value)
                onChange(currentStyle())
            }
        }

        private val materialCombo = JComboBox(MacVibrancyMaterial.entries.toTypedArray()).apply {
            name = "materialCombo"
            selectedItem = initialMaterial
            addActionListener { onChange(currentStyle()) }
        }

        private val blendingCombo = JComboBox(MacVibrancyBlendingMode.entries.toTypedArray()).apply {
            name = "blendingModeCombo"
            selectedItem = MacVibrancyBlendingMode.BEHIND_WINDOW
            addActionListener { onChange(currentStyle()) }
        }

        private val stateCombo = JComboBox(MacVibrancyState.entries.toTypedArray()).apply {
            name = "vibrancyStateCombo"
            selectedItem = MacVibrancyState.FOLLOWS_WINDOW_ACTIVE_STATE
            addActionListener { onChange(currentStyle()) }
        }

        private val emphasizedCheck = JCheckBox("Emphasized").apply {
            name = "emphasizedCheck"
            isOpaque = false
            addActionListener { onChange(currentStyle()) }
        }

        private fun blurStrengthForMaterial(material: MacVibrancyMaterial): Int = when (material) {
            MacVibrancyMaterial.CONTENT_BACKGROUND -> 10
            MacVibrancyMaterial.WINDOW_BACKGROUND -> 30
            MacVibrancyMaterial.SIDEBAR -> 50
            MacVibrancyMaterial.MENU -> 70
            MacVibrancyMaterial.HUD_WINDOW -> 90
            else -> DEFAULT_BLUR_STRENGTH
        }

        private fun materialForBlurStrength(value: Int): MacVibrancyMaterial = when {
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

        val component by lazy { windowBackdropControls() }
        private fun windowBackdropControls(): JPanel {
            val alphaLabel = JLabel("Backdrop alpha")
            val blurLabel = JLabel("Blur strength")
            val materialLabel = JLabel("Material")
            val blendingLabel = JLabel("Blending mode")
            val stateLabel = JLabel("State")

            materialCombo.addActionListener {
                val material = materialCombo.selectedItem as MacVibrancyMaterial
                val strength = blurStrengthForMaterial(material)
                if (blurSlider.value != strength) {
                    blurSlider.value = strength
                }
                onChange(currentStyle())
            }

            val controlsPanel = transparentPanel(GridBagLayout())
            val gbc = GridBagConstraints().apply {
                insets = Insets(4, 8, 4, 8)
                fill = GridBagConstraints.HORIZONTAL
                gridy = 0
                gridx = 0
                gridwidth = 1
            }
            controlsPanel.add(alphaLabel, gbc)
            gbc.gridx = 1
            controlsPanel.add(alphaValue, gbc)

            gbc.apply {
                gridy = 1
                gridx = 0
                weightx = 1.0
                gridwidth = 2
            }
            controlsPanel.add(alphaSlider, gbc)

            gbc.apply {
                gridy = 2
                gridx = 0
                gridwidth = 1
                weightx = 0.0
            }
            controlsPanel.add(blurLabel, gbc)
            gbc.gridx = 1
            controlsPanel.add(blurValue, gbc)

            gbc.apply {
                gridy = 3
                gridx = 0
                weightx = 1.0
                gridwidth = 2
            }
            controlsPanel.add(blurSlider, gbc)

            gbc.apply {
                gridy = 4
                gridx = 0
                gridwidth = 1
                weightx = 0.0
            }
            controlsPanel.add(materialLabel, gbc)
            gbc.gridx = 1
            controlsPanel.add(materialCombo, gbc)

            gbc.apply {
                gridy = 5
                gridx = 0
            }
            controlsPanel.add(blendingLabel, gbc)
            gbc.gridx = 1
            controlsPanel.add(blendingCombo, gbc)

            gbc.apply {
                gridy = 6
                gridx = 0
            }
            controlsPanel.add(stateLabel, gbc)
            gbc.gridx = 1
            controlsPanel.add(stateCombo, gbc)

            gbc.apply {
                gridy = 7
                gridx = 0
                gridwidth = 2
            }
            controlsPanel.add(emphasizedCheck, gbc)
            return controlsPanel
        }

    }

    private class RandomTimeseriesPanel : JPanel() {
        private val values: DoubleArray = DoubleArray(180) { Random.nextDouble(0.0, 1.0) }
        private var areaColor = Color(170, 110, 255, 100)
        private var lineColor = Color(139, 61, 255)
        private var barBackgroundColor = Color(35, 20, 50, 190)
        private var barBorderColor = Color(150, 110, 220, 170)

        init {
            apply {
                isOpaque = false
                preferredSize = java.awt.Dimension(1, 72)
                minimumSize = java.awt.Dimension(1, 56)
                border = BorderFactory.createMatteBorder(0, 0, 1, 0, barBorderColor)
            }
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
            g2.apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                color = barBackgroundColor
                fillRect(0, 0, width, height)
            }

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

            g2.apply {
                color = areaColor
                fill(area)
                color = lineColor
                draw(line)
            }
            g2.dispose()
        }
    }

    private class RootErasingContentPane(
        layout: LayoutManager
    ) : JPanel(layout) {
        init {
            apply {
                isOpaque = false
                background = Color(0, 0, 0, 0)
            }
        }

        override fun paintComponent(g: Graphics) {
            if (!MacBackdropSupport.clearBackgroundIfEnabled(g, this)) {
                super.paintComponent(g)
            }
        }
    }
}
