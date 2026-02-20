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

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridges Java calls to the AppKit main thread.
 * <p>
 * AppKit window mutation APIs must run on the macOS main thread. Violating this rule can
 * crash with {@code SIGTRAP} and messages like "Must only be used from the main thread".
 */
final class MacThreading {
    private static final Method PERFORM_ON_MAIN_THREAD_AFTER_DELAY;

    static {
        try {
            Class<?> lwcToolkitClass = Class.forName("sun.lwawt.macosx.LWCToolkit");
            PERFORM_ON_MAIN_THREAD_AFTER_DELAY =
                lwcToolkitClass.getDeclaredMethod("performOnMainThreadAfterDelay", Runnable.class, long.class);
            PERFORM_ON_MAIN_THREAD_AFTER_DELAY.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private MacThreading() {
    }

    static void runOnAppKitThread(Runnable runnable) {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Runnable wrapped = () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        };

        try {
            // LWCToolkit provides a stable bridge into the real AppKit main thread.
            PERFORM_ON_MAIN_THREAD_AFTER_DELAY.invoke(null, wrapped, 0L);
            done.await();
            if (failure.get() != null) {
                throw new IllegalStateException("Failed to execute on macOS AppKit thread", failure.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for AppKit thread execution", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke LWCToolkit.performOnMainThreadAfterDelay", e);
        }
    }
}
