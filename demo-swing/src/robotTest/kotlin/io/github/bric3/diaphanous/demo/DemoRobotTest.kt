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

import io.github.bric3.diaphanous.backdrop.ComponentBackdropSupport
import io.github.bric3.diaphanous.decorations.MacosToolbarStyle
import io.github.bric3.diaphanous.decorations.MacosWindowAppearanceSpec
import java.awt.Component
import java.awt.Container
import java.awt.EventQueue
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import javax.imageio.ImageIO
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

class DemoRobotTest {
    @Test
    fun decoratedWindowSmoke() {
        assumeTrue(System.getProperty("os.name", "").contains("Mac", ignoreCase = true))
        assumeFalse(GraphicsEnvironment.isHeadless())

        DemoApp.main(arrayOf("decorated"))
        val frame = waitForDemoFrame(Duration.ofSeconds(10))
        assertNotNull(frame, "Demo frame was not found within timeout")
        val demoFrame = requireNotNull(frame)

        val outputDir = resolveOutputDir()
        val report = StringBuilder()
        report.appendLine("Demo robot smoke test (decorated mode)")
        report.appendLine("Timestamp: ${Instant.now()}")
        report.appendLine("Frame bounds: ${demoFrame.bounds}")

        val robot = Robot().apply { autoDelay = 120 }
        capture(demoFrame, outputDir.resolve("01-initial.png"), robot)
        report.appendLine("Captured: 01-initial.png")

        val appearanceCombo = invokeAndWaitResult {
            findByName(demoFrame.rootPane, "appearanceCombo", JComboBox::class.java)
        }
        val toolbarCombo = invokeAndWaitResult {
            findByName(demoFrame.rootPane, "toolbarStyleCombo", JComboBox::class.java)
        }
        val transparentTitleBarCheck = invokeAndWaitResult {
            findByName(demoFrame.rootPane, "transparentTitleBarCheck", JCheckBox::class.java)
        }
        assertNotNull(appearanceCombo, "appearanceCombo not found")
        assertNotNull(toolbarCombo, "toolbarStyleCombo not found")
        assertNotNull(transparentTitleBarCheck, "transparentTitleBarCheck not found")

        clickComponent(robot, requireNotNull(appearanceCombo))
        EventQueue.invokeAndWait {
            appearanceCombo.selectedItem = MacosWindowAppearanceSpec.AQUA
        }
        Thread.sleep(300)
        val disabledOnAqua = invokeAndWaitResult { ComponentBackdropSupport.isEnabledFor(demoFrame.rootPane) }
        assertFalse(disabledOnAqua, "Backdrop erase should be disabled for AQUA")

        clickComponent(robot, requireNotNull(appearanceCombo))
        EventQueue.invokeAndWait {
            appearanceCombo.selectedItem = MacosWindowAppearanceSpec.VIBRANT_DARK
        }
        clickComponent(robot, requireNotNull(toolbarCombo))
        EventQueue.invokeAndWait {
            toolbarCombo.selectedItem = MacosToolbarStyle.EXPANDED
        }
        clickComponent(robot, requireNotNull(transparentTitleBarCheck))
        clickComponent(robot, requireNotNull(transparentTitleBarCheck))

        Thread.sleep(600)
        val enabledOnVibrant = invokeAndWaitResult { ComponentBackdropSupport.isEnabledFor(demoFrame.rootPane) }
        assertTrue(enabledOnVibrant, "Backdrop erase should be enabled for VIBRANT_DARK")
        capture(demoFrame, outputDir.resolve("02-after-controls.png"), robot)
        report.appendLine("Captured: 02-after-controls.png")

        EventQueue.invokeAndWait { demoFrame.dispose() }
        report.appendLine("Result: OK")
        Files.writeString(outputDir.toPath().resolve("report.txt"), report.toString())
    }

    private fun resolveOutputDir(): File {
        val configured = System.getProperty("diaphanous.robot.outputDir")
        val dir = if (configured.isNullOrBlank()) File("build/reports/robotTest") else File(configured)
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

    private fun capture(frame: JFrame, file: File, robot: Robot) {
        val target = invokeAndWaitResult {
            val location = frame.locationOnScreen
            Rectangle(location.x, location.y, frame.width, frame.height)
        }
        val image: BufferedImage = robot.createScreenCapture(target)
        ImageIO.write(image, "png", file)
    }

    private fun clickComponent(robot: Robot, component: Component) {
        val target = invokeAndWaitResult {
            val location = component.locationOnScreen
            Rectangle(location.x, location.y, component.width, component.height)
        }
        val x = target.x + (target.width / 2)
        val y = target.y + (target.height / 2)
        robot.mouseMove(x, y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        robot.waitForIdle()
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
}

private fun <T> invokeAndWaitResult(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }
    var result: T? = null
    EventQueue.invokeAndWait { result = block() }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
