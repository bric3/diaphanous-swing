/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#ifndef DIAPHANOUS_WINDOW_BRIDGE_H
#define DIAPHANOUS_WINDOW_BRIDGE_H

#ifdef __cplusplus
extern "C" {
#endif

int diaphanous_install_vibrant_wrapper(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
);
int diaphanous_update_vibrant_material(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
);
int diaphanous_remove_vibrant_wrapper(void* ns_window_ptr);
int diaphanous_dump_window_state(void* ns_window_ptr);
double diaphanous_default_effect_alpha(void);
int diaphanous_default_effect_material(void);
double diaphanous_read_effect_alpha(void* ns_window_ptr);
int diaphanous_read_effect_material(void* ns_window_ptr);
/**
 * Applies native window style knobs related to titlebar/content integration.
 *
 * Applies `NSWindowStyleMaskFullSizeContentView`, titlebar transparency,
 * title visibility, and optional toolbar style.
 *
 * @param ns_window_ptr borrowed `NSWindow*`.
 * @param transparent_title_bar boolean flag (`0` false, non-zero true).
 * @param full_size_content_view boolean flag (`0` false, non-zero true).
 * @param title_visible boolean flag (`0` hidden, non-zero visible).
 * @param toolbar_style raw `NSWindowToolbarStyle` value.
 * @param has_toolbar_style when non-zero, applies `toolbar_style`.
 */
int diaphanous_apply_window_style(
    void* ns_window_ptr,
    int transparent_title_bar,
    int full_size_content_view,
    int title_visible,
    long toolbar_style,
    int has_toolbar_style
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
