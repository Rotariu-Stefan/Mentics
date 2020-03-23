package com.mentics.physics;

import static com.mentics.math.quaternion.Quaternion.angleQ;
import static com.mentics.math.quaternion.Quaternion.copyToVector;
import static com.mentics.math.vector.VectorUtil.addInto;
import static com.mentics.math.vector.VectorUtil.dot;
import static com.mentics.math.vector.VectorUtil.isSame;
import static com.mentics.math.vector.VectorUtil.isZero;
import static com.mentics.math.vector.VectorUtil.multiplyInto;
import static com.mentics.math.vector.VectorUtil.normalize;
import static com.mentics.math.vector.VectorUtil.set;
import static com.mentics.math.vector.VectorUtil.subtractInto;
import static com.mentics.qd.items.ItemUtil.EPS_CALC;

import java.util.Arrays;

import com.mentics.math.MathUtil;
import com.mentics.math._float.FloatUtil;
import com.mentics.math.vector.VectorUtil;

public class ThrustControl {
	public static float[] moveToward3D(float[] resultAcc, float[] target,
			float[] v0, float maxAcc, float timeStep) {
		final float[] parDir = new float[3];
		set(parDir, target);
		final float distanceToTarget = normalize(parDir);

		// final float parSpeed = Math.abs(dot(v0, parDir)); //
		// GGGGGGGGGGGGGGG!!!
		final float parSpeed = dot(v0, parDir);

		final float[] parComp = new float[3];
		set(parComp, parDir);
		multiplyInto(parComp, parSpeed);

		final float[] orthoDir = new float[3];
		set(orthoDir, v0);
		subtractInto(orthoDir, parComp);
		final float orthoSpeed = normalize(orthoDir);

		float[] acc = moveTowardComponents(maxAcc, timeStep, distanceToTarget,
				parSpeed, orthoSpeed);

		multiplyInto(parDir, acc[0]);
		multiplyInto(orthoDir, acc[1]);
		addInto(parDir, orthoDir);
		set(resultAcc, parDir);
		if (!VectorUtil.isValid(parDir)) {
			System.out.println("Bad acc: " + Arrays.toString(parDir));
		}
		return parDir;
	}

	public static void moveTowardRotation(float[] resultAngAcc,
			float[] targetRotation, float[] angVel, float maxAngAcc,
			float timeStep) {
		float[] velAxis = VectorUtil.copy(angVel);
		float angSpeed = VectorUtil.normalize(velAxis);
		// float[] targetRotation = newRotationalDifference(orient, qTarget);
		float targetAngDist = angleQ(targetRotation);
		float[] parallelAxis = new float[3];
		copyToVector(parallelAxis, targetRotation);
		VectorUtil.normalize(parallelAxis);
		float velAngleCos = VectorUtil.dot(velAxis, parallelAxis);
		if (velAngleCos > 1f) {
			System.out.println("velAngleCos > 1: " + velAngleCos);
			velAngleCos = 1;
		}
		if (velAngleCos < -1f) {
			System.out.println("velAngleCos < -1: " + velAngleCos);
			velAngleCos = -1;
		}
		float velAngle = (float) Math.acos(velAngleCos);
		float parAngSpeed = angSpeed * velAngleCos;

		float[] parComp = VectorUtil.multiply(parallelAxis, parAngSpeed);

		float perpAngSpeed = (float) (angSpeed * Math.sin(velAngle));

		float[] perpComp = VectorUtil.subtract(angVel, parComp);
		float[] perpAxis = VectorUtil.copy(perpComp);
		VectorUtil.normalize(perpAxis);
		float perpSpeed = VectorUtil.magnitude(perpComp);
		// assertEquals(perpSpeed, perpAngSpeed, EPS_ROT);

		// TRY OTHER IDEA START for ang vel improve
		// boolean thisWay = true;
		// if (thisWay) {
		// float parEpsVal = ThrustControl.moveToward1D(velAngle, angSpeed,
		// maxAngAcc, timeStep);
		// float[] parEpsVec = VectorUtil.newMultiply(parallelAxis, parEpsVal);
		// float perpEpsVal = -perpAngSpeed / timeStep;
		// perpEpsVal = capMag(perpEpsVal, maxAngAcc);
		// float[] perpEpsVec = VectorUtil.newMultiply(perpAxis, perpEpsVal);
		// float[] epsSum = VectorUtil.newAdd(parEpsVec, perpEpsVec);
		// capMagVector(epsSum, maxAngAcc);
		// VectorUtil.set(resultAngAcc, epsSum);
		// return;
		// }
		// TRY OTHER IDEA END

		float[] angAccComps = ThrustControl.moveTowardComponents(maxAngAcc,
				timeStep, targetAngDist, parAngSpeed, perpAngSpeed);

		float[] angAcc = VectorUtil.add(
				VectorUtil.multiply(parallelAxis, angAccComps[0]),
				VectorUtil.multiply(perpAxis, angAccComps[1]));
		VectorUtil.set(resultAngAcc, angAcc);

		float exp = (float) (parAngSpeed * timeStep + 0.5 * angAcc[0]
				* timeStep * timeStep);
		System.out.println(targetAngDist + ", " + parAngSpeed + ", "
				+ perpAngSpeed + ", exp: " + exp);

		// TODO
		// Calculate component stuff: parSpeed, orthoSpeed, parRotation,
		// orthoRotation

		// float[] acc = moveTowardComponents(maxAcc, timeStep, distance,
		// parSpeed, orthoSpeed);

		// r1 = multiply acc[0] * parRotation
		// r2 = multiply acc[1] * orthoRotation
		// combine r1 and r2 and return result
	}

