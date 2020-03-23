package com.mentics.math._float;

import static com.mentics.qd.items.ItemUtil.DISTANCE_EPS;
import static com.mentics.qd.items.ItemUtil.EPS_CALC;

public class FloatUtil {

	public static boolean isSame(float f, float g) {
		return FloatUtil.isSame(f, g, EPS_CALC);
	}

	public static boolean isSame(float f, float g, float eps) {
		// return g == 0 ? Math.abs(f) < eps : ((f / g - 1) < eps); // GGGGGGGG
		return Math.abs(f - g) < eps || Math.abs(f / g - 1) < eps;
	}

	public static boolean isZero(float val) {
		return FloatUtil.isZero(val, EPS_CALC);
	}

	public static boolean isZero(float val, float eps) {
		return Math.abs(val) < eps;
	}

	public static boolean validAboveZero(float mag) {
		return !Float.isNaN(mag) && mag > DISTANCE_EPS;
	}

}
