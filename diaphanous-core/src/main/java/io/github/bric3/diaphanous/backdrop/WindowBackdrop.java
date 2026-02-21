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

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;
import javax.swing.*;

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
     * This enables the Swing contentPane erasing. For this to work, it is necessary to use
     * contentPane to be {@link RootErasingContentPane} or use of
     * {@link ComponentBackdropSupport#clearBackgroundIfEnabled(Graphics, JComponent)}.
     *
     * @param window target window
     * @param spec platform backdrop style carrier
     */
    public static void apply(Window window, WindowBackdropSpec spec) {
        apply(window, spec, null);
    }

    /**
     * Applies a platform-specific backdrop style and toggles Swing backdrop erase
     * depending on the activation predicate.
     *
     * Predicate controls whether Swing backdrop erase is enabled. For this to work,
     * it is necessary to use contentPane to be {@link RootErasingContentPane} or use of
     * {@link ComponentBackdropSupport#clearBackgroundIfEnabled(Graphics, JComponent)}.
     *
     * @param window target window
     * @param spec platform backdrop style carrier
     * @param activationPredicate predicate deciding if backdrop erase is enabled
     */
    public static void apply(Window window, WindowBackdropSpec spec, Predicate<Window> activationPredicate) {
        NativeWindowBackdropManager.getSharedInstance().apply(window, spec);
        boolean enabled = shouldEnableErase(window, spec)
            && (activationPredicate == null || activationPredicate.test(window));
        ComponentBackdropSupport.setEraseEnabled(window, enabled);
    }

    /**
     * Clears any installed backdrop in the current platform backend.
     *
     * @param window target window
     */
    public static void remove(Window window) {
        NativeWindowBackdropManager.getSharedInstance().clear(window);
        ComponentBackdropSupport.setEraseEnabled(window, false);
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

    private static boolean shouldEnableErase(Window window, WindowBackdropSpec spec) {
        if (!isSupported() || window == null || spec == null) {
            return false;
        }
        if (!(window instanceof RootPaneContainer)) {
            return false;
        }
        if (spec instanceof MacosVibrancySpec macSpec) {
            return macSpec.enabled();
        }
        return true;
    }
}
