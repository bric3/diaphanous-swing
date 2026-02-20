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
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
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
    private static final long NS_VIEW_WIDTH_SIZABLE = 1L << 1;
    private static final long NS_VIEW_HEIGHT_SIZABLE = 1L << 4;
    private static final long NS_WINDOW_ORDERING_BELOW = -1L;
    private static final long OBJC_ASSOCIATION_ASSIGN = 0L;
    private static final long OBJC_ASSOCIATION_RETAIN_NONATOMIC = 1L;
    private static final MemorySegment VIBRANCY_ASSOCIATED_KEY = Arena.global().allocate(ValueLayout.JAVA_LONG);
    private static final boolean DUMP_NATIVE = Boolean.getBoolean("diaphanous.dump.native");

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

    /**
     * Applies an {@code NSVisualEffectView}-based backdrop (vibrancy) to the window content.
     *
     * @param window the AWT/Swing window to mutate
     * @param style vibrancy configuration
     */
    public static void applyVibrancy(Window window, MacVibrancyStyle style) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(style, "style");

        if (!isSupported()) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        MacThreading.runOnAppKitThread(() -> applyVibrancyOnAppKit(window, style));
    }

    /**
     * Removes the previously installed vibrancy backdrop view from the window, if present.
     *
     * @param window the AWT/Swing window to mutate
     */
    public static void clearVibrancy(Window window) {
        Objects.requireNonNull(window, "window");

        if (!isSupported()) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        MacThreading.runOnAppKitThread(() -> clearVibrancyOnAppKit(window));
    }

    /**
     * Applies a macOS appearance to the native window frame/titlebar.
     *
     * @param window the AWT/Swing window to mutate
     * @param appearance appearance preset; {@link MacWindowAppearance#SYSTEM} restores system default
     */
    public static void applyAppearance(Window window, MacWindowAppearance appearance) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(appearance, "appearance");

        if (!isSupported()) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        MacThreading.runOnAppKitThread(() -> applyAppearanceOnAppKit(window, appearance));
    }

    private static void applyOnAppKit(Window window, MacWindowStyle style) {
        long nsWindowPtr = MacWindowPeerAccess.resolveNSWindowPointer(window);
        if (nsWindowPtr == 0) {
            throw new IllegalStateException("Cannot resolve NSWindow pointer");
        }
        if (DUMP_NATIVE) {
            MacNativeVibrancyBridge.dump(nsWindowPtr);
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

    private static void applyVibrancyOnAppKit(Window window, MacVibrancyStyle style) {
        try {
            MacWindowPeerAccess.setPeerOpaque(window, false);
        } catch (RuntimeException ignored) {
            // Some peers may reject opacity changes depending on lifecycle/state.
        }

        long nsWindowPtr = MacWindowPeerAccess.resolveNSWindowPointer(window);
        if (nsWindowPtr == 0) {
            throw new IllegalStateException("Cannot resolve NSWindow pointer");
        }

        MemorySegment nsWindow = MemorySegment.ofAddress(nsWindowPtr);
        MemorySegment effectView = ObjCRuntime.getAssociatedObject(nsWindow, VIBRANCY_ASSOCIATED_KEY);
        MemorySegment contentView = requireAddress(ObjCRuntime.sendAddress(nsWindow, "contentView"), "NSWindow contentView");

        if (!style.enabled()) {
            MacNativeVibrancyBridge.remove(nsWindowPtr);
            clearVibrancyOnAppKit(window);
            return;
        }

        int material = (int) style.material().nativeValue();
        if (MacNativeVibrancyBridge.isAvailable()) {
            boolean updated = MacNativeVibrancyBridge.update(nsWindowPtr, material, style.backdropAlpha());
            if (updated) {
                if (DUMP_NATIVE) {
                    MacNativeVibrancyBridge.dump(nsWindowPtr);
                }
                return;
            }
            boolean installed = MacNativeVibrancyBridge.install(nsWindowPtr, material, style.backdropAlpha());
            if (installed) {
                if (DUMP_NATIVE) {
                    MacNativeVibrancyBridge.dump(nsWindowPtr);
                }
                return;
            }
        }

        if (isNil(effectView)) {
            effectView = createEffectSiblingView(nsWindow, contentView);
        }

        configureWindowForBackdrop(nsWindow, contentView);
        ObjCRuntime.sendVoidLong(effectView, "setMaterial:", style.material().nativeValue());
        ObjCRuntime.sendVoidLong(effectView, "setBlendingMode:", style.blendingMode().nativeValue());
        ObjCRuntime.sendVoidLong(effectView, "setState:", style.state().nativeValue());
        ObjCRuntime.sendVoidDouble(effectView, "setAlphaValue:", style.backdropAlpha());
        applyVibrantAppearance(nsWindow, effectView);
        if (ObjCRuntime.respondsToSelector(effectView, "setEmphasized:")) {
            ObjCRuntime.sendVoidBool(effectView, "setEmphasized:", style.emphasized());
        }
        if (DUMP_NATIVE) {
            MacNativeVibrancyBridge.dump(nsWindowPtr);
        }
    }

    private static void clearVibrancyOnAppKit(Window window) {
        long nsWindowPtr = MacWindowPeerAccess.resolveNSWindowPointer(window);
        if (nsWindowPtr == 0) {
            throw new IllegalStateException("Cannot resolve NSWindow pointer");
        }

        MemorySegment nsWindow = MemorySegment.ofAddress(nsWindowPtr);
        MemorySegment effectView = ObjCRuntime.getAssociatedObject(nsWindow, VIBRANCY_ASSOCIATED_KEY);
        if (isNil(effectView)) {
            return;
        }

        ObjCRuntime.sendVoid(effectView, "removeFromSuperview");
        ObjCRuntime.setAssociatedObject(
            nsWindow,
            VIBRANCY_ASSOCIATED_KEY,
            MemorySegment.NULL,
            OBJC_ASSOCIATION_ASSIGN
        );
    }

    private static void applyAppearanceOnAppKit(Window window, MacWindowAppearance appearance) {
        long nsWindowPtr = MacWindowPeerAccess.resolveNSWindowPointer(window);
        if (nsWindowPtr == 0) {
            throw new IllegalStateException("Cannot resolve NSWindow pointer");
        }
        MemorySegment nsWindow = MemorySegment.ofAddress(nsWindowPtr);

        if (appearance.nativeName() == null) {
            ObjCRuntime.sendVoidAddress(nsWindow, "setAppearance:", MemorySegment.NULL);
            return;
        }

        MemorySegment nsAppearanceClass = requireAddress(ObjCRuntime.objcClass("NSAppearance"), "NSAppearance class");
        MemorySegment appearanceName = nsString(appearance.nativeName());
        MemorySegment nativeAppearance = ObjCRuntime.sendAddressAddress(
            nsAppearanceClass,
            "appearanceNamed:",
            appearanceName
        );
        ObjCRuntime.sendVoidAddress(nsWindow, "setAppearance:", nativeAppearance);
    }

    private static MemorySegment createEffectSiblingView(MemorySegment nsWindow, MemorySegment contentView) {
        MemorySegment visualEffectViewClass = requireAddress(
            ObjCRuntime.objcClass("NSVisualEffectView"),
            "NSVisualEffectView class"
        );
        MemorySegment effectView = requireAddress(
            ObjCRuntime.sendAddress(ObjCRuntime.sendAddress(visualEffectViewClass, "alloc"), "init"),
            "NSVisualEffectView instance"
        );

        ObjCRuntime.sendVoidBool(effectView, "setTranslatesAutoresizingMaskIntoConstraints:", false);
        ObjCRuntime.sendVoidBool(effectView, "setWantsLayer:", true);
        ObjCRuntime.sendVoidLong(effectView, "setAutoresizingMask:", NS_VIEW_WIDTH_SIZABLE | NS_VIEW_HEIGHT_SIZABLE);

        MemorySegment sibling = findAwtHostView(contentView);
        MemorySegment parent;
        if (isNil(sibling)) {
            parent = contentView;
        } else {
            MemorySegment superview = ObjCRuntime.sendAddress(sibling, "superview");
            if (isNil(superview)) {
                parent = contentView;
                MemorySegment subviews = ObjCRuntime.sendAddress(contentView, "subviews");
                sibling = isNil(subviews) ? MemorySegment.NULL : ObjCRuntime.sendAddress(subviews, "firstObject");
            } else {
                parent = superview;
            }
        }

        if (isNil(sibling)) {
            ObjCRuntime.sendVoidAddress(parent, "addSubview:", effectView);
        } else {
            ObjCRuntime.sendVoidAddressLongAddress(
                parent,
                "addSubview:positioned:relativeTo:",
                effectView,
                NS_WINDOW_ORDERING_BELOW,
                sibling
            );
        }
        pinToParent(effectView, parent);

        ObjCRuntime.setAssociatedObject(
            nsWindow,
            VIBRANCY_ASSOCIATED_KEY,
            effectView,
            OBJC_ASSOCIATION_RETAIN_NONATOMIC
        );
        ObjCRuntime.sendAddress(effectView, "autorelease");

        return effectView;
    }

    private static void applyVibrantAppearance(MemorySegment nsWindow, MemorySegment effectView) {
        MemorySegment contextAppearance = ObjCRuntime.sendAddress(nsWindow, "effectiveAppearance");
        if (isNil(contextAppearance)) {
            return;
        }

        MemorySegment appearanceName = ObjCRuntime.sendAddress(contextAppearance, "name");
        if (isNil(appearanceName)) {
            return;
        }

        MemorySegment darkToken = nsString("Dark");
        boolean dark = ObjCRuntime.sendBoolAddress(appearanceName, "containsString:", darkToken);
        MemorySegment desiredAppearanceName = nsString(dark ? "NSAppearanceNameVibrantDark" : "NSAppearanceNameVibrantLight");

        MemorySegment nsAppearanceClass = requireAddress(ObjCRuntime.objcClass("NSAppearance"), "NSAppearance class");
        MemorySegment vibrantAppearance = ObjCRuntime.sendAddressAddress(
            nsAppearanceClass,
            "appearanceNamed:",
            desiredAppearanceName
        );
        if (!isNil(vibrantAppearance)) {
            ObjCRuntime.sendVoidAddress(effectView, "setAppearance:", vibrantAppearance);
        }
    }

    private static MemorySegment nsString(String value) {
        MemorySegment nsStringClass = requireAddress(ObjCRuntime.objcClass("NSString"), "NSString class");
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment utf8 = arena.allocateFrom(value);
            return requireAddress(
                ObjCRuntime.sendAddressAddress(nsStringClass, "stringWithUTF8String:", utf8),
                "NSString \"" + value + "\""
            );
        }
    }

    private static MemorySegment findAwtHostView(MemorySegment rootView) {
        boolean awtBridge =
            ObjCRuntime.respondsToSelector(rootView, "mouseIsOver")
                && ObjCRuntime.respondsToSelector(rootView, "deliverJavaMouseEvent:");
        if (awtBridge) {
            return rootView;
        }
        MemorySegment subviews = ObjCRuntime.sendAddress(rootView, "subviews");
        if (isNil(subviews)) {
            return MemorySegment.NULL;
        }
        long count = ObjCRuntime.sendLong(subviews, "count");
        for (long i = 0; i < count; i++) {
            MemorySegment child = ObjCRuntime.sendAddressLong(subviews, "objectAtIndex:", i);
            MemorySegment found = findAwtHostView(child);
            if (!isNil(found)) {
                return found;
            }
        }
        return MemorySegment.NULL;
    }

    private static void configureWindowForBackdrop(MemorySegment nsWindow, MemorySegment contentView) {
        if (ObjCRuntime.respondsToSelector(nsWindow, "setOpaque:")) {
            ObjCRuntime.sendVoidBool(nsWindow, "setOpaque:", false);
        }

        if (ObjCRuntime.respondsToSelector(nsWindow, "setBackgroundColor:")) {
            MemorySegment nsColorClass = ObjCRuntime.objcClass("NSColor");
            if (!isNil(nsColorClass)) {
                MemorySegment clear = ObjCRuntime.sendAddress(nsColorClass, "clearColor");
                if (!isNil(clear)) {
                    ObjCRuntime.sendVoidAddress(nsWindow, "setBackgroundColor:", clear);
                }
            }
        }

        MemorySegment awtView = findAwtHostView(contentView);
        if (isNil(awtView)) {
            return;
        }

        if (ObjCRuntime.respondsToSelector(awtView, "setOpaque:")) {
            ObjCRuntime.sendVoidBool(awtView, "setOpaque:", false);
        }
        if (ObjCRuntime.respondsToSelector(awtView, "setWantsLayer:")) {
            ObjCRuntime.sendVoidBool(awtView, "setWantsLayer:", true);
        }
    }

    private static void pinToParent(MemorySegment effectView, MemorySegment parentView) {
        MemorySegment leadingConstraint = ObjCRuntime.sendAddressAddress(
            ObjCRuntime.sendAddress(effectView, "leadingAnchor"),
            "constraintEqualToAnchor:",
            ObjCRuntime.sendAddress(parentView, "leadingAnchor")
        );
        MemorySegment trailingConstraint = ObjCRuntime.sendAddressAddress(
            ObjCRuntime.sendAddress(effectView, "trailingAnchor"),
            "constraintEqualToAnchor:",
            ObjCRuntime.sendAddress(parentView, "trailingAnchor")
        );
        MemorySegment topConstraint = ObjCRuntime.sendAddressAddress(
            ObjCRuntime.sendAddress(effectView, "topAnchor"),
            "constraintEqualToAnchor:",
            ObjCRuntime.sendAddress(parentView, "topAnchor")
        );
        MemorySegment bottomConstraint = ObjCRuntime.sendAddressAddress(
            ObjCRuntime.sendAddress(effectView, "bottomAnchor"),
            "constraintEqualToAnchor:",
            ObjCRuntime.sendAddress(parentView, "bottomAnchor")
        );

        MemorySegment nsArrayClass = requireAddress(ObjCRuntime.objcClass("NSArray"), "NSArray class");
        MemorySegment constraintsArray;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment list = arena.allocate(ValueLayout.ADDRESS, 4);
            list.setAtIndex(ValueLayout.ADDRESS, 0, leadingConstraint);
            list.setAtIndex(ValueLayout.ADDRESS, 1, trailingConstraint);
            list.setAtIndex(ValueLayout.ADDRESS, 2, topConstraint);
            list.setAtIndex(ValueLayout.ADDRESS, 3, bottomConstraint);
            constraintsArray = requireAddress(
                ObjCRuntime.sendAddressAddressLong(nsArrayClass, "arrayWithObjects:count:", list, 4L),
                "NSArray constraints array"
            );
        }

        MemorySegment nsLayoutConstraintClass = requireAddress(
            ObjCRuntime.objcClass("NSLayoutConstraint"),
            "NSLayoutConstraint class"
        );
        ObjCRuntime.sendVoidAddress(nsLayoutConstraintClass, "activateConstraints:", constraintsArray);
    }

    private static MemorySegment requireAddress(MemorySegment value, String what) {
        if (isNil(value)) {
            throw new IllegalStateException("Cannot resolve " + what);
        }
        return value;
    }

    private static boolean isNil(MemorySegment segment) {
        return segment == null || segment.address() == 0L;
    }
}
