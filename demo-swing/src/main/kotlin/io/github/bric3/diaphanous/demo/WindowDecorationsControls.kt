/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.demo

import io.github.bric3.diaphanous.decorations.MacosWindowAppearanceSpec
import io.github.bric3.diaphanous.decorations.MacosWindowDecorationsSpec
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class WindowDecorationsControls(
    initialAppearance: MacosWindowAppearanceSpec,
    private val onStyleChange: (style: MacosWindowDecorationsSpec) -> Unit,
    private val onAppearanceChange: (appearance: MacosWindowAppearanceSpec) -> Unit
) : JPanel() {
    init {
        isOpaque = false
    }

    private val transparentTitleBarCheck = JCheckBox("Transparent title bar", true).apply {
        name = "transparentTitleBarCheck"
        isOpaque = false
        addActionListener { onStyleChange(currentDecorationsSpec()) }
    }
    private val fullSizeContentCheck = JCheckBox("Full-size content view", true).apply {
        name = "fullSizeContentCheck"
        isOpaque = false
        addActionListener { onStyleChange(currentDecorationsSpec()) }
    }
    private val titleVisibleCheck = JCheckBox("Title visible", false).apply {
        name = "titleVisibleCheck"
        isOpaque = false
        addActionListener { onStyleChange(currentDecorationsSpec()) }
    }
    private val appearanceCombo = JComboBox(MacosWindowAppearanceSpec.entries.toTypedArray()).apply {
        name = "appearanceCombo"
        selectedItem = initialAppearance
        addActionListener { onAppearanceChange(currentAppearanceSpec()) }
    }

    fun currentDecorationsSpec(): MacosWindowDecorationsSpec = MacosWindowDecorationsSpec.builder()
        .transparentTitleBar(transparentTitleBarCheck.isSelected)
        .fullSizeContentView(fullSizeContentCheck.isSelected)
        .titleVisible(titleVisibleCheck.isSelected)
        .build()

    fun currentAppearanceSpec(): MacosWindowAppearanceSpec = appearanceCombo.selectedItem as MacosWindowAppearanceSpec

    val component by lazy { windowDecorationControls() }

    private fun transparentPanel(): JPanel = JPanel(GridBagLayout()).apply {
        isOpaque = false
        background = Color(0, 0, 0, 0)
    }

    private fun windowDecorationControls(): JPanel {
        val stylePanel = transparentPanel()
        val styleGbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            fill = GridBagConstraints.HORIZONTAL
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
            gridy = styleRow
            gridx = 0
            gridwidth = 1
        }
        stylePanel.add(JLabel("Appearance"), styleGbc)
        styleGbc.gridx = 1
        stylePanel.add(appearanceCombo, styleGbc)
        return stylePanel
    }
}
