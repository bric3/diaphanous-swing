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

import java.awt.Window;

final class MacosWindowDecorationsProvider implements WindowDecorationsProvider {
    @Override
    public void applyStyle(Window window, WindowDecorationSpec spec) {
        if (!(spec instanceof MacosWindowDecorationsSpec macStyle)) {
            throw new IllegalArgumentException(
                "Unsupported style spec for macOS provider: " + spec.getClass().getName()
            );
        }
        MacosWindowDecorations.applyStyle(window, macStyle);
    }

    @Override
    public void applyAppearance(Window window, WindowAppearanceSpec spec) {
        if (!(spec instanceof MacosWindowAppearanceSpec macAppearance)) {
            throw new IllegalArgumentException(
                "Unsupported appearance spec for macOS provider: " + spec.getClass().getName()
            );
        }
        MacosWindowDecorations.applyAppearance(window, macAppearance);
    }
}
