package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import imgui.ImGui;

final class CameraSection {
    void render(SimulationUiModel.Camera camera, SimulationUiActions.Camera actions) {
        UIControls.sectionHeading("Movement");
        UIControls.settingSlider("Sensitivity", "camera-sensitivity", camera.sensitivity(), 0.0001f, 0.01f, 4,
                actions::setSensitivity);
        UIControls.settingSlider("Fly speed", "camera-speed", camera.flySpeed(), 0.1f, 30.0f, 1,
                actions::setFlySpeed);
        if (ImGui.button("Reset camera##camera-reset")) {
            actions.reset();
        }
        ImGui.spacing();
        ImGui.textDisabled("WASD move  |  Mouse look");
        ImGui.textDisabled("Shift accelerates movement");
    }
}
