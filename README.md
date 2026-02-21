# Diaphanous Swing

`diaphanous-swing` is a Java library to apply native translucency/backdrop and window decoration styles to Swing/AWT windows.

> [!NOTE]
> At this time only macOs support has been implemented.

## Project layout

- `diaphanous-core`: library module.
- `diaphanous-core-macos-native`: macOS native bridge (`NSView` wrapper + effect view management).
- `demo-swing`: sample Swing app using the library.

## What works

### macOS
    
- Window backdrop support via `io.github.bric3.diaphanous.backdrop.ComponentBackdropSupport`

- Simple Windows decorations (however, support is limited, and weisJ/darklaf platform-decorations is preferred)
  - Transparent title bar (`setTitlebarAppearsTransparent:`)
  - Full-size content view style bit (`setStyleMask:` with `NSWindowStyleMaskFullSizeContentView`)
  - Title visibility (`setTitleVisibility:`)
  - Vibrancy backdrop with `NSVisualEffectView` (`WindowBackdrop.apply(...)` / `WindowBackdrop.remove(...)`)

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
import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec;
import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropMaterial;
import io.github.bric3.diaphanous.backdrop.WindowBackgroundEffectSpec;
import io.github.bric3.diaphanous.backdrop.WindowBackdrop;
import io.github.bric3.diaphanous.decorations.MacosWindowAppearanceSpec;
import io.github.bric3.diaphanous.decorations.MacosWindowDecorationsSpec;
import io.github.bric3.diaphanous.decorations.WindowPresentations;

MacosWindowDecorationsSpec style = MacosWindowDecorationsSpec.builder()
    .transparentTitleBar(true)
    .fullSizeContentView(true)
    .titleVisible(false)
    .build();
WindowPresentations.applyDecorations(frame, style);

WindowBackgroundEffectSpec vibrancy = MacosBackdropEffectSpec.builder()
    .material(MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .build();
WindowBackdrop.apply(frame, vibrancy);

WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
```

The facades delegate to platform managers:

- `io.github.bric3.diaphanous.decorations.NativeWindowDecorationsManager`
- `io.github.bric3.diaphanous.backdrop.NativeWindowBackdropManager`

## License

Mozilla Public License 2.0 (`MPL-2.0`). See `LICENSE`.
