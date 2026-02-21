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
#include <jawt.h>
#include <jni.h>

#include "diaphanous_window_bridge.h"

/**
 * Wrapper content view used to host both:
 * - the existing AWT host view (front), and
 * - an NSVisualEffectView (back).
 *
 * Raison d'etre: in decorated Swing windows the AWT host is Metal-backed
 * (`MTLLayer`). Installing effect view via one-shot native calls is not enough;
 * the host must be reliably reparented in a stable native container so the
 * backdrop can be managed without breaking event routing.
 */
@interface DiaphanousWrappedAWTView : NSView

/// Designated initializer with a non-null AWT host view.
- (instancetype) initWithAWTView: (NSView *) view;
/// Creates or updates the effect view using caller-provided knobs.
- (void) installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                  blending: (NSVisualEffectBlendingMode) blendingMode
                                      state: (NSVisualEffectState) state
                                 emphasized: (BOOL) emphasized
                                      alpha: (CGFloat) alpha;
/// Removes only the effect view; keeps wrapper + AWT host intact.
- (void) removeEffect;
/// Returns wrapped AWT host view.
- (NSView *) awtView;
/// Returns installed effect view or nil.
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

- (void) installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                  blending: (NSVisualEffectBlendingMode) blendingMode
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
    effectView.blendingMode = blendingMode;
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

/**
 * Executes an AppKit mutation block on the main thread and returns its status.
 *
 * AppKit requires these operations on main thread; callers may invoke bridge
 * functions from arbitrary Java threads.
 */
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

/**
 * JNI helper: clear pending Java exception and report whether one existed.
 *
 * Native resolver intentionally behaves as best-effort: it clears reflective
 * access exceptions and returns sentinel values so Java can fallback.
 */
static bool clear_exception(JNIEnv *env) {
    if (env != nullptr && env->ExceptionCheck()) {
        env->ExceptionClear();
        return true;
    }
    return false;
}

/**
 * Walks `java.awt.Window -> peer` and returns the AWT peer object.
 *
 * Returns null when graph access fails or is denied.
 */
static jobject resolve_component_peer(JNIEnv *env, jobject window) {
    if (env == nullptr || window == nullptr) {
        return nullptr;
    }
    jclass windowClass = env->GetObjectClass(window);
    if (windowClass == nullptr || clear_exception(env)) {
        return nullptr;
    }
    jfieldID peerField = env->GetFieldID(windowClass, "peer", "Ljava/awt/peer/ComponentPeer;");
    if (peerField == nullptr || clear_exception(env)) {
        return nullptr;
    }
    jobject peer = env->GetObjectField(window, peerField);
    if (clear_exception(env)) {
        return nullptr;
    }
    return peer;
}

/**
 * Resolves native `NSWindow*` pointer from AWT peer internals.
 *
 * Java path: `Window.peer -> platformWindow -> ptr`.
 * Returns 0 on failure.
 */
static jlong resolve_platform_window_ptr(JNIEnv *env, jobject window) {
    jobject peer = resolve_component_peer(env, window);
    if (peer == nullptr) {
        return 0;
    }

    jclass peerClass = env->GetObjectClass(peer);
    if (peerClass == nullptr || clear_exception(env)) {
        return 0;
    }
    jfieldID platformWindowField = env->GetFieldID(peerClass, "platformWindow", "Lsun/lwawt/PlatformWindow;");
    if (platformWindowField == nullptr || clear_exception(env)) {
        return 0;
    }
    jobject platformWindow = env->GetObjectField(peer, platformWindowField);
    if (platformWindow == nullptr || clear_exception(env)) {
        return 0;
    }

    jclass platformWindowClass = env->GetObjectClass(platformWindow);
    if (platformWindowClass == nullptr || clear_exception(env)) {
        return 0;
    }
    jfieldID ptrField = env->GetFieldID(platformWindowClass, "ptr", "J");
    if (ptrField == nullptr || clear_exception(env)) {
        return 0;
    }
    jlong ptr = env->GetLongField(platformWindow, ptrField);
    if (clear_exception(env)) {
        return 0;
    }
    return ptr;
}

/// JNI entry point: resolve `NSWindow*` pointer from AWT peer graph.
extern "C" JNIEXPORT jlong JNICALL
Java_io_github_bric3_diaphanous_platform_macos_MacosNativeWindowHandleBridge_resolveNSWindowPointer0(
    JNIEnv *env,
    jclass,
    jobject window
) {
    return resolve_platform_window_ptr(env, window);
}

