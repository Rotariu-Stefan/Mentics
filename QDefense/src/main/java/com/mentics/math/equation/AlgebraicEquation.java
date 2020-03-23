package com.mentics.math.equation;

import static java.lang.Float.isFinite;
import static java.lang.Math.*;


/**
 * Methods for solving algebraic equations
 */
public class AlgebraicEquation {

    public static float[] solveQuadratic(float a, float b, float c) {
        float[] result;
        if (a == 0) {
            if (b == 0) {
                result = new float[0];
            } else {
                result = new float[1];
                result[0] = -c / b;
            }
        } else {
            float D = b * b - 4 * a * c;
            if (D > 0) {
                result = new float[2];
                result[0] = (-b + (float)Math.sqrt(D)) / (2 * a);
                result[1] = (-b - (float)Math.sqrt(D)) / (2 * a);
            } else if (D == 0) {
                result = new float[1];
                result[0] = -b / (2 * a);
            } else /* if (D < 0) */{
                result = new float[0];
            }
        }
        return result;
    }

    /**
     * Finds real roots of a cubic equation ax^3 + bx^2 + cx + d = 0
     * http://en.wikipedia.org/wiki/Cubic_function#General_formula_for_roots
     *
     * @param a
     *            should not be zero
     * @param b
     * @param c
     * @param d
     * @return an array of distinct real roots.
     */
    public static float[] solveCubic(float a, float b, float c, float d) {
        if (a == 0) {
            return solveQuadratic(b, c, d);
        }
        float[] result = null;

        //Check that coefficients are all finite
        if (!(isFinite(a) && isFinite(b) && isFinite(c) && isFinite(d))) {
            throw new RuntimeException();
        }

        float D0, D1, D;
        while (true) {
            D0 = b * b - 3 * a * c;
            D1 = 2 * b * b * b - 9 * a * b * c + 27 * a * a * d;
            D = 18 * a * b * c * d - 4 * b * b * b * d + b * b * c * c - 4 * a * c * c * c - 27 * a * a * d * d;
            if (isFinite(D0) && isFinite(D1) && isFinite(D)) {
                break;
            } else {
                float maxAbs = max(max(max(abs(a), abs(b)), abs(c)), abs(d));
                a /= maxAbs;
                b /= maxAbs;
                c /= maxAbs;
                d /= maxAbs;
            }
        }

        if (D == 0) {
            if (D0 == 0) {
                // All three roots are equal
                result = new float[1];
                result[0] = -b / 3 * a;
            } else {
                // Double root and simple root
                result = new float[2];
                result[0] = (9 * a * d - b * c) / (2 * D0);
                result[1] = (4 * a * b * c - 9 * a * a * d - b * b * b) / (a * D0);
            }
        } else if (D > 0) {
            // Three distinct roots
            result = new float[3];
            float j = 4 * D0 * D0 * D0 - D1 * D1; // >0
            float C_mod = (float)Math.cbrt(1f / 2f * Math.sqrt(D1 * D1 + j));
            float C_arg = (float)Math.atan2(Math.sqrt(j), D1) / 3;

            for (int i = 0; i < 3; i++) {
                result[i] = (float)(-1 / (3 * a) * (b + 2 * C_mod * Math.cos(C_arg + 2 * Math.PI * i / 3)));
            }
        } else if (D < 0) {
            // One real root and 2 complex roots
            result = new float[1];

            float j = D1 * D1 - 4 * D0 * D0 * D0; // >0
            float C = (float)Math.cbrt((D1 + Math.sqrt(j)) / 2);

            result[0] = -1 / (3 * a) * (b + C + D0 / C);
        }

        return result;
    }

