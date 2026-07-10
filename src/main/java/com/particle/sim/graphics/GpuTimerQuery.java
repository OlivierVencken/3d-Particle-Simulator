package com.particle.sim.graphics;

import static org.lwjgl.opengl.GL43C.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL43C.GL_QUERY_RESULT_AVAILABLE;
import static org.lwjgl.opengl.GL43C.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL43C.glBeginQuery;
import static org.lwjgl.opengl.GL43C.glDeleteQueries;
import static org.lwjgl.opengl.GL43C.glEndQuery;
import static org.lwjgl.opengl.GL43C.glGenQueries;
import static org.lwjgl.opengl.GL43C.glGetQueryObjecti;
import static org.lwjgl.opengl.GL43C.glGetQueryObjectui64;

public final class GpuTimerQuery {
    private static final int QUERY_RING_SIZE = 4;
    private static final double NANOSECONDS_PER_MILLISECOND = 1_000_000.0;

    private final int[] queries = new int[QUERY_RING_SIZE];
    private final boolean[] pending = new boolean[QUERY_RING_SIZE];
    private int writeIndex;
    private int readIndex;
    private boolean active;
    private double latestMilliseconds = -1.0;

    public GpuTimerQuery() {
        for (int i = 0; i < queries.length; i++) {
            queries[i] = glGenQueries();
        }
    }

    public void begin() {
        poll();
        if (pending[writeIndex]) {
            active = false;
            return;
        }

        glBeginQuery(GL_TIME_ELAPSED, queries[writeIndex]);
        active = true;
    }

    public void end() {
        if (!active) {
            return;
        }

        glEndQuery(GL_TIME_ELAPSED);
        pending[writeIndex] = true;
        writeIndex = (writeIndex + 1) % queries.length;
        active = false;
    }

    public double latestMilliseconds() {
        poll();
        return latestMilliseconds;
    }

    public void dispose() {
        for (int query : queries) {
            glDeleteQueries(query);
        }
    }

    private void poll() {
        while (pending[readIndex]
                && glGetQueryObjecti(queries[readIndex], GL_QUERY_RESULT_AVAILABLE) != 0) {
            latestMilliseconds = glGetQueryObjectui64(queries[readIndex], GL_QUERY_RESULT)
                    / NANOSECONDS_PER_MILLISECOND;
            pending[readIndex] = false;
            readIndex = (readIndex + 1) % queries.length;
        }
    }
}
