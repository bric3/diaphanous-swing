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

import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec
import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropEffectBlendingMode
import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropEffectState
import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropMaterial
import io.github.bric3.diaphanous.backdrop.WindowBackdrop
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider

class WindowBackdropControls(
    private val onChange: (style: MacosBackdropEffectSpec) -> Unit
) : JPanel() {
    companion object {
        private const val DEFAULT_BACKDROP_ALPHA = 0.55
    }

    init {
        isOpaque = false
    }
    private val nativeDefaultAlpha = WindowBackdrop.defaultAlpha()
    private val initialAlpha = if (nativeDefaultAlpha in 0.0..1.0) nativeDefaultAlpha else DEFAULT_BACKDROP_ALPHA
    private val initialMaterial = WindowBackdrop.defaultMaterial()
        .filter { it is MacosBackdropMaterial }
        .map { it as MacosBackdropMaterial }
        .orElse(MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)

    private val alphaValue = JLabel("%.2f".format(initialAlpha)).apply {
        foreground = Color(230, 230, 230, 190)
    }

    private val alphaSlider = JSlider(0, 100, (initialAlpha * 100.0).toInt()).apply {
        name = "alphaSlider"
        isOpaque = false
        addChangeListener {
            val alpha = value / 100.0
            alphaValue.text = "%.2f".format(alpha)
            onChange(currentSpec())
        }
    }

    private val materialCombo = JComboBox(MacosBackdropMaterial.entries.toTypedArray()).apply {
        name = "materialCombo"
        selectedItem = initialMaterial
        addActionListener { onChange(currentSpec()) }
    }

    private val blendingCombo = JComboBox(MacosBackdropEffectBlendingMode.entries.toTypedArray()).apply {
        name = "blendingModeCombo"
        selectedItem = MacosBackdropEffectBlendingMode.BEHIND_WINDOW
        addActionListener { onChange(currentSpec()) }
    }

    private val stateCombo = JComboBox(MacosBackdropEffectState.entries.toTypedArray()).apply {
        name = "vibrancyStateCombo"
        selectedItem = MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE
        addActionListener { onChange(currentSpec()) }
    }

    private val emphasizedCheck = JCheckBox("Emphasized").apply {
        name = "emphasizedCheck"
        isOpaque = false
        addActionListener { onChange(currentSpec()) }
    }

    fun currentSpec(): MacosBackdropEffectSpec = MacosBackdropEffectSpec.builder()
        .material(materialCombo.selectedItem as MacosBackdropMaterial)
        .blendingMode(blendingCombo.selectedItem as MacosBackdropEffectBlendingMode)
        .state(stateCombo.selectedItem as MacosBackdropEffectState)
        .emphasized(emphasizedCheck.isSelected)
        .backdropAlpha(alphaSlider.value / 100.0)
        .build()

    val component by lazy { windowBackdropControls() }
    private fun windowBackdropControls(): JPanel {
        val alphaLabel = JLabel("Backdrop alpha")
        val materialLabel = JLabel("Material")
        val blendingLabel = JLabel("Blending mode")
        val stateLabel = JLabel("State")

        val controlsPanel = DemoApp.transparentPanel(GridBagLayout())
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
        controlsPanel.add(materialLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(materialCombo, gbc)

        gbc.apply {
            gridy = 3
            gridx = 0
        }
        controlsPanel.add(blendingLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(blendingCombo, gbc)

        gbc.apply {
            gridy = 4
            gridx = 0
        }
        controlsPanel.add(stateLabel, gbc)
        gbc.gridx = 1
        controlsPanel.add(stateCombo, gbc)

        gbc.apply {
            gridy = 5
            gridx = 0
            gridwidth = 2
        }
        controlsPanel.add(emphasizedCheck, gbc)
        return controlsPanel
    }

}
