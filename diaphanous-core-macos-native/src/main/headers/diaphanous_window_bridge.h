/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#ifndef DIAPHANOUS_WINDOW_BRIDGE_H
#define DIAPHANOUS_WINDOW_BRIDGE_H

/**
 * @file diaphanous_window_bridge.h
 * @brief C ABI exposed by the macOS native bridge used from Java.
 *
 * This header defines a narrow Objective-C/C bridge used by `diaphanous-core`.
 * Callers pass a raw `NSWindow*` pointer obtained on the Java side from the
 * AWT peer graph. The bridge then performs AppKit mutations on the main thread.
 *
 * Return contract:
 * - Functions returning `int` use `0` for success and `-1` for failure.
 * - Reader helpers returning scalar values use sentinel values when unavailable
 *   (documented per function).
 *
 * Pointer contract:
 * - `ns_window_ptr` must be a valid `NSWindow*`.
 * - The pointer is borrowed for the duration of the call only.
 */

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Installs (or reuses) the wrapped content-view hierarchy for vibrancy.
 *
 * Creates `DiaphanousWrappedAWTView` as window content view when needed,
 * reparents the existing AWT host view as a child, and installs an
 * `NSVisualEffectView` behind it.
 *
 * This operation is idempotent for repeated calls on the same window.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @param material `NSVisualEffectMaterial` raw value.
 * @param alpha effect alpha in `[0,1]` (clamped by implementation where needed).
 * @param state `NSVisualEffectState` raw value.
 * @param emphasized boolean flag (`0` false, non-zero true).
 */
int diaphanous_install_backdrop_effect(
    void* ns_window_ptr,
    int material,
    double alpha,
    int state,
    int emphasized
);
/**
 * Updates backdrop parameters for an installed visual effect view.
 *
 * If the wrapper/effect does not exist yet, this behaves like
 * `diaphanous_install_backdrop_effect`.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @param material `NSVisualEffectMaterial` raw value.
 * @param alpha effect alpha in `[0,1]`.
 * @param state `NSVisualEffectState` raw value.
 * @param emphasized boolean flag (`0` false, non-zero true).
 */
int diaphanous_update_backdrop_effect(
    void* ns_window_ptr,
    int material,
    double alpha,
    int state,
    int emphasized
);
/**
 * Removes the backdrop effect view from the wrapper if present.
 *
 * The wrapper and reparented AWT host view remain installed. This allows later
 * re-enable operations without another structural reparenting step.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 */
int diaphanous_remove_backdrop_effect(void* ns_window_ptr);
/**
 * Emits a best-effort diagnostic dump of window/view/layer state to NSLog.
 *
 * Intended for debugging rendering issues (opacity, layer composition, etc.).
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 */
int diaphanous_dump_window_state(void* ns_window_ptr);
/**
 * Reads default `NSVisualEffectView.alphaValue` from a fresh native instance.
 *
 * @return default alpha, or `-1.0` when unavailable.
 */
double diaphanous_default_effect_alpha(void);
/**
 * Reads default `NSVisualEffectView.material` from a fresh native instance.
 *
 * @return default material raw value, or `-1` when unavailable.
 */
int diaphanous_default_effect_material(void);
/**
 * Reads current effect alpha from the installed backdrop on a window.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @return current alpha, or `-1.0` when no effect is installed/unavailable.
 */
double diaphanous_read_effect_alpha(void* ns_window_ptr);
/**
 * Reads current effect material from the installed backdrop on a window.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @return current material raw value, or `-1` when no effect is installed/unavailable.
 */
int diaphanous_read_effect_material(void* ns_window_ptr);
/**
 * Applies native window style knobs related to titlebar/content integration.
 *
 * Applies `NSWindowStyleMaskFullSizeContentView`, titlebar transparency,
 * and title visibility.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @param transparent_title_bar boolean flag (`0` false, non-zero true).
 * @param full_size_content_view boolean flag (`0` false, non-zero true).
 * @param title_visible boolean flag (`0` hidden, non-zero visible).
 */
int diaphanous_apply_window_style(
    void* ns_window_ptr,
    int transparent_title_bar,
    int full_size_content_view,
    int title_visible
);
/**
 * Applies a native appearance by AppKit appearance name.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @param appearance_name_utf8 UTF-8 `NSAppearanceName...` value; `NULL` restores system.
 */
int diaphanous_apply_window_appearance(void* ns_window_ptr, const char* appearance_name_utf8);
/**
 * Sets `NSWindow.alphaValue`.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @param alpha requested alpha; implementation clamps to `[0,1]`.
 */
int diaphanous_set_window_alpha(void* ns_window_ptr, double alpha);

#ifdef __cplusplus
}
#endif

#endif
