/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.robot.macos;

import io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec;
import io.github.bric3.diaphanous.backdrop.RootErasingContentPane;
import io.github.bric3.diaphanous.backdrop.WindowBackdrop;
import io.github.bric3.diaphanous.decorations.MacosWindowAppearanceSpec;
import io.github.bric3.diaphanous.decorations.MacosWindowDecorationsSpec;
import io.github.bric3.diaphanous.decorations.WindowPresentations;
import io.github.bric3.diaphanous.robot.docs.ExamplesDocModels;
import io.github.bric3.diaphanous.robot.RobotScreenshotSupport;
import io.github.bric3.diaphanous.robot.ScreenshotBaseline;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE;
import static io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropEffectState.INACTIVE;
import static io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropMaterial.SIDEBAR;
import static io.github.bric3.diaphanous.backdrop.MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BackdropEffectRenderingRobotTest {
    private static final String API_TYPE = "backdrop/macos";
    private static JFrame sharedProbe;

    @AfterAll
    static void disposeSharedProbe() {
        if (sharedProbe != null) {
            JFrame probe = sharedProbe;
            sharedProbe = null;
            RobotScreenshotSupport.onEdtAndWait(probe::dispose);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void capturesBackdropWindowRendering(TestCase testCase) throws Exception {
        RobotScreenshotSupport.assumeDesktopMacOs();
        assumeTrue(WindowBackdrop.isSupported());
        assumeTrue(WindowPresentations.isSupported());

        ScreenshotBaseline baseline = new ScreenshotBaseline(
            RobotScreenshotSupport.outputDir(),
            RobotScreenshotSupport.baselineDir()
        );
        Robot robot = new Robot();
        robot.setAutoDelay(110);

        JFrame frame = RobotScreenshotSupport.createMinimalWindow("Backdrop Robot");

        try {
            RobotScreenshotSupport.onEdtAndWait(() -> {
                frame.setContentPane(new RootErasingContentPane(new BorderLayout()));
                frame.setVisible(true);
            });
            JFrame shown = RobotScreenshotSupport.waitForFrame("Backdrop Robot", Duration.ofSeconds(5));
            assertNotNull(shown, "Window was not shown");
            Rectangle captureBounds = RobotScreenshotSupport.captureBounds(shown);
            JFrame visibleProbe = acquireSharedProbe(captureBounds);
            RobotScreenshotSupport.onEdtAndWait(() -> {
                visibleProbe.setVisible(true);
                resetFramePresentation(frame, testCase.appearance.spec);
                testCase.shot.apply.accept(frame);
                frame.toFront();
            });
            RobotScreenshotSupport.sleep(800);
            BufferedImage screenshot = RobotScreenshotSupport.captureWindow(robot, captureBounds);
            baseline.assertMatches(API_TYPE, testCase.shot.name + "-" + testCase.appearance.key, screenshot);
        } finally {
            RobotScreenshotSupport.onEdtAndWait(() -> {
                frame.dispose();
            });
        }
    }

    private static JFrame acquireSharedProbe(Rectangle bounds) {
        if (sharedProbe == null) {
            sharedProbe = RobotScreenshotSupport.createBackdropProbe(bounds);
        } else {
            JFrame probe = sharedProbe;
            RobotScreenshotSupport.onEdtAndWait(() -> probe.setBounds(bounds));
        }
        return sharedProbe;
    }

    private static List<TestCase> cases() {
        List<TestCase> cases = new ArrayList<>();
        for (AppearanceVariant appearance : AppearanceVariant.values()) {
            for (Shot shot : shots()) {
                cases.add(new TestCase(shot, appearance));
            }
        }
        return cases;
    }

    private static List<Shot> shots() {
        List<Shot> shots = new ArrayList<>();
        shots.add(new Shot("001-enabled-false", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .enabled(false)
                .build());
            """, frame ->
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder().enabled(false).build())));

        int materialIndex = 10;
        for (MacosBackdropEffectSpec.MacosBackdropMaterial material : MacosBackdropEffectSpec.MacosBackdropMaterial.values()) {
            String materialName = material.name().toLowerCase(Locale.ROOT).replace('_', '-');
            shots.add(new Shot(
                String.format(Locale.ROOT, "%03d-material-%s", materialIndex++, materialName),
                """
                WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                    .material(MacosBackdropEffectSpec.MacosBackdropMaterial.%s)
                    .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                    .emphasized(false)
                    .backdropAlpha(1.0d)
                    .build());
                """.formatted(material.name()),
                frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                    .material(material)
                    .state(ACTIVE)
                    .emphasized(false)
                    .backdropAlpha(1.0d)
                    .build())
            ));
        }

        shots.add(new Shot("100-state-follows-active", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(MacosBackdropEffectSpec.MacosBackdropEffectState.FOLLOWS_WINDOW_ACTIVE_STATE)
            .build())));
        shots.add(new Shot("101-state-active", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(ACTIVE)
            .build())));
        shots.add(new Shot("102-state-inactive", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.INACTIVE)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(INACTIVE)
            .build())));

        shots.add(new Shot("110-sidebar-emphasis-false", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SIDEBAR)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .emphasized(false)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(SIDEBAR)
            .state(ACTIVE)
            .emphasized(false)
            .build())));
        shots.add(new Shot("111-sidebar-emphasis-true", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.SIDEBAR)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .emphasized(true)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(SIDEBAR)
            .state(ACTIVE)
            .emphasized(true)
            .build())));

        shots.add(new Shot("120-alpha-100", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .backdropAlpha(1.0d)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(ACTIVE)
            .backdropAlpha(1.0d)
            .build())));
        shots.add(new Shot("121-alpha-090", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .backdropAlpha(0.90d)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(ACTIVE)
            .backdropAlpha(0.90d)
            .build())));
        shots.add(new Shot("122-alpha-070", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .backdropAlpha(0.70d)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(ACTIVE)
            .backdropAlpha(0.70d)
            .build())));
        shots.add(new Shot("123-alpha-040", """
            WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.UNDER_WINDOW_BACKGROUND)
                .state(MacosBackdropEffectSpec.MacosBackdropEffectState.ACTIVE)
                .backdropAlpha(0.40d)
                .build());
            """, frame -> WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
            .material(UNDER_WINDOW_BACKGROUND)
            .state(ACTIVE)
            .backdropAlpha(0.40d)
            .build())));

        return shots;
    }

    public static List<ExamplesDocModels.BackdropEntry> documentationEntries() {
        return shots().stream()
            .map(shot -> new ExamplesDocModels.BackdropEntry(shot.name, shot.name, shot.codeSnippet))
            .toList();
    }

    private static void resetFramePresentation(JFrame frame, MacosWindowAppearanceSpec appearanceSpec) {
        WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder().build());
        WindowPresentations.applyAppearance(frame, appearanceSpec);
        WindowBackdrop.remove(frame);
    }

    private enum AppearanceVariant {
        LIGHT("light", MacosWindowAppearanceSpec.VIBRANT_LIGHT),
        DARK("dark", MacosWindowAppearanceSpec.VIBRANT_DARK);

        private final String key;
        private final MacosWindowAppearanceSpec spec;

        AppearanceVariant(String key, MacosWindowAppearanceSpec spec) {
            this.key = key;
            this.spec = spec;
        }
    }

    private record Shot(String name, String codeSnippet, Consumer<JFrame> apply) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record TestCase(Shot shot, AppearanceVariant appearance) {
        @Override
        public String toString() {
            return shot.name + "-" + appearance.key;
        }
    }
}
