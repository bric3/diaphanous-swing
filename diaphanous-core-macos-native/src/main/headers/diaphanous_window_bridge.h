/*
 * Diaphanous Swing
 *
 * Copyright (c) ${year} - ${name}
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#ifndef DIAPHANOUS_WINDOW_BRIDGE_H
#define DIAPHANOUS_WINDOW_BRIDGE_H

#ifdef __cplusplus
extern "C" {
#endif

int diaphanous_install_vibrant_wrapper(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
);
int diaphanous_update_vibrant_material(
    void* ns_window_ptr,
    int material,
    double alpha,
    int blending_mode,
    int state,
    int emphasized
);
int diaphanous_remove_vibrant_wrapper(void* ns_window_ptr);
int diaphanous_dump_window_state(void* ns_window_ptr);
double diaphanous_default_effect_alpha(void);
int diaphanous_default_effect_material(void);
double diaphanous_read_effect_alpha(void* ns_window_ptr);
int diaphanous_read_effect_material(void* ns_window_ptr);

#ifdef __cplusplus
}
#endif

#endif
