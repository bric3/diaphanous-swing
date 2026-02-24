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

extern "C" int diaphanous_install_backdrop_effect(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        DiaphanousWrappedAWTView *wrapper = diaphanous_ensure_wrapper(window);
        if (wrapper == nil) {
            return -1;
        }
        diaphanous_configure_window_and_host(window, wrapper);
        [wrapper installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                          blending: (NSVisualEffectBlendingMode) blending_mode
                                              state: (NSVisualEffectState) state
                                         emphasized: emphasized != 0
                                              alpha: (CGFloat) alpha];
        return 0;
    });
}

extern "C" int diaphanous_update_backdrop_effect(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        NSView *content = window.contentView;
        if ([content isKindOfClass: [DiaphanousWrappedAWTView class]]) {
            DiaphanousWrappedAWTView *wrapper = (DiaphanousWrappedAWTView *) content;
            [wrapper installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                              blending: (NSVisualEffectBlendingMode) blending_mode
                                                  state: (NSVisualEffectState) state
                                             emphasized: emphasized != 0
                                                  alpha: (CGFloat) alpha];
            return 0;
        }
        return diaphanous_install_backdrop_effect(
            ns_window_ptr,
            material,
            alpha,
            blending_mode,
            state,
            emphasized
        );
    });
}

extern "C" int diaphanous_remove_backdrop_effect(void* ns_window_ptr) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        NSView *content = window.contentView;
        if ([content isKindOfClass: [DiaphanousWrappedAWTView class]]) {
            DiaphanousWrappedAWTView *wrapper = (DiaphanousWrappedAWTView *) content;
            [wrapper removeEffect];
            return 0;
        }
        return -1;
    });
}

extern "C" double diaphanous_default_effect_alpha(void) {
    __block double value = -1.0;
    diaphanous_run_on_main_sync(^int {
        NSVisualEffectView *view = [[NSVisualEffectView alloc] initWithFrame: NSZeroRect];
        value = view != nil ? (double) view.alphaValue : -1.0;
        return 0;
    });
    return value;
}

extern "C" int diaphanous_default_effect_material(void) {
    __block int value = -1;
    diaphanous_run_on_main_sync(^int {
        NSVisualEffectView *view = [[NSVisualEffectView alloc] initWithFrame: NSZeroRect];
        value = view != nil ? (int) view.material : -1;
        return 0;
    });
    return value;
}

extern "C" double diaphanous_read_effect_alpha(void* ns_window_ptr) {
    __block double value = -1.0;
    diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }
        NSView *content = window.contentView;
        if ([content isKindOfClass: [DiaphanousWrappedAWTView class]]) {
            NSVisualEffectView *view = [(DiaphanousWrappedAWTView *) content effectView];
            if (view != nil) {
                value = (double) view.alphaValue;
                return 0;
            }
        }
        return -1;
    });
    return value;
}

extern "C" int diaphanous_read_effect_material(void* ns_window_ptr) {
    __block int value = -1;
    diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }
        NSView *content = window.contentView;
        if ([content isKindOfClass: [DiaphanousWrappedAWTView class]]) {
            NSVisualEffectView *view = [(DiaphanousWrappedAWTView *) content effectView];
            if (view != nil) {
                value = (int) view.material;
                return 0;
            }
        }
        return -1;
    });
    return value;
}
