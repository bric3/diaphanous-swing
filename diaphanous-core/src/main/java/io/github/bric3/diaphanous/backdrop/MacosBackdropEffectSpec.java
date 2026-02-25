/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.backdrop;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable configuration for macOS {@code NSVisualEffectView} vibrancy.
 * <p>
 * Common combinations:
 * <ul>
 *   <li>Full-window backdrop: {@code BEHIND_WINDOW + FOLLOWS_WINDOW_ACTIVE_STATE}</li>
 *   <li>In-window grouped surfaces: {@code WITHIN_WINDOW + ACTIVE}</li>
 *   <li>Sidebar-like emphasis: {@code material=SIDEBAR} with {@code emphasized=true}</li>
 * </ul>
 *
 * <p>Example presets:
 * <pre>{@code
 * MacosBackdropEffectSpec defaultBackdrop = MacosBackdropEffectSpec.builder()
 *     .build();
 *
 * MacosBackdropEffectSpec strongerDarkBackdrop = MacosBackdropEffectSpec.builder()
 *     .material(MacosBackdropMaterial.HUD_WINDOW)
 *     .state(MacosBackdropEffectState.ACTIVE)
 *     .backdropAlpha(0.85)
 *     .build();
 *
 * MacosBackdropEffectSpec withinWindowSurface = MacosBackdropEffectSpec.builder()
 *     .material(MacosBackdropMaterial.CONTENT_BACKGROUND)
 *     .blendingMode(MacosBackdropEffectBlendingMode.WITHIN_WINDOW)
 *     .state(MacosBackdropEffectState.ACTIVE)
 *     .build();
 * }</pre>
 *
 * @param enabled whether vibrancy should be present on the window
 * @param material vibrancy material
 * @param blendingMode vibrancy blending mode
 * @param state vibrancy active state behavior
 * @param emphasized emphasis flag used by some materials
 * @param backdropAlpha alpha of the native backdrop layer, from {@code 0.0} (fully transparent) to {@code 1.0}
 */
