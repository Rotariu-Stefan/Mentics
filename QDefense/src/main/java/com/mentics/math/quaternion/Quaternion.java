package com.mentics.math.quaternion;

import com.mentics.math._float.FloatUtil;
import com.mentics.math.vector.VectorUtil;


public class Quaternion {
	public static float[] versor(float[] axis, float halfAngle) {
		float[] result = new float[4];
		versorInto(result, axis, halfAngle);
		return result;
	}
	
    public static void versorInto(float[] q, float[] axis, float halfAngle) {
        q[0] = (float)Math.cos(halfAngle);
        float sinA = (float)Math.sin(halfAngle);
        for (int i = 0; i < 3; i++) {
            q[1 + i] = axis[i] * sinA;
        }
    }
    
    // not sure which is from and to yet
    public static float[] from2VectorsQuaternion(float[] from, float[] to) {
    	float[] fromcpy = VectorUtil.copy(from);
    	float[] tocpy = VectorUtil.copy(to);
    	VectorUtil.normalize(fromcpy);
    	VectorUtil.normalize(tocpy);
    	float[] axis = VectorUtil.cross(fromcpy, tocpy);
    	VectorUtil.normalize(axis);
    	float cosangle = VectorUtil.dot(fromcpy, tocpy);
    	float halfangle = (float)(Math.acos(cosangle) / 2.0);
    	float sinhalf = (float)Math.sin(halfangle);
    	float[] res = {(float)Math.cos(halfangle), axis[0] * sinhalf, axis[1] * sinhalf, axis[2] * sinhalf};
    	normalizeQ(res);
    	return res;
    }
    
    
    public static float angleQ(float[] q) {
    	return (float) (2*Math.acos(q[0]));
    }

    public static float[] multiplyQQ(float[] left, float[] right) {
        float[] result = new float[4];
        multiplyQbyQInto(result, left, right);
        return result;
    }

    public static void multiplyQbyQInto(float[] result, float[] left, float[] right) {
        result[0] = left[0] * right[0] - left[1] * right[1] - left[2] * right[2] - left[3] * right[3];
        result[1] = left[0] * right[1] + left[1] * right[0] + left[2] * right[3] - left[3] * right[2];
        result[2] = left[0] * right[2] - left[1] * right[3] + left[2] * right[0] + left[3] * right[1];
        result[3] = left[0] * right[3] + left[1] * right[2] - left[2] * right[1] + left[3] * right[0];
    }

    public static void multiplyVbyQInto(float[] result, float[] left, float[] right) {
        result[0] = -left[0] * right[1] - left[1] * right[2] - left[2] * right[3];
        result[1] = +left[0] * right[0] + left[1] * right[3] - left[2] * right[2];
        result[2] = -left[0] * right[3] + left[1] * right[0] + left[2] * right[1];
        result[3] = +left[0] * right[2] - left[1] * right[1] + left[2] * right[0];
    }

    public static void multiplyQbyVInto(float[] result, float[] left, float[] right) {
        result[0] = -left[1] * right[0] - left[2] * right[0] - left[3] * right[0];
        result[1] = left[0] * right[0] + left[2] * right[0] - left[3] * right[0];
        result[2] = left[0] * right[0] - left[1] * right[0] + left[3] * right[0];
        result[3] = left[0] * right[0] + left[1] * right[0] - left[2] * right[0];
    }

