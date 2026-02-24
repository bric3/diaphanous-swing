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

    // source sets extension is not registered in cpp library, so this is manually created
    val sourceSet = objects.sourceDirectorySet(
        "native",
        "${"native"} sources"
    ).apply {
        srcDirs("src/main/cpp", "src/main/headers")
        include("**/*.c", "**/*.cpp", "**/*.h", "**/*.hpp", "**/*.mm")
    }
    val titleCaseName = "native".replaceFirstChar { it.uppercase() }
    tasks.register<CheckLicenseTask>("checkLicense$titleCaseName", license).configure {
        CheckLicenseTask.configureDefault(license, this.project, sourceSet, "native").execute(this)
    }
    tasks.register<ApplyLicenseTask>("applyLicense$titleCaseName", license).configure {
        ApplyLicenseTask.configureDefault(license, this.project, sourceSet, "native").execute(this)
    }
}
