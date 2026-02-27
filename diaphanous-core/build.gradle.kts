/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import io.github.bric3.diaphanous.buildlogic.gradleOrSystemProperty
import io.github.bric3.diaphanous.buildlogic.passPropToJvm

plugins {
    id("diaphanous.java-library-conventions")
    id("diaphanous.robot-test-conventions")
}

val bundledResourcesDir = layout.buildDirectory.dir("generated/resources/main")
val macosNativeVariant = gradleOrSystemProperty("diaphanous.nativeVariant")
    .orElse("debugRuntimeElements")
    .get()
val macosNativeRuntime by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    dependencies.add(project.dependencies.project(":diaphanous-core-macos-native", macosNativeVariant))
}

val bundleMacosNativeLib by tasks.registering(Sync::class) {
    from(macosNativeRuntime)
    into(bundledResourcesDir.map { it.dir("native/macos") })
    rename { "libdiaphanous-core-macos-native-macos-aarch64.dylib" }
}

sourceSets {
    main {
        resources.srcDir(bundledResourcesDir)
    }
}

tasks.named("processResources") {
    dependsOn(bundleMacosNativeLib)
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.robotTest {
    description = "Runs macOS desktop Robot screenshot tests and compares to baselines."
    systemProperty("diaphanous.robot.baselineDir", layout.projectDirectory.dir("src/robotTest/resources").asFile.absolutePath)
    // Common values:
    // - unset property (system/default appearance)
    // - NSAppearanceNameAqua
    // - NSAppearanceNameDarkAqua
    // - NSAppearanceNameVibrantLight
    // - NSAppearanceNameVibrantDark
    passPropToJvm(
        "apple.awt.application.appearance",
        providers.provider { "NSAppearanceNameDarkAqua" }
    )

    passPropToJvm("diaphanous.robot.record")
    passPropToJvm("diaphanous.robot.tolerance")
}

tasks.register<JavaExec>("generateExamplesMarkdown") {
    description = "Generates markdown examples from robot test shot definitions and baseline image paths."
    group = "documentation"
    dependsOn("robotTestClasses")
    classpath = sourceSets.robotTest.get().runtimeClasspath
    mainClass.set("io.github.bric3.diaphanous.robot.docs.ExamplesMarkdownGenerator")

    val outputPath = gradleOrSystemProperty("diaphanous.examples.output").orElse("EXAMPLES.md")
    args(outputPath.get())
    systemProperty("diaphanous.projectDir", layout.projectDirectory.asFile.absolutePath)
}
