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

@interface DiaphanousWrappedAWTView : NSView

- (instancetype) initWithAWTView: (NSView *) view;
- (void) installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material alpha: (CGFloat) alpha;
- (void) removeEffect;
- (NSView *) awtView;
- (NSVisualEffectView *) effectView;

@end

@implementation DiaphanousWrappedAWTView {
    NSView *awtView;
    NSVisualEffectView *effectView;
}

- (instancetype) initWithAWTView: (NSView *) view {
    self = [super initWithFrame: view.frame];
    if (self) {
        awtView = view;
        self.autoresizesSubviews = YES;
        self.autoresizingMask = NSViewWidthSizable | NSViewHeightSizable;
        self.wantsLayer = YES;

        awtView.autoresizingMask = NSViewWidthSizable | NSViewHeightSizable;
        [awtView setFrame: self.bounds];
        [self addSubview: awtView];
    }
    return self;
}

- (void) setFrameSize: (NSSize) newSize {
    [super setFrameSize: newSize];
    [awtView setFrameSize: newSize];
    if (effectView != nil) {
        [effectView setFrameSize: newSize];
    }
}

- (void) installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material alpha: (CGFloat) alpha {
    if (effectView == nil) {
        effectView = [[NSVisualEffectView alloc] initWithFrame: self.bounds];
        effectView.blendingMode = NSVisualEffectBlendingModeBehindWindow;
        effectView.state = NSVisualEffectStateActive;
        effectView.autoresizingMask = NSViewWidthSizable | NSViewHeightSizable;
        effectView.translatesAutoresizingMaskIntoConstraints = YES;
        [self addSubview: effectView positioned: NSWindowBelow relativeTo: awtView];
    }
    effectView.material = material;
    effectView.alphaValue = alpha;
}

- (void) removeEffect {
    if (effectView != nil) {
        [effectView removeFromSuperview];
        effectView = nil;
    }
}

- (NSView *) awtView {
    return awtView;
}

- (NSVisualEffectView *) effectView {
    return effectView;
}

- (BOOL) mouseIsOver {
    if ([awtView respondsToSelector: @selector(mouseIsOver)]) {
        return [(id) awtView mouseIsOver];
    }
    return NO;
}

- (void) deliverJavaMouseEvent: (NSEvent *) event {
    if ([awtView respondsToSelector: @selector(deliverJavaMouseEvent:)]) {
        [(id) awtView deliverJavaMouseEvent: event];
    }
}

@end

static int run_on_main_sync(int (^block)(void)) {
    if ([NSThread isMainThread]) {
        return block();
    }
    __block int result = -1;
    dispatch_sync(dispatch_get_main_queue(), ^{
        result = block();
    });
    return result;
}

static NSString *color_desc(NSColor *color) {
    if (color == nil) {
        return @"nil";
    }
    NSColor *c = [color colorUsingColorSpace: NSColorSpace.sRGBColorSpace];
    if (c == nil) {
        return @"non-srgb";
    }
    return [NSString stringWithFormat: @"rgba(%.3f, %.3f, %.3f, %.3f)", c.redComponent, c.greenComponent, c.blueComponent, c.alphaComponent];
}

static void dump_view(NSView *view, int depth) {
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
        layerBg = color_desc(layerColor);
    }
    NSLog(@"%@- %@ frame=%@ opaque=%d wantsLayer=%d layer=%@ layerOpaque=%d layerBg=%@",
        indent,
        NSStringFromClass(view.class),
        NSStringFromRect(view.frame),
        view.isOpaque,
        view.wantsLayer,
        layer,
        layer ? layer.opaque : 0,
        layerBg
    );
    for (NSView *child in view.subviews) {
        dump_view(child, depth + 1);
    }
}

static DiaphanousWrappedAWTView *ensure_wrapper(NSWindow *window) {
    NSView *content = window.contentView;
    if ([content isKindOfClass: [DiaphanousWrappedAWTView class]]) {
        return (DiaphanousWrappedAWTView *) content;
    }

    window.contentView = nil;
    DiaphanousWrappedAWTView *wrapper = [[DiaphanousWrappedAWTView alloc] initWithAWTView: content];
    window.contentView = wrapper;
    return wrapper;
}

static void configure_window_and_host(NSWindow *window, DiaphanousWrappedAWTView *wrapper) {
    window.opaque = NO;
    window.backgroundColor = NSColor.clearColor;

    wrapper.wantsLayer = YES;
    if (wrapper.layer != nil) {
        wrapper.layer.opaque = NO;
        wrapper.layer.backgroundColor = NSColor.clearColor.CGColor;
    }

    NSView *awtHost = wrapper.awtView;
    awtHost.wantsLayer = YES;
    if ([awtHost respondsToSelector: @selector(setOpaque:)]) {
        [(id) awtHost setOpaque: NO];
    }
    if ([awtHost respondsToSelector: @selector(setWantsLayer:)]) {
        [(id) awtHost setWantsLayer: YES];
    }
    if (awtHost.layer != nil) {
        awtHost.layer.opaque = NO;
        awtHost.layer.backgroundColor = NSColor.clearColor.CGColor;
    }
}

extern "C" int diaphanous_install_vibrant_wrapper(void* ns_window_ptr, int material, double alpha) {
    return run_on_main_sync(^int {
        if (ns_window_ptr == nullptr) {
            return -1;
        }
        NSWindow *window = (__bridge NSWindow *) ns_window_ptr;
        if (window == nil) {
            return -1;
        }

        DiaphanousWrappedAWTView *wrapper = ensure_wrapper(window);
        if (wrapper == nil) {
            return -1;
        }
        configure_window_and_host(window, wrapper);
        [wrapper installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material alpha: (CGFloat) alpha];
        return 0;
    });
}

extern "C" int diaphanous_update_vibrant_material(void* ns_window_ptr, int material, double alpha) {
    return run_on_main_sync(^int {
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
            [wrapper installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material alpha: (CGFloat) alpha];
            return 0;
        }
        return diaphanous_install_vibrant_wrapper(ns_window_ptr, material, alpha);
    });
}

extern "C" int diaphanous_remove_vibrant_wrapper(void* ns_window_ptr) {
    return run_on_main_sync(^int {
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

extern "C" int diaphanous_dump_window_state(void* ns_window_ptr) {
    return run_on_main_sync(^int {
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
            color_desc(window.backgroundColor)
        );
        NSView *content = window.contentView;
        NSLog(@"[diaphanous] contentView=%@ class=%@", content, NSStringFromClass(content.class));
        dump_view(content, 0);
        NSLog(@"[diaphanous] ---- window dump end ----");
        return 0;
    });
}

extern "C" double diaphanous_default_effect_alpha(void) {
    __block double value = -1.0;
    run_on_main_sync(^int {
        NSVisualEffectView *view = [[NSVisualEffectView alloc] initWithFrame: NSZeroRect];
        value = view != nil ? (double) view.alphaValue : -1.0;
        return 0;
    });
    return value;
}

extern "C" int diaphanous_default_effect_material(void) {
    __block int value = -1;
    run_on_main_sync(^int {
        NSVisualEffectView *view = [[NSVisualEffectView alloc] initWithFrame: NSZeroRect];
        value = view != nil ? (int) view.material : -1;
        return 0;
    });
    return value;
}

extern "C" double diaphanous_read_effect_alpha(void* ns_window_ptr) {
    __block double value = -1.0;
    run_on_main_sync(^int {
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
    run_on_main_sync(^int {
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
