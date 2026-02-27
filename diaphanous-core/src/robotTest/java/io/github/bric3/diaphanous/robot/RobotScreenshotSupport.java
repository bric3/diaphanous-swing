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

import org.junit.jupiter.api.Assumptions;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public final class RobotScreenshotSupport {
    private static final int CAPTURE_MARGIN_PX = 16;

    private RobotScreenshotSupport() {
    }

    public static void assumeDesktopMacOs() {
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("mac"));
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
    }

    public static File outputDir() {
        String configured = System.getProperty("diaphanous.robot.outputDir");
        File dir = configured == null || configured.isBlank()
                   ? new File("build/reports/robotTest")
                   : new File(configured);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to create output directory: " + dir);
        }
        return dir;
    }

    public static File baselineDir() {
        String configured = System.getProperty("diaphanous.robot.baselineDir");
        File dir = configured == null || configured.isBlank()
                   ? new File("src/robotTest/resources")
                   : new File(configured);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to create baseline directory: " + dir);
        }
        return dir;
    }

    public static JFrame createMinimalWindow(String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(480, 310);
        frame.setMinimumSize(new Dimension(400, 260));
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel();
        content.setOpaque(true);
        content.setBackground(new Color(255, 255, 255, 224));
        frame.setContentPane(content);
        return frame;
    }

    public static JFrame createBackdropProbe(Rectangle bounds) {
        JFrame frame = new JFrame("Backdrop Probe");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(bounds);
        Image wallpaper = loadWallpaperFromProperty("diaphanous.robot.wallpaper");
        frame.setContentPane(new WallpaperPanel(wallpaper));
        frame.setAlwaysOnTop(false);
        return frame;
    }

    public static Image loadWallpaperFromProperty(String propertyKey) {
        String configured = System.getProperty(propertyKey);
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException(
                "Missing wallpaper configuration. Set -D" + propertyKey + "=<path or relative path>."
            );
        }
        String requestedPath = configured.trim();
        File explicitFile = new File(requestedPath);
        if (explicitFile.isAbsolute()) {
            if (explicitFile.isFile()) {
                return new ImageIcon(explicitFile.getAbsolutePath()).getImage();
            }
            throw new IllegalStateException("Configured wallpaper file does not exist: " + explicitFile.getAbsolutePath());
        }

        List<File> candidates = new ArrayList<>();
        String projectDir = System.getProperty("diaphanous.projectDir");
        if (projectDir != null && !projectDir.isBlank()) {
            candidates.add(new File(projectDir, requestedPath));
            candidates.add(new File(projectDir, "assets/wallpaper/" + requestedPath));
        }
        candidates.add(new File(requestedPath));
        candidates.add(new File("assets/wallpaper/" + requestedPath));
        for (File candidate : candidates) {
            if (candidate.isFile()) {
                return new ImageIcon(candidate.getAbsolutePath()).getImage();
            }
        }

        throw new IllegalStateException(
            "Wallpaper not found for " + propertyKey + "=" + requestedPath
                + ". Search locations: project-relative path and assets/wallpaper."
        );
    }

    public static JPanel createWallpaperPanel(Image wallpaper) {
        return new WallpaperPanel(wallpaper);
    }

    public static Rectangle captureBounds(Window window) {
        Rectangle bounds = new Rectangle(window.getBounds());
        try {
            return runOnEdtAndWait(() -> {
                var location = window.getLocationOnScreen();
                return new Rectangle(
                    location.x - CAPTURE_MARGIN_PX,
                    location.y - CAPTURE_MARGIN_PX,
                    window.getWidth() + (CAPTURE_MARGIN_PX * 2),
                    window.getHeight() + (CAPTURE_MARGIN_PX * 2)
                );
            });
        } catch (Exception ignored) {
            return bounds;
        }
    }

    public static BufferedImage captureWindow(Robot robot, Rectangle captureBounds) {
        moveMouseOutsideCapture(robot, captureBounds);
        sleep(120);
        MultiResolutionImage multi = robot.createMultiResolutionScreenCapture(captureBounds);
        List<Image> variants = new ArrayList<>();
        for (Object variant : multi.getResolutionVariants()) {
            if (variant instanceof Image image) {
                variants.add(image);
            }
        }
        Image best = variants.stream()
            .max((left, right) -> {
                long leftArea = Math.max(1, left.getWidth(null)) * (long) Math.max(1, left.getHeight(null));
                long rightArea = Math.max(1, right.getWidth(null)) * (long) Math.max(1, right.getHeight(null));
                return Long.compare(leftArea, rightArea);
            })
            .orElseGet(() -> robot.createScreenCapture(captureBounds));

        BufferedImage raw = toBufferedImage(best);
        return downscaleForOutput(raw, captureBounds.width, captureBounds.height);
    }

    public static JFrame waitForFrame(String title, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            for (Frame frame : Frame.getFrames()) {
                if (frame instanceof JFrame candidate && candidate.isShowing() && title.equals(candidate.getTitle())) {
                    return candidate;
                }
            }
            sleep(80);
        }
        return null;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for UI to settle", interruptedException);
        }
    }

    public static void onEdtAndWait(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
            return;
        }
        try {
            EventQueue.invokeAndWait(runnable);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run UI block on EDT", e);
        }
    }

    static <T> T runOnEdtAndWait(Callable<T> block) {
        if (EventQueue.isDispatchThread()) {
            try {
                return block.call();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to run UI block on EDT", e);
            }
        }
        final Object[] result = new Object[1];
        final Exception[] failure = new Exception[1];
        try {
            EventQueue.invokeAndWait(() -> {
                try {
                    result[0] = block.call();
                } catch (Exception e) {
                    failure[0] = e;
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run UI block on EDT", e);
        }
        if (failure[0] != null) {
            throw new IllegalStateException("Failed to run UI block on EDT", failure[0]);
        }
        @SuppressWarnings("unchecked")
        T cast = (T) result[0];
        return cast;
    }

    private static BufferedImage downscaleForOutput(BufferedImage source, int targetWidth, int targetHeight) {
        if (source.getWidth() <= targetWidth || source.getHeight() <= targetHeight) {
            return source;
        }
        BufferedImage out = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return out;
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }
        int width = Math.max(1, image.getWidth(null));
        int height = Math.max(1, image.getHeight(null));
        BufferedImage converted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = converted.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return converted;
    }

    private static void moveMouseOutsideCapture(Robot robot, Rectangle captureBounds) {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return;
        }
        if (!captureBounds.contains(pointerInfo.getLocation())) {
            return;
        }
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int[][] candidates = new int[][]{
            {8, 8},
            {Math.max(0, screen.width - 8), 8},
            {8, Math.max(0, screen.height - 8)},
            {Math.max(0, screen.width - 8), Math.max(0, screen.height - 8)}
        };
        for (int[] candidate : candidates) {
            if (!captureBounds.contains(candidate[0], candidate[1])) {
                robot.mouseMove(candidate[0], candidate[1]);
                robot.waitForIdle();
                return;
            }
        }
    }

    private static final class PatternPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int width = getWidth();
            int height = getHeight();
            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(23, 45, 72), width, height, new Color(178, 119, 62)));
            g2.fillRect(0, 0, width, height);
            g2.setColor(new Color(255, 255, 255, 40));
            for (int x = -height; x < width + height; x += 32) {
                g2.drawLine(x, 0, x - height, height);
            }
            g2.dispose();
        }
    }

    private static final class WallpaperPanel extends JPanel {
        private final Image wallpaper;

        private WallpaperPanel(Image wallpaper) {
            this.wallpaper = wallpaper;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (wallpaper == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(wallpaper, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}
