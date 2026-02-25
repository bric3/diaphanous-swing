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
    id("diaphanous.native-library-conventions")
    `jvm-toolchains`
}

tasks.withType(CppCompile::class).configureEach {
    val includedPlatformDirs = javaToolchains.launcherFor {}
        .map {
            val jvmIncludeDir = it.metadata.installationPath.asFile.resolve("include")
            buildList {
                add("-I${jvmIncludeDir.absolutePath}")
                jvmIncludeDir
                    .listFiles()
                    ?.forEach {
                        if (it.isDirectory && it.resolve("jni_md.h").isFile) {
                            logger.info("Found JNI platform include directory: ${it.absolutePath}")
                            add("-I${it.absolutePath}")
                        }
                    }
            }
        }

    compilerArgs.addAll(includedPlatformDirs)
}
