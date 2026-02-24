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

import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JColorChooser
import javax.swing.JLabel
import javax.swing.JPanel

class TopTimeseriesColorControls(
    private val topSeriesPanel: RandomTimeseriesPanel
) : JPanel() {
    init {
        isOpaque = false
        background = Color(0, 0, 0, 0)
    }

    val component by lazy { buildComponent() }

    private fun buildComponent(): JPanel {
        val colorPanel = JPanel(GridBagLayout()).apply {
            isOpaque = false
            background = Color(0, 0, 0, 0)
        }
        val colorGbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0
            gridy = 0
            gridwidth = 2
        }
        colorPanel.add(JLabel("Color settings"), colorGbc)

        addColorControl(
            colorPanel = colorPanel,
            gbc = colorGbc,
            row = 1,
            label = "Timeseries line",
            current = { topSeriesPanel.lineColor },
            apply = { color -> topSeriesPanel.lineColor = color }
        )
        addColorControl(
            colorPanel = colorPanel,
            gbc = colorGbc,
            row = 2,
            label = "Timeseries fill",
            current = { topSeriesPanel.areaColor },
            apply = { color -> topSeriesPanel.areaColor = color }
        )
        return colorPanel
    }

    private fun addColorControl(
        colorPanel: JPanel,
        gbc: GridBagConstraints,
        row: Int,
        label: String,
        current: () -> Color,
        apply: (Color) -> Unit
    ) {
        val pickButton = JButton("Pick").apply {
            isOpaque = false
            background = Color(0, 0, 0, 0)
            addActionListener {
                val selected = JColorChooser.showDialog(topSeriesPanel, "Choose $label", current()) ?: return@addActionListener
                apply(selected)
            }
        }
        gbc.apply {
            gridy = row
            gridx = 0
            gridwidth = 1
        }
        colorPanel.add(JLabel(label), gbc)
        gbc.gridx = 1
        colorPanel.add(pickButton, gbc)
    }
}
