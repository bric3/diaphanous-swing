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
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.EventQueue
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.Robot
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.MultiResolutionImage
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import javax.imageio.ImageIO
import javax.swing.JComboBox
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

class ScreenshotRobotTest {
    private val captureMarginPx = 20
    private val captureAlphaOverride: Int? = System.getProperty("diaphanous.robot.alpha")
        ?.toIntOrNull()
        ?.coerceIn(0, 100)
    private val diagonalMode: DiagonalMode = DiagonalMode.fromProperty(
        System.getProperty("diaphanous.robot.diagonal")
    )

    @Test
    fun captureSingleScreenshot() {
        assumeTrue(System.getProperty("os.name", "").contains("Mac", ignoreCase = true))
        assumeFalse(GraphicsEnvironment.isHeadless())

        DemoApp.main(arrayOf("decorated", "--appearance=vibrant_dark"))
        val frame = waitForDemoFrame(Duration.ofSeconds(10))
        assertNotNull(frame, "Demo frame was not found within timeout")
        val demoFrame = requireNotNull(frame)

        val captureTarget = invokeAndWaitResultShot {
            val location = demoFrame.locationOnScreen
            Rectangle(
                location.x - captureMarginPx,
                location.y - captureMarginPx,
                demoFrame.width + (captureMarginPx * 2),
                demoFrame.height + (captureMarginPx * 2)
            )
        }
        val backdropProbe = createBackdropProbe(captureTarget)
        EventQueue.invokeAndWait {
            backdropProbe.isVisible = true
            demoFrame.toFront()
        }

        EventQueue.invokeAndWait {
            findByName(demoFrame.rootPane, "appearanceCombo", JComboBox::class.java)?.selectedItem = MacosWindowAppearanceSpec.VIBRANT_DARK
        }

        val alphaDefault = invokeAndWaitResultShot {
            findByName(demoFrame.rootPane, "alphaSlider", JSlider::class.java)?.value
        } ?: -1
        val outputDir = resolveOutputDir()
        val robot = Robot().apply { autoDelay = 120 }
        Thread.sleep(900)
        EventQueue.invokeAndWait {
            if (captureAlphaOverride != null) {
                findByName(demoFrame.rootPane, "alphaSlider", JSlider::class.java)?.value = captureAlphaOverride
            }
        }
        Thread.sleep(450)
        EventQueue.invokeAndWait {
            findByName(demoFrame.rootPane, "appearanceCombo", JComboBox::class.java)?.selectedItem = MacosWindowAppearanceSpec.VIBRANT_DARK
        }
        Thread.sleep(700)
        moveMouseOutsideCapture(robot, captureTarget)
        Thread.sleep(120)
        val darkImageRaw = captureBestResolution(robot, captureTarget)
        val darkImage = downscaleForOutput(darkImageRaw, captureTarget.width, captureTarget.height)
        ImageIO.write(darkImage, "png", outputDir.toPath().resolve("screenshot-dark.png").toFile())

        EventQueue.invokeAndWait {
            findByName(demoFrame.rootPane, "appearanceCombo", JComboBox::class.java)?.selectedItem = MacosWindowAppearanceSpec.VIBRANT_LIGHT
        }
        Thread.sleep(700)
        moveMouseOutsideCapture(robot, captureTarget)
        Thread.sleep(120)
        val lightImageRaw = captureBestResolution(robot, captureTarget)
        val lightImage = downscaleForOutput(lightImageRaw, captureTarget.width, captureTarget.height)
        ImageIO.write(lightImage, "png", outputDir.toPath().resolve("screenshot-light.png").toFile())

        val mix = createDiagonalMix(lightImage, darkImage, diagonalMode)
        ImageIO.write(mix, "png", outputDir.toPath().resolve("screenshot-mix-diagonal.png").toFile())

        val report = buildString {
            appendLine("Screenshot robot capture")
            appendLine("Timestamp: ${Instant.now()}")
            appendLine("Frame bounds: ${demoFrame.bounds}")
            appendLine("Capture bounds (extended): $captureTarget")
            appendLine("Capture margin (px): $captureMarginPx")
            appendLine("Backdrop probe: enabled")
            appendLine("Appearance captures: VIBRANT_DARK and VIBRANT_LIGHT")
            appendLine("Alpha slider (default): $alphaDefault")
            appendLine(
                if (captureAlphaOverride != null) {
                    "Alpha slider (capture override): $captureAlphaOverride"
                } else {
                    "Alpha slider (capture override): unchanged"
                }
            )
            appendLine("Diagonal mix mode: ${diagonalMode.propertyValue}")
            appendLine("Captured: screenshot-dark.png")
            appendLine("Captured: screenshot-light.png")
            appendLine("Captured: screenshot-mix-diagonal.png")
            appendLine("Dark image raw pixels: ${darkImageRaw.width}x${darkImageRaw.height}")
            appendLine("Light image raw pixels: ${lightImageRaw.width}x${lightImageRaw.height}")
            appendLine("Output image pixels: ${darkImage.width}x${darkImage.height}")
        }
        Files.writeString(outputDir.toPath().resolve("report.txt"), report)

        EventQueue.invokeAndWait {
            demoFrame.dispose()
            backdropProbe.dispose()
        }
    }

