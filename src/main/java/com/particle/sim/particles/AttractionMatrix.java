package com.particle.sim.particles;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public final class AttractionMatrix {
    private static final int HISTORY_LIMIT = 64;
    private static final float ANIMATED_MUTATION_PER_SECOND = 0.18f;
    private final float[] matrix;
    private final int maximumGroupCount;
    private final Random random = new Random();
    private final Deque<State> undoHistory = new ArrayDeque<>();
    private final Deque<State> redoHistory = new ArrayDeque<>();
    private int groupCount;
    private boolean animatedMutation;

    public AttractionMatrix(int groupCount, int maximumGroupCount) {
        this.maximumGroupCount = maximumGroupCount;
        this.groupCount = clampGroupCount(groupCount);
        this.matrix = new float[maximumGroupCount * maximumGroupCount];
        fillRandom();
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
        float randomness = clamp01(variation);
        switch (pattern == null ? AttractionPattern.RANDOM : pattern) {
            case RANDOM -> fillRandom();
            case STABLE -> generateStable(randomness);
            case SYMMETRIC -> generateSymmetric(randomness);
            case PREDATOR_PREY -> generatePredatorPrey(randomness);
            case ROCK_PAPER_SCISSORS -> generateRockPaperScissors(randomness);
            case TEAMS -> generateTeams(randomness);
            case SPARSE -> generateSparse(randomness);
            case MUTUALISM -> generateMutualism(randomness);
            case PARASITISM -> generateParasitism(randomness);
            case HIERARCHY -> generateHierarchy(randomness);
            case RING -> generateRing(randomness, true);
            case CHAIN -> generateRing(randomness, false);
        }
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
        mutateWithoutHistory(Math.max(0.0f, Math.min(1.0f, amount)));
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
                // Loading settings or a preset is an explicit replacement, not a bulk edit.
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
        return !undoHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    public void undo() {
        if (undoHistory.isEmpty()) {
            return;
        }
        redoHistory.push(snapshot());
        restore(undoHistory.pop());
        animatedMutation = false;
    }

    public void redo() {
        if (redoHistory.isEmpty()) {
            return;
        }
        pushLimited(undoHistory, snapshot());
        restore(redoHistory.pop());
        animatedMutation = false;
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
        mutateWithoutHistory(Math.min(0.05f, ANIMATED_MUTATION_PER_SECOND * deltaTime));
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

    void randomSeed(long seed) {
        random.setSeed(seed);
    }

    void clearHistory() {
        undoHistory.clear();
        redoHistory.clear();
    }

    private void fillRandom() {
        forEachCell(
                (row, column) -> {
                    float value = -0.6f + random.nextFloat() * 1.4f;
                    if (row == column) {
                        value += 0.25f;
                    }
                    set(row, column, value);
                });
    }

    private void generateStable(float variation) {
        generateFrom((row, column) -> row == column ? 0.55f : -0.04f, variation);
    }

    private void generateSymmetric(float variation) {
        for (int row = 0; row < groupCount; row++) {
            setPattern(row, row, 0.35f, variation);
            for (int column = row + 1; column < groupCount; column++) {
                float value = clamp(randomBase(-0.25f, 0.65f) + noise(variation));
                set(row, column, value);
                set(column, row, value);
            }
        }
    }

    private void generatePredatorPrey(float variation) {
        generateFrom(
                (row, column) -> {
                    if (row == column) {
                        return 0.2f;
                    }
                    if (column == (row + 1) % groupCount) {
                        return 0.8f;
                    }
                    if (row == (column + 1) % groupCount) {
                        return -0.7f;
                    }
                    return -0.08f;
                },
                variation);
    }

    private void generateRockPaperScissors(float variation) {
        generateFrom(
                (row, column) -> {
                    int rowFaction = row % 3;
                    int columnFaction = column % 3;
                    if (rowFaction == columnFaction) {
                        return 0.35f;
                    }
                    return columnFaction == (rowFaction + 1) % 3 ? 0.75f : -0.65f;
                },
                variation);
    }

    private void generateTeams(float variation) {
        int split = Math.max(1, (groupCount + 1) / 2);
        generateFrom((row, column) -> (row < split) == (column < split) ? 0.65f : -0.5f, variation);
    }

    private void generateSparse(float variation) {
        forEachCell(
                (row, column) -> {
                    if (row == column) {
                        setPattern(row, column, 0.3f, variation);
                    } else if (random.nextFloat() < 0.2f) {
                        set(row, column, clamp(randomBase(-0.8f, 0.8f) + noise(variation)));
                    } else {
                        set(row, column, 0.0f);
                    }
                });
    }

    private void generateMutualism(float variation) {
        for (int row = 0; row < groupCount; row++) {
            for (int column = row; column < groupCount; column++) {
                float base = row == column ? 0.45f : randomBase(0.3f, 0.8f);
                float value = clamp(base + noise(variation));
                set(row, column, value);
                set(column, row, value);
            }
        }
    }

    private void generateParasitism(float variation) {
        generateFrom(
                (row, column) -> {
                    if (row == column) {
                        return 0.15f;
                    }
                    if (row % 2 == 0 && column == Math.min(row + 1, groupCount - 1)) {
                        return 0.8f;
                    }
                    if (column % 2 == 0 && row == Math.min(column + 1, groupCount - 1)) {
                        return -0.65f;
                    }
                    return -0.05f;
                },
                variation);
    }

    private void generateHierarchy(float variation) {
        generateFrom(
                (row, column) -> {
                    if (row == column) {
                        return 0.3f;
                    }
                    float distance = Math.abs(row - column) / (float) Math.max(1, groupCount - 1);
                    return column < row ? 0.75f - 0.3f * distance : -0.55f + 0.2f * distance;
                },
                variation);
    }

    private void generateRing(float variation, boolean closed) {
        generateFrom(
                (row, column) -> {
                    if (row == column) {
                        return 0.25f;
                    }
                    boolean next =
                            column == row + 1 || (closed && row == groupCount - 1 && column == 0);
                    boolean previous =
                            row == column + 1 || (closed && column == groupCount - 1 && row == 0);
                    if (next) {
                        return 0.8f;
                    }
                    if (previous) {
                        return 0.3f;
                    }
                    return -0.12f;
                },
                variation);
    }

    private void generateFrom(ValueGenerator generator, float variation) {
        forEachCell(
                (row, column) -> setPattern(row, column, generator.value(row, column), variation));
    }

    private void setPattern(int row, int column, float base, float variation) {
        set(row, column, clamp(base + noise(variation)));
    }

    private float noise(float variation) {
        return (random.nextFloat() * 2.0f - 1.0f) * variation;
    }

    private float randomBase(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }

    private void mutateWithoutHistory(float amount) {
        if (amount <= 0.0f) {
            return;
        }
        forEachCell(
                (row, column) ->
                        set(
                                row,
                                column,
                                attraction(row, column)
                                        + (random.nextFloat() * 2.0f - 1.0f) * amount));
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
        pushLimited(undoHistory, snapshot());
        redoHistory.clear();
    }

    private State snapshot() {
        return new State(matrix.clone());
    }

    private void restore(State state) {
        System.arraycopy(state.values, 0, matrix, 0, matrix.length);
    }

    private static void pushLimited(Deque<State> history, State state) {
        history.push(state);
        while (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }
    }

    private int index(int groupA, int groupB) {
        return groupA * maximumGroupCount + groupB;
    }

    private static float clamp(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private int clampGroupCount(int groupCount) {
        return Math.max(1, Math.min(maximumGroupCount, groupCount));
    }

    private record State(float[] values) {}

    @FunctionalInterface
    private interface CellConsumer {
        void accept(int row, int column);
    }

    @FunctionalInterface
    private interface ValueGenerator {
        float value(int row, int column);
    }
}
