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
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Manager that selects and owns the active native window decorations provider.
 *
 * This follows the same indirection pattern as Darklaf's native decorations
 * manager: API callers use a stable entry point while the manager picks the
 * OS-specific implementation.
 */
public final class NativeWindowDecorationsManager {
    private static NativeWindowDecorationsManager sharedInstance;

    private final WindowDecorationsProvider provider;

    /**
     * @return shared manager instance
     */
    public static NativeWindowDecorationsManager getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new NativeWindowDecorationsManager();
        }
        return sharedInstance;
    }

    /**
     * Replaces the shared manager instance.
     *
     * @param manager new manager
     */
    public static void setSharedInstance(NativeWindowDecorationsManager manager) {
        sharedInstance = Objects.requireNonNull(manager, "manager");
    }

    /**
     * Creates a manager with native providers enabled.
     */
    public NativeWindowDecorationsManager() {
        this.provider = selectProvider();
    }

    /**
     * @return whether the active provider supports native decorations
     */
    public boolean isSupported() {
        return !(provider instanceof NoOpWindowDecorationsProvider);
    }

    /**
     * Applies a style spec to a window.
     *
     * @param window target window
     * @param spec style spec
     */
    public void applyDecorations(Window window, WindowDecorationSpec spec) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(spec, "spec");
        provider.applyDecorations(window, spec);
    }

    /**
     * Applies an appearance spec to a window.
     *
     * @param window target window
     * @param spec appearance spec
     */
    public void applyAppearance(Window window, WindowAppearanceSpec spec) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(spec, "spec");
        provider.applyAppearance(window, spec);
    }

    /**
     * Returns backdrop compatibility predicate from the active OS provider.
     *
     * @return compatibility predicate
     */
    public Predicate<Window> isCompatibleWithBackdropPredicate() {
        return provider.isCompatibleWithBackdropPredicate();
    }

    private static WindowDecorationsProvider selectProvider() {
        String osName = System.getProperty("os.name", "");
        if (osName.contains("Mac")) {
            return new MacosWindowDecorationsProvider();
        }
        return new NoOpWindowDecorationsProvider();
    }
}
