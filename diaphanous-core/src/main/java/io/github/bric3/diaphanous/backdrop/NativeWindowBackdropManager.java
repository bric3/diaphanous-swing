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
import java.util.Objects;
import java.util.Optional;

/**
 * Manager that selects and owns the active native backdrop provider.
 */
public final class NativeWindowBackdropManager {
    private static NativeWindowBackdropManager sharedInstance;

    private final WindowBackdropProvider provider;

    /**
     * @return shared manager instance
     */
    public static NativeWindowBackdropManager getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new NativeWindowBackdropManager();
        }
        return sharedInstance;
    }

    /**
     * Replaces the shared manager instance.
     *
     * @param manager new manager
     */
    public static void setSharedInstance(NativeWindowBackdropManager manager) {
        sharedInstance = Objects.requireNonNull(manager, "manager");
    }

    /**
     * Creates a manager with native providers enabled.
     */
    public NativeWindowBackdropManager() {
        this.provider = selectProvider();
    }

    /**
     * @return whether the active provider supports native backdrop operations
     */
    public boolean isSupported() {
        return !(provider instanceof NoOpWindowBackdropProvider);
    }

    public void apply(Window window, WindowBackdropSpec spec) {
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(spec, "spec");
        provider.apply(window, spec);
    }

    public void clear(Window window) {
        Objects.requireNonNull(window, "window");
        provider.clear(window);
    }

    public void setWindowAlpha(Window window, double alpha) {
        Objects.requireNonNull(window, "window");
        provider.setWindowAlpha(window, alpha);
    }

    public double defaultAlpha() {
        return provider.defaultAlpha();
    }

    public Optional<Double> readAlpha(Window window) {
        Objects.requireNonNull(window, "window");
        return provider.readAlpha(window);
    }

    public Optional<WindowBackdropMaterialSpec> defaultMaterial() {
        return provider.defaultMaterial();
    }

    public Optional<WindowBackdropMaterialSpec> readMaterial(Window window) {
        Objects.requireNonNull(window, "window");
        return provider.readMaterial(window);
    }

    private static WindowBackdropProvider selectProvider() {
        String osName = System.getProperty("os.name", "");
        if (osName.contains("Mac")) {
            return new MacosWindowBackdropProvider();
        }
        return new NoOpWindowBackdropProvider();
    }
}
