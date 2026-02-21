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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Optional bridge to the macOS native helper library that can wrap the AWT host view.
 */
final class MacNativeVibrancyBridge {
    private static final String LIB_PATH_PROPERTY = "diaphanous.macos.nativeLib";
    private static final String DEFAULT_RELATIVE_LIB =
        "diaphanous-core-macos-native/build/lib/main/debug/libdiaphanous-core-macos-native.dylib";
    private static final NativeFns FNS = loadNativeFns().orElse(null);

    private MacNativeVibrancyBridge() {
    }

    static boolean isAvailable() {
        return FNS != null;
    }

    static boolean install(
        long nsWindowPtr,
        int material,
        double alpha,
        int blendingMode,
        int state,
        boolean emphasized
    ) {
        if (FNS == null || nsWindowPtr == 0L) {
            return false;
        }
        return FNS.install(nsWindowPtr, material, alpha, blendingMode, state, emphasized) == 0;
    }

    static boolean update(
        long nsWindowPtr,
        int material,
        double alpha,
        int blendingMode,
        int state,
        boolean emphasized
    ) {
        if (FNS == null || nsWindowPtr == 0L) {
            return false;
        }
        return FNS.update(nsWindowPtr, material, alpha, blendingMode, state, emphasized) == 0;
    }

    static boolean remove(long nsWindowPtr) {
        if (FNS == null || nsWindowPtr == 0L) {
            return false;
        }
        return FNS.remove(nsWindowPtr) == 0;
    }

    static boolean dump(long nsWindowPtr) {
        if (FNS == null || nsWindowPtr == 0L) {
            return false;
        }
        return FNS.dump(nsWindowPtr) == 0;
    }

    static double defaultAlpha() {
        if (FNS == null) {
            return -1.0;
        }
        return FNS.defaultAlpha();
    }

    static int defaultMaterial() {
        if (FNS == null) {
            return -1;
        }
        return FNS.defaultMaterial();
    }

    static double readAlpha(long nsWindowPtr) {
        if (FNS == null || nsWindowPtr == 0L) {
            return -1.0;
        }
        return FNS.readAlpha(nsWindowPtr);
    }

    static int readMaterial(long nsWindowPtr) {
        if (FNS == null || nsWindowPtr == 0L) {
            return -1;
        }
        return FNS.readMaterial(nsWindowPtr);
    }

