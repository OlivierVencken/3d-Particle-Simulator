package com.particle.sim.settings;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebouncedSettingsSaverTest {
    @Test
    void savesOnlyAfterDebounceWindowExpires() {
        AtomicInteger saves = new AtomicInteger();
        DebouncedSettingsSaver saver = new DebouncedSettingsSaver(0.5, saves::incrementAndGet);

        saver.requestSave(10.0);
        saver.saveIfDue(10.49);

        assertEquals(0, saves.get());
        assertTrue(saver.hasPendingSave());

        saver.saveIfDue(10.5);

        assertEquals(1, saves.get());
        assertFalse(saver.hasPendingSave());
    }

    @Test
    void laterRequestsPushBackSaveTime() {
        AtomicInteger saves = new AtomicInteger();
        DebouncedSettingsSaver saver = new DebouncedSettingsSaver(0.5, saves::incrementAndGet);

        saver.requestSave(10.0);
        saver.requestSave(10.4);
        saver.saveIfDue(10.8);

        assertEquals(0, saves.get());

        saver.saveIfDue(10.9);

        assertEquals(1, saves.get());
    }

    @Test
    void flushSavesPendingChangeImmediately() {
        AtomicInteger saves = new AtomicInteger();
        DebouncedSettingsSaver saver = new DebouncedSettingsSaver(0.5, saves::incrementAndGet);

        saver.requestSave(10.0);
        saver.flush();
        saver.flush();

        assertEquals(1, saves.get());
        assertFalse(saver.hasPendingSave());
    }

    @Test
    void rejectsNegativeDebounceDuration() {
        assertThrows(IllegalArgumentException.class, () -> new DebouncedSettingsSaver(-0.1, () -> {
        }));
    }
}
