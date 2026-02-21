/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#ifndef DIAPHANOUS_WINDOW_BRIDGE_INTERNAL_H
#define DIAPHANOUS_WINDOW_BRIDGE_INTERNAL_H

#import <Cocoa/Cocoa.h>

/**
 * Internal wrapper content view that hosts:
 * - the original AWT host view (front), and
 * - one NSVisualEffectView (back).
 */
@interface DiaphanousWrappedAWTView : NSView
- (instancetype) initWithAWTView: (NSView *) view;
- (void) installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                  blending: (NSVisualEffectBlendingMode) blendingMode
                                      state: (NSVisualEffectState) state
                                 emphasized: (BOOL) emphasized
                                      alpha: (CGFloat) alpha;
- (void) removeEffect;
- (NSView *) awtView;
- (NSVisualEffectView *) effectView;
@end

/**
 * Executes an AppKit block synchronously on main thread.
 *
 * @return block result, or -1 when scheduling fails.
 */
int diaphanous_run_on_main_sync(int (^block)(void));

/**
 * Ensures the window contentView is wrapped in DiaphanousWrappedAWTView.
 */
DiaphanousWrappedAWTView *diaphanous_ensure_wrapper(NSWindow *window);

/**
 * Applies transparent host/window flags required for backdrop rendering.
 */
void diaphanous_configure_window_and_host(NSWindow *window, DiaphanousWrappedAWTView *wrapper);

/**
 * Utility used by diagnostics dump.
 */
NSString *diaphanous_color_desc(NSColor *color);
void diaphanous_dump_view(NSView *view, int depth);

#endif
