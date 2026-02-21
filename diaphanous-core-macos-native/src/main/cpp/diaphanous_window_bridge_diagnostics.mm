/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#import <Cocoa/Cocoa.h>
#include "diaphanous_window_bridge.h"
#include "diaphanous_window_bridge_internal.h"

NSString *diaphanous_color_desc(NSColor *color) {
    if (color == nil) {
        return @"nil";
    }
    NSColor *c = [color colorUsingColorSpace: NSColorSpace.sRGBColorSpace];
    if (c == nil) {
        return @"non-srgb";
    }
    return [NSString stringWithFormat: @"rgba(%.3f, %.3f, %.3f, %.3f)", c.redComponent, c.greenComponent, c.blueComponent, c.alphaComponent];
}

void diaphanous_dump_view(NSView *view, int depth) {
    if (view == nil) {
        return;
    }
    NSMutableString *indent = [NSMutableString string];
    for (int i = 0; i < depth; i++) {
        [indent appendString: @"  "];
    }
    CALayer *layer = view.layer;
    NSString *layerBg = @"nil";
    if (layer != nil && layer.backgroundColor != nil) {
        NSColor *layerColor = [NSColor colorWithCGColor: layer.backgroundColor];
        layerBg = diaphanous_color_desc(layerColor);
    }
    NSLog(@"%@- %@ frame=%@ opaque=%d wantsLayer=%d layer=%@ layerOpaque=%d layerBg=%@",
          indent,
          NSStringFromClass(view.class),
          NSStringFromRect(view.frame),
          view.isOpaque,
          view.wantsLayer,
          layer,
          layer ? layer.opaque : 0,
          layerBg);
    for (NSView *child in view.subviews) {
        diaphanous_dump_view(child, depth + 1);
    }
}

extern "C" int diaphanous_dump_window_state(void* ns_window_ptr) {
    return diaphanous_run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            NSLog(@"[diaphanous] dump: ns_window_ptr=null");
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            NSLog(@"[diaphanous] dump: window=nil");
            return -1;
        }

        NSLog(@"[diaphanous] ---- window dump begin ----");
        NSLog(@"[diaphanous] window=%@ opaque=%d styleMask=0x%lx titlebarTransparent=%d bg=%@",
              window,
              window.isOpaque,
              (unsigned long) window.styleMask,
              window.titlebarAppearsTransparent,
              diaphanous_color_desc(window.backgroundColor));
        NSView *content = window.contentView;
        NSLog(@"[diaphanous] contentView=%@ class=%@", content, NSStringFromClass(content.class));
        diaphanous_dump_view(content, 0);
        NSLog(@"[diaphanous] ---- window dump end ----");
        return 0;
    });
}
