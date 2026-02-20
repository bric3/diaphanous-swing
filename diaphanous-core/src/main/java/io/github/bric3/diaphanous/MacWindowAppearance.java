/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous;

/**
 * macOS appearance presets applicable to {@code NSWindow}.
 */
public enum MacWindowAppearance {
    SYSTEM(null),
    AQUA("NSAppearanceNameAqua"),
    DARK_AQUA("NSAppearanceNameDarkAqua"),
    VIBRANT_LIGHT("NSAppearanceNameVibrantLight"),
    VIBRANT_DARK("NSAppearanceNameVibrantDark");

    private final String nativeName;

    MacWindowAppearance(String nativeName) {
        this.nativeName = nativeName;
    }

    String nativeName() {
        return nativeName;
    }
}
