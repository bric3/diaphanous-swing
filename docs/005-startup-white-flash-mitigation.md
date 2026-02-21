# startup white-frame flash mitigation

## Summary

After vibrancy became visible, a short white flash could still appear during window startup.

## Observation

1. Showing the `JFrame` could briefly present a default compositor frame before the first fully styled/painted state.
2. Moving the window offscreen was not reliable because macOS could clamp or adjust window placement.
3. AWT `frame.opacity` timing alone was not consistently early enough.

## Applied mitigation

1. Create peer with `addNotify()`.
2. Apply initial appearance/style/vibrancy before first show.
3. Set native `NSWindow.alphaValue = 0.0` before `isVisible = true`.
4. Restore native `NSWindow.alphaValue = 1.0` after first paint and a short minimum hidden interval.
5. Use a bounded fallback timeout to guarantee reveal even if paint observation is delayed.

## Implementation details

1. `MacWindowBackdrop.setWindowAlpha(Window, double)` is used to apply native `setAlphaValue:`.
2. Startup sequencing was extracted to `MacStartupReveal.show(Window)` in `diaphanous-core`.
3. `MacStartupReveal.show(Window)` now ensures peer creation (`addNotify()`) before applying native alpha.
4. Reveal now uses a paint-gated strategy:
   - waits for a paint event for the target window,
   - enforces a minimum hidden duration (`60ms` by default),
   - force-reveals after timeout (`650ms` by default).
5. Tuning properties:
   - `diaphanous.startupReveal.minHiddenMillis`
   - `diaphanous.startupReveal.forceRevealMillis`

Primary code locations:

- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacStartupReveal.java`
- `diaphanous-core/src/main/java/io/github/bric3/diaphanous/MacWindowStyler.java`
- `demo-swing/src/main/kotlin/io/github/bric3/diaphanous/demo/DemoApp.kt`