    private static Optional<NativeFns> loadNativeFns() {
        Path libPath = resolveLibraryPath();
        if (libPath == null || !Files.isRegularFile(libPath)) {
            return Optional.empty();
        }

        try {
            SymbolLookup lookup = SymbolLookup.libraryLookup(libPath, Arena.global());
            Linker linker = Linker.nativeLinker();

            MethodHandle installHandle = linker.downcallHandle(
                lookup.find("diaphanous_install_vibrant_wrapper")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_install_vibrant_wrapper")),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
                )
            );
            MethodHandle updateHandle = linker.downcallHandle(
                lookup.find("diaphanous_update_vibrant_material")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_update_vibrant_material")),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_DOUBLE,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
                )
            );
            MethodHandle removeHandle = linker.downcallHandle(
                lookup.find("diaphanous_remove_vibrant_wrapper")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_remove_vibrant_wrapper")),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            MethodHandle dumpHandle = linker.downcallHandle(
                lookup.find("diaphanous_dump_window_state")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_dump_window_state")),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            MethodHandle defaultAlphaHandle = linker.downcallHandle(
                lookup.find("diaphanous_default_effect_alpha")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_default_effect_alpha")),
                FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE)
            );
            MethodHandle defaultMaterialHandle = linker.downcallHandle(
                lookup.find("diaphanous_default_effect_material")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_default_effect_material")),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
            );
            MethodHandle readAlphaHandle = linker.downcallHandle(
                lookup.find("diaphanous_read_effect_alpha")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_read_effect_alpha")),
                FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS)
            );
            MethodHandle readMaterialHandle = linker.downcallHandle(
                lookup.find("diaphanous_read_effect_material")
                    .orElseThrow(() -> new IllegalStateException("Missing symbol diaphanous_read_effect_material")),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );

            return Optional.of(
                new NativeFns(
                    installHandle,
                    updateHandle,
                    removeHandle,
                    dumpHandle,
                    defaultAlphaHandle,
                    defaultMaterialHandle,
                    readAlphaHandle,
                    readMaterialHandle
                )
            );
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    private static Path resolveLibraryPath() {
        String explicit = System.getProperty(LIB_PATH_PROPERTY);
        if (explicit != null && !explicit.isBlank()) {
            return Path.of(explicit).toAbsolutePath().normalize();
        }

        Path fromCwd = Path.of(System.getProperty("user.dir", "."), DEFAULT_RELATIVE_LIB).toAbsolutePath().normalize();
        if (Files.isRegularFile(fromCwd)) {
            return fromCwd;
        }
        return null;
    }

    private static final class NativeFns {
        private final MethodHandle installHandle;
        private final MethodHandle updateHandle;
        private final MethodHandle removeHandle;
        private final MethodHandle dumpHandle;
        private final MethodHandle defaultAlphaHandle;
        private final MethodHandle defaultMaterialHandle;
        private final MethodHandle readAlphaHandle;
        private final MethodHandle readMaterialHandle;

        private NativeFns(
            MethodHandle installHandle,
            MethodHandle updateHandle,
            MethodHandle removeHandle,
            MethodHandle dumpHandle,
            MethodHandle defaultAlphaHandle,
            MethodHandle defaultMaterialHandle,
            MethodHandle readAlphaHandle,
            MethodHandle readMaterialHandle
        ) {
            this.installHandle = installHandle;
            this.updateHandle = updateHandle;
            this.removeHandle = removeHandle;
            this.dumpHandle = dumpHandle;
            this.defaultAlphaHandle = defaultAlphaHandle;
            this.defaultMaterialHandle = defaultMaterialHandle;
            this.readAlphaHandle = readAlphaHandle;
            this.readMaterialHandle = readMaterialHandle;
        }

        private int install(
            long nsWindowPtr,
            int material,
            double alpha,
            int blendingMode,
            int state,
            boolean emphasized
        ) {
            try {
                return (int) installHandle.invokeExact(
                    MemorySegment.ofAddress(nsWindowPtr),
                    material,
                    alpha,
                    blendingMode,
                    state,
                    emphasized ? 1 : 0
                );
            } catch (Throwable t) {
                throw new IllegalStateException("Native install call failed", t);
            }
        }

        private int update(
            long nsWindowPtr,
            int material,
            double alpha,
            int blendingMode,
            int state,
            boolean emphasized
        ) {
            try {
                return (int) updateHandle.invokeExact(
                    MemorySegment.ofAddress(nsWindowPtr),
                    material,
                    alpha,
                    blendingMode,
                    state,
                    emphasized ? 1 : 0
                );
            } catch (Throwable t) {
                throw new IllegalStateException("Native update call failed", t);
            }
        }

        private int remove(long nsWindowPtr) {
            try {
                return (int) removeHandle.invokeExact(MemorySegment.ofAddress(nsWindowPtr));
            } catch (Throwable t) {
                throw new IllegalStateException("Native remove call failed", t);
            }
        }

        private int dump(long nsWindowPtr) {
            try {
                return (int) dumpHandle.invokeExact(MemorySegment.ofAddress(nsWindowPtr));
            } catch (Throwable t) {
                throw new IllegalStateException("Native dump call failed", t);
            }
        }

        private double defaultAlpha() {
            try {
                return (double) defaultAlphaHandle.invokeExact();
            } catch (Throwable t) {
                throw new IllegalStateException("Native default alpha call failed", t);
            }
        }

        private int defaultMaterial() {
            try {
                return (int) defaultMaterialHandle.invokeExact();
            } catch (Throwable t) {
                throw new IllegalStateException("Native default material call failed", t);
            }
        }

        private double readAlpha(long nsWindowPtr) {
            try {
                return (double) readAlphaHandle.invokeExact(MemorySegment.ofAddress(nsWindowPtr));
            } catch (Throwable t) {
                throw new IllegalStateException("Native read alpha call failed", t);
            }
        }

        private int readMaterial(long nsWindowPtr) {
            try {
                return (int) readMaterialHandle.invokeExact(MemorySegment.ofAddress(nsWindowPtr));
            } catch (Throwable t) {
                throw new IllegalStateException("Native read material call failed", t);
            }
        }
    }
}
