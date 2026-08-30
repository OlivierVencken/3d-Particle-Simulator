package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.FourDVisualizationMode;
import com.particle.sim.particles.SimulationDimension;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import imgui.ImGui;

final class CameraSection {
    private static final String[] FOUR_D_VISUALIZATION_MODES = { "Perspective", "W slice", "W color" };
    private static final float MAX_ROTATION_DEGREES = 180.0f;
    private static final float MAX_AUTO_ROTATION_SPEED = 90.0f;
    private final UIControls controls = new UIControls();

    void render(SimulationUiModel.Simulation simulation, SimulationUiModel.Camera camera,
            SimulationUiActions.Camera actions) {
        UIControls.sectionHeading("Movement");
        controls.settingSlider("Sensitivity", "camera-sensitivity", camera.sensitivity(), 0.0001f, 0.01f, 4,
                actions::setSensitivity);
        controls.settingSlider("Fly speed", "camera-speed", camera.flySpeed(), 0.1f, 30.0f, 1,
                actions::setFlySpeed);
        ImGui.spacing();
        if (UIButton.text("Reset camera", "camera-reset", UIComponentVariant.SECONDARY)) {
            actions.reset();
        }

        if (showsFourDControls(simulation.simulationDimension())) {
            renderFourDView(simulation, camera, actions);
        }

        UIText.divider();
        UIControls.sectionHeading("Controls");
        UIText.helper("Hold the primary mouse button in the simulation viewport to look around.");
        UIText.helper("WASD move  |  Shift/Control move up/down");
        UIText.helper("Home resets the camera.");
    }

    private void renderFourDView(SimulationUiModel.Simulation simulation, SimulationUiModel.Camera camera,
            SimulationUiActions.Camera actions) {
        UIText.divider();
        UIControls.sectionHeading("4D view");
        UIText.helper("The 3D camera moves around the projected result. These controls rotate through W.");
        controls.settingCombo("Visualization", "four-d-visualization",
                camera.fourDVisualizationMode().ordinal(), FOUR_D_VISUALIZATION_MODES,
                value -> actions.setFourDVisualizationMode(FourDVisualizationMode.values()[value]));
        controls.settingCheckbox("Pause view motion", "four-d-motion-paused",
                camera.fourDViewMotionPaused(), actions::setFourDViewMotionPaused);
        if (UIButton.text("Reset 4D orientation", "four-d-reset", UIComponentVariant.SECONDARY)) {
            actions.resetFourDOrientation();
        }

        ImGui.spacing();
        UIControls.sectionHeading("Orientation");
        rotationControls("XW", "xw", camera.fourDXwAngleDegrees(), camera.fourDXwAutoEnabled(),
                camera.fourDXwAutoSpeedDegrees(), actions::setFourDXwAngleDegrees,
                actions::setFourDXwAutoEnabled, actions::setFourDXwAutoSpeedDegrees);
        rotationControls("YW", "yw", camera.fourDYwAngleDegrees(), camera.fourDYwAutoEnabled(),
                camera.fourDYwAutoSpeedDegrees(), actions::setFourDYwAngleDegrees,
                actions::setFourDYwAutoEnabled, actions::setFourDYwAutoSpeedDegrees);
        rotationControls("ZW", "zw", camera.fourDZwAngleDegrees(), camera.fourDZwAutoEnabled(),
                camera.fourDZwAutoSpeedDegrees(), actions::setFourDZwAngleDegrees,
                actions::setFourDZwAutoEnabled, actions::setFourDZwAutoSpeedDegrees);

        ImGui.spacing();
        switch (camera.fourDVisualizationMode()) {
            case PERSPECTIVE -> renderPerspectiveControls(simulation, camera, actions);
            case SLICE -> renderSliceControls(simulation, camera, actions);
            case W_COLOR -> renderWColorControls(simulation, camera, actions);
        }
    }

    private void rotationControls(String plane, String id, float angle, boolean automatic, float speed,
            UIControls.FloatSetter angleSetter, UIControls.BooleanSetter automaticSetter,
            UIControls.FloatSetter speedSetter) {
        controls.settingSlider(plane + " angle (deg)", "four-d-" + id + "-angle", angle,
                -MAX_ROTATION_DEGREES, MAX_ROTATION_DEGREES, 1, angleSetter);
        controls.settingCheckbox("Automatic " + plane + " rotation", "four-d-" + id + "-automatic",
                automatic, automaticSetter);
        controls.settingSlider(plane + " auto speed (deg/s)", "four-d-" + id + "-speed", speed,
                -MAX_AUTO_ROTATION_SPEED, MAX_AUTO_ROTATION_SPEED, 1, speedSetter);
    }

    private void renderPerspectiveControls(SimulationUiModel.Simulation simulation,
            SimulationUiModel.Camera camera, SimulationUiActions.Camera actions) {
        UIControls.sectionHeading("Perspective");
        float minimum = camera.minimumFourDPerspectiveDistance();
        float maximum = Math.max(40.0f, Math.max(minimum + 1.0f, simulation.bounds() * 5.0f));
        controls.settingSlider("Projection distance", "four-d-perspective-distance",
                camera.fourDPerspectiveDistance(), minimum, maximum, 2,
                actions::setFourDPerspectiveDistance);
        UIText.helper("The safe minimum keeps the projection plane beyond the particle bounds.");
    }

    private void renderSliceControls(SimulationUiModel.Simulation simulation,
            SimulationUiModel.Camera camera, SimulationUiActions.Camera actions) {
        UIControls.sectionHeading("W slice");
        float bounds = simulation.bounds();
        controls.settingSlider("Slice center W", "four-d-slice-center", camera.fourDSliceCenterW(),
                -bounds, bounds, 2, actions::setFourDSliceCenterW);
        controls.settingSlider("Slice thickness", "four-d-slice-thickness", camera.fourDSliceThickness(),
                0.05f, bounds * 2.0f, 2, actions::setFourDSliceThickness);
        controls.settingSlider("Edge feather", "four-d-slice-feather", camera.fourDSliceFeather(),
                0.0f, Math.max(0.01f, camera.fourDSliceThickness() * 0.5f), 2,
                actions::setFourDSliceFeather);
        controls.settingCheckbox("Automatic slice sweep", "four-d-slice-sweep",
                camera.fourDSliceSweepEnabled(), actions::setFourDSliceSweepEnabled);
        controls.settingSlider("Sweep speed", "four-d-slice-sweep-speed", camera.fourDSliceSweepSpeed(),
                0.05f, Math.max(1.0f, bounds * 2.0f), 2, actions::setFourDSliceSweepSpeed);
    }

    private void renderWColorControls(SimulationUiModel.Simulation simulation,
            SimulationUiModel.Camera camera, SimulationUiActions.Camera actions) {
        UIControls.sectionHeading("W color");
        controls.settingSlider("W color range", "four-d-color-range", camera.fourDColorRange(),
                0.1f, simulation.bounds() * 2.0f, 2, actions::setFourDColorRange);
        UIText.helper("Blue shows negative W, a light midpoint shows W = 0, and orange shows positive W.");
    }

    static boolean showsFourDControls(SimulationDimension dimension) {
        return dimension == SimulationDimension.FOUR_D;
    }
}
