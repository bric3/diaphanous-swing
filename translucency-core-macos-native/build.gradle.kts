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
}

group = "io.github.bric3"
version = "0.1.0-SNAPSHOT"

library {
    linkage.add(org.gradle.nativeplatform.Linkage.SHARED)
    targetMachines.add(machines.macOS)
}

tasks.withType(CppCompile::class).configureEach {
    source.from(fileTree("src/main/cpp") { include("**/*.mm") })
    compilerArgs.addAll(listOf("-std=c++17", "-x", "objective-c++", "-fobjc-arc"))
}

tasks.withType(LinkSharedLibrary::class).configureEach {
    linkerArgs.addAll(listOf("-framework", "Cocoa", "-framework", "Foundation"))
}
