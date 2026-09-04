package com.particle.sim.particles.attraction;

import java.util.ArrayDeque;
import java.util.Deque;

/** Owns bounded undo and redo snapshots independently of matrix editing behavior. */
final class AttractionMatrixHistory {
    private final int limit;
    private final Deque<float[]> undo = new ArrayDeque<>();
    private final Deque<float[]> redo = new ArrayDeque<>();

    AttractionMatrixHistory(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("History limit must be positive");
        }
        this.limit = limit;
    }

    void remember(float[] values) {
        pushLimited(undo, values.clone());
        redo.clear();
    }

    boolean canUndo() {
        return !undo.isEmpty();
    }

    boolean canRedo() {
        return !redo.isEmpty();
    }

    boolean undo(float[] values) {
        if (undo.isEmpty()) {
            return false;
        }
        redo.push(values.clone());
        restore(values, undo.pop());
        return true;
    }

    boolean redo(float[] values) {
        if (redo.isEmpty()) {
            return false;
        }
        pushLimited(undo, values.clone());
        restore(values, redo.pop());
        return true;
    }

    void clear() {
        undo.clear();
        redo.clear();
    }

    private void pushLimited(Deque<float[]> history, float[] values) {
        history.push(values);
        while (history.size() > limit) {
            history.removeLast();
        }
    }

    private static void restore(float[] target, float[] snapshot) {
        System.arraycopy(snapshot, 0, target, 0, target.length);
    }
}
