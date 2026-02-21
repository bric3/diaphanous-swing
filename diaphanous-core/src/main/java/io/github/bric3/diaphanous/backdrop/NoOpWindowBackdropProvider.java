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

import java.awt.Window;
import java.util.Optional;

final class NoOpWindowBackdropProvider implements WindowBackdropProvider {
    @Override
    public void apply(Window window, WindowBackgroundEffectSpec spec) {
        throw unsupported("style", spec);
    }

    @Override
    public void clear(Window window) {
    }

    @Override
    public void setWindowAlpha(Window window, double alpha) {
    }

    @Override
    public double defaultAlpha() {
        return -1.0d;
    }

    @Override
    public Optional<WindowBackdropMaterialSpec> defaultMaterial() {
        return Optional.empty();
    }

    @Override
    public Optional<WindowBackdropMaterialSpec> readMaterial(Window window) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> readAlpha(Window window) {
        return Optional.empty();
    }

    private IllegalArgumentException unsupported(String kind, Object spec) {
        return new IllegalArgumentException(
            "No window backdrop provider for this OS. Unsupported " + kind + " spec: " + spec.getClass().getName()
        );
    }
}
