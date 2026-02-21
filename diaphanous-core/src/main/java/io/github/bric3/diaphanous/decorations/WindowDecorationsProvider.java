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
import java.util.function.Predicate;

/**
 * Internal SPI for OS-specific decoration backends.
 */
interface WindowDecorationsProvider {
    void applyDecorations(Window window, WindowDecorationSpec spec);

    void applyAppearance(Window window, WindowAppearanceSpec spec);

    Predicate<Window> isCompatibleWithBackdropPredicate();
}