public record MacosBackdropEffectSpec(
    boolean enabled,
    MacosBackdropMaterial material,
    MacosBackdropEffectBlendingMode blendingMode,
    MacosBackdropEffectState state,
    boolean emphasized,
    double backdropAlpha
) implements WindowBackgroundEffectSpec {
    /**
     * Values mapped to macOS {@code NSVisualEffectMaterial}.
     * <p>
     * Apple discussion: materials are dynamic; their exact rendering depends on effective appearance,
     * blending mode, state, emphasis, and other factors.
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterial">NSVisualEffectMaterial</a>,
     * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectview/material">NSVisualEffectView.material</a>.
     */
    public enum MacosBackdropMaterial implements WindowBackdropMaterialSpec {
        /**
         * Native mapping: {@code NSVisualEffectMaterialAppearanceBased}.
         * <p>
         * Apple describes this as a default material based on effective appearance.
         * Apple also deprecated it in favor of semantic materials.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialappearancebased">NSVisualEffectMaterialAppearanceBased</a>.
         *
         * @deprecated Apple deprecated {@code NSVisualEffectMaterialAppearanceBased} in macOS 10.14.
         */
        @Deprecated
        APPEARANCE_BASED(0, false),

        /**
         * Native mapping: {@code NSVisualEffectMaterialLight}.
         * <p>
         * Legacy light look.
         * Deprecated by Apple in favor of semantic materials.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmateriallight">NSVisualEffectMaterialLight</a>.
         *
         * @deprecated Apple deprecated {@code NSVisualEffectMaterialLight} in macOS 10.14.
         */
        @Deprecated
        LIGHT(1, false),

        /**
         * Native mapping: {@code NSVisualEffectMaterialDark}.
         * <p>
         * Legacy dark look.
         * Deprecated by Apple in favor of semantic materials.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialdark">NSVisualEffectMaterialDark</a>.
         *
         * @deprecated Apple deprecated {@code NSVisualEffectMaterialDark} in macOS 10.14.
         */
        @Deprecated
        DARK(2, false),

        /**
         * Native mapping: {@code NSVisualEffectMaterialTitlebar}.
         * <p>
         * Material intended for window title bars.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialtitlebar">NSVisualEffectMaterialTitlebar</a>.
         */
        TITLEBAR(3, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialSelection}.
         * <p>
         * Material used to indicate selection in some table/menu contexts.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialselection">NSVisualEffectMaterialSelection</a>.
         */
        SELECTION(4, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialMenu}.
         * <p>
         * Material used by menus.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialmenu">NSVisualEffectMaterialMenu</a>.
         */
        MENU(5, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialPopover}.
         * <p>
         * Material used for {@code NSPopover} window backgrounds.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialpopover">NSVisualEffectMaterialPopover</a>.
         */
        POPOVER(6, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialSidebar}.
         * <p>
         * Material intended for window sidebar backgrounds.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialsidebar">NSVisualEffectMaterialSidebar</a>.
         */
        SIDEBAR(7, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialHeaderView}.
         * <p>
         * Material used in inline header/footer regions (for example table headers).
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialheaderview">NSVisualEffectMaterialHeaderView</a>.
         */
        HEADER_VIEW(10, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialSheet}.
         * <p>
         * Material used for sheet window backgrounds.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialsheet">NSVisualEffectMaterialSheet</a>.
         */
        SHEET(11, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialWindowBackground}.
         * <p>
         * Material used by opaque window backgrounds.
         * Apple discussion: this material supports Desktop Tinting in Dark Mode,
         * where the system dynamically incorporates color from the desktop image.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialwindowbackground">NSVisualEffectMaterialWindowBackground</a>.
         */
        WINDOW_BACKGROUND(12, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialHUDWindow}.
         * <p>
         * Material used for heads-up-display style windows.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialhudwindow">NSVisualEffectMaterialHUDWindow</a>.
         */
        HUD_WINDOW(13, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialFullScreenUI}.
         * <p>
         * Material used for full-screen modal UI backgrounds.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialfullscreenui">NSVisualEffectMaterialFullScreenUI</a>.
         */
        FULL_SCREEN_UI(15, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialToolTip}.
         * <p>
         * Material used for tooltip backgrounds.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialtooltip">NSVisualEffectMaterialToolTip</a>.
         */
        TOOLTIP(17, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialContentBackground}.
         * <p>
         * Material used as an opaque background for content regions, such as
         * {@code NSScrollView}, {@code NSTableView}, and {@code NSCollectionView}.
         * Apple discussion: this material supports Desktop Tinting in Dark Mode,
         * where the system dynamically incorporates color from the desktop image.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialcontentbackground">NSVisualEffectMaterialContentBackground</a>.
         */
        CONTENT_BACKGROUND(18, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialUnderWindowBackground}.
         * <p>
         * Material used under window background surfaces.
         * Apple discussion: use this material with
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectview/blendingmode">NSVisualEffectView.blendingMode</a>
         * set to
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectblendingmode/nsvisualeffectblendingmodebehindwindow">NSVisualEffectBlendingModeBehindWindow</a>
         * to create a sense of peeking through the back of the window.
         * This effect can create an illusion that the window background peeled away
         * to reveal what is underneath.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialunderwindowbackground">NSVisualEffectMaterialUnderWindowBackground</a>.
         */
        UNDER_WINDOW_BACKGROUND(21, true),

        /**
         * Native mapping: {@code NSVisualEffectMaterialUnderPageBackground}.
         * <p>
         * Material used behind document pages.
         * Apple discussion: this material supports Desktop Tinting in Dark Mode,
         * where the system dynamically incorporates color from the desktop image.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectmaterialunderpagebackground">NSVisualEffectMaterialUnderPageBackground</a>.
         */
        UNDER_PAGE_BACKGROUND(22, true);

        private final long nativeValue;

        /**
         * Indicates whether this material is a semantic AppKit material.
         *
         * {@code true} for semantic materials, {@code false} for deprecated/non-semantic ones.
         */
        public final boolean semantic;

        MacosBackdropMaterial(long nativeValue, boolean semantic) {
            this.nativeValue = nativeValue;
            this.semantic = semantic;
        }

        /**
         * Returns the mapped enum for a native {@code NSVisualEffectMaterial} value.
         *
         * @param nativeValue native material value
         * @return matching material when known
         */
        public static Optional<MacosBackdropMaterial> fromNativeValue(long nativeValue) {
            for (MacosBackdropMaterial material : values()) {
                if (material.nativeValue == nativeValue) {
                    return Optional.of(material);
                }
            }
            return Optional.empty();
        }

        public long nativeValue() {
            return nativeValue;
        }
    }

    /**
     * Values mapped 1:1 to AppKit {@code NSVisualEffectState} used by {@code NSVisualEffectView.state}.
     * <p>
     * Apple discussion: this controls when the effect takes on the active look; the default behavior
     * follows window activation.
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectstate">NSVisualEffectState</a>,
     * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectview/state">NSVisualEffectView.state</a>.
     */
    public enum MacosBackdropEffectState {
        /**
         * Native mapping: {@code NSVisualEffectStateFollowsWindowActiveState}.
         * <p>
         * The backdrop should automatically appear active when the window is active,
         * and inactive when it is not.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectstatefollowswindowactivestate">NSVisualEffectStateFollowsWindowActiveState</a>.
         */
        FOLLOWS_WINDOW_ACTIVE_STATE(0),
        /**
         * Native mapping: {@code NSVisualEffectStateActive}.
         * <p>
         * Forces active styling regardless of window focus.
         * Apple discussion: pins the effect to active appearance at all times.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectstateactive">NSVisualEffectStateActive</a>.
         */
        ACTIVE(1),
        /**
         * Native mapping: {@code NSVisualEffectStateInactive}.
         * <p>
         * Forces inactive styling regardless of window focus.
         * Apple discussion: pins the effect to inactive appearance at all times.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectstateinactive">NSVisualEffectStateInactive</a>.
         */
        INACTIVE(2);

        private final long nativeValue;

        MacosBackdropEffectState(long nativeValue) {
            this.nativeValue = nativeValue;
        }

        public long nativeValue() {
            return nativeValue;
        }
    }

    /**
     * Values mapped 1:1 to AppKit {@code NSVisualEffectBlendingMode} used by
     * {@code NSVisualEffectView.blendingMode}.
     * <p>
     * Apple discussion: not all materials support both blending modes, and AppKit may fall back
     * to a more appropriate mode.
     * <p>
     * See:
     * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectblendingmode">NSVisualEffectBlendingMode</a>,
     * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectview/blendingmode">NSVisualEffectView.blendingMode</a>.
     */
    public enum MacosBackdropEffectBlendingMode {
        /**
         * Native mapping: {@code NSVisualEffectBlendingModeBehindWindow}.
         * <p>
         * Blends against content behind the window (desktop/other windows).
         * Apple discussion: intended for whole-window backdrop style effects.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectblendingmodebehindwindow">NSVisualEffectBlendingModeBehindWindow</a>.
         */
        BEHIND_WINDOW(0),
        /**
         * Native mapping: {@code NSVisualEffectBlendingModeWithinWindow}.
         * <p>
         * Blends against content behind the view but within the same window.
         * Apple discussion: intended for localized in-window vibrancy composition.
         * <p>
         * See:
         * <a href="https://developer.apple.com/documentation/appkit/nsvisualeffectblendingmodewithinwindow">NSVisualEffectBlendingModeWithinWindow</a>.
         */
        WITHIN_WINDOW(1);

        private final long nativeValue;

        MacosBackdropEffectBlendingMode(long nativeValue) {
            this.nativeValue = nativeValue;
        }

        public long nativeValue() {
            return nativeValue;
        }
    }

    /**
     * @return a builder with practical defaults for a backdrop effect behind window content
     *
     * <p>Defaults:
     * <ul>
     *   <li>{@code material=UNDER_WINDOW_BACKGROUND}</li>
     *   <li>{@code blendingMode=BEHIND_WINDOW}</li>
     *   <li>{@code state=FOLLOWS_WINDOW_ACTIVE_STATE}</li>
     *   <li>{@code emphasized=false}</li>
     *   <li>{@code backdropAlpha=1.0}</li>
     * </ul>
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link MacosBackdropEffectSpec}.
     */
    public static final class Builder {
        private boolean enabled = true;
        private MacosBackdropMaterial material = MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND;
        private MacosBackdropEffectBlendingMode blendingMode = MacosBackdropEffectBlendingMode.BEHIND_WINDOW;
        private MacosBackdropEffectState state = MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE;
        private boolean emphasized;
        private double backdropAlpha = 1.0d;

        /**
         * @param enabled whether vibrancy should be installed
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * @param material vibrancy material to apply
         * @return this builder
         */
        public Builder material(MacosBackdropMaterial material) {
            this.material = Objects.requireNonNull(material, "material");
            return this;
        }

        /**
         * @param blendingMode vibrancy blending mode
         *
         * <p>Use {@code BEHIND_WINDOW} for full-window backdrops. Use {@code WITHIN_WINDOW}
         * for effects intended to blend inside window content regions.
         * @return this builder
         */
        public Builder blendingMode(MacosBackdropEffectBlendingMode blendingMode) {
            this.blendingMode = Objects.requireNonNull(blendingMode, "blendingMode");
            return this;
        }

        /**
         * @param state vibrancy state behavior
         *
         * <p>Typical pairing is:
         * <ul>
         *   <li>{@code FOLLOWS_WINDOW_ACTIVE_STATE} with standard window backdrops</li>
         *   <li>{@code ACTIVE} when the effect should remain visually active regardless of focus</li>
         * </ul>
         * @return this builder
         */
        public Builder state(MacosBackdropEffectState state) {
            this.state = Objects.requireNonNull(state, "state");
            return this;
        }

        /**
         * @param emphasized emphasis hint for materials that support it
         *
         * <p>This is most meaningful with semantic materials such as {@code SIDEBAR} and
         * {@code TITLEBAR}. It may have no visible effect for other materials.
         * @return this builder
         */
        public Builder emphasized(boolean emphasized) {
            this.emphasized = emphasized;
            return this;
        }

        /**
         * @param alpha alpha value in {@code [0.0, 1.0]}
         * @return this builder
         */
        public Builder backdropAlpha(double alpha) {
            this.backdropAlpha = alpha;
            return this;
        }

        /**
         * @return immutable vibrancy configuration
         */
        public MacosBackdropEffectSpec build() {
            if (backdropAlpha < 0.0d || backdropAlpha > 1.0d) {
                throw new IllegalArgumentException("backdropAlpha must be within [0.0, 1.0]");
            }
            return new MacosBackdropEffectSpec(enabled, material, blendingMode, state, emphasized, backdropAlpha);
        }
    }
}
