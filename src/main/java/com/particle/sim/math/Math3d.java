package com.particle.sim.math;

public final class Math3d {
    private Math3d() {
    }

    public static float[] perspective(float fovY, float aspect, float near, float far) {
        float f = 1.0f / (float) Math.tan(fovY * 0.5f);
        float range = 1.0f / (near - far);

        return new float[] {
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (far + near) * range, -1,
                0, 0, 2 * far * near * range, 0
        };
    }

    public static float[] lookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ) {
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;
        float fLength = length(fx, fy, fz);
        fx /= fLength;
        fy /= fLength;
        fz /= fLength;

        float upX = 0.0f;
        float upY = 1.0f;
        float upZ = 0.0f;

        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;
        float sLength = length(sx, sy, sz);
        sx /= sLength;
        sy /= sLength;
        sz /= sLength;

        float ux = sy * fz - sz * fy;
        float uy = sz * fx - sx * fz;
        float uz = sx * fy - sy * fx;

        return new float[] {
                sx, ux, -fx, 0,
                sy, uy, -fy, 0,
                sz, uz, -fz, 0,
                -dot(sx, sy, sz, eyeX, eyeY, eyeZ),
                -dot(ux, uy, uz, eyeX, eyeY, eyeZ),
                dot(fx, fy, fz, eyeX, eyeY, eyeZ),
                1
        };
    }

    public static float[] multiply(float[] left, float[] right) {
        float[] result = new float[16];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result[col * 4 + row] = left[row] * right[col * 4] +
                        left[4 + row] * right[col * 4 + 1] +
                        left[8 + row] * right[col * 4 + 2] +
                        left[12 + row] * right[col * 4 + 3];
            }
        }
        return result;
    }

    public static float[] normalize(float x, float y, float z) {
        float vectorLength = length(x, y, z);
        if (vectorLength == 0.0f) {
            return new float[] { 0.0f, 0.0f, 0.0f };
        }
        return new float[] { x / vectorLength, y / vectorLength, z / vectorLength };
    }

    public static float[] normalize(float[] vector) {
        return normalize(vector[0], vector[1], vector[2]);
    }

    public static float[] cross(float ax, float ay, float az, float bx, float by, float bz) {
        return new float[] {
                ay * bz - az * by,
                az * bx - ax * bz,
                ax * by - ay * bx
        };
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int previousPrime(int value) {
        int candidate = Math.max(3, value | 1);
        while (!isPrime(candidate)) {
            candidate -= 2;
        }
        return candidate;
    }

    public static boolean isPrime(int value) {
        if (value < 2) {
            return false;
        }
        if (value == 2) {
            return true;
        }
        if (value % 2 == 0) {
            return false;
        }
        for (int divisor = 3; (long) divisor * divisor <= value; divisor += 2) {
            if (value % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    private static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private static float dot(float ax, float ay, float az, float bx, float by, float bz) {
        return ax * bx + ay * by + az * bz;
    }
}
