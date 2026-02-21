# Diaphanous Swing

`diaphanous-swing` is a Java library that allows to make Swing/AWT windows us the translucent style. 

> [!NOTE]
> At this time only macOs support has been implemented.

## Project layout

- `diaphanous-core`: library module.
- `diaphanous-core-macos-native`: macOS native bridge (`NSView` wrapper + effect view management).
- `demo-swing`: sample Swing app using the library.

## What works

### macOS
    
- Window backdrop support via `MacBackdropSupport`

- Simple Windows decorations (however, support is limited, and weisJ/darklaf platform-decorations is preferred)
  - Transparent title bar (`setTitlebarAppearsTransparent:`)
  - Full-size content view style bit (`setStyleMask:` with `NSWindowStyleMaskFullSizeContentView`)
  - Title visibility (`setTitleVisibility:`)
  - Toolbar style when available (`setToolbarStyle:`)
  - Vibrancy backdrop with `NSVisualEffectView` (`MacWindowBackdrop.apply(...)` / `MacWindowBackdrop.clear(...)`)

## Run the demo

Decorated mode:

```bash
./gradlew :demo-swing:run
```

The demo is preconfigured with this JVM argument:

- `--enable-native-access=ALL-UNNAMED`

`diaphanous-core` bundles a simple macOS native library that is loaded it from classpath by default.
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
