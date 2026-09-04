package com.particle.sim.particles.attraction;

import java.util.Random;

/** Generates and mutates attraction values without owning edit history. */
final class AttractionPatternGenerator {
    private final float[] matrix;
    private final int stride;
    private final Random random;

    AttractionPatternGenerator(float[] matrix, int stride, Random random) {
        this.matrix = matrix;
        this.stride = stride;
        this.random = random;
    }

    void generate(AttractionPattern pattern, float variation, int groupCount) {
        switch (pattern == null ? AttractionPattern.RANDOM : pattern) {
            case RANDOM -> fillRandom(groupCount);
            case STABLE ->
                    generateFrom(
                            (row, column) -> row == column ? 0.55f : -0.04f, variation, groupCount);
            case SYMMETRIC -> generateSymmetric(variation, groupCount);
            case PREDATOR_PREY -> generatePredatorPrey(variation, groupCount);
            case ROCK_PAPER_SCISSORS -> generateRockPaperScissors(variation, groupCount);
            case TEAMS -> generateTeams(variation, groupCount);
            case SPARSE -> generateSparse(variation, groupCount);
            case MUTUALISM -> generateMutualism(variation, groupCount);
            case PARASITISM -> generateParasitism(variation, groupCount);
            case HIERARCHY -> generateHierarchy(variation, groupCount);
            case RING -> generateRing(variation, groupCount, true);
            case CHAIN -> generateRing(variation, groupCount, false);
        }
    }

    void mutate(float amount, int groupCount) {
        if (amount <= 0.0f) {
            return;
        }
        forEachCell(
                groupCount,
                (row, column) ->
                        set(
                                row,
                                column,
                                value(row, column) + (random.nextFloat() * 2.0f - 1.0f) * amount));
    }

    private void fillRandom(int groupCount) {
        forEachCell(
                groupCount,
                (row, column) -> {
                    float generated = -0.6f + random.nextFloat() * 1.4f;
                    if (row == column) {
                        generated += 0.25f;
                    }
                    set(row, column, generated);
                });
    }

    private void generateSymmetric(float variation, int groupCount) {
        for (int row = 0; row < groupCount; row++) {
            setPattern(row, row, 0.35f, variation);
            for (int column = row + 1; column < groupCount; column++) {
                float generated = clamp(randomBase(-0.25f, 0.65f) + noise(variation));
                set(row, column, generated);
                set(column, row, generated);
            }
        }
    }

    private void generatePredatorPrey(float variation, int groupCount) {
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
                variation,
                groupCount);
    }

    private void generateRockPaperScissors(float variation, int groupCount) {
        generateFrom(
                (row, column) -> {
                    int rowFaction = row % 3;
                    int columnFaction = column % 3;
                    if (rowFaction == columnFaction) {
                        return 0.35f;
                    }
                    return columnFaction == (rowFaction + 1) % 3 ? 0.75f : -0.65f;
                },
                variation,
                groupCount);
    }

    private void generateTeams(float variation, int groupCount) {
        int split = Math.max(1, (groupCount + 1) / 2);
        generateFrom(
                (row, column) -> (row < split) == (column < split) ? 0.65f : -0.5f,
                variation,
                groupCount);
    }

    private void generateSparse(float variation, int groupCount) {
        forEachCell(
                groupCount,
                (row, column) -> {
                    if (row == column) {
                        setPattern(row, column, 0.3f, variation);
                    } else if (random.nextFloat() < 0.2f) {
                        set(row, column, randomBase(-0.8f, 0.8f) + noise(variation));
                    } else {
                        set(row, column, 0.0f);
                    }
                });
    }

    private void generateMutualism(float variation, int groupCount) {
        for (int row = 0; row < groupCount; row++) {
            for (int column = row; column < groupCount; column++) {
                float base = row == column ? 0.45f : randomBase(0.3f, 0.8f);
                float generated = clamp(base + noise(variation));
                set(row, column, generated);
                set(column, row, generated);
            }
        }
    }

    private void generateParasitism(float variation, int groupCount) {
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
                variation,
                groupCount);
    }

    private void generateHierarchy(float variation, int groupCount) {
        generateFrom(
                (row, column) -> {
                    if (row == column) {
                        return 0.3f;
                    }
                    float distance = Math.abs(row - column) / (float) Math.max(1, groupCount - 1);
                    return column < row ? 0.75f - 0.3f * distance : -0.55f + 0.2f * distance;
                },
                variation,
                groupCount);
    }

    private void generateRing(float variation, int groupCount, boolean closed) {
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
                variation,
                groupCount);
    }

    private void generateFrom(ValueGenerator generator, float variation, int groupCount) {
        forEachCell(
                groupCount,
                (row, column) -> setPattern(row, column, generator.value(row, column), variation));
    }

    private void setPattern(int row, int column, float base, float variation) {
        set(row, column, base + noise(variation));
    }

    private float noise(float variation) {
        return (random.nextFloat() * 2.0f - 1.0f) * variation;
    }

    private float randomBase(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }

    private void forEachCell(int groupCount, CellConsumer consumer) {
        for (int row = 0; row < groupCount; row++) {
            for (int column = 0; column < groupCount; column++) {
                consumer.accept(row, column);
            }
        }
    }

    private float value(int row, int column) {
        return matrix[row * stride + column];
    }

    private void set(int row, int column, float value) {
        matrix[row * stride + column] = clamp(value);
    }

    private static float clamp(float value) {
        return Float.isFinite(value) ? Math.max(-1.0f, Math.min(1.0f, value)) : 0.0f;
    }

    @FunctionalInterface
    private interface CellConsumer {
        void accept(int row, int column);
    }

    @FunctionalInterface
    private interface ValueGenerator {
        float value(int row, int column);
    }
}
