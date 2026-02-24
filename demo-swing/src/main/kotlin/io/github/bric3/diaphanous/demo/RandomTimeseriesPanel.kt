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
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.GeneralPath
import javax.swing.BorderFactory
import javax.swing.JPanel
import kotlin.random.Random

class RandomTimeseriesPanel(noBackground: Boolean = true) : JPanel() {
    private val values: DoubleArray = DoubleArray(180) { Random.nextDouble(0.0, 1.0) }
    var areaColor = Color(170, 110, 255, 100)
        set(value) {
            field = value
            repaint()
        }
    
    var lineColor = Color(139, 61, 255)
        set(value) {
            field = value
            repaint()
        }

    var barBackgroundColor = Color(35, 20, 50, 190)
        set(value) {
            field = value
            repaint()
        }

    private var barBorderColor = Color(150, 110, 220, 170)

    init {
        isOpaque = noBackground.not()
        preferredSize = Dimension(1, 72)
        minimumSize = Dimension(1, 56)
        border = BorderFactory.createMatteBorder(0, 0, 0, 0, barBorderColor)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        if (isOpaque) {
            g2.run {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                color = barBackgroundColor
                fillRect(0, 0, width, height)

            }
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

        g2.run {
            color = areaColor
            fill(area)
            color = lineColor
            draw(line)
        }
        g2.dispose()
    }
}