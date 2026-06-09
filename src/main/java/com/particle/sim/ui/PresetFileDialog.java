package com.particle.sim.ui;

import com.particle.sim.settings.AppSettings;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDSaveDialogArgs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_CANCEL;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_FreePath;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_Init;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_OpenDialog;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_Quit;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_SaveDialog_With;

public final class PresetFileDialog {
    private static final String DEFAULT_SAVE_NAME = "preset" + AppSettings.PRESET_EXTENSION;

    private static boolean initialized;

    private PresetFileDialog() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        if (NFD_Init() != NFD_OKAY) {
            throw new IllegalStateException("Could not initialize the native file dialog.");
        }

        initialized = true;
    }

    public static void shutdown() {
        if (!initialized) {
            return;
        }

        NFD_Quit();
        initialized = false;
    }

    public static Path defaultDirectory() {
        return Path.of(System.getProperty("user.home"), ".particle-simulator", "presets");
    }

    public static Optional<Path> showOpenDialog() {
        ensureInitialized();
        return showDialog(false);
    }

    public static Optional<Path> showSaveDialog() {
        ensureInitialized();
        return showDialog(true);
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("PresetFileDialog.init() must be called before showing dialogs.");
        }
    }

    private static Optional<Path> showDialog(boolean save) {
        Path defaultDirectory = ensureDefaultDirectory();

        try (var stack = stackPush()) {
            var filters = NFDFilterItem.malloc(1, stack);
            filters.get(0)
                    .name(stack.UTF8("3D Particle Simulator preset"))
                    .spec(stack.UTF8(AppSettings.PRESET_EXTENSION.substring(1)));

            var outPath = stack.mallocPointer(1);
            int result = save
                    ? NFD_SaveDialog_With(outPath, NFDSaveDialogArgs.calloc(stack)
                            .filterList(filters)
                            .defaultPath(stack.UTF8(defaultDirectory.toString()))
                            .defaultName(stack.UTF8(DEFAULT_SAVE_NAME)))
                    : NFD_OpenDialog(outPath, filters, defaultDirectory.toString());

            if (result == NFD_CANCEL) {
                return Optional.empty();
            }
            if (result != NFD_OKAY) {
                return Optional.empty();
            }

            long nativePath = outPath.get(0);
            try {
                Path selectedPath = Path.of(outPath.getStringUTF8(0));
                return Optional.of(save ? AppSettings.ensurePresetExtension(selectedPath) : selectedPath);
            } finally {
                NFD_FreePath(nativePath);
            }
        }
    }

    private static Path ensureDefaultDirectory() {
        Path directory = defaultDirectory();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            return Path.of(System.getProperty("user.home"));
        }
        return directory;
    }
}
