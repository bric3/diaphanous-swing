/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.decorations;

import java.awt.Window;
import java.util.Optional;
import java.util.function.Predicate;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;

/**
 * OS-agnostic facade for native window decoration controls.
 *
 * The facade accepts generic option markers and dispatches to the matching
 * platform implementation. Current built-in backend is macOS.
 */
public final class WindowPresentations {
    private static final String APPEARANCE_KEY = "diaphanous.windowAppearanceSpec";

    private WindowPresentations() {
    }

    /**
     * @return {@code true} when at least one built-in backend is available
     */
    public static boolean isSupported() {
        return NativeWindowDecorationsManager.getSharedInstance().isSupported();
    }

    /**
     * Applies a platform-specific decoration style.
     *
     * @param window target window
     * @param spec platform style carrier
     */
    public static void applyDecorations(Window window, WindowDecorationSpec spec) {
        NativeWindowDecorationsManager.getSharedInstance().applyDecorations(window, spec);
    }

    /**
     * Applies a platform-specific window appearance.
     *
     * @param window target window
     * @param spec platform appearance carrier
     */
    public static void applyAppearance(Window window, WindowAppearanceSpec spec) {
        NativeWindowDecorationsManager.getSharedInstance().applyAppearance(window, spec);
        if (window instanceof RootPaneContainer container) {
            JRootPane rootPane = container.getRootPane();
            if (rootPane != null) {
                rootPane.putClientProperty(APPEARANCE_KEY, spec);
            }
        }
    }

    /**
     * Returns the last applied appearance spec tracked on this window.
     *
     * @param window target window
     * @return tracked appearance spec when available
     */
    public static Optional<WindowAppearanceSpec> currentAppearance(Window window) {
        if (!(window instanceof RootPaneContainer container)) {
            return Optional.empty();
        }
        JRootPane rootPane = container.getRootPane();
        if (rootPane == null) {
            return Optional.empty();
        }
        Object value = rootPane.getClientProperty(APPEARANCE_KEY);
        if (value instanceof WindowAppearanceSpec spec) {
            return Optional.of(spec);
        }
        return Optional.empty();
    }

    /**
     * Predicate used by backdrop integration to decide whether Swing erase
     * should be enabled for the current window appearance.
     *
     * @return compatibility predicate
     */
    public static Predicate<Window> isCompatibleWithBackdropPredicate() {
        return NativeWindowDecorationsManager.getSharedInstance().isCompatibleWithBackdropPredicate();
    }
}
