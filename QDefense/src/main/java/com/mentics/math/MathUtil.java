package com.mentics.math;

import com.mentics.math._float.FloatUtil;


public class MathUtil {
    public static int integerMultiple(float value, float division) {
        return FloatUtil.isZero(value) ? 0 : (int)Math.ceil(value / division);
    }

}
