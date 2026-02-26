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
#include "diaphanous_window_bridge_internal.h"

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

- (void) installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                      state: (NSVisualEffectState) state
                                 emphasized: (BOOL) emphasized
                                      alpha: (CGFloat) alpha {
    if (effectView == nil) {
        effectView = [[NSVisualEffectView alloc] initWithFrame: self.bounds];
        effectView.autoresizingMask = NSViewWidthSizable | NSViewHeightSizable;
        effectView.translatesAutoresizingMaskIntoConstraints = YES;
        [self addSubview: effectView positioned: NSWindowBelow relativeTo: awtView];
    }
    effectView.material = material;
    // Swing integration uses a wrapped top-level host view, so behind-window blending
    // is the only practical mode and is intentionally fixed in the native layer.
    effectView.blendingMode = NSVisualEffectBlendingModeBehindWindow;
    effectView.state = state;
    if ([effectView respondsToSelector: @selector(setEmphasized:)]) {
        effectView.emphasized = emphasized;
    }
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

int diaphanous_run_on_main_sync(int (^block)(void)) {
    if ([NSThread isMainThread]) {
        return block();
    }
    __block int result = -1;
    dispatch_sync(dispatch_get_main_queue(), ^{
        result = block();
    });
    return result;
}

DiaphanousWrappedAWTView *diaphanous_ensure_wrapper(NSWindow *window) {
    NSView *content = window.contentView;
    if ([content isKindOfClass: [DiaphanousWrappedAWTView class]]) {
        return (DiaphanousWrappedAWTView *) content;
    }

    window.contentView = nil;
    DiaphanousWrappedAWTView *wrapper = [[DiaphanousWrappedAWTView alloc] initWithAWTView: content];
    window.contentView = wrapper;
    return wrapper;
}

void diaphanous_configure_window_and_host(NSWindow *window, DiaphanousWrappedAWTView *wrapper) {
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
