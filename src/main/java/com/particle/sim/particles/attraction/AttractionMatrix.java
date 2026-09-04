package com.particle.sim.particles.attraction;

import java.util.Random;

/** Editable attraction values with bounded history and optional continuous mutation. */
public final class AttractionMatrix {
    private static final int HISTORY_LIMIT = 64;
    private static final float ANIMATED_MUTATION_PER_SECOND = 0.18f;
    private final float[] matrix;
    private final int maximumGroupCount;
    private final Random random = new Random();
    private final AttractionMatrixHistory history = new AttractionMatrixHistory(HISTORY_LIMIT);
    private final AttractionPatternGenerator patternGenerator;
    private int groupCount;
    private boolean animatedMutation;

    public AttractionMatrix(int groupCount, int maximumGroupCount) {
        this.maximumGroupCount = maximumGroupCount;
        this.groupCount = clampGroupCount(groupCount);
        matrix = new float[maximumGroupCount * maximumGroupCount];
        patternGenerator = new AttractionPatternGenerator(matrix, maximumGroupCount, random);
        patternGenerator.generate(AttractionPattern.RANDOM, 0.0f, this.groupCount);
    }

    public float attraction(int groupA, int groupB) {
        return matrix[index(groupA, groupB)];
    }

    public void attraction(int groupA, int groupB, float value) {
        rememberState();
        set(groupA, groupB, value);
    }

    public void adjustAttraction(int groupA, int groupB, float delta) {
        attraction(groupA, groupB, attraction(groupA, groupB) + delta);
    }

    public void randomize() {
        generate(AttractionPattern.RANDOM, 0.0f);
    }

    public void generate(AttractionPattern pattern, float variation) {
        rememberState();
        patternGenerator.generate(pattern, clamp01(variation), groupCount);
    }

    public void zero() {
        rememberState();
        forEachCell((row, column) -> set(row, column, 0.0f));
    }

    public void symmetrize() {
        rememberState();
        for (int row = 0; row < groupCount; row++) {
            for (int column = row + 1; column < groupCount; column++) {
                float value = (attraction(row, column) + attraction(column, row)) * 0.5f;
                set(row, column, value);
                set(column, row, value);
            }
        }
    }

    public void invert() {
        rememberState();
        forEachCell((row, column) -> set(row, column, -attraction(row, column)));
    }

    public void mutate(float amount) {
        rememberState();
        patternGenerator.mutate(clamp01(amount), groupCount);
    }

    public void normalize() {
        float maximum = 0.0f;
        for (int row = 0; row < groupCount; row++) {
            for (int column = 0; column < groupCount; column++) {
                maximum = Math.max(maximum, Math.abs(attraction(row, column)));
            }
        }
        if (maximum <= 0.000001f) {
            return;
        }
        rememberState();
        float scale = 1.0f / maximum;
        forEachCell((row, column) -> set(row, column, attraction(row, column) * scale));
    }

    public void activeValues(float[] values) {
        int valueIndex = 0;
        for (int row = 0; row < groupCount; row++) {
            for (int column = 0; column < groupCount; column++) {
                if (valueIndex < values.length) {
                    set(row, column, values[valueIndex]);
                }
                valueIndex++;
            }
        }
        animatedMutation = false;
        clearHistory();
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    public void undo() {
        if (history.undo(matrix)) {
            animatedMutation = false;
        }
    }

    public void redo() {
        if (history.redo(matrix)) {
            animatedMutation = false;
        }
    }

    public boolean animatedMutation() {
        return animatedMutation;
    }

    public void animatedMutation(boolean enabled) {
        if (animatedMutation == enabled) {
            return;
        }
        if (enabled) {
            rememberState();
        }
        animatedMutation = enabled;
    }

    public void advanceAnimation(float deltaTime) {
        if (!animatedMutation || !Float.isFinite(deltaTime) || deltaTime <= 0.0f) {
            return;
        }
        patternGenerator.mutate(
                Math.min(0.05f, ANIMATED_MUTATION_PER_SECOND * deltaTime), groupCount);
    }

    public float[] values() {
        return matrix;
    }

    public int groupCount() {
        return groupCount;
    }

    public void groupCount(int groupCount) {
        this.groupCount = clampGroupCount(groupCount);
    }

    public int maximumGroupCount() {
        return maximumGroupCount;
    }

    public void randomSeed(long seed) {
        random.setSeed(seed);
    }

    public void clearHistory() {
        history.clear();
    }

    private void forEachCell(CellConsumer consumer) {
        for (int row = 0; row < groupCount; row++) {
            for (int column = 0; column < groupCount; column++) {
                consumer.accept(row, column);
            }
        }
    }

    private void set(int row, int column, float value) {
        matrix[index(row, column)] = clamp(value);
    }

    private void rememberState() {
        history.remember(matrix);
    }

    private int index(int groupA, int groupB) {
        return groupA * maximumGroupCount + groupB;
    }

    private static float clamp(float value) {
        return Float.isFinite(value) ? Math.max(-1.0f, Math.min(1.0f, value)) : 0.0f;
    }

    private static float clamp01(float value) {
        return Float.isFinite(value) ? Math.max(0.0f, Math.min(1.0f, value)) : 0.0f;
    }

    private int clampGroupCount(int groupCount) {
        return Math.max(1, Math.min(maximumGroupCount, groupCount));
    }

    @FunctionalInterface
    private interface CellConsumer {
        void accept(int row, int column);
    }
}
