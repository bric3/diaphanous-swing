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

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Resolves the native {@code NSWindow*} pointer from an AWT {@link Window} through
 * internal LWAWT peer objects.
 */
final class MacWindowPeerAccess {
    private static final String LW_WINDOW_PEER = "sun.lwawt.LWWindowPeer";
    private static final String CF_RETAINED_RESOURCE = "sun.lwawt.macosx.CFRetainedResource";

    private static final Field COMPONENT_PEER_FIELD;
    private static final Field CF_PTR_FIELD;
    private static final Method GET_PLATFORM_WINDOW;

    static {
        try {
            COMPONENT_PEER_FIELD = Component.class.getDeclaredField("peer");
            COMPONENT_PEER_FIELD.setAccessible(true);

            Class<?> lwWindowPeerClass = Class.forName(LW_WINDOW_PEER);
            GET_PLATFORM_WINDOW = lwWindowPeerClass.getMethod("getPlatformWindow");
            GET_PLATFORM_WINDOW.setAccessible(true);

            Class<?> cfRetainedResourceClass = Class.forName(CF_RETAINED_RESOURCE);
            CF_PTR_FIELD = cfRetainedResourceClass.getDeclaredField("ptr");
            CF_PTR_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private MacWindowPeerAccess() {
    }

    /**
     * Resolves a native {@code NSWindow*} pointer.
     *
     * @param window AWT window whose peer is backed by a macOS native window
     * @return raw native pointer value
     */
    static long resolveNSWindowPointer(Window window) {
        if (!window.isDisplayable()) {
            window.addNotify();
        }

        Object peer = getPeer(window);
        if (peer == null) {
            throw new IllegalStateException("AWT peer is not initialized yet; make sure the window is displayable");
        }

        Object platformWindow = invokeGetPlatformWindow(peer);
        return getPointer(platformWindow);
    }

    private static Object getPeer(Window window) {
        try {
            return COMPONENT_PEER_FIELD.get(window);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access AWT peer field", e);
        }
    }

    private static Object invokeGetPlatformWindow(Object peer) {
        try {
            return GET_PLATFORM_WINDOW.invoke(peer);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot resolve CPlatformWindow from AWT peer", e);
        }
    }

    private static long getPointer(Object platformWindow) {
        try {
            return CF_PTR_FIELD.getLong(platformWindow);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access native NSWindow pointer", e);
        }
    }
}
