# Examples

## Decorations

### 001-style-default-modern

Baseline Screenshot

![001-style-default-modern](src/robotTest/resources/decorations/macos/001-style-default-modern.png)

Code snippet

```java
// resetFramePresentation(frame) already applies default decorations + SYSTEM appearance.
```

### 002-style-title-visible

Baseline Screenshot

![002-style-title-visible](src/robotTest/resources/decorations/macos/002-style-title-visible.png)

Code snippet

```java
WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
    .titleVisible(true)
    .build());
```

### 003-style-opaque-standard

Baseline Screenshot

![003-style-opaque-standard](src/robotTest/resources/decorations/macos/003-style-opaque-standard.png)

Code snippet

```java
WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
    .transparentTitleBar(false)
    .fullSizeContentView(false)
    .titleVisible(true)
    .build());
```

### 004-style-transparent-titlebar-only

Baseline Screenshot

![004-style-transparent-titlebar-only](src/robotTest/resources/decorations/macos/004-style-transparent-titlebar-only.png)

Code snippet

```java
WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
    .transparentTitleBar(true)
    .fullSizeContentView(false)
    .titleVisible(true)
    .build());
```

### 010-appearance-system-no-backdrop

Baseline Screenshot

![010-appearance-system-no-backdrop](src/robotTest/resources/decorations/macos/010-appearance-system-no-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
```

### 011-appearance-aqua-no-backdrop

Baseline Screenshot

![011-appearance-aqua-no-backdrop](src/robotTest/resources/decorations/macos/011-appearance-aqua-no-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
```

### 012-appearance-dark-aqua-no-backdrop

Baseline Screenshot

![012-appearance-dark-aqua-no-backdrop](src/robotTest/resources/decorations/macos/012-appearance-dark-aqua-no-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.DARK_AQUA);
```

### 013-appearance-vibrant-light-no-backdrop

Baseline Screenshot

![013-appearance-vibrant-light-no-backdrop](src/robotTest/resources/decorations/macos/013-appearance-vibrant-light-no-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_LIGHT);
```

### 014-appearance-vibrant-dark-no-backdrop

Baseline Screenshot

![014-appearance-vibrant-dark-no-backdrop](src/robotTest/resources/decorations/macos/014-appearance-vibrant-dark-no-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_DARK);
```

### 020-appearance-system-with-backdrop

Baseline Screenshot

![020-appearance-system-with-backdrop](src/robotTest/resources/decorations/macos/020-appearance-system-with-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
installBackdrop(frame);
```

### 021-appearance-aqua-with-backdrop

Baseline Screenshot

![021-appearance-aqua-with-backdrop](src/robotTest/resources/decorations/macos/021-appearance-aqua-with-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
installBackdrop(frame);
```

### 022-appearance-dark-aqua-with-backdrop

Baseline Screenshot

![022-appearance-dark-aqua-with-backdrop](src/robotTest/resources/decorations/macos/022-appearance-dark-aqua-with-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.DARK_AQUA);
installBackdrop(frame);
```

### 023-appearance-vibrant-light-with-backdrop

Baseline Screenshot

![023-appearance-vibrant-light-with-backdrop](src/robotTest/resources/decorations/macos/023-appearance-vibrant-light-with-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_LIGHT);
installBackdrop(frame);
```

### 024-appearance-vibrant-dark-with-backdrop

Baseline Screenshot

![024-appearance-vibrant-dark-with-backdrop](src/robotTest/resources/decorations/macos/024-appearance-vibrant-dark-with-backdrop.png)

Code snippet

```java
WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_DARK);
installBackdrop(frame);
```

## Backdrop

### 001-enabled-false

| Light | Dark |
| --- | --- |
| ![001-enabled-false light](src/robotTest/resources/backdrop/macos/001-enabled-false-light.png) | ![001-enabled-false dark](src/robotTest/resources/backdrop/macos/001-enabled-false-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .enabled(false)
    .build());
```

### 010-material-appearance-based

| Light | Dark |
| --- | --- |
| ![010-material-appearance-based light](src/robotTest/resources/backdrop/macos/010-material-appearance-based-light.png) | ![010-material-appearance-based dark](src/robotTest/resources/backdrop/macos/010-material-appearance-based-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.APPEARANCE_BASED)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 011-material-light

| Light | Dark |
| --- | --- |
| ![011-material-light light](src/robotTest/resources/backdrop/macos/011-material-light-light.png) | ![011-material-light dark](src/robotTest/resources/backdrop/macos/011-material-light-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.LIGHT)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 012-material-dark

| Light | Dark |
| --- | --- |
| ![012-material-dark light](src/robotTest/resources/backdrop/macos/012-material-dark-light.png) | ![012-material-dark dark](src/robotTest/resources/backdrop/macos/012-material-dark-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.DARK)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 013-material-titlebar

| Light | Dark |
| --- | --- |
| ![013-material-titlebar light](src/robotTest/resources/backdrop/macos/013-material-titlebar-light.png) | ![013-material-titlebar dark](src/robotTest/resources/backdrop/macos/013-material-titlebar-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.TITLEBAR)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 014-material-selection

| Light | Dark |
| --- | --- |
| ![014-material-selection light](src/robotTest/resources/backdrop/macos/014-material-selection-light.png) | ![014-material-selection dark](src/robotTest/resources/backdrop/macos/014-material-selection-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SELECTION)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 015-material-menu

| Light | Dark |
| --- | --- |
| ![015-material-menu light](src/robotTest/resources/backdrop/macos/015-material-menu-light.png) | ![015-material-menu dark](src/robotTest/resources/backdrop/macos/015-material-menu-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.MENU)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 016-material-popover

| Light | Dark |
| --- | --- |
| ![016-material-popover light](src/robotTest/resources/backdrop/macos/016-material-popover-light.png) | ![016-material-popover dark](src/robotTest/resources/backdrop/macos/016-material-popover-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.POPOVER)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 017-material-sidebar

| Light | Dark |
| --- | --- |
| ![017-material-sidebar light](src/robotTest/resources/backdrop/macos/017-material-sidebar-light.png) | ![017-material-sidebar dark](src/robotTest/resources/backdrop/macos/017-material-sidebar-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SIDEBAR)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 018-material-header-view

| Light | Dark |
| --- | --- |
| ![018-material-header-view light](src/robotTest/resources/backdrop/macos/018-material-header-view-light.png) | ![018-material-header-view dark](src/robotTest/resources/backdrop/macos/018-material-header-view-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.HEADER_VIEW)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 019-material-sheet

| Light | Dark |
| --- | --- |
| ![019-material-sheet light](src/robotTest/resources/backdrop/macos/019-material-sheet-light.png) | ![019-material-sheet dark](src/robotTest/resources/backdrop/macos/019-material-sheet-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SHEET)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 020-material-window-background

| Light | Dark |
| --- | --- |
| ![020-material-window-background light](src/robotTest/resources/backdrop/macos/020-material-window-background-light.png) | ![020-material-window-background dark](src/robotTest/resources/backdrop/macos/020-material-window-background-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 021-material-hud-window

| Light | Dark |
| --- | --- |
| ![021-material-hud-window light](src/robotTest/resources/backdrop/macos/021-material-hud-window-light.png) | ![021-material-hud-window dark](src/robotTest/resources/backdrop/macos/021-material-hud-window-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.HUD_WINDOW)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 022-material-full-screen-ui

| Light | Dark |
| --- | --- |
| ![022-material-full-screen-ui light](src/robotTest/resources/backdrop/macos/022-material-full-screen-ui-light.png) | ![022-material-full-screen-ui dark](src/robotTest/resources/backdrop/macos/022-material-full-screen-ui-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.FULL_SCREEN_UI)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 023-material-tooltip

| Light | Dark |
| --- | --- |
| ![023-material-tooltip light](src/robotTest/resources/backdrop/macos/023-material-tooltip-light.png) | ![023-material-tooltip dark](src/robotTest/resources/backdrop/macos/023-material-tooltip-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.TOOLTIP)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 024-material-content-background

| Light | Dark |
| --- | --- |
| ![024-material-content-background light](src/robotTest/resources/backdrop/macos/024-material-content-background-light.png) | ![024-material-content-background dark](src/robotTest/resources/backdrop/macos/024-material-content-background-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.CONTENT_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 025-material-under-window-background

| Light | Dark |
| --- | --- |
| ![025-material-under-window-background light](src/robotTest/resources/backdrop/macos/025-material-under-window-background-light.png) | ![025-material-under-window-background dark](src/robotTest/resources/backdrop/macos/025-material-under-window-background-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 026-material-under-page-background

| Light | Dark |
| --- | --- |
| ![026-material-under-page-background light](src/robotTest/resources/backdrop/macos/026-material-under-page-background-light.png) | ![026-material-under-page-background dark](src/robotTest/resources/backdrop/macos/026-material-under-page-background-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_PAGE_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .emphasized(false)
    .backdropAlpha(1.0d)
    .build());
```

### 100-state-follows-active

| Light | Dark |
| --- | --- |
| ![100-state-follows-active light](src/robotTest/resources/backdrop/macos/100-state-follows-active-light.png) | ![100-state-follows-active dark](src/robotTest/resources/backdrop/macos/100-state-follows-active-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
    .build());
```

### 101-state-active

| Light | Dark |
| --- | --- |
| ![101-state-active light](src/robotTest/resources/backdrop/macos/101-state-active-light.png) | ![101-state-active dark](src/robotTest/resources/backdrop/macos/101-state-active-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .build());
```

### 102-state-inactive

| Light | Dark |
| --- | --- |
| ![102-state-inactive light](src/robotTest/resources/backdrop/macos/102-state-inactive-light.png) | ![102-state-inactive dark](src/robotTest/resources/backdrop/macos/102-state-inactive-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.INACTIVE)
    .build());
```

### 110-sidebar-emphasis-false

| Light | Dark |
| --- | --- |
| ![110-sidebar-emphasis-false light](src/robotTest/resources/backdrop/macos/110-sidebar-emphasis-false-light.png) | ![110-sidebar-emphasis-false dark](src/robotTest/resources/backdrop/macos/110-sidebar-emphasis-false-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SIDEBAR)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .emphasized(false)
    .build());
```

### 111-sidebar-emphasis-true

| Light | Dark |
| --- | --- |
| ![111-sidebar-emphasis-true light](src/robotTest/resources/backdrop/macos/111-sidebar-emphasis-true-light.png) | ![111-sidebar-emphasis-true dark](src/robotTest/resources/backdrop/macos/111-sidebar-emphasis-true-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SIDEBAR)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .emphasized(true)
    .build());
```

### 120-alpha-100

| Light | Dark |
| --- | --- |
| ![120-alpha-100 light](src/robotTest/resources/backdrop/macos/120-alpha-100-light.png) | ![120-alpha-100 dark](src/robotTest/resources/backdrop/macos/120-alpha-100-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .backdropAlpha(1.0d)
    .build());
```

### 121-alpha-090

| Light | Dark |
| --- | --- |
| ![121-alpha-090 light](src/robotTest/resources/backdrop/macos/121-alpha-090-light.png) | ![121-alpha-090 dark](src/robotTest/resources/backdrop/macos/121-alpha-090-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .backdropAlpha(0.90d)
    .build());
```

### 122-alpha-070

| Light | Dark |
| --- | --- |
| ![122-alpha-070 light](src/robotTest/resources/backdrop/macos/122-alpha-070-light.png) | ![122-alpha-070 dark](src/robotTest/resources/backdrop/macos/122-alpha-070-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .backdropAlpha(0.70d)
    .build());
```

### 123-alpha-040

| Light | Dark |
| --- | --- |
| ![123-alpha-040 light](src/robotTest/resources/backdrop/macos/123-alpha-040-light.png) | ![123-alpha-040 dark](src/robotTest/resources/backdrop/macos/123-alpha-040-dark.png) |

Code snippet

```java
WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
    .backdropAlpha(0.40d)
    .build());
```

