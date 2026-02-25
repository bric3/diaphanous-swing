/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.decorations;

/**
 * macOS appearance presets applicable to {@code NSWindow}.
 * <p>
 * References:
 * <a href="https://developer.apple.com/documentation/appkit/nsappearance">NSAppearance</a>,
 * <a href="https://developer.apple.com/documentation/appkit/nsappearancecustomization/effectiveappearance">effectiveAppearance</a>.
 */
public enum MacosWindowAppearanceSpec implements WindowAppearanceSpec {
    /**
     * Uses the system-resolved appearance by clearing any explicit window appearance.
     * <p>
     * Native mapping: {@code NSWindow.appearance = nil}.
     * <p>
     * Also, Apple documentation (in {@code NSAppearance.h}) states:
     * "This is not the correct way to determine the 'system' appearance. Use a view's,
     * window's, or the app's effectiveAppearance."
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsappearancecustomization/effectiveappearance">effectiveAppearance</a>.
     */
    SYSTEM(null),

    /**
     * Standard Aqua appearance ({@code NSAppearanceNameAqua}).
     * <p>
     * Apple documentation (in {@code NSAppearance.h}) states:
     * "For standard appearances such as NSAppearanceNameAqua, a built-in appearance is returned."
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsappearance/name-swift.struct/aqua">NSAppearance.Name.aqua</a>.
     */
    AQUA("NSAppearanceNameAqua"),

    /**
     * Standard Dark Aqua appearance ({@code NSAppearanceNameDarkAqua}).
     * <p>
     * Available since macOS 10.14 in AppKit's {@code NSAppearance.h}.
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsappearance/name-swift.struct/darkaqua">NSAppearance.Name.darkAqua</a>.
     */
    DARK_AQUA("NSAppearanceNameDarkAqua"),

    /**
     * Vibrant Light appearance ({@code NSAppearanceNameVibrantLight}).
     * <p>
     * Apple documentation (in {@code NSAppearance.h}) states:
     * "The following two Vibrant appearances should only be set on an NSVisualEffectView,
     * or one of its container subviews."
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsappearance/name-swift.struct/vibrantlight">NSAppearance.Name.vibrantLight</a>.
     */
    VIBRANT_LIGHT("NSAppearanceNameVibrantLight"),

    /**
     * Vibrant Dark appearance ({@code NSAppearanceNameVibrantDark}).
     * <p>
     * Apple documentation (in {@code NSAppearance.h}) states:
     * "The following two Vibrant appearances should only be set on an NSVisualEffectView,
     * or one of its container subviews."
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsappearance/name-swift.struct/vibrantdark">NSAppearance.Name.vibrantDark</a>.
     */
    VIBRANT_DARK("NSAppearanceNameVibrantDark");

    private final String nativeName;

    MacosWindowAppearanceSpec(String nativeName) {
        this.nativeName = nativeName;
    }

    /**
     * Returns the native AppKit appearance name constant value.
     * <p>
     * Example values: {@code NSAppearanceNameAqua}, {@code NSAppearanceNameDarkAqua},
     * {@code NSAppearanceNameVibrantLight}, {@code NSAppearanceNameVibrantDark}.
     *
     * @return native AppKit appearance name, or {@code null} for {@link #SYSTEM}
     */
    public String nativeName() {
        return nativeName;
    }
}
