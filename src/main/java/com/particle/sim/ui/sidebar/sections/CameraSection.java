package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Controls;
import com.particle.sim.ui.components.Text;
import com.particle.sim.ui.theme.ComponentVariant;
import imgui.ImGui;

final class CameraSection {
    private final Controls controls = new Controls();

    void render(SimulationViewModel.Camera camera, SimulationViewActions.Camera actions) {
        Controls.sectionHeading("Movement");
        controls.settingSlider(
                "Sensitivity",
                "camera-sensitivity",
                camera.sensitivity(),
                0.0001f,
                0.01f,
                4,
                actions::setSensitivity);
        controls.settingSlider(
                "Fly speed",
                "camera-speed",
                camera.flySpeed(),
                0.1f,
                30.0f,
                1,
                actions::setFlySpeed);
        ImGui.spacing();
        if (Button.text("Reset camera", "camera-reset", ComponentVariant.SECONDARY)) {
            actions.reset();
        }

        Text.divider();
        Controls.sectionHeading("Controls");
        Text.helper("Hold the primary mouse button in the simulation viewport to look around.");
        Text.helper("WASD move  |  Shift/Control move up/down");
        Text.helper("Home resets the camera.");
    }
}
