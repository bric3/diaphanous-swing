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
    id("diaphanous.java-library-conventions")
}

val bundledResourcesDir = layout.buildDirectory.dir("generated/resources/main")
val macosNativeVariant = providers.gradleProperty("diaphanous.nativeVariant")
    .orElse("debugRuntimeElements")
    .get()
val macosNativeRuntime by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    add(macosNativeRuntime.name, project(path = ":diaphanous-core-macos-native", configuration = macosNativeVariant))
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
