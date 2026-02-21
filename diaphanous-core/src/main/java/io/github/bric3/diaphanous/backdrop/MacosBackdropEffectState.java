/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.backdrop;

/**
 * Values mapped 1:1 to AppKit {@code NSVisualEffectState} used by {@code NSVisualEffectView.state}.
 */
public enum MacosBackdropEffectState {
    FOLLOWS_WINDOW_ACTIVE_STATE(0),
    ACTIVE(1),
    INACTIVE(2);

    private final long nativeValue;

    MacosBackdropEffectState(long nativeValue) {
        this.nativeValue = nativeValue;
    }

    public long nativeValue() {
        return nativeValue;
    }
}
