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
- Vibrancy backdrop with `NSVisualEffectView` (`applyVibrancy(...)` / `clearVibrancy(...)`)
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

The demo is preconfigured with these JVM arguments:

- `--add-opens=java.desktop/java.awt=ALL-UNNAMED`
- `--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED`
- `--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED`

These are required to access the AWT macOS peer and obtain `NSWindow*`.

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

MacWindowStyler.apply(frame, style);

MacVibrancyStyle vibrancy = MacVibrancyStyle.builder()
    .material(MacVibrancyMaterial.UNDER_WINDOW_BACKGROUND)
    .build();
MacWindowStyler.applyVibrancy(frame, vibrancy);

MacWindowStyler.applyAppearance(frame, MacWindowAppearance.SYSTEM);
MacBackdropSupport.configure(frame, MacWindowAppearance.SYSTEM);
```

## License

Mozilla Public License 2.0 (`MPL-2.0`). See `LICENSE`.
