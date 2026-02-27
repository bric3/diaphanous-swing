<!--
  Diaphanous Swing

  Copyright (c) 2026 - Brice Dutheil

  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

# technical note 007: window type coverage (frame, dialog, popup)

## question

Can the library tweak backdrop/decorations for windows created differently than `JFrame`
(for example, dialogs or popup-like windows)?

## Short answer

Yes for real AWT/Swing windows (`java.awt.Window` subclasses).  
Partially for popup mechanisms, depending on whether a real heavyweight `Window` exists and is accessible.

## Blockers

1. Native handle resolution:
   - implementation resolves an `NSWindow*` from the provided Java `Window`.
   - if Window ref is not available or if the peer is missing, 
2. How to handle the AWT / macOs native pre-paint, no control over visibility.
3. No control on the `contentPane` of the other type of window, preventing the ability to "erase" the background.

### Popup-specific behavior

Not all popup APIs expose a direct `Window` object:

- `JPopupMenu` is usually lightweight (component painting), so there is no separate native window to style.
- `PopupFactory` may create heavyweight popups in some situations (often backed by `JWindow`), but that window is not always directly exposed for user code.

So practical support is:

- direct window instances (`JDialog`, `JWindow`): supported.
- popup abstractions without a reachable `Window`: not directly targetable through current public API.

### concrete Swing APIs

APIs with explicit window control

- `new JFrame(...); frame.setVisible(true);`
- `new JDialog(owner, ...); dialog.setVisible(true);`
- `new JWindow(owner); window.setVisible(true);`

APIs without proper window control

- `JOptionPane.showMessageDialog/showConfirmDialog/showInputDialog/...`
- `JFileChooser.showOpenDialog/showSaveDialog`
- `JColorChooser.showDialog`
- `JPopupMenu.show(...)`
- `PopupFactory.getPopup(...).show()`
- `ToolTipManager`-managed tooltips (`JToolTip`)

These APIs often create/manage internal dialog or popup windows and do not provide a stable,
user-owned `Window` reference at the right time for deterministic native styling/backdrop setup.

