package com.particle.sim.ui.workspace;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiTabItemFlags;
import imgui.flag.ImGuiWindowFlags;

final class WorkspaceSidebar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final int TAB_BAR_FLAGS = ImGuiTabBarFlags.FittingPolicyResizeDown
            | ImGuiTabBarFlags.NoCloseWithMiddleMouseButton;

    private final InspectorSections sections = new InspectorSections();

    void render(WorkspaceLayout.Panel panel, WorkspaceState state, GpuParticleSystem particles,
            CameraController camera, Runnable settingsChanged) {
        if (!panel.visible()) {
            return;
        }

        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        if (ImGui.begin("##workspace-sidebar", WINDOW_FLAGS)) {
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 5.0f, 7.0f);
            renderTabs(state, particles, camera, settingsChanged);
            ImGui.popStyleVar();
        }
        ImGui.end();
    }

    private void renderTabs(WorkspaceState state, GpuParticleSystem particles, CameraController camera,
            Runnable settingsChanged) {
        if (!ImGui.beginTabBar("##settings-tabs", TAB_BAR_FLAGS)) {
            return;
        }

        for (UISection section : UISection.values()) {
            int flags = state.selectionRequested(section) ? ImGuiTabItemFlags.SetSelected
                    : ImGuiTabItemFlags.None;
            if (ImGui.beginTabItem(section.label() + "##settings-tab-" + section.name(), flags)) {
                state.activate(section);
                renderTabContent(section, particles, camera, settingsChanged);
                ImGui.endTabItem();
            }
        }

        ImGui.endTabBar();
    }

    private void renderTabContent(UISection section, GpuParticleSystem particles, CameraController camera,
            Runnable settingsChanged) {
        if (ImGui.beginChild("##settings-content-" + section.name(), 0.0f, 0.0f, false)) {
            ImGui.dummy(0.0f, 4.0f);
            ImGui.textDisabled(sectionDescription(section));
            ImGui.separator();
            sections.render(section, particles, camera, settingsChanged);
        }
        ImGui.endChild();
    }

    private String sectionDescription(UISection section) {
        return switch (section) {
            case SIMULATION -> "World boundaries and motion";
            case PARTICLES -> "Population and spawning";
            case APPEARANCE -> "Rendering, glow, and trails";
            case CAMERA -> "Navigation and movement";
            case INTERACTIONS -> "Attraction matrix and group forces";
        };
    }

    int customSpawnAmount() {
        return sections.customSpawnAmount();
    }

    void setCustomSpawnAmount(int amount) {
        sections.setCustomSpawnAmount(amount);
    }

    float matrixEditStep() {
        return sections.matrixEditStep();
    }

    void setMatrixEditStep(float step) {
        sections.setMatrixEditStep(step);
    }
}
