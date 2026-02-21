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
import java.awt.GridBagLayout
import java.awt.LayoutManager
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.JComponent

/**
 * Manual demo for toggling native macOS style attributes on a Swing frame.
 */
object DemoApp {
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
        val windowDecorationControls = WindowDecorationControls(
            initialAppearance = options.appearance,
            onStyleChange = { style -> MacWindowDecorations.applyStyle(frame, style) },
            onAppearanceChange = { appearance ->
                MacWindowDecorations.applyAppearance(frame, appearance)
                MacBackdropSupport.configure(frame, appearance)
            }
        )
        val topTimeseriesColorControls = TopTimeseriesColorControls(
            parent = frame,
            currentLineColor = { topSeriesPanel.lineColor },
            setLineColor = { color -> topSeriesPanel.lineColor = color },
            currentFillColor = { topSeriesPanel.areaColor },
            setFillColor = { color -> topSeriesPanel.areaColor = color }
        )

        val settingsPanel = transparentPanel(BorderLayout(0, 8))
        settingsPanel.add(windowDecorationControls.component, BorderLayout.NORTH)
        settingsPanel.add(windowBackdropControls.component, BorderLayout.CENTER)
        settingsPanel.preferredSize = Dimension(320, settingsPanel.preferredSize.height)
        mainContentPanel.add(settingsPanel, BorderLayout.EAST)
        mainContentPanel.add(topTimeseriesColorControls.component, BorderLayout.WEST)
        centerPanel.add(mainContentPanel, BorderLayout.CENTER)

        val rootContentPane = RootErasingContentPane(BorderLayout(16, 16))
        val centered = transparentPanel(GridBagLayout())
        centered.add(centerPanel)
        rootContentPane.add(topSeriesPanel, BorderLayout.NORTH)
        rootContentPane.add(centered, BorderLayout.CENTER)

        frame.contentPane = rootContentPane
        val initialAppearance = windowDecorationControls.currentAppearance()
        MacWindowDecorations.applyAppearance(frame, initialAppearance)
        MacBackdropSupport.configure(frame, initialAppearance)
        MacWindowDecorations.applyStyle(frame, windowDecorationControls.currentStyle())

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
