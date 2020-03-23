package com.mentics.math.vector;

import static com.mentics.qd.items.ItemUtil.EPS_CALC;
import static java.lang.Float.isFinite;

import java.util.Random;

import com.mentics.math._float.FloatUtil;

/**
 * All basic 3-place vector operations
 */
public class VectorUtil {

	public static final float[] ZERO = { 0, 0, 0 };

	public static void zero(float[] v) {
		v[0] = 0f;
		v[1] = 0f;
		v[2] = 0f;
	}

	public static boolean isSame(float[] v0, float[] v1) {
		return isSame(v0, v1, EPS_CALC);
	}

	public static boolean isSame(float[] v0, float[] v1, float eps) {
		return FloatUtil.isSame(v0[0], v1[0], eps) && FloatUtil.isSame(v0[1], v1[1], eps)
				&& FloatUtil.isSame(v0[2], v1[2], eps);
	}

	// public static boolean isZero(float[] v) {
	// return v[0] == 0f && v[1] == 0f && v[2] == 0f;
	// }
	public static boolean isZero(float[] v) {
		return FloatUtil.isZero(v[0]) && FloatUtil.isZero(v[1]) && FloatUtil.isZero(v[2]);
	}

	public static void subtractInto(float[] target, float[] subtrahend) {
		target[0] -= subtrahend[0];
		target[1] -= subtrahend[1];
		target[2] -= subtrahend[2];
	}

	public static void addInto(float[] target, float[] add) {
		target[0] += add[0];
		target[1] += add[1];
		target[2] += add[2];
	}

	/**
	 * If magnitude is zero, sets value to zero vector.
	 *
	 * @return magnitude
	 */
	public static float normalize(float[] value) {
		final float mag = magnitude(value);
		if (FloatUtil.isZero(mag, EPS_CALC)) {
			zero(value);
			return 0f;
		}
		value[0] /= mag;
		value[1] /= mag;
		value[2] /= mag;
		return mag;
	}

	public static void set(float[] target, float[] value) {
		target[0] = value[0];
		target[1] = value[1];
		target[2] = value[2];
	}

	public static void multiplyInto(float[] target, float value) {
		target[0] *= value;
		target[1] *= value;
		target[2] *= value;
	}

	/**
	 * Adding these "new" methods as convenience, but might want to optimize
	 * away there use at some point. Renaming according to new conventions at 19
	 * 01 2015
	 */
	public static float[] multiply(float[] value, float mult) {
		float[] target = new float[3];
		target[0] = value[0] * mult;
		target[1] = value[1] * mult;
		target[2] = value[2] * mult;
		return target;
	}

	public static float[] subtract(float[] value, float[] sub) {
		float[] target = new float[3];
		target[0] = value[0] - sub[0];
		target[1] = value[1] - sub[1];
		target[2] = value[2] - sub[2];
		return target;
	}

	public static float[] add(float[] value, float[] sub) {
		float[] target = new float[3];
		target[0] = value[0] + sub[0];
		target[1] = value[1] + sub[1];
		target[2] = value[2] + sub[2];
		return target;
	}

	public static float[] cross(float[] left, float[] right) {
		float[] target = new float[3];
		set(target, left);
		crossInto(target, right);
		return target;
	}

	public static float magnitude(float[] value) {
		return (float) Math.sqrt(value[0] * value[0] + value[1] * value[1]
				+ value[2] * value[2]);
	}

	public static float dot(float[] first, float[] second) {
		return (first[0] * second[0] + first[1] * second[1] + first[2]
				* second[2]);
	}

	public static void crossInto(float[] left, float[] right) {
		final float x = left[1] * right[2] - left[2] * right[1], y = left[2]
				* right[0] - left[0] * right[2], z = left[0] * right[1]
				- left[1] * right[0];
		left[0] = x;
		left[1] = y;
		left[2] = z;
	}

	public static float distance(float[] from, float[] to) {
		return (float) Math.sqrt(distance2(from, to));
	}

	public static float distance2(float[] from, float[] to) {
		final float x = from[0] - to[0];
		final float y = from[1] - to[1];
		final float z = from[2] - to[2];
		return (x * x + y * y + z * z);
	}

	private static Random random = new Random(1);

	public static float[] randomize(float[] v, float scale) {
		v[0] = scale * (random.nextFloat() - 0.5f);
		v[1] = scale * (random.nextFloat() - 0.5f);
		v[2] = scale * (random.nextFloat() - 0.5f);
		return v;
	}

	/**
	 *
	 * @param radius
	 *            Generate random numbers between a circle is a little tricky
	 *            Just for 2D
	 */
	public static void randomizeRadius(float[] v, float radius) {
		final float t = (float) (2 * Math.PI * (random.nextFloat() * radius));
		final float u = (random.nextFloat() * radius)
				+ (random.nextFloat() * radius);
		final float r = (radius < u) ? 2 - u : u;
		v[0] = (float) (Math.cos(t) * r);
		v[1] = (float) (Math.sin(t) * r);
		v[2] = 0f;
	}

	public static float[] random3DVectorUnit() {
		float[] v;
		do {
			v = new float[] { random.nextFloat(), random.nextFloat(),
					random.nextFloat() };
		} while (isZero(v));
		normalize(v);
		return v;
	}

