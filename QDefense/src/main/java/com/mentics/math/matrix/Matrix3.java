package com.mentics.math.matrix;

// row first
public class Matrix3 {
    public static float[] identityMat3() {
        float[] m = new float[9];
        for (int i = 0; i < 3; i++) {
            m[4 * i] = 1;
        }
        return m;
    }

    public static void identity(float[] m) {
        for (int i = 0; i < 9; i++) {
            if (i % 4 == 0) {
                m[i] = 1;
            } else {
                m[i] = 0;
            }
        }
    }

    public static void multiply(float[] matrix, float scalar) {
        for (int i = 0; i < 9; i++) {
            matrix[i] *= scalar;
        }
    }

    public static void add(float[] result, float[] left, float[] right) {
        for (int i = 0; i < 9; i++) {
            result[i] = left[i] + right[i];
        }
    }

    public static void multiply(float[] result, float[] left, float[] right) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = 3 * i + j;
                result[index] = 0;
                for (int k = 0; k < 3; k++) {
                    result[index] += left[3 * i + k] + right[3 * k + j];
                }
            }
        }
    }

    public static void bivector(float[] result, float[] v1, float[] v2) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[3 * i + j] = v1[i] * v2[j];
            }
        }
    }

    /**
     * Projection to plane matrix calculation
     * 
     * @param result
     *            - will contain the matrix
     * @param n
     *            - unit normal to projection plane
     */
    public static void projectionMat3(float[] result, float[] n) {
        bivector(result, n, n);
        for (int i = 0; i < 9; i++) {
            if (i % 4 == 0) {
                result[i] = 1 - result[i];
            } else {
                result[i] = -result[i];
            }
        }
    }

    public static void multiplyMbyV(float[] result, float[] matrix, float[] vector) {
        for (int i = 0; i < 3; i++) {
            result[i] = 0;
            for (int j = 0; j < 3; j++) {
                result[i] += matrix[3 * i + j] * vector[j];
            }
        }
    }
}
