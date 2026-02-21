# NSVisualEffectView reverse-engineering notes

Reference: <https://oskargroth.com/blog/reverse-engineering-nsvisualeffectview>

## Public API that can be modeled safely

The article confirms the practical public knobs exposed by `NSVisualEffectView`:

- `material`
- `blendingMode`
- `state`
- `emphasized`
- `appearance` (including vibrant variants)
- `alphaValue`

These map cleanly to `MacVibrancyStyle` and can be applied through stable AppKit selectors.

## What is not publicly controllable

The article also shows that the visible blur pipeline is implemented through private backdrop/filter internals (`CABackdropLayer`, `CAFilter`, and related parameters).

Implications:

- There is no stable public “blur radius” API on `NSVisualEffectView`.
- Material/state/blending combinations are profile choices, not direct low-level filter tuning.
- Private filter hacking is possible but intentionally avoided for library stability and forward-compatibility.

## Missing demo examples that were identified

Compared to the available public API, the demo previously focused mainly on:

- alpha slider
- blur proxy slider (material mapping)
- appearance

Missing examples were:

- explicit `material` selection
- `blendingMode` switching (`BEHIND_WINDOW` / `WITHIN_WINDOW`)
- `state` switching (`FOLLOWS_WINDOW_ACTIVE_STATE` / `ACTIVE` / `INACTIVE`)
- `emphasized` toggle

## Demo updates applied

The demo now includes controls for all those missing public knobs:

- `material` combo
- `blending mode` combo
- `state` combo
- `emphasized` checkbox

The native bridge was also expanded so decorated-mode wrapper updates carry the full set (`material`, `blendingMode`, `state`, `emphasized`, `alpha`) instead of only material/alpha.
