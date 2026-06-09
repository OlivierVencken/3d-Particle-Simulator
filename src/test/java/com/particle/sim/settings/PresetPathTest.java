package com.particle.sim.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PresetPathTest {
    @TempDir
    Path tempDir;

    @Test
    void appendsPresetExtensionWhenMissing() {
        Path path = tempDir.resolve("my-preset");

        assertEquals(tempDir.resolve("my-preset" + AppSettings.PRESET_EXTENSION), AppSettings.ensurePresetExtension(path));
    }

    @Test
    void keepsExistingPresetExtension() {
        Path path = tempDir.resolve("my-preset" + AppSettings.PRESET_EXTENSION);

        assertEquals(path, AppSettings.ensurePresetExtension(path));
    }

    @Test
    void derivesPresetNameFromFileName() {
        assertEquals("my-preset", AppSettings.presetNameFromPath(tempDir.resolve("my-preset" + AppSettings.PRESET_EXTENSION)));
        assertEquals("Untitled preset", AppSettings.presetNameFromPath(tempDir.resolve(AppSettings.PRESET_EXTENSION)));
    }
}
