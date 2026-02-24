/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.decorations;

/**
 * macOS appearance presets applicable to {@code NSWindow}.
 */
public enum MacosWindowAppearanceSpec implements WindowAppearanceSpec {
    SYSTEM(null),
    AQUA("NSAppearanceNameAqua"),
    DARK_AQUA("NSAppearanceNameDarkAqua"),
    VIBRANT_LIGHT("NSAppearanceNameVibrantLight"),
    VIBRANT_DARK("NSAppearanceNameVibrantDark");

    private final String nativeName;

    MacosWindowAppearanceSpec(String nativeName) {
        this.nativeName = nativeName;
    }

    public String nativeName() {
        return nativeName;
    }
}
