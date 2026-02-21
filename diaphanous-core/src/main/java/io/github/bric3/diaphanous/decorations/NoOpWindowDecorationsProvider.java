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

import java.awt.Window;

final class NoOpWindowDecorationsProvider implements WindowDecorationsProvider {
    @Override
    public void applyStyle(Window window, WindowDecorationSpec spec) {
        throw unsupported("style", spec);
    }

    @Override
    public void applyAppearance(Window window, WindowAppearanceSpec spec) {
        throw unsupported("appearance", spec);
    }

    private IllegalArgumentException unsupported(String kind, Object spec) {
        return new IllegalArgumentException(
            "No window decorations provider for this OS. Unsupported " + kind + " spec: " + spec.getClass().getName()
        );
    }
}
