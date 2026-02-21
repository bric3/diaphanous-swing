/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
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
     */
    public enum MacosBackdropMaterial implements WindowBackdropMaterialSpec {
        APPEARANCE_BASED(0),
        LIGHT(1),
        DARK(2),
        TITLEBAR(3),
        SELECTION(4),
        MENU(5),
        POPOVER(6),
        SIDEBAR(7),
        HEADER_VIEW(10),
        SHEET(11),
        WINDOW_BACKGROUND(12),
        HUD_WINDOW(13),
        FULL_SCREEN_UI(15),
        TOOLTIP(17),
        CONTENT_BACKGROUND(18),
        UNDER_WINDOW_BACKGROUND(21),
        UNDER_PAGE_BACKGROUND(22);

        private final long nativeValue;

        MacosBackdropMaterial(long nativeValue) {
            this.nativeValue = nativeValue;
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
     */
    public enum MacosBackdropEffectState {
        FOLLOWS_WINDOW_ACTIVE_STATE(0),
        ACTIVE(1),
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
     */
    public enum MacosBackdropEffectBlendingMode {
        BEHIND_WINDOW(0),
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
