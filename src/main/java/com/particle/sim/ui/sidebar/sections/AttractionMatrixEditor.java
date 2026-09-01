package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.AttractionPattern;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIAttractionMatrix;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;

final class AttractionMatrixEditor {
    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    private float generatorVariation = 0.12f;
    private int generatorPattern = AttractionPattern.STABLE.ordinal();
    private final UIControls controls = new UIControls();

    void renderSettings(SimulationUiModel.Particles particles, SimulationUiActions.Particles actions) {
        UIControls.sectionHeading("Generator");
        controls.settingCombo("Pattern", "matrix-generator-pattern", generatorPattern,
                AttractionPattern.labels(), value -> generatorPattern = value);
        controls.settingSlider("Random variation", "matrix-generator-variation", generatorVariation,
                0.0f, 0.5f, 2, value -> generatorVariation = value);
        if (UIButton.text("Generate matrix", "matrix-generate", UIComponentVariant.PRIMARY,
                0.0f, UITheme.tokens().controlHeight())) {
            actions.generateAttractionMatrix(AttractionPattern.values()[generatorPattern], generatorVariation);
        }

        UIText.divider();

        UIControls.sectionHeading("Editing");
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
