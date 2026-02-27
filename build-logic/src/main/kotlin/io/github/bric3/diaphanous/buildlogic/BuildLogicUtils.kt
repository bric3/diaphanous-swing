/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.buildlogic

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test

fun Project.gradleOrSystemProperty(key: String): Provider<String> {
    return providers.gradleProperty(key).orElse(providers.systemProperty(key))
}

fun Test.passPropToJvm(key: String, valueProvider: Provider<String>? = null) {
    val value = if (valueProvider != null) {
        project.gradleOrSystemProperty(key).orElse(valueProvider).orNull
    } else {
        project.gradleOrSystemProperty(key).orNull
    }
    if (!value.isNullOrBlank()) {
        systemProperty(key, value)
    }
}

