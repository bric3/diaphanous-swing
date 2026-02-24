/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.nativeplatform.tasks.LinkSharedLibrary

plugins {
    `cpp-library`
    id("diaphanous.license-conventions")
}

group = "io.github.bric3"
version = "0.1.0-SNAPSHOT"

library {
    linkage.add(Linkage.SHARED)
    targetMachines.add(machines.macOS)
}

tasks.withType(CppCompile::class).configureEach {
    val javaHome = providers.systemProperty("java.home").get()
    val javaHomeDir = file(javaHome).let { if (it.name == "jre") it.parentFile else it }
    source.from(fileTree("src/main/cpp") { include("**/*.mm") })
    compilerArgs.addAll(
        listOf(
            "-std=c++17",
            "-x",
            "objective-c++",
            "-fobjc-arc",
            "-I${javaHomeDir.absolutePath}/include",
            "-I${javaHomeDir.absolutePath}/include/darwin"
        )
    )
}

tasks.withType(LinkSharedLibrary::class).configureEach {
    linkerArgs.addAll(listOf(
        "-framework",
        "Cocoa",
        "-framework",
        "Foundation"
    ))
}
