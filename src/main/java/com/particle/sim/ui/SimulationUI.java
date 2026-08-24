package com.particle.sim.ui;

import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.commandbar.CommandBar;
import com.particle.sim.ui.components.DebugPanel;
import com.particle.sim.ui.sidebar.Sidebar;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiPopupFlags;
import imgui.type.ImBoolean;

import java.util.Objects;

public final class SimulationUI {
    private final UIState state = new UIState();
    private final CommandBar commandBar = new CommandBar();
    private final Sidebar sidebar = new Sidebar();
    private final DebugPanel debugPanel = new DebugPanel();
    private final ImBoolean showDebug = new ImBoolean(false);

    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private int fpsCap = SimulationDefaults.FPS_CAP;
    private boolean paused;
    private boolean hidden;
    private PreparedUiFrame preparedFrame;
    private SimulationUiModel model;
    private SimulationUiActions actions;

    public void connect(SimulationUiModel model, SimulationUiActions actions) {
        this.model = Objects.requireNonNull(model, "model");
        this.actions = Objects.requireNonNull(actions, "actions");
    }

    public void render(float deltaTime) {
        updateFps(deltaTime);
        if (hidden) {
            return;
        }
        if (model == null || actions == null) {
            throw new IllegalStateException("Simulation UI must be connected before rendering");
        }

        if (preparedFrame == null) {
            throw new IllegalStateException("Simulation UI frame must be prepared before rendering");
        }

        UILayout layout = preparedFrame.layout();
        state.setLayoutMode(layout.mode());
        commandBar.render(layout, state, model, actions, currentFps, showDebug);
        sidebar.render(layout.sidebar(), state, model, actions);
        if (showDebug.get()) {
            debugPanel.render(deltaTime, currentFps, model, actions, showDebug);
        }
    }

    public PreparedUiFrame prepareFrame(int framebufferWidth, int framebufferHeight) {
        UIDesignTokens tokens = UITheme.tokens();
        ImGuiIO io = ImGui.getIO();
        UiDisplayMetrics displayMetrics = UiDisplayMetrics.from(
                io, tokens.scale(), framebufferWidth, framebufferHeight);
        UILayout layout = UILayoutCalculator.calculate(
                displayMetrics.logicalWidth(), displayMetrics.logicalHeight(),
                state.sidebarVisible(), !hidden, tokens);
        boolean modalOpen = !hidden && (commandBar.hasOpenModal() || sidebar.hasOpenModal());
        boolean popupOpen = !hidden && (commandBar.hasOpenWindow()
                || ImGui.isPopupOpen("", ImGuiPopupFlags.AnyPopup));
        UiInputOwnership inputOwnership = UiInputOwnership.resolve(
                layout, !hidden,
                io.getMousePosX(), io.getMousePosY(),
                io.getWantCaptureMouse(), io.getWantCaptureKeyboard(),
                popupOpen, modalOpen);
        preparedFrame = new PreparedUiFrame(
                displayMetrics,
                layout,
                displayMetrics.toFramebuffer(layout.simulation()),
                !hidden,
                inputOwnership);
        state.setLayoutMode(layout.mode());
        return preparedFrame;
    }

    public PreparedUiFrame preparedFrame() {
        if (preparedFrame == null) {
            throw new IllegalStateException("Simulation UI frame has not been prepared");
        }
        return preparedFrame;
    }

    private void updateFps(float deltaTime) {
        fpsTimeAccumulator += deltaTime;
        fpsFrameAccumulator++;

        if (fpsTimeAccumulator >= 0.35f) {
            currentFps = fpsFrameAccumulator / fpsTimeAccumulator;
            fpsTimeAccumulator = 0.0f;
            fpsFrameAccumulator = 0;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public float matrixEditStep() {
        return sidebar.matrixEditStep();
    }

    public void setMatrixEditStep(float matrixEditStep) {
        sidebar.setMatrixEditStep(matrixEditStep);
    }

    public int customSpawnAmount() {
        return sidebar.customSpawnAmount();
    }

    public void setCustomSpawnAmount(int customSpawnAmount) {
        sidebar.setCustomSpawnAmount(customSpawnAmount);
    }

    public int fpsCap() {
        return fpsCap;
    }

    public void setFpsCap(int fpsCap) {
        if (fpsCap <= 0) {
            this.fpsCap = 0;
            return;
        }

        this.fpsCap = Math.max(SimulationDefaults.MIN_FPS_CAP,
                Math.min(SimulationDefaults.MAX_FPS_CAP, fpsCap));
    }

    public void togglePause() {
        setPaused(!isPaused());
    }

    public void toggleUi() {
        if (isHidden()) {
            show();
        } else {
            hide();
        }
    }

    public void toggleDebug() {
        showDebug.set(!showDebug.get());
    }

    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        hidden = true;
    }

    public void show() {
        hidden = false;
    }

    public void showError(String summary, String details) {
        hidden = false;
        commandBar.showError(summary, details);
    }

    public void dispose() {
        commandBar.dispose();
    }
}
