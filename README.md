# Diaphanous Swing

`diaphanous-swing` is a Java library that applies macOS `NSWindow` style changes to Swing/AWT windows.

## Project layout

- `diaphanous-core`: library module.
- `diaphanous-core-macos-native`: macOS native bridge (`NSView` wrapper + effect view management).
- `demo-swing`: sample Swing app using the library.

## What works (macOS)

- Transparent title bar (`setTitlebarAppearsTransparent:`)
- Full-size content view style bit (`setStyleMask:` with `NSWindowStyleMaskFullSizeContentView`)
- Title visibility (`setTitleVisibility:`)
- Toolbar style when available (`setToolbarStyle:`)
- Vibrancy backdrop with `NSVisualEffectView` (`MacWindowBackdrop.apply(...)` / `MacWindowBackdrop.clear(...)`)
- Swing-side backdrop support (`MacBackdropSupport`) to prevent Java overpaint in decorated mode.

## Run the demo

Decorated mode:

```bash
./gradlew :demo-swing:run --args='--decorated'
```

Undecorated mode:

```bash
./gradlew :demo-swing:run --args='--undecorated'
```

The demo is preconfigured with this JVM argument:

- `--enable-native-access=ALL-UNNAMED`

`diaphanous-core` bundles the macOS native library (`native/macos/libdiaphanous-core-macos-native-macos-aarch64.dylib`) and loads it from classpath by default.
For local override/debug, set `-Ddiaphanous.macos.nativeLib=/absolute/path/to/libdiaphanous-core-macos-native.dylib`.

Robot smoke test:

```bash
./gradlew :demo-swing:robotTest
```

## Library usage

```java
MacWindowStyle style = MacWindowStyle.builder()
    .transparentTitleBar(true)
    .fullSizeContentView(true)
    .titleVisible(false)
    .toolbarStyle(MacToolbarStyle.UNIFIED_COMPACT)
    .build();

MacWindowDecorations.applyStyle(frame, style);

MacVibrancyStyle vibrancy = MacVibrancyStyle.builder()
    .material(MacVibrancyMaterial.UNDER_WINDOW_BACKGROUND)
    .build();
MacWindowBackdrop.apply(frame, vibrancy);

MacWindowDecorations.applyAppearance(frame, MacWindowAppearance.SYSTEM);
MacBackdropSupport.configure(frame, MacWindowAppearance.SYSTEM);
```

## License

Mozilla Public License 2.0 (`MPL-2.0`). See `LICENSE`.
