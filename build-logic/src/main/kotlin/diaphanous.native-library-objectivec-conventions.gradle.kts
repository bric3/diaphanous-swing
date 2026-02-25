/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import org.gradle.api.file.SourceDirectorySet
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.nativeplatform.tasks.LinkSharedLibrary

plugins {
    id("diaphanous.native-library-conventions")
    id("diaphanous.license-conventions")
}

val sourceSets = extensions.getByType<NamedDomainObjectContainer<SourceDirectorySet>>()

val privateHeadersSource = sourceSets.register("privateHeaders") {
    srcDir("src/main/headers")
    include("**/*.h", "**/*.hpp")
}

val publicHeadersSource = sourceSets.register("publicHeaders") {
    srcDir("src/main/public")
    include("**/*.h", "**/*.hpp")
}

val objectiveCppSource = sourceSets.register("objectiveCpp") {
    srcDir("src/main/cpp")
    include("**/*.mm")
}

library {
    linkage.add(Linkage.SHARED)
    targetMachines.add(machines.macOS)
    privateHeaders.from(privateHeadersSource.map { it.srcDirs })
    publicHeaders.from(publicHeadersSource.map { it.srcDirs })
}

tasks.withType(CppCompile::class).configureEach {
    source.from(objectiveCppSource)
    compilerArgs.addAll(
        "-std=c++17",
        "-x",
        "objective-c++",
        "-fobjc-arc",
    )
}

tasks.withType(LinkSharedLibrary::class).configureEach {
    linkerArgs.addAll("-framework", "Cocoa", "-framework", "Foundation")
}
