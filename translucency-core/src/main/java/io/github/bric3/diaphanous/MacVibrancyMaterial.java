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
 * Values mapped to macOS {@code NSVisualEffectMaterial}.
 */
public enum MacVibrancyMaterial {
    APPEARANCE_BASED(0),
    LIGHT(1),
    DARK(2),
    TITLEBAR(3),
    SELECTION(4),
    MENU(5),
    POPOVER(6),
    SIDEBAR(7),
    HEADER_VIEW(10),
    SHEET(11),
    WINDOW_BACKGROUND(12),
    HUD_WINDOW(13),
    FULL_SCREEN_UI(15),
    TOOLTIP(17),
    CONTENT_BACKGROUND(18),
    UNDER_WINDOW_BACKGROUND(21),
    UNDER_PAGE_BACKGROUND(22);

    private final long nativeValue;

    MacVibrancyMaterial(long nativeValue) {
        this.nativeValue = nativeValue;
    }

    long nativeValue() {
        return nativeValue;
    }
}