    /**
     * Finds distinct real roots of quartic equation ax^4 + bx^3 + cx^2 + dx + e = 0;
     * http://en.wikipedia.org/wiki/Quartic_function#Converting_to_a_depressed_quartic
     *
     * @param a
     *            if a = 0 solveCubic will be used
     * @param b
     * @param c
     * @param d
     * @param e
     * @return an array of distinct real roots.
     */
    public static float[] solveQuartic(float a, float b, float c, float d, float e) {
        if (a == 0) {
            return solveCubic(b, c, d, e);
        }
        float[] result;

        // We will convert to a depressed quartic
        float p = (8 * a * c - 3 * b * b) / (8 * a * a);
        float q = (float)((pow(b, 3) - 4 * a * b * c + 8 * pow(a, 2) * d) / (8 * pow(a, 3)));
        float r =
                (float)((-3 * pow(b, 4) + 256 * pow(a, 3) * e - 64 * pow(a, 2) * b * d + 16 * a * pow(b, 2) * c) / (256 * pow(
                        a, 4)));

        if (q == 0) {
            // biquadratic equation
            float D = p * p - 4 * r;
            if (D > 0) {
                float z1 = (-p + (float)sqrt(D)) / 2;
                float z2 = (-p - (float)sqrt(D)) / 2;
                if (z1 < 0) {
                    result = new float[0];
                } else if (z1 == 0) {
                    result = new float[1];
                    result[0] = 0;
                } else if (z2 < 0) {
                    result = new float[2];
                    result[0] = (float)sqrt(z1);
                    result[1] = (float)-sqrt(z1);
                } else if (z2 == 0) {
                    result = new float[3];
                    result[0] = (float)sqrt(z1);
                    result[1] = (float)-sqrt(z1);
                    result[2] = 0;
                } else /* if (z2 > 0) */{
                    result = new float[4];
                    result[0] = (float)sqrt(z1);
                    result[1] = (float)-sqrt(z1);
                    result[2] = (float)sqrt(z2);
                    result[3] = (float)-sqrt(z2);
                }
            } else if (D == 0) {
                float z = -p / 2;
                if (z > 0) {
                    result = new float[2];
                    result[0] = (float)sqrt(z);
                    result[1] = (float)-sqrt(z);
                } else if (z == 0) {
                    result = new float[1];
                    result[0] = 0;
                } else {
                    result = new float[0];
                }
            } else {
                result = new float[0];
            }
        } else {
            float y = solveCubic(1, 5f * p / 2, 2 * p * p - r, (p * p * p / 2 - p * r / 2 - q * q / 8))[0];
            float j = p + 2 * y;
            if (j > 0) {
                float sj = (float)sqrt(j);
                int n = 0;// number of distinct roots
                float[] u = new float[4];
                float t1 = -(3 * p + 2 * y + 2 * q / sj);
                float t2 = -(3 * p + 2 * y - 2 * q / sj);
                if (t1 == 0) {
                    u[n++] = sj / 2;
                } else if (t1 > 0) {
                    u[n++] = (sj + (float)sqrt(t1)) / 2;
                    u[n++] = (sj - (float)sqrt(t1)) / 2;
                }
                if (t2 == 0) {
                    u[n++] = -sj / 2;
                } else if (t2 > 0) {
                    u[n++] = (-sj + (float)sqrt(t2)) / 2;
                    u[n++] = (-sj - (float)sqrt(t2)) / 2;
                }
                result = uniqueElements(u, n);
            } else /*if (j < 0) */{
                // We will only have real root if certain condition is met
                if (pow(q, 2) + 2 * pow(p + 2 * y, 2) * (p + y) == 0) {
                    result = new float[1];
                    result[0] = q / (2 * (p + 2 * y));
                } else {
                    result = new float[0];
                }
            }
        }

        for (int i = 0; i < result.length; i++) {
            // convert back to original equation
            result[i] += -b / (4 * a);
        }
        return result;
    }

    // TODO move somewhere else
    public static float[] uniqueElements(float[] in, int inLength) {
        float[] result;
        int n = inLength;
        // check if we still have equal roots
        for (int i = 0; i < inLength; i++) {
            for (int k = 0; k < i; k++) {
                if (in[i] == in[k]) {
                    n--;
                    break;
                }
            }
        }
        result = new float[n];
        int count = 0;
        outer_loop:
        for (int i = 0; i < inLength; i++) {
            for (int k = 0; k < count; k++) {
                if (result[k] == in[i]) {
                    continue outer_loop;
                }
            }
            result[count++] = in[i];
        }

        return result;
    }

    public static float[] uniqueElements(float[] in) {
        return uniqueElements(in, in.length);
    }
}
