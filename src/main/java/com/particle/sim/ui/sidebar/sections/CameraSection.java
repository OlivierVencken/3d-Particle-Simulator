package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import imgui.ImGui;

final class CameraSection {
    private final UIControls controls = new UIControls();

    void render(SimulationUiModel.Camera camera, SimulationUiActions.Camera actions) {
        UIControls.sectionHeading("Movement");
        controls.settingSlider("Sensitivity", "camera-sensitivity", camera.sensitivity(), 0.0001f, 0.01f, 4,
                actions::setSensitivity);
        UIText.helper("Mouse-look sensitivity: 0.0001–0.0100");
        controls.settingSlider("Fly speed", "camera-speed", camera.flySpeed(), 0.1f, 30.0f, 1,
                actions::setFlySpeed);
        UIText.helper("Movement speed: 0.1–30.0 world units per second");
        ImGui.spacing();
        if (UIButton.text("Reset camera", "camera-reset", UIComponentVariant.SECONDARY)) {
            actions.reset();
        }

        UIText.divider();
        UIControls.sectionHeading("Controls");
        UIText.helper("Hold the primary mouse button in the simulation viewport to look around.");
        UIText.helper("WASD move  |  Shift/Control move up/down");
        UIText.helper("Home resets the camera.");
    }
}
