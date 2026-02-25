/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import dev.yumi.gradle.licenser.api.comment.CStyleHeaderComment
import dev.yumi.gradle.licenser.task.ApplyLicenseTask
import dev.yumi.gradle.licenser.task.CheckLicenseTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.SourceDirectorySet
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

plugins {
    id("dev.yumi.gradle.licenser")
}

license {
    rule(rootProject.file("HEADER"))

    // Cover source and Gradle script files across JVM and native modules.
    include(
        "**/*.java",
        "**/*.kt",
        "**/*.c",
        "**/*.cpp",
        "**/*.h",
        "**/*.hpp",
        "**/*.mm"
    )
}

plugins.withId("cpp-library") {
    license.headerCommentManager.register(
        setOf("mm"),
        CStyleHeaderComment.INSTANCE
    )

    val nativeSourceSets = extensions.getByType<NamedDomainObjectContainer<SourceDirectorySet>>()

    tasks.register<CheckLicenseTask>("checkLicenseNative", license).configure {
        description = "Checks whether source files in the native source set contain a valid license header."
        sourceFiles.from(nativeSourceSets)
        reportFile.set(layout.buildDirectory.file("reports/licenses/native-check-license-report.txt"))
    }
    tasks.register<ApplyLicenseTask>("applyLicenseNative", license).configure {
        description = "Applies the correct license headers to source files in the native source set."
        sourceFiles.from(nativeSourceSets)
        reportFile.set(layout.buildDirectory.file("reports/licenses/native-apply-license-report.txt"))
    }
}
