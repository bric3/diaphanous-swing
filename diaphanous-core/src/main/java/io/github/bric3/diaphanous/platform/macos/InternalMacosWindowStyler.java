/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.platform.macos;

import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec;
import io.github.bric3.diaphanous.decorations.MacosWindowAppearanceSpec;
import io.github.bric3.diaphanous.decorations.MacosWindowDecorationsSpec;

import java.awt.Window;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;
import java.util.Optional;

/**
 * Internal macOS native styling implementation shared by public split APIs:
 * {@link io.github.bric3.diaphanous.decorations.MacosWindowDecorations} and
 * {@link io.github.bric3.diaphanous.backdrop.WindowBackdrop}.
 * <p>
 * This implementation relies on internal AWT peer classes and Objective-C runtime calls.
 */
public final class InternalMacosWindowStyler {
    private static final boolean IS_MAC = System.getProperty("os.name", "").contains("Mac");
    private static final long NS_WINDOW_STYLE_MASK_FULL_SIZE_CONTENT_VIEW = 1L << 15;
    private static final int NS_VISUAL_EFFECT_BLENDING_MODE_BEHIND_WINDOW = 0;
    private static final long NS_WINDOW_TITLE_VISIBLE = 0;
    private static final long NS_WINDOW_TITLE_HIDDEN = 1;
    private static final long NS_VIEW_WIDTH_SIZABLE = 1L << 1;
    private static final long NS_VIEW_HEIGHT_SIZABLE = 1L << 4;
    private static final long NS_WINDOW_ORDERING_BELOW = -1L;
    private static final long OBJC_ASSOCIATION_ASSIGN = 0L;
    private static final long OBJC_ASSOCIATION_RETAIN_NONATOMIC = 1L;
    private static final MemorySegment VIBRANCY_ASSOCIATED_KEY = Arena.global().allocate(ValueLayout.JAVA_LONG);
    private static final boolean DUMP_NATIVE = Boolean.getBoolean("diaphanous.dump.native");

    private InternalMacosWindowStyler() {
    }

    /**
     * Applies a style configuration to the provided window.
     *
     * @param window the AWT/Swing window to style
     * @param style style options to apply
     * @throws UnsupportedOperationException when called on a non-macOS runtime
     * @throws IllegalStateException when the native {@code NSWindow} cannot be resolved or a native call fails
     */
    public static void apply(Window window, MacosWindowDecorationsSpec style) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(style, "style");

