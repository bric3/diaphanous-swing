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
    private static final MethodHandle OBJC_GET_CLASS = downcall(
        "objc_getClass",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_LONG = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_VOID = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_VOID_LONG = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    private static final MethodHandle MSG_SEND_VOID_ADDR = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_VOID_ADDR_LONG_ADDR = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS
        )
    );
    private static final MethodHandle MSG_SEND_VOID_BYTE = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE)
    );
    private static final MethodHandle MSG_SEND_BOOL_ADDR = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_VOID_DOUBLE = downcall(
        "objc_msgSend",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE)
    );
    private static final MethodHandle MSG_SEND_ADDR = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_ADDR_ADDR = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    private static final MethodHandle MSG_SEND_ADDR_LONG = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    private static final MethodHandle MSG_SEND_ADDR_ADDR_LONG = downcall(
        "objc_msgSend",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG
        )
    );
    private static final MethodHandle OBJC_SET_ASSOCIATED_OBJECT = downcall(
        "objc_setAssociatedObject",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    private static final MethodHandle OBJC_GET_ASSOCIATED_OBJECT = downcall(
        "objc_getAssociatedObject",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );

    private ObjCRuntime() {
    }

    private static MemorySegment selector(String name) {
        return SELECTOR_CACHE.computeIfAbsent(name, ObjCRuntime::registerSelector);
    }

    static MemorySegment objcClass(String className) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cString = arena.allocateFrom(className);
            return (MemorySegment) OBJC_GET_CLASS.invokeExact(cString);
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot resolve Objective-C class: " + className, t);
        }
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
     * Sends a selector with no arguments and no return value.
     */
    static void sendVoid(MemorySegment receiver, String selectorName) {
        try {
            MSG_SEND_VOID.invokeExact(receiver, selector(selectorName));
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
     * Sends a selector with one object argument and no return value.
     */
    static void sendVoidAddress(MemorySegment receiver, String selectorName, MemorySegment value) {
        try {
            MSG_SEND_VOID_ADDR.invokeExact(receiver, selector(selectorName), value);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector with signature {@code (id, SEL, id, long, id) -> void}.
     */
    static void sendVoidAddressLongAddress(
        MemorySegment receiver,
        String selectorName,
        MemorySegment object,
        long number,
        MemorySegment anotherObject
    ) {
        try {
            MSG_SEND_VOID_ADDR_LONG_ADDR.invokeExact(receiver, selector(selectorName), object, number, anotherObject);
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
     * Sends a selector with one {@code double} argument and no return value.
     */
    static void sendVoidDouble(MemorySegment receiver, String selectorName, double value) {
        try {
            MSG_SEND_VOID_DOUBLE.invokeExact(receiver, selector(selectorName), value);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector and expects an object return value.
     */
    static MemorySegment sendAddress(MemorySegment receiver, String selectorName) {
        try {
            return (MemorySegment) MSG_SEND_ADDR.invokeExact(receiver, selector(selectorName));
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector with one object argument and expects an object return value.
     */
    static MemorySegment sendAddressAddress(MemorySegment receiver, String selectorName, MemorySegment argument) {
        try {
            return (MemorySegment) MSG_SEND_ADDR_ADDR.invokeExact(receiver, selector(selectorName), argument);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector with one {@code long} argument and expects an object return value.
     */
    static MemorySegment sendAddressLong(MemorySegment receiver, String selectorName, long argument) {
        try {
            return (MemorySegment) MSG_SEND_ADDR_LONG.invokeExact(receiver, selector(selectorName), argument);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    /**
     * Sends a selector with signature {@code (id, SEL, id, long) -> id}.
     */
    static MemorySegment sendAddressAddressLong(
        MemorySegment receiver,
        String selectorName,
        MemorySegment object,
        long number
    ) {
        try {
            return (MemorySegment) MSG_SEND_ADDR_ADDR_LONG.invokeExact(receiver, selector(selectorName), object, number);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
        }
    }

    static void setAssociatedObject(MemorySegment object, MemorySegment key, MemorySegment value, long policy) {
        try {
            OBJC_SET_ASSOCIATED_OBJECT.invokeExact(object, key, value, policy);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_setAssociatedObject failed", t);
        }
    }

    static MemorySegment getAssociatedObject(MemorySegment object, MemorySegment key) {
        try {
            return (MemorySegment) OBJC_GET_ASSOCIATED_OBJECT.invokeExact(object, key);
        } catch (Throwable t) {
            throw new IllegalStateException("objc_getAssociatedObject failed", t);
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

    /**
     * Sends a selector with one object argument and expects a boolean return value.
     */
    static boolean sendBoolAddress(MemorySegment receiver, String selectorName, MemorySegment argument) {
        try {
            byte response = (byte) MSG_SEND_BOOL_ADDR.invokeExact(receiver, selector(selectorName), argument);
            return response != 0;
        } catch (Throwable t) {
            throw new IllegalStateException("objc_msgSend failed for selector " + selectorName, t);
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
