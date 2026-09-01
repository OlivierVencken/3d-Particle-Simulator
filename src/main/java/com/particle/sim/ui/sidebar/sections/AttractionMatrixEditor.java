package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.AttractionPattern;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.components.AttractionMatrixControl;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Controls;
import com.particle.sim.ui.components.Text;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;

final class AttractionMatrixEditor {
    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    private float generatorVariation = 0.12f;
    private int generatorPattern = AttractionPattern.STABLE.ordinal();
    private final Controls controls = new Controls();

    void renderSettings(
            SimulationViewModel.Particles particles, SimulationViewActions.Particles actions) {
        Controls.sectionHeading("Generator");
        controls.settingCombo(
                "Pattern",
                "matrix-generator-pattern",
                generatorPattern,
                AttractionPattern.labels(),
                value -> generatorPattern = value);
        controls.settingSlider(
                "Random variation",
                "matrix-generator-variation",
                generatorVariation,
                0.0f,
                0.5f,
                2,
                value -> generatorVariation = value);
        if (Button.text(
                "Generate matrix",
                "matrix-generate",
                ComponentVariant.PRIMARY,
                0.0f,
                Theme.tokens().controlHeight())) {
            actions.generateAttractionMatrix(
                    AttractionPattern.values()[generatorPattern], generatorVariation);
        }

        Text.divider();

        Controls.sectionHeading("Editing");
        controls.settingSlider(
                "Edit step",
                "matrix-edit-step",
                particles.matrixEditStep(),
                0.01f,
                0.5f,
                2,
                actions::setMatrixEditStep);
        AttractionMatrixControl.render(particles, actions);
    }

    static float fittedCellSize(float availableWidth, int groupCount) {
        return fittedCellSize(availableWidth, groupCount, DesignTokens.unscaled().matrixGap());
    }

    static float fittedCellSize(float availableWidth, int groupCount, float matrixGap) {
        if (availableWidth <= 0.0f || groupCount <= 0) {
            return 0.0f;
        }
        return Math.max(
                0.0f, (availableWidth - groupCount * Math.max(0.0f, matrixGap)) / (groupCount + 1));
    }

    float matrixEditStep() {
        return matrixEditStep;
    }

    void setMatrixEditStep(float matrixEditStep) {
        this.matrixEditStep = Math.max(0.01f, Math.min(0.5f, matrixEditStep));
    }
}
