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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacWindowStyleTest {
    @Test
    void builderCreatesExpectedDefaults() {
        MacWindowStyle style = MacWindowStyle.builder().build();

        assertTrue(style.transparentTitleBar());
        assertTrue(style.fullSizeContentView());
        assertFalse(style.titleVisible());
        assertEquals(MacToolbarStyle.UNIFIED, style.toolbarStyle());
    }
}
