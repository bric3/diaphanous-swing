/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.decorations;

/**
 * Toolbar styles mapped to macOS {@code NSWindowToolbarStyle} values.
 */
public enum MacosToolbarStyle {
    AUTOMATIC(0),
    EXPANDED(1),
    PREFERENCE(2),
    UNIFIED(3),
    UNIFIED_COMPACT(4);

    private final long nativeValue;

    MacosToolbarStyle(long nativeValue) {
        this.nativeValue = nativeValue;
    }

    public long nativeValue() {
        return nativeValue;
    }
}
