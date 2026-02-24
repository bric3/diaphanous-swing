/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.platform.macos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

final class MacosNativeLibrary {
    static final String LIB_PATH_PROPERTY = "diaphanous.macos.nativeLib";
    static final String DEFAULT_RELATIVE_LIB =
        "diaphanous-core-macos-native/build/lib/main/debug/libdiaphanous-core-macos-native.dylib";
    static final String BUNDLED_RESOURCE_PATH = "/native/macos/libdiaphanous-core-macos-native-macos-aarch64.dylib";

    private static volatile boolean loadAttempted;
    private static volatile boolean loaded;
    private static volatile Path extractedLibraryPath;

    private MacosNativeLibrary() {
    }

    static Path resolveLibraryPath() {
        String explicit = System.getProperty(LIB_PATH_PROPERTY);
        if (explicit != null && !explicit.isBlank()) {
            return Path.of(explicit).toAbsolutePath().normalize();
        }

        Path fromCwd = Path.of(System.getProperty("user.dir", "."), DEFAULT_RELATIVE_LIB).toAbsolutePath().normalize();
        if (Files.isRegularFile(fromCwd)) {
            return fromCwd;
        }
        return resolveBundledLibraryPath();
    }

    static boolean ensureLoaded() {
        if (loaded) {
            return true;
        }
        if (loadAttempted) {
            return false;
        }
        synchronized (MacosNativeLibrary.class) {
            if (loaded) {
                return true;
            }
            if (loadAttempted) {
                return false;
            }
            loadAttempted = true;
            Path path = resolveLibraryPath();
            if (path == null || !Files.isRegularFile(path)) {
                return false;
            }
            try {
                System.load(path.toString());
                loaded = true;
                return true;
            } catch (Throwable ignored) {
                loaded = false;
                return false;
            }
        }
    }

    private static Path resolveBundledLibraryPath() {
        if (!isMac()) {
            return null;
        }
        Path existing = extractedLibraryPath;
        if (existing != null && Files.isRegularFile(existing)) {
            return existing;
        }
        synchronized (MacosNativeLibrary.class) {
            existing = extractedLibraryPath;
            if (existing != null && Files.isRegularFile(existing)) {
                return existing;
            }
            Path extracted = extractBundledLibrary();
            extractedLibraryPath = extracted;
            return extracted;
        }
    }

    private static Path extractBundledLibrary() {
        try (InputStream input = MacosNativeLibrary.class.getResourceAsStream(BUNDLED_RESOURCE_PATH)) {
            if (input == null) {
                return null;
            }
            Path tempDir = Files.createTempDirectory("diaphanous-macos-native-");
            tempDir.toFile().deleteOnExit();
            Path dylib = tempDir.resolve("libdiaphanous-core-macos-native-macos-aarch64.dylib");
            Files.copy(input, dylib, StandardCopyOption.REPLACE_EXISTING);
            dylib.toFile().deleteOnExit();
            return dylib;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("mac");
    }
}
