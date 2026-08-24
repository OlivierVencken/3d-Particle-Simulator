package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIAttractionMatrix;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.theme.UIDesignTokens;

final class AttractionMatrixEditor {
    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    private final UIControls controls = new UIControls();

    void renderSettings(SimulationUiModel.Particles particles, SimulationUiActions.Particles actions) {
        controls.settingSlider("Edit step", "matrix-edit-step", particles.matrixEditStep(),
                0.01f, 0.5f, 2, actions::setMatrixEditStep);
        UIAttractionMatrix.render(particles, actions);
    }

    static float fittedCellSize(float availableWidth, int groupCount) {
        return fittedCellSize(availableWidth, groupCount, UIDesignTokens.unscaled().matrixGap());
    }

    static float fittedCellSize(float availableWidth, int groupCount, float matrixGap) {
        if (availableWidth <= 0.0f || groupCount <= 0) {
            return 0.0f;
        }
        return Math.max(0.0f,
                (availableWidth - groupCount * Math.max(0.0f, matrixGap)) / (groupCount + 1));
    }

    float matrixEditStep() {
        return matrixEditStep;
    }

    void setMatrixEditStep(float matrixEditStep) {
        this.matrixEditStep = Math.max(0.01f, Math.min(0.5f, matrixEditStep));
    }
}