    private fun resolveOutputDir(): File {
        val configured = System.getProperty("diaphanous.robot.outputDir")
        val dir = if (configured.isNullOrBlank()) File("build/reports/robotShot") else File(configured)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun waitForDemoFrame(timeout: Duration): JFrame? {
        val deadline = System.nanoTime() + timeout.toNanos()
        while (System.nanoTime() < deadline) {
            val frame = Frame.getFrames()
                .filterIsInstance<JFrame>()
                .firstOrNull { it.title == "Diaphanous Swing Demo" && it.isShowing }
            if (frame != null) {
                return frame
            }
            Thread.sleep(100)
        }
        return null
    }

    private fun createBackdropProbe(bounds: Rectangle): JFrame {
        val frame = JFrame("Backdrop Probe")
        frame.isUndecorated = true
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setBounds(bounds)
        val wallpaper = loadWallpaper()
        frame.contentPane = if (wallpaper != null) WallpaperPanel(wallpaper) else PatternPanel()
        frame.isAlwaysOnTop = false
        return frame
    }

    private fun loadWallpaper(): Image? {
        val explicit = System.getProperty("diaphanous.robot.wallpaper")
        val wallpaperName = System.getProperty("diaphanous.robot.wallpaperName")?.trim()?.lowercase()
        val bundled = when (wallpaperName) {
            "macbook" -> "/wallpapers/macbook-keyboard-apple-event-apple-keyboard-ambient-lighting-3840x2160-6689.jpg"
            "java", "java-logo" -> "/wallpapers/java-logo-3840x2160-15990.png"
            null, "" -> "/wallpapers/macbook-keyboard-apple-event-apple-keyboard-ambient-lighting-3840x2160-6689.jpg"
            else -> "/wallpapers/$wallpaperName"
        }
        val classpath = javaClass.getResource(bundled)
        if (classpath != null) {
            return ImageIO.read(classpath)
        }
        val candidates = listOf(
            explicit?.takeIf { it.isNotBlank() }?.let { File(it) },
            File("java-logo-3840x2160-15990.png"),
            File("../java-logo-3840x2160-15990.png"),
            System.getProperty("diaphanous.projectDir")?.let { File(it, "java-logo-3840x2160-15990.png") },
            System.getProperty("diaphanous.projectDir")?.let { File(it, "macbook-keyboard-apple-event-apple-keyboard-ambient-lighting-3840x2160-6689.jpg") },
            System.getProperty("diaphanous.projectDir")?.let { File(it, "demo-swing/src/robotTest/resources/wallpapers/java-logo-3840x2160-15990.png") }
            ,
            System.getProperty("diaphanous.projectDir")?.let { File(it, "demo-swing/src/robotTest/resources/wallpapers/macbook-keyboard-apple-event-apple-keyboard-ambient-lighting-3840x2160-6689.jpg") }
        ).filterNotNull()

        val file = candidates.firstOrNull { it.isFile } ?: return null
        if (System.getProperty("diaphanous.dump.swing") == "true") {
            println("Using wallpaper: ${file.absolutePath}")
        }
        return ImageIcon(file.absolutePath).image
    }

    private fun <T : Component> findByName(root: Container, name: String, type: Class<T>): T? {
        for (component in root.components) {
            if (type.isInstance(component) && component.name == name) {
                return type.cast(component)
            }
            if (component is Container) {
                val nested = findByName(component, name, type)
                if (nested != null) {
                    return nested
                }
            }
        }
        return null
    }

    private fun createDiagonalMix(light: BufferedImage, dark: BufferedImage, mode: DiagonalMode): BufferedImage {
        val w = minOf(light.width, dark.width)
        val h = minOf(light.height, dark.height)
        val scale = 4
        val hiW = w * scale
        val hiH = h * scale
        val hi = BufferedImage(hiW, hiH, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until hiH) {
            val sy = (y / scale).coerceIn(0, h - 1)
            for (x in 0 until hiW) {
                val sx = (x / scale).coerceIn(0, w - 1)
                val topLeftToBottomRight = y.toLong() * hiW <= x.toLong() * hiH
                val topRightToBottomLeft = y.toLong() * hiW <= (hiW - 1L - x.toLong()) * hiH
                val pixel = when (mode) {
                    DiagonalMode.TL_LIGHT_BR_DARK -> if (topLeftToBottomRight) light.getRGB(sx, sy) else dark.getRGB(sx, sy)
                    DiagonalMode.TL_DARK_BR_LIGHT -> if (topLeftToBottomRight) dark.getRGB(sx, sy) else light.getRGB(sx, sy)
                    DiagonalMode.TR_LIGHT_BL_DARK -> if (topRightToBottomLeft) light.getRGB(sx, sy) else dark.getRGB(sx, sy)
                    DiagonalMode.TR_DARK_BL_LIGHT -> if (topRightToBottomLeft) dark.getRGB(sx, sy) else light.getRGB(sx, sy)
                }
                hi.setRGB(x, y, pixel)
            }
        }
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g2 = out.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(hi, 0, 0, w, h, null)
        g2.dispose()
        return out
    }

    private fun moveMouseOutsideCapture(robot: Robot, capture: Rectangle) {
        val screen = Toolkit.getDefaultToolkit().screenSize
        val candidates = listOf(
            8 to 8,
            (screen.width - 8).coerceAtLeast(0) to 8,
            8 to (screen.height - 8).coerceAtLeast(0),
            (screen.width - 8).coerceAtLeast(0) to (screen.height - 8).coerceAtLeast(0)
        )
        val target = candidates.firstOrNull { (x, y) -> !capture.contains(x, y) } ?: return
        robot.mouseMove(target.first, target.second)
        robot.waitForIdle()
    }

    private fun captureBestResolution(robot: Robot, capture: Rectangle): BufferedImage {
        val multi = robot.createMultiResolutionScreenCapture(capture) as MultiResolutionImage
        val variants = multi.resolutionVariants.filterIsInstance<Image>()
        val best = variants.maxByOrNull {
            it.getWidth(null).toLong().coerceAtLeast(1L) * it.getHeight(null).toLong().coerceAtLeast(1L)
        } ?: return robot.createScreenCapture(capture)
        return if (best is BufferedImage) {
            best
        } else {
            val width = best.getWidth(null).coerceAtLeast(1)
            val height = best.getHeight(null).coerceAtLeast(1)
            BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also { image ->
                val g2 = image.createGraphics()
                g2.drawImage(best, 0, 0, null)
                g2.dispose()
            }
        }
    }

    private fun downscaleForOutput(source: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        if (source.width <= targetWidth || source.height <= targetHeight) {
            return source
        }
        var current = source
        var w = source.width
        var h = source.height
        while (w / 2 >= targetWidth && h / 2 >= targetHeight) {
            w /= 2
            h /= 2
            current = scaleImage(current, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        }
        return scaleImage(current, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    }

    private fun scaleImage(source: BufferedImage, width: Int, height: Int, interpolation: Any): BufferedImage {
        val out = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = out.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        g2.drawImage(source, 0, 0, width, height, null)
        g2.dispose()
        return out
    }
}

private enum class DiagonalMode(val propertyValue: String) {
    TL_LIGHT_BR_DARK("tl_light_br_dark"),
    TL_DARK_BR_LIGHT("tl_dark_br_light"),
    TR_LIGHT_BL_DARK("tr_light_bl_dark"),
    TR_DARK_BL_LIGHT("tr_dark_bl_light");

    companion object {
        fun fromProperty(value: String?): DiagonalMode {
            val normalized = value?.trim()?.lowercase() ?: return TL_LIGHT_BR_DARK
            return entries.firstOrNull { it.propertyValue == normalized } ?: TL_LIGHT_BR_DARK
        }
    }
}

private class WallpaperPanel(private val wallpaper: Image) : JPanel() {
    init {
        isOpaque = true
        background = Color.BLACK
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        val iw = wallpaper.getWidth(this).coerceAtLeast(1)
        val ih = wallpaper.getHeight(this).coerceAtLeast(1)
        val sx = width.toDouble() / iw.toDouble()
        val sy = height.toDouble() / ih.toDouble()
        val scale = minOf(sx, sy)
        val dw = (iw * scale).toInt().coerceAtLeast(1)
        val dh = (ih * scale).toInt().coerceAtLeast(1)
        val dx = (width - dw) / 2
        val dy = (height - dh) / 2
        g2.drawImage(wallpaper, dx, dy, dw, dh, this)
        g2.dispose()
    }
}

private class PatternPanel : JPanel() {
    init {
        isOpaque = true
        background = Color.BLACK
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val stripe = 36
        for (x in 0 until width step stripe) {
            g2.color = if ((x / stripe) % 2 == 0) Color(0x2E, 0x9A, 0xFF) else Color(0xFF, 0x58, 0x6E)
            g2.fillRect(x, 0, stripe, height)
        }
        g2.color = Color(255, 255, 255, 110)
        g2.fillOval(width / 5, height / 6, width / 2, height / 2)
        g2.color = Color(255, 230, 80, 140)
        g2.fillRect(width / 3, height / 2, width / 2, height / 3)
        g2.dispose()
    }
}

private fun <T> invokeAndWaitResultShot(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }
    var result: T? = null
    EventQueue.invokeAndWait { result = block() }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
