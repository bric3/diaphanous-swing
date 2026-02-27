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

import io.github.bric3.diaphanous.platform.macos.InternalMacosWindowStyler;

import java.awt.Window;
import java.util.Objects;

/**
 * macOS window decoration API (title bar / full-size content / toolbar style / appearance).
 * <p>
 * This type intentionally mirrors the scope of native decoration providers such as Darklaf platform
 * decorations, while excluding vibrancy/backdrop operations.
 */
public final class MacosWindowDecorations {
    private MacosWindowDecorations() {
    }

    /**
     * @return {@code true} when running on macOS
     */
    public static boolean isSupported() {
        return System.getProperty("os.name", "").contains("Mac");
    }

    /**
     * Applies title-bar and toolbar-related style flags on the native {@code NSWindow}.
     *
     * @param window target window
     * @param style decoration style configuration
     */
    public static void applyStyle(Window window, MacosWindowDecorationsSpec style) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(style, "style");
        InternalMacosWindowStyler.apply(window, style);
    }

    /**
     * Applies a native window appearance (system, aqua, dark aqua, vibrant variants).
     *
     * @param window target window
     * @param appearance appearance to apply
     */
    public static void applyAppearance(Window window, MacosWindowAppearanceSpec appearance) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(appearance, "appearance");
        InternalMacosWindowStyler.applyAppearance(window, appearance);
    }
}
