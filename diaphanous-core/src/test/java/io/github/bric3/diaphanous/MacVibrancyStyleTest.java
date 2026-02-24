/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous;

import io.github.bric3.diaphanous.backdrop.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacosBackdropEffectSpecTest {
    @Test
    void builderCreatesExpectedDefaults() {
        MacosBackdropEffectSpec style = MacosBackdropEffectSpec.builder().build();

        assertTrue(style.enabled());
        assertEquals(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND, style.material());
        assertEquals(MacosBackdropEffectSpec.MacosBackdropEffectBlendingMode.BEHIND_WINDOW, style.blendingMode());
        assertEquals(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE, style.state());
        assertFalse(style.emphasized());
        assertEquals(1.0d, style.backdropAlpha());
    }
}