	public static float[] randomUnitVecInPlaneNormToCurrVec(float[] src) {
		float[] res = new float[3];
		if (isSame(src, res))
			return res;
		float[] n = new float[3];
		set(n, src);
		normalize(n);
		float x = (FloatUtil.isSame(n[1], 0) && FloatUtil.isSame(n[2], 0)) ? 0
				: random.nextFloat() - 0.5f;
		float y = (FloatUtil.isSame(n[2], 0) && FloatUtil.isSame(n[0], 0)) ? 0
				: random.nextFloat() - 0.5f;
		float z = (FloatUtil.isSame(n[0], 0) && FloatUtil.isSame(n[1], 0)) ? 0
				: random.nextFloat() - 0.5f;
		if (FloatUtil.isSame(n[2], 0f)) {
			if (!FloatUtil.isSame(n[0], 0f) && (!FloatUtil.isSame(n[1], 0f))) {
				x = -(n[1] * y) / n[0];
			}
		} else {
			z = -(n[0] * x + n[1] * y) / n[2];
		}
		res[0] = x;
		res[1] = y;
		res[2] = z;
		normalize(res);
		return res;
	}

	public static void rotateVectorPerpendicularToAxis(float[] vec,
			float[] axis, float angle) {
		// Formula for rotation is
		// v' = v cos(a) + (u x v) sin(a)
		// where
		// v - rotating vector, perpendicular to rotation axis
		// u - rotation axis
		// x - vector multiplication (cross)
		// a - rotation angle
		// Source: wikipedia
		final float[] temp = new float[3];

		set(temp, axis);
		crossInto(temp, vec);
		multiplyInto(temp, (float) Math.sin(angle));

		multiplyInto(vec, (float) Math.cos(angle));

		addInto(vec, temp);
	}

	public static double[] rotate(double[] point, double[] center,
			double[] axis, double angle) {
		final double[] newPoint = new double[3];
		newPoint[0] = (point[0] - center[0])
				* (Math.cos(angle) + (axis[0] * axis[0] * (1 - Math.cos(angle))))
				+ (point[1] - center[1])
				* (axis[0] * axis[1] * (1 - Math.cos(angle)) - (axis[2] * Math
						.sin(angle)))
				+ (point[2] - center[2])
				* (axis[0] * axis[2] * (1 - Math.cos(angle)) + axis[1]
						* Math.sin(angle));

		newPoint[1] = (point[0] - center[0])
				* (axis[1] * axis[0] * (1 - Math.cos(angle)) + axis[2]
						* Math.sin(angle))
				+ (point[1] - center[1])
				* (Math.cos(angle) + axis[1] * axis[1] * (1 - Math.cos(angle)))
				+ (point[2] - center[2])
				* (axis[1] * axis[2] * (1 - Math.cos(angle)) - axis[0]
						* Math.sin(angle));

		newPoint[2] = (point[0] - center[0])
				* (axis[2] * axis[0] * (1 - Math.cos(angle)) - axis[1]
						* Math.sin(angle))
				+ (point[1] - center[1])
				* (axis[2] * axis[1] * (1 - Math.cos(angle)) + axis[0]
						* Math.sin(angle))
				+ (point[2] - center[2])
				* (Math.cos(angle) + (axis[2] * axis[2] * (1 - Math.cos(angle))));
		newPoint[0] += center[0];
		newPoint[1] += center[1];
		newPoint[2] += center[2];

		return newPoint;
	}

	public static boolean isZero(float[] val, float eps) {
		return FloatUtil.isZero(val[0], eps) && FloatUtil.isZero(val[1], eps)
				&& FloatUtil.isZero(val[2], eps);
	}

	public static boolean isValid(float[] v) {
		return isFinite(v[0]) && isFinite(v[1]) && isFinite(v[2]);
	}

	public static boolean isParallelVectors(float[] v1, float[] v2) {
		float[] cross = new float[3];
		set(cross, v1);
		crossInto(cross, v2);
		return isSame(cross, ZERO);
	}

	public static boolean isCoplanar3Vectors(float[] v1, float[] v2, float[] v3) {
		float[] res = new float[3];
		set(res, v1);
		multiplyInto(res, dot(v3, v1));
		float[] add2 = new float[3];
		set(add2, v2);
		multiplyInto(res, dot(v3, v2));
		addInto(res, add2);
		return isSame(res, v3);
	}

	public static float[] newArbitraryPerpendicular(float[] from) {
		if (from[0] == 0 && from[1] == 0) {
			if (from[2] == 0) {
				throw new IllegalArgumentException(
						"0 vector passed to perpendicular");
			} else {
				return new float[] { 0, 1, 0 };
			}
		} else {
			return new float[] { -from[1], from[0], 0 };
		}
	}

	public static float[] projectVectorOnAnotherV(float[] vToProj,
			float[] vOnProj) {
		float magVTP = magnitude(vToProj);
		float magVOP = magnitude(vOnProj);
		if (FloatUtil.isZero(magVTP) || FloatUtil.isZero(magVOP))
			return new float[] { 0, 0, 0 };
		float cosAng = dot(vToProj, vOnProj) / magVOP / magVTP;
		float[] res = new float[3];
		set(res, vOnProj);
		normalize(res);
		multiplyInto(res, cosAng * magVTP);
		return res;
	}

	public static float[] copy(float[] v) {
		return new float[] { v[0], v[1], v[2] };
	}

	public static float[] addMultiplied(float[] v1, float[] v2, float s) {
		return new float[] { v1[0] + v2[0] * s, v1[1] + v2[1] * s,
				v1[2] + v2[2] * s };
	}

	public static void capMagVector(float[] vec, float val) {
		float mag = magnitude(vec);
		if (mag > val) {
			normalize(vec);
			multiplyInto(vec, val);
		}
	}
}
