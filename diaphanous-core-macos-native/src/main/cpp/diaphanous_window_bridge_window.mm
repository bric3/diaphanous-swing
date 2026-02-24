/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#import <Cocoa/Cocoa.h>
#include "diaphanous_window_bridge.h"
#include "diaphanous_window_bridge_internal.h"

extern "C" int diaphanous_apply_window_style(
    void* ns_window_ptr,
    int transparent_title_bar,
    int full_size_content_view,
    int title_visible
) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        NSUInteger styleMask = window.styleMask;
        if (full_size_content_view != 0) {
            styleMask |= NSWindowStyleMaskFullSizeContentView;
        } else {
            styleMask &= ~NSWindowStyleMaskFullSizeContentView;
        }
        window.styleMask = styleMask;
        window.titlebarAppearsTransparent = transparent_title_bar != 0 ? YES : NO;
        window.titleVisibility = title_visible != 0 ? NSWindowTitleVisible : NSWindowTitleHidden;
        return 0;
    });
}

extern "C" int diaphanous_apply_window_appearance(void* ns_window_ptr, const char* appearance_name_utf8) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        if (appearance_name_utf8 == nullptr) {
            window.appearance = nil;
            return 0;
        }

        NSString *name = [NSString stringWithUTF8String: appearance_name_utf8];
        if (name == nil) {
            return -1;
        }
        NSAppearance *appearance = [NSAppearance appearanceNamed: name];
        window.appearance = appearance;
        return 0;
    });
}

extern "C" int diaphanous_set_window_alpha(void* ns_window_ptr, double alpha) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        CGFloat clamped = (CGFloat) alpha;
        if (clamped < 0.0) clamped = 0.0;
        if (clamped > 1.0) clamped = 1.0;
        window.alphaValue = clamped;
        return 0;
    });
}
