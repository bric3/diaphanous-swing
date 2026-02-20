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
 * Values mapped to macOS {@code NSVisualEffectState}.
 */
public enum MacVibrancyState {
    FOLLOWS_WINDOW_ACTIVE_STATE(0),
    ACTIVE(1),
    INACTIVE(2);

    private final long nativeValue;

    MacVibrancyState(long nativeValue) {
        this.nativeValue = nativeValue;
    }

    long nativeValue() {
        return nativeValue;
    }
}
