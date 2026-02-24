/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.backdrop;

import java.awt.Window;
import java.util.Optional;

/**
 * Internal SPI for OS-specific backdrop backends.
 */
interface WindowBackdropProvider {
    void apply(Window window, WindowBackgroundEffectSpec spec);

    void clear(Window window);

    void setWindowAlpha(Window window, double alpha);

    double defaultAlpha();

    Optional<WindowBackdropMaterialSpec> defaultMaterial();

    Optional<WindowBackdropMaterialSpec> readMaterial(Window window);

    Optional<Double> readAlpha(Window window);
}
