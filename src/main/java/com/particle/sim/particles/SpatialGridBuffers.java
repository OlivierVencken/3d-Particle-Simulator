package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_COPY_READ_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_COPY_WRITE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL43C.GL_INT;
import static org.lwjgl.opengl.GL43C.GL_R32I;
import static org.lwjgl.opengl.GL43C.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL43C.glBindBuffer;
import static org.lwjgl.opengl.GL43C.glBufferData;
import static org.lwjgl.opengl.GL43C.glClearBufferSubData;
import static org.lwjgl.opengl.GL43C.glCopyBufferSubData;
import static org.lwjgl.opengl.GL43C.glDeleteBuffers;
import static org.lwjgl.opengl.GL43C.glGenBuffers;
import static org.lwjgl.opengl.GL43C.glGetBufferSubData;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.util.ArrayList;
import java.util.List;
import java.nio.IntBuffer;

final class SpatialGridBuffers {
    static final int SCAN_ELEMENTS_PER_GROUP = 512;

    private int particleIdsSsbo;
    private int countsSsbo;
    private int offsetsSsbo;
    private int cursorsSsbo;
    private int particleCapacity;
    private int cellCapacity;
    private final List<Integer> blockSumsSsbos = new ArrayList<>();
    private final List<Integer> scannedBlockSumsSsbos = new ArrayList<>();
    private final List<Integer> scanScratchElementCounts = new ArrayList<>();

    void init(int initialParticleCount, int initialCellCount) {
        particleIdsSsbo = glGenBuffers();
        countsSsbo = glGenBuffers();
        offsetsSsbo = glGenBuffers();
        cursorsSsbo = glGenBuffers();
        ensureCapacity(initialParticleCount, initialCellCount);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    void ensureCapacity(int requiredParticles, int requiredCells) {
        int desiredParticleCapacity = grownCapacity(particleCapacity, requiredParticles);
        if (desiredParticleCapacity != particleCapacity) {
            particleCapacity = desiredParticleCapacity;
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, particleIdsSsbo);
            glBufferData(GL_SHADER_STORAGE_BUFFER, (long) particleCapacity * Integer.BYTES, GL_DYNAMIC_DRAW);
        }

        int desiredCellCapacity = grownCapacity(cellCapacity, requiredCells);
        if (desiredCellCapacity != cellCapacity) {
            cellCapacity = desiredCellCapacity;
            allocateCellBuffer(countsSsbo);
            allocateCellBuffer(offsetsSsbo);
            allocateCellBuffer(cursorsSsbo);
            allocateScanScratch();
        }
    }

    void clearCounts(int cellCount) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, countsSsbo);
        glClearBufferSubData(GL_SHADER_STORAGE_BUFFER, GL_R32I, 0, (long) cellCount * Integer.BYTES,
                GL_RED_INTEGER, GL_INT, new int[] { 0 });
    }

    void copyOffsetsToCursors(int cellCount) {
        glBindBuffer(GL_COPY_READ_BUFFER, offsetsSsbo);
        glBindBuffer(GL_COPY_WRITE_BUFFER, cursorsSsbo);
        glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0,
                (long) cellCount * Integer.BYTES);
    }

    int particleIdsSsbo() {
        return particleIdsSsbo;
    }

    int countsSsbo() {
        return countsSsbo;
    }

    int offsetsSsbo() {
        return offsetsSsbo;
    }

    int cursorsSsbo() {
        return cursorsSsbo;
    }

    int blockSumsSsbo(int level) {
        return blockSumsSsbos.get(level);
    }

    int scannedBlockSumsSsbo(int level) {
        return scannedBlockSumsSsbos.get(level);
    }

    int scanLevelCount() {
        return blockSumsSsbos.size();
    }

    long allocatedBytes() {
        long bytes = (long) particleCapacity * Integer.BYTES + (long) cellCapacity * 3L * Integer.BYTES;
        for (int elementCount : scanScratchElementCounts) {
            bytes += (long) elementCount * 2L * Integer.BYTES;
        }
        return bytes;
    }

    int[] readCounts(int cellCount) {
        return readIntBuffer(countsSsbo, cellCount);
    }

    int[] readParticleIds(int particleCount) {
        return readIntBuffer(particleIdsSsbo, particleCount);
    }

    void dispose() {
        glDeleteBuffers(particleIdsSsbo);
        glDeleteBuffers(countsSsbo);
        glDeleteBuffers(offsetsSsbo);
        glDeleteBuffers(cursorsSsbo);
        deleteScanScratch();
    }

    private void allocateCellBuffer(int buffer) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) cellCapacity * Integer.BYTES, GL_STREAM_DRAW);
    }

    private void allocateScanScratch() {
        deleteScanScratch();

        int levelElements = cellCapacity;
        while (true) {
            int groups = Math.ceilDiv(levelElements, SCAN_ELEMENTS_PER_GROUP);
            blockSumsSsbos.add(allocateIntBuffer(groups));
            scannedBlockSumsSsbos.add(allocateIntBuffer(groups));
            scanScratchElementCounts.add(groups);
            if (groups <= 1) {
                return;
            }
            levelElements = groups;
        }
    }

    private int allocateIntBuffer(int elementCount) {
        int buffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) Math.max(elementCount, 1) * Integer.BYTES, GL_STREAM_DRAW);
        return buffer;
    }

    private static int[] readIntBuffer(int buffer, int elementCount) {
        IntBuffer data = memAllocInt(elementCount);
        try {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
            glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
            int[] result = new int[elementCount];
            data.get(result);
            return result;
        } finally {
            memFree(data);
        }
    }

    private void deleteScanScratch() {
        for (int buffer : blockSumsSsbos) {
            glDeleteBuffers(buffer);
        }
        for (int buffer : scannedBlockSumsSsbos) {
            glDeleteBuffers(buffer);
        }
        blockSumsSsbos.clear();
        scannedBlockSumsSsbos.clear();
        scanScratchElementCounts.clear();
    }

    private static int grownCapacity(int currentCapacity, int requiredCapacity) {
        int required = Math.max(requiredCapacity, 1);
        int capacity = Math.max(currentCapacity, 1);
        while (capacity < required) {
            int grown = capacity + Math.max(capacity / 2, 1);
            if (grown < 0 || grown > Integer.MAX_VALUE / Integer.BYTES) {
                return required;
            }
            capacity = grown;
        }
        return capacity;
    }
}
