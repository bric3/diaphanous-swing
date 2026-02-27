/*
 * Diaphanous Swing
 *
 * Copyright (c) 2026 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.diaphanous.robot.docs;

import io.github.bric3.diaphanous.robot.macos.BackdropEffectRenderingRobotTest;
import io.github.bric3.diaphanous.robot.macos.DecorationsRenderingRobotTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExamplesMarkdownGenerator {
    private static final String DECORATIONS_BASE = "src/robotTest/resources/decorations/macos";
    private static final String BACKDROP_BASE = "src/robotTest/resources/backdrop/macos";

    private ExamplesMarkdownGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path projectDir = resolveProjectDir();
        Path outputFile = resolveOutputFile(projectDir, args);
        String markdown = buildMarkdown(
            projectDir,
            outputFile,
            DecorationsRenderingRobotTest.documentationEntries(),
            BackdropEffectRenderingRobotTest.documentationEntries()
        );
        Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, markdown);
    }

    private static Path resolveProjectDir() {
        String projectDirProperty = System.getProperty("diaphanous.projectDir");
        if (projectDirProperty != null && !projectDirProperty.isBlank()) {
            return Path.of(projectDirProperty);
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    private static Path resolveOutputFile(Path projectDir, String[] args) {
        if (args.length > 0 && !args[0].isBlank()) {
            return toAbsolute(projectDir, Path.of(args[0]));
        }
        String configured = System.getProperty("diaphanous.examples.output");
        if (configured != null && !configured.isBlank()) {
            return toAbsolute(projectDir, Path.of(configured));
        }
        return projectDir.resolve("EXAMPLES.md").normalize();
    }

    private static Path toAbsolute(Path projectDir, Path path) {
        return path.isAbsolute() ? path.normalize() : projectDir.resolve(path).normalize();
    }

    private static String buildMarkdown(
        Path projectDir,
        Path outputFile,
        List<ExamplesDocModels.DecorationEntry> decorations,
        List<ExamplesDocModels.BackdropEntry> backdrops
    ) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Examples\n\n");

        markdown.append("## Decorations\n\n");
        for (ExamplesDocModels.DecorationEntry entry : decorations) {
            markdown.append("### ").append(entry.title()).append("\n\n");
            markdown.append("Baseline Screenshot\n\n");
            markdown.append("![").append(entry.title()).append("](")
                .append(relativePath(outputFile, projectDir.resolve(DECORATIONS_BASE).resolve(entry.screenshotKey() + ".png")))
                .append(")\n\n");
            markdown.append("Code snippet\n\n");
            markdown.append("```java\n").append(entry.codeSnippet().strip()).append("\n```\n\n");
        }

        markdown.append("## Backdrop\n\n");
        for (ExamplesDocModels.BackdropEntry entry : backdrops) {
            markdown.append("### ").append(entry.title()).append("\n\n");
            markdown.append("| Light | Dark |\n");
            markdown.append("| --- | --- |\n");
            markdown.append("| ![").append(entry.title()).append(" light](")
                .append(relativePath(outputFile, projectDir.resolve(BACKDROP_BASE).resolve(entry.screenshotKey() + "-light.png")))
                .append(") | ![").append(entry.title()).append(" dark](")
                .append(relativePath(outputFile, projectDir.resolve(BACKDROP_BASE).resolve(entry.screenshotKey() + "-dark.png")))
                .append(") |\n\n");
            markdown.append("Code snippet\n\n");
            markdown.append("```java\n").append(entry.codeSnippet().strip()).append("\n```\n\n");
        }
        return markdown.toString();
    }

    private static String relativePath(Path outputFile, Path targetFile) {
        Path parent = outputFile.getParent() == null ? Path.of(".") : outputFile.getParent();
        return parent.relativize(targetFile).toString().replace('\\', '/');
    }
}