/// JNI entry point: best-effort `ComponentPeer.setOpaque(boolean)` bridge.
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_bric3_diaphanous_platform_macos_MacosNativeWindowHandleBridge_setPeerOpaque0(
    JNIEnv *env,
    jclass,
    jobject window,
    jboolean opaque
) {
    jobject peer = resolve_component_peer(env, window);
    if (peer == nullptr) {
        return JNI_FALSE;
    }
    jclass peerClass = env->GetObjectClass(peer);
    if (peerClass == nullptr || clear_exception(env)) {
        return JNI_FALSE;
    }
    jmethodID setOpaque = env->GetMethodID(peerClass, "setOpaque", "(Z)V");
    if (setOpaque == nullptr || clear_exception(env)) {
        return JNI_FALSE;
    }
    env->CallVoidMethod(peer, setOpaque, opaque);
    if (clear_exception(env)) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
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

/**
 * Ensures the window content view is the wrapper type.
 *
 * When content view is not already wrapped, detaches current content view,
 * creates wrapper, reparents previous content as child, and installs wrapper
 * as `window.contentView`.
 */
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

/**
 * Configures transparency for window, wrapper layer, and AWT host layer.
 *
 * This removes obvious opaque fills. It does not stop Java rendering from
 * blitting a full frame in the host surface; that concern is handled on the
 * Java side by the backdrop eraser strategy.
 */
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

/**
 * Installs/reuses wrapper + backdrop for the target window.
 *
 * Threading: may be called from any Java thread; execution is synchronized to
 * AppKit main thread internally.
 *
 * @return 0 on success, -1 on failure.
 */
extern "C" int diaphanous_install_vibrant_wrapper(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
) {
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
        [wrapper installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                          blending: (NSVisualEffectBlendingMode) blending_mode
                                              state: (NSVisualEffectState) state
                                         emphasized: emphasized != 0
                                              alpha: (CGFloat) alpha];
        return 0;
    });
}

/**
 * Updates effect parameters when wrapper/effect already exists.
 *
 * If wrapper/effect is not present yet, this function delegates to
 * `diaphanous_install_vibrant_wrapper`.
 *
 * Threading: may be called from any Java thread; execution is synchronized to
 * AppKit main thread internally.
 *
 * @return 0 on success, -1 on failure.
 */
extern "C" int diaphanous_update_vibrant_material(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
) {
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
            [wrapper installOrUpdateEffectWithMaterial: (NSVisualEffectMaterial) material
                                              blending: (NSVisualEffectBlendingMode) blending_mode
                                                  state: (NSVisualEffectState) state
                                             emphasized: emphasized != 0
                                                  alpha: (CGFloat) alpha];
            return 0;
        }
        return diaphanous_install_vibrant_wrapper(
            ns_window_ptr,
            material,
            alpha,
            blending_mode,
            state,
            emphasized
        );
    });
}

/**
 * Removes only the `NSVisualEffectView` from wrapper content.
 *
 * Wrapper and AWT host reparenting are preserved to keep event and hierarchy
 * stable for possible re-enable operations.
 *
 * @return 0 on success, -1 when wrapper/window is unavailable.
 */
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

/**
 * Applies style-related AppKit properties to an existing NSWindow.
 *
 * This mutates:
 * - `NSWindowStyleMaskFullSizeContentView`
 * - `titlebarAppearsTransparent`
 * - `titleVisibility`
 * - `toolbarStyle` (when requested/supported)
 *
 * @return 0 on success, -1 on failure.
 */
extern "C" int diaphanous_apply_window_style(
    void* ns_window_ptr,
    int transparent_title_bar,
    int full_size_content_view,
    int title_visible,
    long toolbar_style,
    int has_toolbar_style
) {
    return run_on_main_sync(^int {
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
        if (has_toolbar_style != 0 && [window respondsToSelector: @selector(setToolbarStyle:)]) {
            window.toolbarStyle = (NSWindowToolbarStyle) toolbar_style;
        }
        return 0;
    });
}

/**
 * Applies named NSAppearance or resets to system appearance.
 *
 * Pass `appearance_name_utf8 == nullptr` to clear explicit appearance.
 *
 * @return 0 on success, -1 on invalid pointer/invalid UTF-8 name conversion.
 */
extern "C" int diaphanous_apply_window_appearance(void* ns_window_ptr, const char* appearance_name_utf8) {
    return run_on_main_sync(^int {
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

/**
 * Sets `NSWindow.alphaValue` with clamp to `[0,1]`.
 *
 * @return 0 on success, -1 on failure.
 */
extern "C" int diaphanous_set_window_alpha(void* ns_window_ptr, double alpha) {
    return run_on_main_sync(^int {
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

/**
 * Logs a structured native dump for diagnostics.
 *
 * Output includes window properties and recursive view/layer tree state.
 *
 * @return 0 when dump is produced, -1 on invalid window pointer.
 */
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

/**
 * Reads default alpha from a newly created `NSVisualEffectView`.
 *
 * @return alpha default, or -1.0 when unavailable.
 */
extern "C" double diaphanous_default_effect_alpha(void) {
    __block double value = -1.0;
    run_on_main_sync(^int {
        NSVisualEffectView *view = [[NSVisualEffectView alloc] initWithFrame: NSZeroRect];
        value = view != nil ? (double) view.alphaValue : -1.0;
        return 0;
    });
    return value;
}

/**
 * Reads default material from a newly created `NSVisualEffectView`.
 *
 * @return material raw value, or -1 when unavailable.
 */
extern "C" int diaphanous_default_effect_material(void) {
    __block int value = -1;
    run_on_main_sync(^int {
        NSVisualEffectView *view = [[NSVisualEffectView alloc] initWithFrame: NSZeroRect];
        value = view != nil ? (int) view.material : -1;
        return 0;
    });
    return value;
}

/**
 * Reads current alpha from installed effect view in wrapped window content.
 *
 * @return effect alpha, or -1.0 when wrapper/effect is unavailable.
 */
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

/**
 * Reads current material from installed effect view in wrapped window content.
 *
 * @return material raw value, or -1 when wrapper/effect is unavailable.
 */
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
