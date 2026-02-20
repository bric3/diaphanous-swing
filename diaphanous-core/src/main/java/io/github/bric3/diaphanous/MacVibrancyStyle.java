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
         * @return this builder
         */
        public Builder blendingMode(MacVibrancyBlendingMode blendingMode) {
            this.blendingMode = Objects.requireNonNull(blendingMode, "blendingMode");
            return this;
        }

        /**
         * @param state vibrancy state behavior
         * @return this builder
         */
        public Builder state(MacVibrancyState state) {
            this.state = Objects.requireNonNull(state, "state");
            return this;
        }

        /**
         * @param emphasized emphasis hint for materials that support it
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
