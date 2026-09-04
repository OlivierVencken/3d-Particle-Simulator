package com.particle.sim.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelDependencyBoundaryTest {
    private static final Path MAIN = Path.of("src", "main", "java", "com", "particle", "sim");
    private static final List<String> FORBIDDEN_IMPORTS =
            List.of("import imgui.", "import org.lwjgl.", "import com.particle.sim.ui.");

    @Test
    void simulationModelDoesNotDependOnUiOrNativeToolkits() throws IOException {
        List<Path> sources = new ArrayList<>();
        sources.addAll(javaFiles(MAIN.resolve("camera")));
        sources.addAll(javaFiles(MAIN.resolve("settings")));
        sources.addAll(javaFiles(MAIN.resolve("particles").resolve("attraction")));
        sources.addAll(javaFiles(MAIN.resolve("particles").resolve("spawning")));
        sources.add(MAIN.resolve("particles").resolve("DistanceMetric.java"));
        sources.add(MAIN.resolve("particles").resolve("ParticleSimulationConfig.java"));
        sources.add(MAIN.resolve("particles").resolve("ParticleSystem.java"));

        for (Path source : sources) {
            String content = Files.readString(source);
            for (String forbiddenImport : FORBIDDEN_IMPORTS) {
                assertFalse(
                        content.contains(forbiddenImport),
                        () -> source + " crosses the model boundary with " + forbiddenImport);
            }
        }
    }

    private static List<Path> javaFiles(Path directory) throws IOException {
        try (var files = Files.walk(directory)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }
}
