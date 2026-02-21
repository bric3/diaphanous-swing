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

/**
 * OS-agnostic facade for native window decoration controls.
 *
 * The facade accepts generic option markers and dispatches to the matching
 * platform implementation. Current built-in backend is macOS.
 */
public final class WindowDecorations {
    private WindowDecorations() {
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
    public static void applyStyle(Window window, WindowDecorationSpec spec) {
        NativeWindowDecorationsManager.getSharedInstance().applyStyle(window, spec);
    }

    /**
     * Applies a platform-specific window appearance.
     *
     * @param window target window
     * @param spec platform appearance carrier
     */
    public static void applyAppearance(Window window, WindowAppearanceSpec spec) {
        NativeWindowDecorationsManager.getSharedInstance().applyAppearance(window, spec);
    }
}
