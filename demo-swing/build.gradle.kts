/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
    id("diaphanous.kotlin-application-conventions")
}

val robotTest by sourceSets.creating
robotTest.compileClasspath += sourceSets.main.get().output
robotTest.runtimeClasspath += sourceSets.main.get().output

dependencies {
    implementation(project(":translucency-core"))
    add("robotTestImplementation", platform(libs.junit.bom))
    add("robotTestImplementation", libs.junit.jupiter)
    add("robotTestRuntimeOnly", libs.junit.platform.launcher)
}

application {
    mainClass = "io.github.bric3.diaphanous.demo.DemoApp"
}

tasks.named<JavaExec>("run") {
    dependsOn(":translucency-core-macos-native:assemble")
    val nativeLib = rootProject.layout.projectDirectory
        .dir("translucency-core-macos-native/build/lib/main/debug")
        .file("libtranslucency-core-macos-native.dylib")
        .asFile
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
    )
    systemProperty("diaphanous.macos.nativeLib", nativeLib.absolutePath)
    val dumpSwing = System.getProperty("diaphanous.dump.swing")
    if (!dumpSwing.isNullOrBlank()) {
        systemProperty("diaphanous.dump.swing", dumpSwing)
    }
    val dumpNative = System.getProperty("diaphanous.dump.native")
    if (!dumpNative.isNullOrBlank()) {
        systemProperty("diaphanous.dump.native", dumpNative)
    }
}

configurations[robotTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[robotTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

fun passRobotPropToJvm(task: Test, key: String) {
    val value = providers.gradleProperty(key)
        .orElse(providers.systemProperty(key))
        .orNull
    if (!value.isNullOrBlank()) {
        task.systemProperty(key, value)
    }
}

val robotTestTask = tasks.register<Test>("robotTest") {
    dependsOn(":translucency-core-macos-native:assemble")
    val nativeLib = rootProject.layout.projectDirectory
        .dir("translucency-core-macos-native/build/lib/main/debug")
        .file("libtranslucency-core-macos-native.dylib")
        .asFile
    description = "Runs macOS desktop Robot smoke tests and writes screenshots/reports."
    group = "verification"
    testClassesDirs = robotTest.output.classesDirs
    classpath = robotTest.runtimeClasspath
    outputs.dir(layout.buildDirectory.dir("reports/robotTest"))

    useJUnitPlatform()
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
    )
    systemProperty("diaphanous.robot.outputDir", layout.buildDirectory.dir("reports/robotTest").get().asFile.absolutePath)
    systemProperty("diaphanous.macos.nativeLib", nativeLib.absolutePath)
    passRobotPropToJvm(this, "diaphanous.robot.wallpaperName")
    passRobotPropToJvm(this, "diaphanous.robot.wallpaper")
    passRobotPropToJvm(this, "diaphanous.robot.alpha")
    passRobotPropToJvm(this, "diaphanous.robot.diagonal")
}

tasks.register<Test>("robotShot") {
    dependsOn(":translucency-core-macos-native:assemble")
    val nativeLib = rootProject.layout.projectDirectory
        .dir("translucency-core-macos-native/build/lib/main/debug")
        .file("libtranslucency-core-macos-native.dylib")
        .asFile
    description = "Captures a screenshot of the demo app via Robot."
    group = "verification"
    testClassesDirs = robotTest.output.classesDirs
    classpath = robotTest.runtimeClasspath
    outputs.dir(layout.buildDirectory.dir("reports/robotShot"))

    useJUnitPlatform()
    filter {
        includeTestsMatching("io.github.bric3.diaphanous.demo.ScreenshotRobotTest")
    }
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
    )
    systemProperty("diaphanous.robot.outputDir", layout.buildDirectory.dir("reports/robotShot").get().asFile.absolutePath)
    systemProperty("diaphanous.macos.nativeLib", nativeLib.absolutePath)
    systemProperty("diaphanous.projectDir", rootProject.layout.projectDirectory.asFile.absolutePath)
    passRobotPropToJvm(this, "diaphanous.robot.wallpaperName")
    passRobotPropToJvm(this, "diaphanous.robot.wallpaper")
    passRobotPropToJvm(this, "diaphanous.robot.alpha")
    passRobotPropToJvm(this, "diaphanous.robot.diagonal")
}

tasks.named("check") {
    dependsOn(robotTestTask)
}
