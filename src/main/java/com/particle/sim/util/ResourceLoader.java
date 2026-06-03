package com.particle.sim.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.lwjgl.BufferUtils;

public class ResourceLoader {

    public static ByteBuffer loadBytes(String path) {
        try (InputStream stream = ResourceLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing resource: " + path);
            }
            byte[] bytes = stream.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read resource: " + path, e);
        }
    }

    public static byte[] loadBytesArray(String path) {
        try (InputStream stream = ResourceLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing resource: " + path);
            }
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read resource: " + path, e);
        }
    }

    public static String loadString(String path) {
        try (InputStream stream = ResourceLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read resource: " + path, e);
        }
    }
}
