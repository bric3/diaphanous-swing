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
import io.github.bric3.diaphanous.robot.docs.ExamplesDocModels;
import io.github.bric3.diaphanous.decorations.MacosWindowAppearanceSpec;
import io.github.bric3.diaphanous.decorations.MacosWindowDecorationsSpec;
import io.github.bric3.diaphanous.decorations.WindowPresentations;
import io.github.bric3.diaphanous.robot.RobotScreenshotSupport;
import io.github.bric3.diaphanous.robot.ScreenshotBaseline;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DecorationsRenderingRobotTest {
    private static final String API_TYPE = "decorations/macos";
    private static final String CONTENT_IMAGE_KEY = "diaphanous.robot.content.image";
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
    @MethodSource("shots")
    void capturesDecorationsWindowRendering(Shot shot) throws Exception {
        RobotScreenshotSupport.assumeDesktopMacOs();
        assumeTrue(WindowPresentations.isSupported());

        ScreenshotBaseline baseline = new ScreenshotBaseline(
                RobotScreenshotSupport.outputDir(),
                RobotScreenshotSupport.baselineDir()
        );
        Robot robot = new Robot();
        robot.setAutoDelay(110);

        JFrame frame = RobotScreenshotSupport.createMinimalWindow("Decorations Robot");

        try {
            RobotScreenshotSupport.onEdtAndWait(() -> {
                resetFramePresentation(frame);
                RootErasingContentPane contentPane = new RootErasingContentPane(new BorderLayout());
                if (shot.wallpaperProperty != null) {
                    contentPane.add(
                        RobotScreenshotSupport.createWallpaperPanel(
                            RobotScreenshotSupport.loadWallpaperFromProperty(shot.wallpaperProperty)
                        ),
                        BorderLayout.CENTER
                    );
                }
                frame.setContentPane(contentPane);
                shot.apply.accept(frame);
                frame.setVisible(true);
            });
            JFrame shown = RobotScreenshotSupport.waitForFrame("Decorations Robot", Duration.ofSeconds(5));
            assertNotNull(shown, "Window was not shown");
            Rectangle captureBounds = RobotScreenshotSupport.captureBounds(shown);
            JFrame visibleProbe = acquireSharedProbe(captureBounds);
            RobotScreenshotSupport.onEdtAndWait(() -> {
                visibleProbe.setVisible(true);
                frame.toFront();
            });
            RobotScreenshotSupport.sleep(700);
            BufferedImage screenshot = RobotScreenshotSupport.captureWindow(robot, captureBounds);
            baseline.assertMatches(API_TYPE, shot.name, screenshot);
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

    private static List<Shot> shots() {
        List<Shot> shots = new ArrayList<>();

        shots.add(new Shot("001-style-default-modern", """
            // resetFramePresentation(frame) already applies default decorations + SYSTEM appearance.
            """, frame -> {
        }));
        shots.add(new Shot("002-style-title-visible", """
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                .titleVisible(true)
                .build());
            """, frame -> {
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                    .titleVisible(true)
                    .build());
        }));
        shots.add(new Shot("003-style-opaque-standard", """
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                .transparentTitleBar(false)
                .fullSizeContentView(false)
                .titleVisible(true)
                .build());
            """, frame -> {
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                    .transparentTitleBar(false)
                    .fullSizeContentView(false)
                    .titleVisible(true)
                    .build());
        }));
        shots.add(new Shot("004-style-transparent-titlebar-only", """
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                .transparentTitleBar(true)
                .fullSizeContentView(true)
                .titleVisible(true)
                .build());
            """, frame -> {
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                    .transparentTitleBar(true)
                    .fullSizeContentView(true)
                    .titleVisible(true)
                    .build());
        }, CONTENT_IMAGE_KEY));
        shots.add(new Shot("005-style-hidden-title-full-size-content", """
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                .transparentTitleBar(false)
                .fullSizeContentView(true)
                .titleVisible(false)
                .build());
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
            // Note: true NSWindow translucent titlebar style is not currently exposed
            // by the decorations API; this shot is the closest approximation.
            // Content image is installed by the test content pane.
            """, frame -> {
            WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder()
                    .transparentTitleBar(false)
                    .fullSizeContentView(true)
                    .titleVisible(false)
                    .build());
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
        }, CONTENT_IMAGE_KEY));
        // Appearance-only shots (no backdrop applied).
        shots.add(new Shot("010-appearance-system-no-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
        }));
        shots.add(new Shot("011-appearance-aqua-no-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
        }));
        shots.add(new Shot("012-appearance-dark-aqua-no-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.DARK_AQUA);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.DARK_AQUA);
        }));
        shots.add(new Shot("013-appearance-vibrant-light-no-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_LIGHT);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_LIGHT);
        }));
        shots.add(new Shot("014-appearance-vibrant-dark-no-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_DARK);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_DARK);
        }));

        // Sibling shots: same appearances with APPEARANCE_BASED backdrop applied.
        shots.add(new Shot("020-appearance-system-with-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
            installBackdrop(frame);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
            installBackdrop(frame);
        }));
        shots.add(new Shot("021-appearance-aqua-with-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
            installBackdrop(frame);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.AQUA);
            installBackdrop(frame);
        }));
        shots.add(new Shot("022-appearance-dark-aqua-with-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.DARK_AQUA);
            installBackdrop(frame);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.DARK_AQUA);
            installBackdrop(frame);
        }));
        shots.add(new Shot("023-appearance-vibrant-light-with-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_LIGHT);
            installBackdrop(frame);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_LIGHT);
            installBackdrop(frame);
        }));
        shots.add(new Shot("024-appearance-vibrant-dark-with-backdrop", """
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_DARK);
            installBackdrop(frame);
            """, frame -> {
            WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.VIBRANT_DARK);
            installBackdrop(frame);
        }));
        return shots;
    }

    public static List<ExamplesDocModels.DecorationEntry> documentationEntries() {
        return shots().stream()
            .map(shot -> new ExamplesDocModels.DecorationEntry(shot.name, shot.name, shot.codeSnippet))
            .toList();
    }

    private static void resetFramePresentation(JFrame frame) {
        frame.setSize(480, 310);
        WindowPresentations.applyDecorations(frame, MacosWindowDecorationsSpec.builder().build());
        WindowPresentations.applyAppearance(frame, MacosWindowAppearanceSpec.SYSTEM);
        WindowBackdrop.remove(frame);
    }

    private static void installBackdrop(JFrame frame) {
        WindowBackdrop.apply(frame, MacosBackdropEffectSpec.builder()
                .material(MacosBackdropEffectSpec.MacosBackdropMaterial.APPEARANCE_BASED)
                .build());
    }

    private static final class Shot {
        final String name;
        final String codeSnippet;
        final Consumer<JFrame> apply;
        final String wallpaperProperty;

        private Shot(String name, String codeSnippet, Consumer<JFrame> apply) {
            this(name, codeSnippet, apply, null);
        }

        private Shot(String name, String codeSnippet, Consumer<JFrame> apply, String wallpaperProperty) {
            this.name = name;
            this.codeSnippet = codeSnippet;
            this.apply = apply;
            this.wallpaperProperty = wallpaperProperty;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