	public static float[] moveTowardComponents(float maxAcc, float timeStep,
			final float distanceToTarget, final float parSpeed,
			final float orthoSpeed) {
		float parMaxAcc;
		float orthoMaxAcc;
		if (!FloatUtil.isZero(orthoSpeed, EPS_CALC)) {
			final float parIdealT2 = idealT2(distanceToTarget, parSpeed, maxAcc);
			final float parIdealT1 = idealT1(parIdealT2, parSpeed, maxAcc);
			final float parTotalTime = parIdealT1 + parIdealT2;

			final float orthoIdealT2 = idealT2(0, orthoSpeed, maxAcc);
			final float orthoIdealT1 = idealT1(orthoIdealT2, orthoSpeed, maxAcc);
			final float orthoTotalTime = orthoIdealT1 + orthoIdealT2;

			final float parTestAcc = moveToward1D(distanceToTarget, parSpeed,
					maxAcc, timeStep);
			final float orthoTestAcc = moveToward1D(0, orthoSpeed, maxAcc,
					timeStep);

			// a2+b2=c2
			// paracc^2 + orthoacc^2 = maxAcc^2
			// k = partt/orthott
			// paracc = orthoacc * k
			// (orthoacc*k)^2 + orthoacc^2 = maxACc^2
			// oa^2*k^2 + oa^2 = max^2
			// (k^2+1)*oa^2 = max^2
			// oa^2 = max^2/(k^2+1)
			// final float k = parTotalTime / orthoTotalTime;
			final float k = Math.abs(parTestAcc / orthoTestAcc);
			// final float k = .5f;
			// final float k = parSpeed / orthoSpeed;
			if (k < 0) {
				System.out.println("** shouldnt happen, -k");
			}
			// final float k = 0;
			orthoMaxAcc = (float) Math.sqrt(maxAcc * maxAcc / (k * k + 1));
			parMaxAcc = orthoMaxAcc * k;
		} else {
			orthoMaxAcc = 0f;
			parMaxAcc = maxAcc;
		}

		assert FloatUtil.isSame(orthoMaxAcc * orthoMaxAcc + parMaxAcc * parMaxAcc, maxAcc
				* maxAcc);

		final float parAcc = moveToward1D(distanceToTarget, parSpeed,
				parMaxAcc, timeStep);
		final float orthoAcc = moveToward1D(0, orthoSpeed, orthoMaxAcc,
				timeStep);
		float[] acc = new float[] { parAcc, orthoAcc };
		return acc;
	}

	public static float moveToward1D(float target, float v0, float maxAcc,
			float timeStep) {
		if (FloatUtil.isZero(maxAcc) || (FloatUtil.isZero(target) && FloatUtil.isZero(v0))) {
			return 0f;
		}

		final float t2 = idealT2(target, v0, maxAcc);
		final float t1 = idealT1(t2, v0, maxAcc);

		if (t1 < 0) {
			if (-t1 >= timeStep) {
				return -maxAcc;
			} else {
				// System.out.println("t1 was negative and smaller than timeStep: "
				// + t1);
				// TODO: add in 1/2at2 component
				// TODO: there are 3 places for this, combine them into a
				// function
				return -Math.min(v0 / timeStep, maxAcc);
			}
		}
		// t1 must be > 0 now

		final int numTimeSteps1 = MathUtil.integerMultiple(t1, timeStep);
		final int numTimeSteps2 = MathUtil.integerMultiple(t2, timeStep);

		if (numTimeSteps1 > 1) {
			return maxAcc;
		}

		final float t1discrete = numTimeSteps1 * timeStep;
		final float t2discrete = numTimeSteps2 * timeStep;

		if (t1discrete == 0) {
			if (t2discrete == 0) {
				System.out
						.println("There are special cases where t1, t2 are 0.");
				// TODO: add in 1/2at2 component
				return v0 / timeStep;
			} else {
				// TODO: basic calculation for deceleration over t2discrete
				System.out.println("0 t1discrete");
				// return -v0 / t2discrete;
				// TODO: add in 1/2at2 component
				return -Math.min(v0 / t2discrete, maxAcc);
			}
		}

		final float a1 = 2f * (target - v0 * (t1discrete + t2discrete / 2f))
				/ (t1discrete * (t1discrete + t2discrete));
		final float a2 = (-v0 - a1 * t1discrete) / t2discrete;

		return capMag(a1, maxAcc);
	}

	private static float capMag(float value, float maxAcc) {
		float mag = Math.abs(value);
		return mag > maxAcc ? maxAcc * Math.signum(value) : value;
	}

	public static float idealT2(float target, float v0, float maxAcc) {
		return (float) Math.sqrt((target + v0 * v0 / 2 / maxAcc) / maxAcc);
	}

	public static float idealT1(float t2, float v0, float maxAcc) {
		return t2 - v0 / maxAcc;
	}
}
