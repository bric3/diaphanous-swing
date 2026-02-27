/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.robot;

import org.junit.jupiter.api.Assertions;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ScreenshotBaseline {
    private final File outputDir;
    private final File baselineDir;
    private final TestMode testMode;
    private final double tolerance;

    public ScreenshotBaseline(File outputDir, File baselineDir) {
        this.outputDir = outputDir;
        this.baselineDir = baselineDir;
        this.testMode = parseMode(System.getProperty("diaphanous.robot.test-mode"));
        this.tolerance = parseTolerance(System.getProperty("diaphanous.robot.tolerance"));
    }

    public void assertMatches(String apiType, String shotName, BufferedImage actualImage) {
        if (testMode == TestMode.DRY_RUN) {
            return;
        }
        Path baselinePath = baselineDir.toPath().resolve(apiType).resolve(shotName + ".png");
        Path actualPath = outputDir.toPath().resolve("actual").resolve(apiType).resolve(shotName + ".png");
        Path diffPath = outputDir.toPath().resolve("diff").resolve(apiType).resolve(shotName + ".png");
        Path candidatePath = outputDir.toPath().resolve("candidate").resolve(apiType).resolve(shotName + ".png");

        write(actualPath, actualImage);

        if (testMode == TestMode.RECORD) {
            write(baselinePath, actualImage);
            return;
        }

        if (!Files.isRegularFile(baselinePath)) {
            write(candidatePath, actualImage);
            return;
        }

        BufferedImage expected = read(baselinePath);
        Comparison comparison = compare(expected, actualImage);
        if (comparison.matchRatio >= (1.0d - tolerance)) {
            try {
                Files.deleteIfExists(diffPath);
            } catch (IOException ignored) {
                // best effort
            }
            return;
        }
        write(diffPath, comparison.diffImage);
        String ratio = String.format(Locale.ROOT, "%.5f", comparison.matchRatio);
        String toleranceLabel = String.format(Locale.ROOT, "%.5f", tolerance);
        Assertions.fail(
            "Screenshot mismatch for [" + apiType + "/" + shotName + "], matchRatio=" + ratio
            + ", tolerance=" + toleranceLabel
            + ", actual=" + actualPath
            + ", baseline=" + baselinePath
            + ", diff=" + diffPath
        );
    }

    private static TestMode parseMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return TestMode.TEST;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "test" -> TestMode.TEST;
            case "record" -> TestMode.RECORD;
            case "dry-run" -> TestMode.DRY_RUN;
            default -> throw new IllegalArgumentException(
                "diaphanous.robot.test-mode must be one of: test, record, dry-run; got " + raw
            );
        };
    }

    private static Comparison compare(BufferedImage expected, BufferedImage actual) {
        int width = Math.max(expected.getWidth(), actual.getWidth());
        int height = Math.max(expected.getHeight(), actual.getHeight());
        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        long totalPixels = (long) width * height;
        long equalPixels = 0L;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean inExpected = x < expected.getWidth() && y < expected.getHeight();
                boolean inActual = x < actual.getWidth() && y < actual.getHeight();
                int expectedArgb = inExpected ? expected.getRGB(x, y) : Color.BLACK.getRGB();
                int actualArgb = inActual ? actual.getRGB(x, y) : Color.BLACK.getRGB();
                if (expectedArgb == actualArgb && inExpected == inActual) {
                    equalPixels++;
                    int gray = luminance(actualArgb);
                    diff.setRGB(x, y, new Color(gray, gray, gray, 90).getRGB());
                } else {
                    int highlight = blendRed(expectedArgb, actualArgb, inExpected, inActual);
                    diff.setRGB(x, y, highlight);
                }
            }
        }
        double matchRatio = totalPixels == 0L ? 1.0d : ((double) equalPixels) / totalPixels;
        return new Comparison(matchRatio, diff);
    }

    private static int luminance(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    private static int blendRed(int expectedArgb, int actualArgb, boolean inExpected, boolean inActual) {
        int expectedR = (expectedArgb >> 16) & 0xFF;
        int actualR = (actualArgb >> 16) & 0xFF;
        int expectedG = (expectedArgb >> 8) & 0xFF;
        int actualG = (actualArgb >> 8) & 0xFF;
        int expectedB = expectedArgb & 0xFF;
        int actualB = actualArgb & 0xFF;
        int delta = Math.abs(expectedR - actualR) + Math.abs(expectedG - actualG) + Math.abs(expectedB - actualB);
        int intensity = Math.min(255, 80 + (delta / 2));
        int alpha = (inExpected && inActual) ? 220 : 255;
        return new Color(intensity, 10, 10, alpha).getRGB();
    }

    private static double parseTolerance(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0.0d;
        }
        try {
            double value = Double.parseDouble(raw);
            if (value < 0.0d || value > 1.0d) {
                throw new IllegalArgumentException("diaphanous.robot.tolerance must be in [0,1], got " + raw);
            }
            return value;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("diaphanous.robot.tolerance must be a double, got " + raw, nfe);
        }
    }

    private static void write(Path path, BufferedImage image) {
        try {
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException ioException) {
            throw new IllegalStateException("Failed to write image: " + path, ioException);
        }
    }

    private static BufferedImage read(Path path) {
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException ioException) {
            throw new IllegalStateException("Failed to read baseline image: " + path, ioException);
        }
    }

    private enum TestMode {
        TEST,
        RECORD,
        DRY_RUN
    }

    private record Comparison(double matchRatio, BufferedImage diffImage) {
    }
}
