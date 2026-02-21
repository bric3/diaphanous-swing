/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous;

import java.util.Objects;

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
 * MacVibrancyStyle defaultBackdrop = MacVibrancyStyle.builder()
 *     .build();
 *
 * MacVibrancyStyle strongerDarkBackdrop = MacVibrancyStyle.builder()
 *     .material(MacVibrancyMaterial.HUD_WINDOW)
 *     .state(MacVibrancyState.ACTIVE)
 *     .backdropAlpha(0.85)
 *     .build();
 *
 * MacVibrancyStyle withinWindowSurface = MacVibrancyStyle.builder()
 *     .material(MacVibrancyMaterial.CONTENT_BACKGROUND)
 *     .blendingMode(MacVibrancyBlendingMode.WITHIN_WINDOW)
 *     .state(MacVibrancyState.ACTIVE)
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
public record MacVibrancyStyle(
    boolean enabled,
    MacVibrancyMaterial material,
    MacVibrancyBlendingMode blendingMode,
    MacVibrancyState state,
    boolean emphasized,
    double backdropAlpha
) {
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
     * Builder for {@link MacVibrancyStyle}.
     */
    public static final class Builder {
        private boolean enabled = true;
        private MacVibrancyMaterial material = MacVibrancyMaterial.UNDER_WINDOW_BACKGROUND;
        private MacVibrancyBlendingMode blendingMode = MacVibrancyBlendingMode.BEHIND_WINDOW;
        private MacVibrancyState state = MacVibrancyState.FOLLOWS_WINDOW_ACTIVE_STATE;
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
        public Builder material(MacVibrancyMaterial material) {
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
        public Builder blendingMode(MacVibrancyBlendingMode blendingMode) {
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
        public Builder state(MacVibrancyState state) {
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
        public MacVibrancyStyle build() {
            if (backdropAlpha < 0.0d || backdropAlpha > 1.0d) {
                throw new IllegalArgumentException("backdropAlpha must be within [0.0, 1.0]");
            }
            return new MacVibrancyStyle(enabled, material, blendingMode, state, emphasized, backdropAlpha);
        }
    }
}
