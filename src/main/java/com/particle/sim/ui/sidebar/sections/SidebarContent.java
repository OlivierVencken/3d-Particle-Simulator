package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.ui.sidebar.SidebarSection;

public final class SidebarContent {
    private final SimulationSection simulationSection = new SimulationSection();
    private final ParticlesSection particlesSection = new ParticlesSection();
    private final VisualsSection visualsSection = new VisualsSection();
    private final CameraSection cameraSection = new CameraSection();
    private final AttractionMatrixEditor matrixEditor = new AttractionMatrixEditor();

    public void render(SidebarSection section, GpuParticleSystem particles, CameraController camera,
            Runnable settingsChanged) {
        switch (section) {
            case SIMULATION -> simulationSection.render(particles, settingsChanged);
            case PARTICLES -> particlesSection.render(particles, settingsChanged);
            case VISUALS -> visualsSection.render(particles, settingsChanged);
            case CAMERA -> cameraSection.render(camera, settingsChanged);
            case MATRIX -> matrixEditor.renderSettings(particles, settingsChanged);
        }
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
