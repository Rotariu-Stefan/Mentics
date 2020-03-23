package com.mentics.math.matrix;

public class Matrix4 {
    public static float[] translationMat4(float x, float y, float z) {
        float[] result = identityMat4();
        result[12] = x;
        result[13] = y;
        result[14] = z;
        return result;
    }

    public static float[] translationMat4(float[] vec) {
        return translationMat4(vec[0], vec[1], vec[2]);
    }

    public static float[] identityMat4() {
        float[] result = new float[16];
        for (int i = 0; i < 4; i++) {
            result[i * 4 + i] = 1.0f;
        }
        return result;
    }
}
