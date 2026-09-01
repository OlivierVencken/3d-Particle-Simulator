package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.sidebar.SidebarSection;

public final class SidebarContent {
    private final SimulationSection simulationSection = new SimulationSection();
    private final ParticlesSection particlesSection = new ParticlesSection();
    private final VisualsSection visualsSection = new VisualsSection();
    private final CameraSection cameraSection = new CameraSection();
    private final AttractionMatrixEditor matrixEditor = new AttractionMatrixEditor();

    public void render(
            SidebarSection section, SimulationViewModel model, SimulationViewActions actions) {
        switch (section) {
            case SIMULATION ->
                    simulationSection.render(
                            model.simulation(), model.application(), actions.simulation());
            case PARTICLES -> particlesSection.render(model.particles(), actions.particles());
            case VISUALS -> visualsSection.render(model.visuals(), actions.visuals());
            case CAMERA -> cameraSection.render(model.camera(), actions.camera());
            case MATRIX -> matrixEditor.renderSettings(model.particles(), actions.particles());
        }
    }

    public void renderPopups(SimulationViewActions actions) {
        particlesSection.renderPopups(actions.particles());
    }

    public boolean hasOpenModal() {
        return particlesSection.hasOpenModal();
    }

    public int customSpawnAmount() {
        return particlesSection.customSpawnAmount();
    }

    public void setCustomSpawnAmount(int amount) {
        particlesSection.setCustomSpawnAmount(amount);
    }

    public float matrixEditStep() {
        return matrixEditor.matrixEditStep();
    }

    public void setMatrixEditStep(float step) {
        matrixEditor.setMatrixEditStep(step);
    }
}
