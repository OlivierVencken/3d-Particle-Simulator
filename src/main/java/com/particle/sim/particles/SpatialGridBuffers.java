package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL43C.GL_INT;
import static org.lwjgl.opengl.GL43C.GL_R32I;
import static org.lwjgl.opengl.GL43C.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL43C.glBindBuffer;
import static org.lwjgl.opengl.GL43C.glBufferData;
import static org.lwjgl.opengl.GL43C.glClearBufferData;
import static org.lwjgl.opengl.GL43C.glDeleteBuffers;
import static org.lwjgl.opengl.GL43C.glGenBuffers;

final class SpatialGridBuffers {
    static final int MAX_PARTICLES_PER_CELL = 128;

    private int dataSsbo;
    private int countsSsbo;
    private int keysSsbo;
    private int mapSize;

    void init(int initialMapSize) {
        dataSsbo = glGenBuffers();
        countsSsbo = glGenBuffers();
        keysSsbo = glGenBuffers();
        allocate(initialMapSize);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    void ensureCapacity(int desiredMapSize) {
        if (desiredMapSize != mapSize) {
            allocate(desiredMapSize);
        }
    }

    private void allocate(int newMapSize) {
        mapSize = newMapSize;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, dataSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) mapSize * MAX_PARTICLES_PER_CELL * Integer.BYTES,
                GL_DYNAMIC_DRAW);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, countsSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) mapSize * Integer.BYTES, GL_STREAM_DRAW);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, keysSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) mapSize * Integer.BYTES, GL_STREAM_DRAW);
    }

    void clear() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, countsSsbo);
        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R32I, GL_RED_INTEGER, GL_INT, new int[] { 0 });
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, keysSsbo);
        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R32I, GL_RED_INTEGER, GL_INT, new int[] { 0 });
    }

    int dataSsbo() {
        return dataSsbo;
    }

    int countsSsbo() {
        return countsSsbo;
    }

    int keysSsbo() {
        return keysSsbo;
    }

    int mapSize() {
        return mapSize;
    }

    void dispose() {
        glDeleteBuffers(dataSsbo);
        glDeleteBuffers(countsSsbo);
        glDeleteBuffers(keysSsbo);
    }
}
