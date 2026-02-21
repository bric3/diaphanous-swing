/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.decorations;

/**
 * Immutable style configuration applied to a macOS {@code NSWindow}.
 *
 * @param transparentTitleBar whether the title bar background is transparent
 * @param fullSizeContentView whether content extends into the title bar area
 * @param titleVisible whether the window title text is visible
 */
public record MacosWindowDecorationsSpec(
    boolean transparentTitleBar,
    boolean fullSizeContentView,
    boolean titleVisible
) implements WindowDecorationSpec {
    /**
     * Creates a builder with defaults that match a modern translucent macOS window style.
     *
     * @return style builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link MacosWindowDecorationsSpec}.
     */
    public static final class Builder {
        private boolean transparentTitleBar = true;
        private boolean fullSizeContentView = true;
        private boolean titleVisible;

        /**
         * @param enabled whether the title bar background should be transparent
         * @return this builder
         */
        public Builder transparentTitleBar(boolean enabled) {
            this.transparentTitleBar = enabled;
            return this;
        }

        /**
         * @param enabled whether content should extend into the title bar
         * @return this builder
         */
        public Builder fullSizeContentView(boolean enabled) {
            this.fullSizeContentView = enabled;
            return this;
        }

        /**
         * @param visible whether the title text should be visible
         * @return this builder
         */
        public Builder titleVisible(boolean visible) {
            this.titleVisible = visible;
            return this;
        }

        /**
         * @return immutable style configuration
         */
        public MacosWindowDecorationsSpec build() {
            return new MacosWindowDecorationsSpec(transparentTitleBar, fullSizeContentView, titleVisible);
        }
    }
}
