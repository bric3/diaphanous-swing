/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import io.github.bric3.diaphanous.buildlogic.SwingRobotTestExtension
import io.github.bric3.diaphanous.buildlogic.gradleOrSystemProperty
import io.github.bric3.diaphanous.buildlogic.passPropToJvm
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

plugins {
    java
}

val robotTestExtension = extensions.create("robotTest", SwingRobotTestExtension::class.java)
val sourceSets = extensions.getByType(SourceSetContainer::class.java)
val main = sourceSets.named("main")
val robotTest = sourceSets.findByName("robotTest") ?: sourceSets.create("robotTest")

robotTest.compileClasspath += main.get().output
robotTest.runtimeClasspath += main.get().output
robotTest.resources.srcDir(robotTestExtension.wallpaperAssetDir)

configurations.named(robotTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(robotTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    add(robotTest.implementationConfigurationName, platform(libs.findLibrary("junit.bom").get()))
    add(robotTest.implementationConfigurationName, libs.findLibrary("junit.jupiter").get())
    add(robotTest.runtimeOnlyConfigurationName, libs.findLibrary("junit.platform.launcher").get())
}

val robotTestTask = tasks.register("robotTest", Test::class.java) {
    description = "Runs desktop Robot tests."
    group = "verification"
    testClassesDirs = robotTest.output.classesDirs
    classpath = robotTest.runtimeClasspath
    outputs.dir(layout.buildDirectory.dir("reports/robotTest"))

    useJUnitPlatform()
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED"
    )
    systemProperty(
        "diaphanous.robot.outputDir",
        layout.buildDirectory.dir("reports/robotTest").get().asFile.absolutePath
    )
    systemProperty("diaphanous.projectDir", rootProject.layout.projectDirectory.asFile.absolutePath)

    passPropToJvm("diaphanous.robot.wallpaper", robotTestExtension.wallpaper)
}

tasks.named("check") {
    dependsOn(robotTestTask)
}
