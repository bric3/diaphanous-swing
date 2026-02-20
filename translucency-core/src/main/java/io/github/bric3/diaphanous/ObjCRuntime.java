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

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal Objective-C runtime bridge backed by FFM downcalls to {@code libobjc}.
 */
final class ObjCRuntime {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup OBJC = SymbolLookup.libraryLookup("/usr/lib/libobjc.A.dylib", Arena.global());
    private static final Map<String, MemorySegment> SELECTOR_CACHE = new ConcurrentHashMap<>();

    private static final MethodHandle SEL_REGISTER_NAME = downcall(
        "sel_registerName",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_LONG = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_VOID_LONG = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    private static final MethodHandle MSG_SEND_VOID_BYTE = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    );
    private static final MethodHandle MSG_SEND_BOOL_ADDR = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    private ObjCRuntime() {
    }

    private static MemorySegment selector(String name) {
        return SELECTOR_CACHE.computeIfAbsent(name, ObjCRuntime::registerSelector);
    }

    /**
     * Sends a selector and expects a {@code long} return value.
     */
    static long sendLong(MemorySegment receiver, String selectorName) {
        try {
            return (long) MSG_SEND_LONG.invokeExact(receiver, selector(selectorName));
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector with one {@code long} argument and no return value.
     */
    static void sendVoidLong(MemorySegment receiver, String selectorName, long value) {
        try {
            MSG_SEND_VOID_LONG.invokeExact(receiver, selector(selectorName), value);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector with one boolean argument and no return value.
     */
    static void sendVoidBool(MemorySegment receiver, String selectorName, boolean value) {
        try {
            byte bool = (byte) (value ? 1 : 0);
            MSG_SEND_VOID_BYTE.invokeExact(receiver, selector(selectorName), bool);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Checks whether the receiver implements the provided selector.
     */
    static boolean respondsToSelector(MemorySegment receiver, String selectorName) {
        MemorySegment queriedSelector = selector(selectorName);
        try {
            byte response = (byte) MSG_SEND_BOOL_ADDR.invokeExact(receiver, selector("respondsToSelector:"), queriedSelector);
            return response != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector respondsToSelector:", t);
        }
    }

    private static MethodHandle downcall(String symbol, FunctionDescriptor descriptor) {
        MemorySegment address = OBJC.find(symbol)
            .orElseThrow(() -> new IllegalStateException("Cannot find symbol in libobjc: " + symbol));
        return LINKER.downcallHandle(address, descriptor);
    }

    private static MemorySegment registerSelector(String name) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cString = arena.allocateFrom(name);
            return (MemorySegment) SEL_REGISTER_NAME.invokeExact(cString);
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot register Objective-C selector: " + name, t);
        }
    }
}
