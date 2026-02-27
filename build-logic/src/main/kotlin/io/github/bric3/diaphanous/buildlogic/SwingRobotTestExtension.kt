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

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class SwingRobotTestExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val wallpaperAssetDir: DirectoryProperty = objects.directoryProperty()
        .convention(layout.projectDirectory.dir("assets/wallpaper"))

    val javaLogoResourcePath: Property<String> = objects.property(String::class.java)
        .convention("java-logo-3840x2160-15990.png")

    val macbookKeyboardResourcePath: Property<String> = objects.property(String::class.java)
        .convention("macbook-keyboard-apple-event-apple-keyboard-ambient-lighting-3840x2160-6689.jpg")

    val wallpaper: Property<String> = objects.property(String::class.java)
        .convention(macbookKeyboardResourcePath)
}
