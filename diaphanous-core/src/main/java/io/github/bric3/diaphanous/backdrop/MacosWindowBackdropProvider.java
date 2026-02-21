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

import io.github.bric3.diaphanous.platform.macos.MacosWindowStyler;
import java.awt.Window;
import java.util.Objects;
import java.util.Optional;

final class MacosWindowBackdropProvider implements WindowBackdropProvider {
    @Override
    public void apply(Window window, WindowBackdropSpec spec) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(spec, "spec");
        if (!(spec instanceof MacosVibrancySpec macStyle)) {
            throw new IllegalArgumentException(
                "Unsupported backdrop spec for macOS provider: " + spec.getClass().getName()
            );
        }
        MacosWindowStyler.applyVibrancy(window, macStyle);
    }

    @Override
    public void clear(Window window) {
        Objects.requireNonNull(window, "window");
        MacosWindowStyler.clearVibrancy(window);
    }

    @Override
    public void setWindowAlpha(Window window, double alpha) {
        Objects.requireNonNull(window, "window");
        MacosWindowStyler.setWindowAlpha(window, alpha);
    }

    @Override
    public double defaultAlpha() {
        return MacosWindowStyler.defaultBackdropAlpha();
    }

    @Override
    public Optional<WindowBackdropMaterialSpec> defaultMaterial() {
        return MacosWindowStyler.defaultBackdropMaterial().map(value -> (WindowBackdropMaterialSpec) value);
    }

    @Override
    public Optional<WindowBackdropMaterialSpec> readMaterial(Window window) {
        Objects.requireNonNull(window, "window");
        return MacosWindowStyler.readBackdropMaterial(window).map(value -> (WindowBackdropMaterialSpec) value);
    }

    @Override
    public Optional<Double> readAlpha(Window window) {
        Objects.requireNonNull(window, "window");
        return MacosWindowStyler.readBackdropAlpha(window);
    }
}
