import org.gradle.kotlin.dsl.robotTest
import org.gradle.kotlin.dsl.sourceSets
import io.github.bric3.diaphanous.buildlogic.passPropToJvm

/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
    id("diaphanous.kotlin-application-conventions")
    id("diaphanous.robot-test-conventions")
}

dependencies {
    implementation(project(":diaphanous-core"))
}

application {
    mainClass = "io.github.bric3.diaphanous.demo.DemoApp"
}

tasks.named<JavaExec>("run") {
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED"
    )
    val dumpSwing = System.getProperty("diaphanous.dump.swing")
    if (!dumpSwing.isNullOrBlank()) {
        systemProperty("diaphanous.dump.swing", dumpSwing)
    }
    val dumpNative = System.getProperty("diaphanous.dump.native")
    if (!dumpNative.isNullOrBlank()) {
        systemProperty("diaphanous.dump.native", dumpNative)
    }
}

tasks.robotTest {
    description = "Runs macOS desktop Robot smoke tests and writes screenshots/reports."

    passPropToJvm("diaphanous.robot.alpha")
    passPropToJvm("diaphanous.robot.diagonal")
}

tasks.register<Test>("robotShot") {
    description = "Captures a screenshot of the demo app via Robot."
    group = "verification"
    testClassesDirs = sourceSets.robotTest.get().output.classesDirs
    classpath = sourceSets.robotTest.get().runtimeClasspath
    outputs.dir(layout.buildDirectory.dir("reports/robotShot"))

    useJUnitPlatform()
    filter {
        includeTestsMatching("io.github.bric3.diaphanous.demo.ScreenshotRobotTest")
    }
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED"
    )
    systemProperty("diaphanous.robot.outputDir", layout.buildDirectory.dir("reports/robotShot").get().asFile.absolutePath)
    systemProperty("diaphanous.projectDir", rootProject.layout.projectDirectory.asFile.absolutePath)
    passPropToJvm("diaphanous.robot.wallpaper", robotTest.wallpaper)
    passPropToJvm("diaphanous.robot.alpha")
    passPropToJvm("diaphanous.robot.diagonal")
}
