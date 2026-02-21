<!--
  Diaphanous Swing

  Copyright (c) ${year} - ${name}

  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

# removing `--add-opens` requirement

## context

The current implementation reaches native macOS window objects from Java by traversing AWT internals (`java.awt` peer state and `sun.lwawt*` classes).  
On JDK 9+, that reflective path is strongly encapsulated by the module system, so runtime access requires:

- `--add-opens=java.desktop/java.awt=ALL-UNNAMED`
- `--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED`
- `--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED`

Without these flags, reflective access fails before style/backdrop code can resolve the target native window.

## why the flags are needed today

`MacosWindowPeerAccess` currently resolves `NSWindow*` from Java-side peer internals.  
This design is straightforward for prototyping and debugging, but it depends on non-public JDK implementation details and open modules.

## path to remove the requirement

The robust direction is to move native handle resolution into the macOS native library:

1. Use JNI/JAWT to resolve the platform drawing surface from `java.awt.Window` or peer-owned native view.
2. Extract native macOS view handle from JAWT structures.
3. Resolve `NSWindow` from that view in Objective-C (`[view window]`).
4. Run all AppKit mutations (style, appearance, effect view install/update/remove) from native code.
5. Keep Java API as a typed facade passing options only; no reflective traversal of `sun.*`.

After this change, application launch should no longer require `--add-opens` for styling/backdrop features.

## expected tradeoffs

- pros
- Stronger compatibility with module boundaries.
- Fewer startup/runtime JVM flags for users.
- Lower coupling to JDK-internal Java class layouts.

- cons
- More JNI/JAWT and Objective-C code to maintain.
- More native error-handling and lifecycle concerns.
- Additional cross-JDK verification needed for JAWT behavior.

## migration strategy

1. Keep current Java-based resolver as fallback behind a feature flag.
2. Implement native resolver path in `diaphanous-core-macos-native`.
3. Switch default to native resolver in demo and tests.
4. Remove `--add-opens` from demo runtime configuration.
5. Validate on supported JDKs/macOS versions; then delete fallback.

## status update

The project now includes a native JNI resolver in the existing macOS bridge:

- `Window -> peer -> platformWindow -> ptr` is resolved from native code (`MacosNativeWindowHandleBridge`).
- `MacosWindowPeerAccess` prefers that native resolver and uses Java reflection only as fallback.
- demo runtime tasks removed `--add-opens` flags and now keep only `--enable-native-access=ALL-UNNAMED`.
- style/appearance/alpha application also has native bridge entry points, reducing dependence on Java-side AppKit threading reflection.

## implemented change explained

The implementation changed from a Java-reflection-first design to a native-first design.

### before

1. Java code accessed `Component.peer`, `LWWindowPeer`, `platformWindow`, and native `ptr` with reflection.
2. Java code reflected into `sun.lwawt.macosx.LWCToolkit` to dispatch some operations to AppKit main thread.
3. This required `--add-opens` at runtime.

### after

1. The existing native library now exports JNI methods that accept a Java `Window` and resolve the native pointer directly.
2. `MacosWindowPeerAccess` now tries native resolver first, and uses reflection only as fallback.
3. Native bridge also exposes style/appearance/alpha operations, each marshaled onto AppKit main thread inside native code.
4. Demo and robot tasks no longer pass `--add-opens`; only `--enable-native-access=ALL-UNNAMED` remains.

### key Java-side flow

1. Public API call (`MacosWindowDecorations` / `MacWindowBackdrop`) reaches `MacosWindowStyler`.
2. `MacosWindowStyler` detects native bridge availability.
3. If available:
   - pointer comes from `MacosNativeWindowHandleBridge`,
   - operations are sent to native exports (`diaphanous_apply_window_style`, `diaphanous_apply_window_appearance`, `diaphanous_set_window_alpha`, vibrancy install/update/remove).
4. If unavailable:
   - old reflective path remains as compatibility fallback.

### why this removes `--add-opens` in normal runs

When the native bridge is present, the runtime no longer needs reflective access to `java.desktop` internals for the hot path.  
That is why demo execution works without `--add-opens`.

### remaining fallback behavior

The reflective path is intentionally still present as a safety net.  
If the native bridge is not built/loaded, fallback may still require `--add-opens`.

### changed files (implementation)

- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacosNativeLibrary.java`
- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacosNativeWindowHandleBridge.java`
- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacosWindowPeerAccess.java`
- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacosNativeVibrancyBridge.java`
- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacosWindowStyler.java`
- `diaphanous-core-macos-native/src/main/cpp/diaphanous_window_bridge.mm`
- `diaphanous-core-macos-native/src/main/headers/diaphanous_window_bridge.h`
- `diaphanous-core-macos-native/build.gradle.kts`
- `demo-swing/build.gradle.kts`