        if (!IS_MAC) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        if (MacosNativeBackdropBridge.isAvailable()) {
            applyWithNativeBridge(window, style);
            return;
        }
        MacosThreading.runOnAppKitThread(() -> applyDecorationsOnAppKit(window, style));
    }

    /**
     * Applies an {@code NSVisualEffectView}-based backdrop to the window content.
     * <p>
     * The style object can be partial: properties that are not explicitly changed by caller code
     * are taken from {@link MacosBackdropEffectSpec#builder()} defaults.
     *
     * <p>Examples:
     * <pre>{@code
     * MacosWindowStyler.applyBackdrop(
     *     window,
     *     MacosBackdropEffectSpec.builder()
     *         .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
     *         .build()
     * );
     *
     * MacosWindowStyler.applyBackdrop(
     *     window,
     *     MacosBackdropEffectSpec.builder()
     *         .material(MacosBackdropEffectSpec.MacosBackdropMaterial.CONTENT_BACKGROUND)
     *         .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
     *         .backdropAlpha(0.75)
     *         .build()
     * );
     * }</pre>
     *
     * @param window the AWT/Swing window to mutate
     * @param style backdrop configuration
     */
    public static void applyBackdrop(Window window, MacosBackdropEffectSpec style) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(style, "style");

        if (!IS_MAC) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        if (MacosNativeBackdropBridge.isAvailable()) {
            applyBackdropWithNativeBridge(window, style);
            return;
        }
        MacosThreading.runOnAppKitThread(() -> applyBackdropOnAppKit(window, style));
    }

    /**
     * Installs a backdrop using platform defaults.
     *
     * <p>This is a convenience variant that does not require passing an explicit
     * {@link MacosBackdropEffectSpec} from user code.
     *
     * @param window the AWT/Swing window to mutate
     */
    public static void installBackdrop(Window window) {
        Objects.requireNonNull(window, "window");

        if (!IS_MAC) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        if (MacosNativeBackdropBridge.isAvailable()) {
            makeWindowNonOpaque(window);
            long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
            if (!MacosNativeBackdropBridge.installDefault(nsWindowPtr)) {
                throw new IllegalStateException("Cannot install native default backdrop");
            }
            dumpWindowStateIfEnabled(nsWindowPtr);
            return;
        }
        MacosThreading.runOnAppKitThread(() -> installBackdropOnAppKit(window));
    }

    /**
     * Removes the previously installed backdrop view from the window, if present.
     *
     * @param window the AWT/Swing window to mutate
     */
    public static void clearBackdrop(Window window) {
        Objects.requireNonNull(window, "window");

        if (!IS_MAC) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        if (MacosNativeBackdropBridge.isAvailable()) {
            long nsWindowPtr = MacosWindowPeerAccess.resolveNSWindowPointer(window);
            if (nsWindowPtr == 0L) {
                throw new IllegalStateException("Cannot resolve NSWindow pointer");
            }
            MacosNativeBackdropBridge.remove(nsWindowPtr);
            return;
        }
        MacosThreading.runOnAppKitThread(() -> clearBackdropOnAppKit(window));
    }

    /**
     * Applies a macOS appearance to the native window frame/titlebar.
     *
     * @param window the AWT/Swing window to mutate
     * @param appearance appearance preset; {@link MacosWindowAppearanceSpec#SYSTEM} restores system default
     */
    public static void applyAppearance(Window window, MacosWindowAppearanceSpec appearance) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(appearance, "appearance");

        if (!IS_MAC) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        if (MacosNativeBackdropBridge.isAvailable()) {
            long nsWindowPtr = MacosWindowPeerAccess.resolveNSWindowPointer(window);
            if (nsWindowPtr == 0L) {
                throw new IllegalStateException("Cannot resolve NSWindow pointer");
            }
            if (!MacosNativeBackdropBridge.applyWindowAppearance(nsWindowPtr, appearance.nativeName())) {
                throw new IllegalStateException("Cannot apply native window appearance");
            }
            return;
        }
        MacosThreading.runOnAppKitThread(() -> applyAppearanceOnAppKit(window, appearance));
    }

    /**
     * Sets native window alpha ({@code NSWindow.alphaValue}) on AppKit thread.
     *
     * @param window target window
     * @param alpha alpha in range {@code [0,1]}
     *
     * <p>References:
     * <ul>
     *   <li><a href="https://developer.apple.com/documentation/appkit/nswindow/1419167-alphavalue">Apple NSWindow.alphaValue</a></li>
     *   <li><a href="https://developer.apple.com/documentation/appkit/nswindow/1419515-setalphavalue">Apple NSWindow.setAlphaValue(_:)</a></li>
     * </ul>
     */
    public static void setWindowAlpha(Window window, double alpha) {
        Objects.requireNonNull(window, "window");

        if (!IS_MAC) {
            throw new UnsupportedOperationException("macOS window styling is only supported on macOS");
        }

        double clamped = Math.max(0.0, Math.min(1.0, alpha));
        if (MacosNativeBackdropBridge.isAvailable()) {
            long nsWindowPtr = MacosWindowPeerAccess.resolveNSWindowPointer(window);
            if (nsWindowPtr == 0L) {
                throw new IllegalStateException("Cannot resolve NSWindow pointer");
            }
            if (!MacosNativeBackdropBridge.setWindowAlpha(nsWindowPtr, clamped)) {
                throw new IllegalStateException("Cannot set native window alpha");
            }
            return;
        }
        MacosThreading.runOnAppKitThread(() -> setWindowAlphaOnAppKit(window, clamped));
    }

    /**
     * Returns the platform default alpha for {@code NSVisualEffectView}, when available.
     *
     * @return default alpha value in range {@code [0,1]}, or {@code -1} when unavailable
     *
     * <p>The value is read from a freshly created native effect view and reflects the current OS/JDK default.
     */
    public static double defaultBackdropAlpha() {
        if (!IS_MAC || !MacosNativeBackdropBridge.isAvailable()) {
            return -1.0;
        }
        return MacosNativeBackdropBridge.defaultAlpha();
    }

    /**
     * Returns the current native backdrop alpha from the installed effect view.
     *
     * @param window target window
     * @return optional alpha value when effect view is installed
     *
     * <p>Returns empty when no native wrapper/effect view is installed or native bridge is unavailable.
     */
    public static Optional<Double> readBackdropAlpha(Window window) {
        Objects.requireNonNull(window, "window");
        if (!IS_MAC || !MacosNativeBackdropBridge.isAvailable()) {
            return Optional.empty();
        }

        long nsWindowPtr = MacosWindowPeerAccess.resolveNSWindowPointer(window);
        double value = nsWindowPtr == 0L ? -1.0 : MacosNativeBackdropBridge.readAlpha(nsWindowPtr);
        return value >= 0.0 ? Optional.of(value) : Optional.empty();
    }

    /**
     * Returns the platform default material for {@code NSVisualEffectView}, when available.
     *
     * @return optional default material
     *
     * <p>The value is read from a freshly created native effect view and reflects current OS defaults.
     */
    public static Optional<MacosBackdropEffectSpec.MacosBackdropMaterial> defaultBackdropMaterial() {
        if (!IS_MAC || !MacosNativeBackdropBridge.isAvailable()) {
            return Optional.empty();
        }
        int value = MacosNativeBackdropBridge.defaultMaterial();
        return value >= 0 ? MacosBackdropEffectSpec.MacosBackdropMaterial.fromNativeValue(value) : Optional.empty();
    }

    /**
     * Returns the current native backdrop material from the installed effect view.
     *
     * @param window target window
     * @return optional material when effect view is installed
     *
     * <p>Returns empty when no native wrapper/effect view is installed or native bridge is unavailable.
     */
    public static Optional<MacosBackdropEffectSpec.MacosBackdropMaterial> readBackdropMaterial(Window window) {
        Objects.requireNonNull(window, "window");
        if (!IS_MAC || !MacosNativeBackdropBridge.isAvailable()) {
            return Optional.empty();
        }

        long nsWindowPtr = MacosWindowPeerAccess.resolveNSWindowPointer(window);
        int value = nsWindowPtr == 0L ? -1 : MacosNativeBackdropBridge.readMaterial(nsWindowPtr);
        return value >= 0 ? MacosBackdropEffectSpec.MacosBackdropMaterial.fromNativeValue(value) : Optional.empty();
    }

    private static void applyWithNativeBridge(Window window, MacosWindowDecorationsSpec style) {
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
        if (!MacosNativeBackdropBridge.applyWindowStyle(nsWindowPtr, style)) {
            throw new IllegalStateException("Cannot apply native window style");
        }
    }

    private static void applyBackdropWithNativeBridge(Window window, MacosBackdropEffectSpec style) {
        makeWindowNonOpaque(window);
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
        if (!style.enabled()) {
            MacosNativeBackdropBridge.remove(nsWindowPtr);
            return;
        }
        if (!installOrUpdateNativeBackdrop(
            nsWindowPtr,
            (int) style.material().nativeValue(),
            style.backdropAlpha(),
            (int) style.state().nativeValue(),
            style.emphasized()
        )) {
            throw new IllegalStateException("Cannot apply native backdrop");
        }
        dumpWindowStateIfEnabled(nsWindowPtr);
    }

    private static void applyDecorationsOnAppKit(Window window, MacosWindowDecorationsSpec style) {
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
        dumpWindowStateIfEnabled(nsWindowPtr);

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
    }

    private static void applyBackdropOnAppKit(Window window, MacosBackdropEffectSpec style) {
        makeWindowNonOpaque(window);
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);

        if (!style.enabled()) {
            MacosNativeBackdropBridge.remove(nsWindowPtr);
            clearBackdropOnAppKit(window);
            return;
        }

        int material = (int) style.material().nativeValue();
        int state = (int) style.state().nativeValue();
        if (MacosNativeBackdropBridge.isAvailable()) {
            if (installOrUpdateNativeBackdrop(nsWindowPtr, material, style.backdropAlpha(), state, style.emphasized())) {
                dumpWindowStateIfEnabled(nsWindowPtr);
                return;
            }
        }

        AppKitBackdropContext context = appKitBackdropContext(nsWindowPtr);

        configureWindowForBackdrop(context.nsWindow(), context.contentView());
        ObjCRuntime.sendVoidLong(context.effectView(), "setMaterial:", style.material().nativeValue());
        ObjCRuntime.sendVoidLong(context.effectView(), "setBlendingMode:", NS_VISUAL_EFFECT_BLENDING_MODE_BEHIND_WINDOW);
        ObjCRuntime.sendVoidLong(context.effectView(), "setState:", style.state().nativeValue());
        ObjCRuntime.sendVoidDouble(context.effectView(), "setAlphaValue:", style.backdropAlpha());
        applyVibrantAppearance(context.nsWindow(), context.effectView());
        if (ObjCRuntime.respondsToSelector(context.effectView(), "setEmphasized:")) {
            ObjCRuntime.sendVoidBool(context.effectView(), "setEmphasized:", style.emphasized());
        }
        dumpWindowStateIfEnabled(nsWindowPtr);
    }

    private static void installBackdropOnAppKit(Window window) {
        makeWindowNonOpaque(window);
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
        AppKitBackdropContext context = appKitBackdropContext(nsWindowPtr);

        configureWindowForBackdrop(context.nsWindow(), context.contentView());
        ObjCRuntime.sendVoidLong(context.effectView(), "setBlendingMode:", NS_VISUAL_EFFECT_BLENDING_MODE_BEHIND_WINDOW);
        applyVibrantAppearance(context.nsWindow(), context.effectView());
        dumpWindowStateIfEnabled(nsWindowPtr);
    }

    private static void clearBackdropOnAppKit(Window window) {
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);

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

    private static void applyAppearanceOnAppKit(Window window, MacosWindowAppearanceSpec appearance) {
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
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

    private static void setWindowAlphaOnAppKit(Window window, double alpha) {
        long nsWindowPtr = resolveNsWindowPointerOrThrow(window);
        MemorySegment nsWindow = MemorySegment.ofAddress(nsWindowPtr);
        ObjCRuntime.sendVoidDouble(nsWindow, "setAlphaValue:", alpha);
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

    private static AppKitBackdropContext appKitBackdropContext(long nsWindowPtr) {
        MemorySegment nsWindow = MemorySegment.ofAddress(nsWindowPtr);
        MemorySegment contentView = requireAddress(ObjCRuntime.sendAddress(nsWindow, "contentView"), "NSWindow contentView");
        MemorySegment effectView = ObjCRuntime.getAssociatedObject(nsWindow, VIBRANCY_ASSOCIATED_KEY);
        if (isNil(effectView)) {
            effectView = createEffectSiblingView(nsWindow, contentView);
        }
        return new AppKitBackdropContext(nsWindow, contentView, effectView);
    }

    private static boolean installOrUpdateNativeBackdrop(
        long nsWindowPtr,
        int material,
        double alpha,
        int state,
        boolean emphasized
    ) {
        return MacosNativeBackdropBridge.update(nsWindowPtr, material, alpha, state, emphasized)
            || MacosNativeBackdropBridge.install(nsWindowPtr, material, alpha, state, emphasized);
    }

    private static long resolveNsWindowPointerOrThrow(Window window) {
        long nsWindowPtr = MacosWindowPeerAccess.resolveNSWindowPointer(window);
        if (nsWindowPtr == 0L) {
            throw new IllegalStateException("Cannot resolve NSWindow pointer");
        }
        return nsWindowPtr;
    }

    private static void makeWindowNonOpaque(Window window) {
        try {
            MacosWindowPeerAccess.setPeerOpaque(window, false);
        } catch (RuntimeException ignored) {
            // Some peers may reject opacity changes depending on lifecycle/state.
        }
    }

    private static void dumpWindowStateIfEnabled(long nsWindowPtr) {
        if (DUMP_NATIVE) {
            MacosNativeBackdropBridge.dump(nsWindowPtr);
        }
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

    private record AppKitBackdropContext(
        MemorySegment nsWindow,
        MemorySegment contentView,
        MemorySegment effectView
    ) {
    }
}
