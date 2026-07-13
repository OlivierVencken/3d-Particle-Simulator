package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.camera.CameraController;
import com.particle.sim.ui.components.UIControls;
import imgui.ImGui;

final class CameraSection {
    void render(CameraController camera, Runnable settingsChanged) {
        UIControls.sectionHeading("Movement");
        UIControls.settingSlider("Sensitivity", "camera-sensitivity", camera.getSensitivity(), 0.0001f, 0.01f, 4,
                camera::setSensitivity, settingsChanged);
        UIControls.settingSlider("Fly speed", "camera-speed", camera.getFlySpeed(), 0.1f, 30.0f, 1,
                camera::setFlySpeed, settingsChanged);
        if (ImGui.button("Reset camera##camera-reset")) {
            camera.reset();
        }
        ImGui.spacing();
        ImGui.textDisabled("WASD move  |  Mouse look");
        ImGui.textDisabled("Shift accelerates movement");
    }
}
