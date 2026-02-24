/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#include <jni.h>

static bool clear_exception(JNIEnv *env) {
    if (env != nullptr && env->ExceptionCheck()) {
        env->ExceptionClear();
        return true;
    }
    return false;
}

static jobject resolve_component_peer(JNIEnv *env, jobject window) {
    if (env == nullptr || window == nullptr) {
        return nullptr;
    }
    jclass windowClass = env->GetObjectClass(window);
    if (windowClass == nullptr || clear_exception(env)) {
        return nullptr;
    }
    jfieldID peerField = env->GetFieldID(windowClass, "peer", "Ljava/awt/peer/ComponentPeer;");
    if (peerField == nullptr || clear_exception(env)) {
        return nullptr;
    }
    jobject peer = env->GetObjectField(window, peerField);
    if (clear_exception(env)) {
        return nullptr;
    }
    return peer;
}

static jlong resolve_platform_window_ptr(JNIEnv *env, jobject window) {
    jobject peer = resolve_component_peer(env, window);
    if (peer == nullptr) {
        return 0;
    }

    jclass peerClass = env->GetObjectClass(peer);
    if (peerClass == nullptr || clear_exception(env)) {
        return 0;
    }
    jfieldID platformWindowField = env->GetFieldID(peerClass, "platformWindow", "Lsun/lwawt/PlatformWindow;");
    if (platformWindowField == nullptr || clear_exception(env)) {
        return 0;
    }
    jobject platformWindow = env->GetObjectField(peer, platformWindowField);
    if (platformWindow == nullptr || clear_exception(env)) {
        return 0;
    }

    jclass platformWindowClass = env->GetObjectClass(platformWindow);
    if (platformWindowClass == nullptr || clear_exception(env)) {
        return 0;
    }
    jfieldID ptrField = env->GetFieldID(platformWindowClass, "ptr", "J");
    if (ptrField == nullptr || clear_exception(env)) {
        return 0;
    }
    jlong ptr = env->GetLongField(platformWindow, ptrField);
    if (clear_exception(env)) {
        return 0;
    }
    return ptr;
}

extern "C" JNIEXPORT jlong JNICALL
Java_io_github_bric3_diaphanous_platform_macos_MacosNativeWindowHandleBridge_resolveNSWindowPointer0(
    JNIEnv *env,
    jclass,
    jobject window
) {
    return resolve_platform_window_ptr(env, window);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_bric3_diaphanous_platform_macos_MacosNativeWindowHandleBridge_setPeerOpaque0(
    JNIEnv *env,
    jclass,
    jobject window,
    jboolean opaque
) {
    jobject peer = resolve_component_peer(env, window);
    if (peer == nullptr) {
        return JNI_FALSE;
    }
    jclass peerClass = env->GetObjectClass(peer);
    if (peerClass == nullptr || clear_exception(env)) {
        return JNI_FALSE;
    }
    jmethodID setOpaque = env->GetMethodID(peerClass, "setOpaque", "(Z)V");
    if (setOpaque == nullptr || clear_exception(env)) {
        return JNI_FALSE;
    }
    env->CallVoidMethod(peer, setOpaque, opaque);
    if (clear_exception(env)) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}
