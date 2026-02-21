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

/**
 * OS-agnostic facade for native window backdrop effects.
 *
 * The facade accepts generic option markers and dispatches to the matching
 * platform implementation. Current built-in backend is macOS.
 */
public final class WindowBackdrop {
    private WindowBackdrop() {
    }

    /**
     * @return {@code true} when at least one built-in backdrop backend is available
     */
    public static boolean isSupported() {
        return NativeWindowBackdropManager.getSharedInstance().isSupported();
    }

    /**
     * Applies a platform-specific backdrop style.
     *
     * @param window target window
     * @param spec platform backdrop style carrier
     */
    public static void apply(Window window, WindowBackdropSpec spec) {
        NativeWindowBackdropManager.getSharedInstance().apply(window, spec);
    }

    /**
     * Clears any installed backdrop in the current platform backend.
     *
     * @param window target window
     */
    public static void clear(Window window) {
        NativeWindowBackdropManager.getSharedInstance().clear(window);
    }

    /**
     * Sets native window alpha when supported by the current backend.
     *
     * @param window target window
     * @param alpha alpha in range {@code [0,1]}
     */
    public static void setWindowAlpha(Window window, double alpha) {
        NativeWindowBackdropManager.getSharedInstance().setWindowAlpha(window, alpha);
    }

    /**
     * @return default backdrop alpha reported by the active backend, or {@code -1}
     */
    public static double defaultAlpha() {
        return NativeWindowBackdropManager.getSharedInstance().defaultAlpha();
    }

    /**
     * Reads the currently installed backdrop alpha.
     *
     * @param window target window
     * @return current alpha when available
     */
    public static Optional<Double> readAlpha(Window window) {
        return NativeWindowBackdropManager.getSharedInstance().readAlpha(window);
    }

    /**
     * @return default platform material when available
     */
    public static Optional<WindowBackdropMaterialSpec> defaultMaterial() {
        return NativeWindowBackdropManager.getSharedInstance().defaultMaterial();
    }

    /**
     * Reads the currently installed material.
     *
     * @param window target window
     * @return current platform material when available
     */
    public static Optional<WindowBackdropMaterialSpec> readMaterial(Window window) {
        return NativeWindowBackdropManager.getSharedInstance().readMaterial(window);
    }
}
