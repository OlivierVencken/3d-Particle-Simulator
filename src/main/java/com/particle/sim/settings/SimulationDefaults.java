package com.particle.sim.settings;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.SpawnMode;

public final class SimulationDefaults {
    private SimulationDefaults() {
    }

    public static final int PARTICLE_COUNT = 65_536;
    public static final int MAX_PARTICLE_COUNT = 1_000_000;
    public static final int GROUP_COUNT = 6;
    public static final int MAX_GROUP_COUNT = 16;

    public static final float POINT_SIZE = 2.2f;
    public static final boolean FIXED_PARTICLE_SCREEN_SIZE = false;
    public static final EffectMode EFFECT_MODE = EffectMode.NONE;
    public static final float POINT_SIZE_REFERENCE_DISTANCE = 16.0f;
    public static final float BOUNDS = 4.0f;
    public static final float FORCE_FACTOR = 1.0f;
    public static final float VELOCITY_DAMPING = 0.965f;
    public static final float INTERACTION_RANGE = 0.95f;
    public static final float REPULSION_RADIUS = 0.3f;
    public static final float MAX_VELOCITY = 4.0f;
    public static final float BOUNDARY_BOUNCE = 0.65f;
    public static final boolean TOROIDAL_WRAP = false;
    public static final boolean DENSITY_REGULATION_ENABLED = false;
    public static final float DENSITY_LIMIT = 200.0f;
    public static final DistanceMetric DISTANCE_METRIC = DistanceMetric.EUCLIDEAN;
    public static final ColorMode COLOR_MODE = ColorMode.GROUP;
    public static final SpawnMode SPAWN_MODE = SpawnMode.RANDOM;

    public static final float CAMERA_SENSITIVITY = 0.002f;
    public static final float CAMERA_FLY_SPEED = 8.0f;

    public static final boolean PAUSED = false;
    public static final double SIMULATION_STEP_SECONDS = 1.0 / 60.0;
    public static final int FPS_CAP = 60;
    public static final int MIN_FPS_CAP = 30;
    public static final int MAX_FPS_CAP = 360;
    public static final float MATRIX_EDIT_STEP = 0.1f;
    public static final int CUSTOM_SPAWN_AMOUNT = 5_000;
}
