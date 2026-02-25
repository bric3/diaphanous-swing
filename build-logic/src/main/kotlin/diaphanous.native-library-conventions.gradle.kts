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
import org.gradle.kotlin.dsl.`cpp-library`
import org.gradle.kotlin.dsl.domainObjectContainer

plugins {
    `cpp-library`
    id("diaphanous.base-conventions")
}

if (extensions.findByName("sourceSets") == null) {
    val sourceSets = objects.domainObjectContainer(SourceDirectorySet::class) {
        objects.sourceDirectorySet(it, "$it sources")
    }
    extensions.add("sourceSets", sourceSets)
}
