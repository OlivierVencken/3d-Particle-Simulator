package com.particle.sim.ui.components;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NSVGImage;

import com.particle.sim.util.ResourceLoader;

import static org.lwjgl.nanovg.NanoSVG.nsvgCreateRasterizer;
import static org.lwjgl.nanovg.NanoSVG.nsvgDelete;
import static org.lwjgl.nanovg.NanoSVG.nsvgDeleteRasterizer;
import static org.lwjgl.nanovg.NanoSVG.nsvgParse;
import static org.lwjgl.nanovg.NanoSVG.nsvgRasterize;
import static org.lwjgl.opengl.GL43C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL43C.GL_LINEAR;
import static org.lwjgl.opengl.GL43C.GL_RGBA;
import static org.lwjgl.opengl.GL43C.GL_RGBA8;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL43C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL43C.glBindTexture;
import static org.lwjgl.opengl.GL43C.glDeleteTextures;
import static org.lwjgl.opengl.GL43C.glGenTextures;
import static org.lwjgl.opengl.GL43C.glTexImage2D;
import static org.lwjgl.opengl.GL43C.glTexParameteri;

public final class SvgIconTexture {
    private final String resourcePath;
    private final int rasterSize;
    private int textureId;

    public SvgIconTexture(String resourcePath, int rasterSize) {
        this.resourcePath = resourcePath;
        this.rasterSize = rasterSize;
    }

    public int textureId() {
        if (textureId == 0) {
            textureId = loadTexture();
        }
        return textureId;
    }

    public void dispose() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    private int loadTexture() {
        ByteBuffer pixels = rasterizeResource(resourcePath, rasterSize);

        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, rasterSize, rasterSize, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);
        return texture;
    }

    public static ByteBuffer rasterizeResource(String resourcePath, int rasterSize) {
        NSVGImage image = nsvgParse(ResourceLoader.loadString(resourcePath), "px", 96.0f);
        if (image == null) {
            throw new IllegalStateException("Could not parse SVG resource: " + resourcePath);
        }

        long rasterizer = nsvgCreateRasterizer();
        if (rasterizer == 0L) {
            nsvgDelete(image);
            throw new IllegalStateException("Could not create SVG rasterizer");
        }

        ByteBuffer pixels = BufferUtils.createByteBuffer(rasterSize * rasterSize * 4);
        try {
            float scale = Math.min(rasterSize / image.width(), rasterSize / image.height());
            float offsetX = (rasterSize - image.width() * scale) * 0.5f;
            float offsetY = (rasterSize - image.height() * scale) * 0.5f;
            nsvgRasterize(rasterizer, image, offsetX, offsetY, scale,
                    pixels, rasterSize, rasterSize, rasterSize * 4);
        } finally {
            nsvgDeleteRasterizer(rasterizer);
            nsvgDelete(image);
        }
        return pixels;
    }
}
