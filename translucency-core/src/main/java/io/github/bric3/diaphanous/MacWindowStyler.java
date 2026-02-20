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
import java.lang.foreign.MemorySegment;
import java.util.Locale;
import java.util.Objects;

/**
 * Applies macOS-specific native window style attributes to Swing/AWT windows.
 * <p>
 * This implementation relies on internal AWT peer classes and Objective-C runtime calls.
 */
public final class MacWindowStyler {
    private static final long NS_WINDOW_STYLE_MASK_FULL_SIZE_CONTENT_VIEW = 1L << 15;
    private static final long NS_WINDOW_TITLE_VISIBLE = 0;
    private static final long NS_WINDOW_TITLE_HIDDEN = 1;

    private MacWindowStyler() {
    }

    /**
     * @return {@code true} when running on macOS
     */
    public static boolean isSupported() {
        return System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("mac");
    }

    /**
     * Applies a style configuration to the provided window.
     *
     * @param window the AWT/Swing window to style
     * @param style style options to apply
     * @throws UnsupportedOperationException when called on a non-macOS runtime
     * @throws IllegalStateException when the native {@code NSWindow} cannot be resolved or a native call fails
     */
    public static void apply(Window window, MacWindowStyle style) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(style, "style");

        if (!isSupported()) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        MacThreading.runOnAppKitThread(() -> applyOnAppKit(window, style));
    }

    private static void applyOnAppKit(Window window, MacWindowStyle style) {
        long nsWindowPtr = MacWindowPeerAccess.resolveNSWindowPointer(window);
        if (nsWindowPtr == 0) {
            throw new IllegalStateException("Cannot resolve NSWindow pointer");
        }

        MemorySegment nsWindow = MemorySegment.ofAddress(nsWindowPtr);

        long styleMask = ObjCRuntime.sendLong(nsWindow, "styleMask");
        if (style.fullSizeContentView()) {
            styleMask |= NS_WINDOW_STYLE_MASK_FULL_SIZE_CONTENT_VIEW;
        } else {
            styleMask &= ~NS_WINDOW_STYLE_MASK_FULL_SIZE_CONTENT_VIEW;
        }

        ObjCRuntime.sendVoidLong(nsWindow, "setStyleMask:", styleMask);
        ObjCRuntime.sendVoidBool(nsWindow, "setTitlebarAppearsTransparent:", style.transparentTitleBar());
        ObjCRuntime.sendVoidLong(
            nsWindow,
            "setTitleVisibility:",
            style.titleVisible() ? NS_WINDOW_TITLE_VISIBLE : NS_WINDOW_TITLE_HIDDEN
        );

        if (style.toolbarStyle() != null && ObjCRuntime.respondsToSelector(nsWindow, "setToolbarStyle:")) {
            ObjCRuntime.sendVoidLong(nsWindow, "setToolbarStyle:", style.toolbarStyle().nativeValue());
        }
    }
}