    public static float[] multiplyQbyScalar(float[] q0, float sc) {
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            res[i] = q0[i] * sc;
        }
        return res;
    }

    public static float[] subtractQfromQ(float[] ql, float[] qr) {
        float[] res = new float[4];
        for (int i = 0; i < 4; i++) {
            res[i] = ql[i] - qr[i];
        }
        return res;
    }

    public static float[] addQtoScalar(float[] q0, float sc) {
        float[] res = new float[4];
        copyToOtherQ(res, q0);
        res[0] += sc;
        return res;
    }

    public static void copyToVector(float[] vector, float[] quaternion) {
        for (int i = 0; i < 3; i++) {
            vector[i] = quaternion[1 + i];
        }
    }

    public static void copyToOtherQ(float[] q1, float[] q2) {
        for (int i = 0; i < 4; i++) {
            q1[i] = q2[i];
        }
    }

    public static float[] pointAsQuaternion(float[] pt) {
        float[] qpt = new float[4];
        for (int i = 0; i < pt.length; i++) {
            qpt[i + 1] = pt[i];
        }
        return qpt;
    }

    public static float[] conjugateQ(float[] q) {
        float[] res = new float[4];
        res[0] = q[0];
        for (int i = 1; i < q.length; i++) {
            res[i] = -q[i];
        }
        return res;
    }

    public static float[] inverseQ(float[] q) {
        float[] res1 = conjugateQ(q);
        float norm = magnitudeQ(q);
        float[] res = multiplyQbyScalar(res1, 1f / norm / norm);
        return res;
    }

    public static float magnitudeQ(float[] q) {
        return (float)Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
    }

    public static void normalizeQ(float[] q) {
        float norm = magnitudeQ(q);
        for (int i = 0; i < 4; i++) {
            q[i] /= norm;
        }
    }

    public static float[] rotatePoint(float[] pt, float[] axis, float rotationAngle) {
        float[] res1 = new float[4];
        float[] quatRot = new float[4];
        Quaternion.versorInto(quatRot, axis, rotationAngle / 2);
        float[] ptQ = Quaternion.pointAsQuaternion(pt);
        Quaternion.multiplyQbyQInto(res1, quatRot, ptQ);
        float[] res2 = new float[4];
        Quaternion.multiplyQbyQInto(res2, res1, Quaternion.conjugateQ(quatRot));
        float[] resV = new float[3];
        Quaternion.copyToVector(resV, res2);
        return resV;
    }

    public static float[] rotateVector(float[] vector, float[] quatRot) {
        float[] qvect = Quaternion.pointAsQuaternion(vector);
        float[] res1 = new float[4];
        Quaternion.multiplyQbyQInto(res1, quatRot, qvect);
        float[] res2 = new float[4];
        Quaternion.multiplyQbyQInto(res2, res1, Quaternion.conjugateQ(quatRot));
        float[] resV = new float[3];
        Quaternion.copyToVector(resV, res2);
        return resV;
    }

    public static float[] rotateQuaternion(float[] quatToRotate, float[] quatRotation) {
        float[] result = new float[4];
        multiplyQbyQInto(result, quatToRotate, quatRotation);
        return result;
    }

    public static float[] copy(float[] quat) {
        return new float[] { quat[0], quat[1], quat[2], quat[3] };
    }

    public static float[] rotationalDifference(float[] from, float[] to) {
        return multiplyQQ(conjugateQ(from), to);
    }

    public static float dotQ(float[] left, float[] right) {
        return left[0] * right[0] + left[1] * right[1] + left[2] * right[2] + left[3] * right[3];
    }
    
	public static float imaginaryDot(float[] left, float[] right) {
        return left[1] * right[1] + left[2] * right[2] + left[3] * right[3];
	}
	
	public static float angleBetweenQ(float[] left, float[] right) {
//        q = q1^-1 * q2 and than angle = 2*atan2(q.vec.length(), q.w)
		float[] rotDiff = rotationalDifference(left, right);
		return (float) (2 * Math.atan2(imaginaryMagnitude(rotDiff), rotDiff[0]));
	}

	public static float imaginaryMagnitude(float[] q) {
		return (float) Math.sqrt(q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
	}

	public static boolean isSame(float[] q1, float[] q2, float eps) {
		return FloatUtil.isSame(q1[0], q2[0], eps)
				&& FloatUtil.isSame(q1[1], q2[1], eps)
				&& FloatUtil.isSame(q1[2], q2[2], eps)
				&& FloatUtil.isSame(q1[3], q2[3], eps);
	}
}
