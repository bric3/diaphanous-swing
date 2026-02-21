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
 * Values mapped 1:1 to AppKit {@code NSVisualEffectBlendingMode} used by
 * {@code NSVisualEffectView.blendingMode}.
 */
public enum MacosBackdropEffectBlendingMode {
    BEHIND_WINDOW(0),
    WITHIN_WINDOW(1);

    private final long nativeValue;

    MacosBackdropEffectBlendingMode(long nativeValue) {
        this.nativeValue = nativeValue;
    }

    public long nativeValue() {
        return nativeValue;
    }
}
