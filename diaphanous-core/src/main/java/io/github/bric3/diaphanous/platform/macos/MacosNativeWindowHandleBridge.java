/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.platform.macos;

import java.awt.Window;

/**
 * Allow access to the macOs native {@code NSWindow*} pointer.
 *
 * This is a workaround using JNI instead of {@code --add-opens} access.
 * Unfortunately, FFM does not support this use case : <em>AWT peer/object-graph access</em>,
 * only JNI does.
 *
 * <p>Rationale:
 * the library uses FFM for native C/Objective-C operations once a raw {@code NSWindow*}
 * is known, but obtaining that pointer from a Java {@link Window} requires reading
 * JVM-managed AWT peer internals ({@code Window -> peer -> platformWindow -> ptr}) and
 * invoking peer methods such as {@code setOpaque(boolean)}. Those operations require
 * JNI access to Java objects and {@code JNIEnv}, and cannot be replaced by pure FFM
 * downcalls without reintroducing reflective {@code --add-opens} access.
 *
 * <p>Scope:
 * this class intentionally keeps JNI usage minimal and focused on object-graph bridging.
 * All non-object native work is delegated to FFM-based bridges.
 */
final class MacosNativeWindowHandleBridge {
    private static final boolean AVAILABLE = MacosNativeLibrary.ensureLoaded();

    private MacosNativeWindowHandleBridge() {
    }

    static boolean isAvailable() {
        return AVAILABLE;
    }

    static long resolveNSWindowPointer(Window window) {
        if (!AVAILABLE || window == null) {
            return 0L;
        }
        try {
            return resolveNSWindowPointer0(window);
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    static boolean setPeerOpaque(Window window, boolean opaque) {
        if (!AVAILABLE || window == null) {
            return false;
        }
        try {
            return setPeerOpaque0(window, opaque);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static native long resolveNSWindowPointer0(Window window);

    private static native boolean setPeerOpaque0(Window window, boolean opaque);
}
