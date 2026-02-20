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
    id("diaphanous.kotlin-application-conventions")
}

dependencies {
    implementation(project(":translucency-core"))
}

application {
    mainClass = "io.github.bric3.diaphanous.demo.DemoApp"
}

tasks.named<JavaExec>("run") {
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
    )
}
