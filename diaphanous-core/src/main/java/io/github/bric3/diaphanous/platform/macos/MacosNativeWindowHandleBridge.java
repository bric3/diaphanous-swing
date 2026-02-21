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

