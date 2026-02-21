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

import java.awt.Window;
import java.util.Objects;
import java.util.Optional;

/**
 * macOS backdrop API ({@code NSVisualEffectView}-based vibrancy and related runtime values).
 * <p>
 * This type is intentionally separate from {@link MacWindowDecorations} so decoration flags and
 * backdrop concerns can be managed independently.
 */
public final class MacWindowBackdrop {
    private MacWindowBackdrop() {
    }

    /**
     * @return {@code true} when running on macOS
     */
    public static boolean isSupported() {
        return MacWindowStyler.isSupported();
    }

    /**
     * Applies vibrancy/backdrop to the target window.
     *
     * @param window target window
     * @param style vibrancy style
     */
    public static void apply(Window window, MacVibrancyStyle style) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(style, "style");
        MacWindowStyler.applyVibrancy(window, style);
    }

    /**
     * Removes previously installed vibrancy/backdrop from the target window.
     *
     * @param window target window
     */
    public static void clear(Window window) {
        Objects.requireNonNull(window, "window");
        MacWindowStyler.clearVibrancy(window);
    }

    /**
     * Sets {@code NSWindow.alphaValue} on AppKit thread.
     *
     * @param window target window
     * @param alpha alpha in range {@code [0,1]}
     */
    public static void setWindowAlpha(Window window, double alpha) {
        Objects.requireNonNull(window, "window");
        MacWindowStyler.setWindowAlpha(window, alpha);
    }

    /**
     * @return native default alpha for a fresh {@code NSVisualEffectView}; {@code -1} if unavailable
     */
    public static double defaultAlpha() {
        return MacWindowStyler.defaultBackdropAlpha();
    }

    /**
     * @param window target window
     * @return currently installed backdrop alpha when available
     */
    public static Optional<Double> readAlpha(Window window) {
        Objects.requireNonNull(window, "window");
        return MacWindowStyler.readBackdropAlpha(window);
    }

    /**
     * @return native default material for a fresh {@code NSVisualEffectView}, when available
     */
    public static Optional<MacVibrancyMaterial> defaultMaterial() {
        return MacWindowStyler.defaultBackdropMaterial();
    }

    /**
     * @param window target window
     * @return currently installed backdrop material when available
     */
    public static Optional<MacVibrancyMaterial> readMaterial(Window window) {
        Objects.requireNonNull(window, "window");
        return MacWindowStyler.readBackdropMaterial(window);
    }
}

