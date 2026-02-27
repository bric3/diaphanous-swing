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

import io.github.bric3.diaphanous.platform.macos.InternalMacosWindowStyler;

import java.awt.Window;
import java.util.Objects;
import java.util.Optional;

final class MacosWindowBackdropProvider implements WindowBackdropProvider {
    @Override
    public void install(Window window) {
        Objects.requireNonNull(window, "window");
        InternalMacosWindowStyler.installBackdrop(window);
    }

    @Override
    public void apply(Window window, WindowBackgroundEffectSpec spec) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(spec, "spec");
        if (!(spec instanceof MacosBackdropEffectSpec macStyle)) {
            throw new IllegalArgumentException(
                "Unsupported backdrop spec for macOS provider: " + spec.getClass().getName()
            );
        }
        InternalMacosWindowStyler.applyBackdrop(window, macStyle);
    }

    @Override
    public void clear(Window window) {
        Objects.requireNonNull(window, "window");
        InternalMacosWindowStyler.clearBackdrop(window);
    }

    @Override
    public void setWindowAlpha(Window window, double alpha) {
        Objects.requireNonNull(window, "window");
        InternalMacosWindowStyler.setWindowAlpha(window, alpha);
    }

    @Override
    public double defaultAlpha() {
        return InternalMacosWindowStyler.defaultBackdropAlpha();
    }

    @Override
    public Optional<WindowBackdropMaterialSpec> defaultMaterial() {
        return InternalMacosWindowStyler.defaultBackdropMaterial().map(value -> (WindowBackdropMaterialSpec) value);
    }

    @Override
    public Optional<WindowBackdropMaterialSpec> readMaterial(Window window) {
        Objects.requireNonNull(window, "window");
        return InternalMacosWindowStyler.readBackdropMaterial(window).map(value -> (WindowBackdropMaterialSpec) value);
    }

    @Override
    public Optional<Double> readAlpha(Window window) {
        Objects.requireNonNull(window, "window");
        return InternalMacosWindowStyler.readBackdropAlpha(window);
    }
}
