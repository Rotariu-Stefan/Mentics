package com.mentics.physics;

import static com.mentics.math.quaternion.Quaternion.angleBetweenQ;
import static com.mentics.math.quaternion.Quaternion.conjugateQ;
import static com.mentics.math.quaternion.Quaternion.normalizeQ;
import static com.mentics.math.quaternion.Quaternion.rotateQuaternion;
import static com.mentics.math.quaternion.Quaternion.rotateVector;
import static com.mentics.math.quaternion.Quaternion.rotationalDifference;
import static com.mentics.math.quaternion.Quaternion.versor;
import static com.mentics.math.vector.VectorUtil.cross;
import static com.mentics.math.vector.VectorUtil.magnitude;
import static com.mentics.math.vector.VectorUtil.multiplyInto;
import static com.mentics.math.vector.VectorUtil.subtract;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.mentics.math.quaternion.Quaternion;
import com.mentics.math.vector.VectorUtil;
import com.mentics.qd.commands.global.AutoCameraCommand;
import com.mentics.qd.items.ItemUtil;

public class RotateTest {
	public static float EPS_ROT = 3e-4f;
	public static float EPS_ANG_VEL = 1e-3f; // This must be near zero too as it
												// is relative I suppose
	static float[] IDENTITY = new float[4];
	static float[] rotateX90 = new float[4];
	static float[] rotateY90 = new float[4];
	static float[] rotateZ90 = new float[4];
	static float[] rotateXY45 = new float[4];

	static {
		Quaternion.versorInto(IDENTITY, new float[] { 1, 0, 0 }, (float) 0);
		Quaternion.versorInto(rotateX90, new float[] { 1, 0, 0 },
				(float) Math.PI / 4);
		Quaternion.versorInto(rotateY90, new float[] { 0, 1, 0 },
				(float) Math.PI / 4);
		Quaternion.versorInto(rotateZ90, new float[] { 0, 0, 1 },
				(float) Math.PI / 4);
		Quaternion.versorInto(rotateXY45, new float[] { 0, 0, 1 },
				(float) Math.PI / 8);
	}

	@Test
	public void testRotateX90() {

		float[] X1 = new float[] { 1, 0, 0 };
		float[] Y1 = new float[] { 0, 1, 0 };
		float[] Z1 = new float[] { 0, 0, 1 };

		float[] resultX = Quaternion.rotateVector(X1, rotateX90);
		float[] resultY = Quaternion.rotateVector(Y1, rotateX90);
		float[] resultZ = Quaternion.rotateVector(Z1, rotateX90);

		Assert.assertTrue(VectorUtil.isSame(resultX, new float[] { 1, 0, 0 }));
		Assert.assertTrue(VectorUtil.isSame(resultY, new float[] { 0, 0, 1 }));
		Assert.assertTrue(VectorUtil.isSame(resultZ, new float[] { 0, -1, 0 }));
	}

	@Test
	public void testRotateY90() {

		float[] X1 = new float[] { 1, 0, 0 };
		float[] Y1 = new float[] { 0, 1, 0 };
		float[] Z1 = new float[] { 0, 0, 1 };

		float[] resultX = Quaternion.rotateVector(X1, rotateY90);
		float[] resultY = Quaternion.rotateVector(Y1, rotateY90);
		float[] resultZ = Quaternion.rotateVector(Z1, rotateY90);

		Assert.assertTrue(VectorUtil.isSame(resultX, new float[] { 0, 0, -1 }));
		Assert.assertTrue(VectorUtil.isSame(resultY, new float[] { 0, 1, 0 }));
		Assert.assertTrue(VectorUtil.isSame(resultZ, new float[] { 1, 0, 0 }));
	}

	@Test
	public void testRotateZ90() {
		float[] X1 = new float[] { 1, 0, 0 };
		float[] Y1 = new float[] { 0, 1, 0 };
		float[] Z1 = new float[] { 0, 0, 1 };

		float[] resultX = Quaternion.rotateVector(X1, rotateZ90);
		float[] resultY = Quaternion.rotateVector(Y1, rotateZ90);
		float[] resultZ = Quaternion.rotateVector(Z1, rotateZ90);

		Assert.assertTrue(VectorUtil.isSame(resultX, new float[] { 0, 1, 0 }));
		Assert.assertTrue(VectorUtil.isSame(resultY, new float[] { -1, 0, 0 }));
		Assert.assertTrue(VectorUtil.isSame(resultZ, new float[] { 0, 0, 1 }));
	}

	@Test
	public void testCombineRotations() {
		float[] x = new float[] { 1, 0, 0 };

		float[] camera = Quaternion.copy(IDENTITY);
		System.out.println(Arrays.toString(camera) + " - initial camera");

		float[] result = rotateVector(x, camera);
		System.out.println(Arrays.toString(result)
				+ " - x rotated by initial camera");

		camera = rotateQuaternion(camera, rotateX90);
		System.out.println(Arrays.toString(camera) + " - X quat");
		System.out.println(Arrays.toString(rotateX90) + " - rotateX90");

		result = rotateVector(x, camera);
		// System.out.println(Arrays.toString(result) +
		// " - x rotated by X quat");
		Assert.assertTrue(VectorUtil.isSame(x, new float[] { 1, 0, 0 }));

		camera = rotateQuaternion(camera, rotateY90);
		System.out.println(Arrays.toString(camera) + " - X and Y quat");

		result = rotateVector(x, camera);
		System.out.println(Arrays.toString(result) + " - x rotated by XY quat");
		assertArrayEquals(new float[] { 0, 1, 0 }, result, EPS_ROT);
	}

	@Test
	public void testRotationalDifference() {
		float[] test;
		float[] result = rotationalDifference(IDENTITY, rotateX90);
		assertArrayEquals(rotateX90, result, EPS_ROT);
		result = rotationalDifference(rotateX90, IDENTITY);
		assertArrayEquals(conjugateQ(rotateX90), result, EPS_ROT);
		test = rotateQuaternion(IDENTITY, result);
		assertArrayEquals(conjugateQ(rotateX90), test, EPS_ROT);

		result = rotationalDifference(IDENTITY, rotateY90);
		assertArrayEquals(rotateY90, result, EPS_ROT);
		result = rotationalDifference(rotateY90, IDENTITY);
		assertArrayEquals(conjugateQ(rotateY90), result, EPS_ROT);
		test = rotateQuaternion(IDENTITY, result);
		assertArrayEquals(conjugateQ(rotateY90), test, EPS_ROT);

		result = rotationalDifference(IDENTITY, rotateZ90);
		assertArrayEquals(rotateZ90, result, EPS_ROT);
		result = rotationalDifference(rotateZ90, IDENTITY);
		assertArrayEquals(conjugateQ(rotateZ90), result, EPS_ROT);
		test = rotateQuaternion(IDENTITY, result);
		assertArrayEquals(conjugateQ(rotateZ90), test, EPS_ROT);

		float[] diffQ = rotationalDifference(rotateX90, rotateY90);
		result = rotateQuaternion(rotateX90, diffQ);
		assertArrayEquals(rotateY90, result, EPS_ROT);

		diffQ = rotationalDifference(rotateZ90, rotateY90);
		result = rotateQuaternion(rotateZ90, diffQ);
		assertArrayEquals(rotateY90, result, EPS_ROT);

		diffQ = rotationalDifference(rotateY90, rotateX90);
		result = rotateQuaternion(rotateY90, diffQ);
		assertArrayEquals(rotateX90, result, EPS_ROT);
	}

	@Test
	public void testAngleBetweenQ() {
		assertEquals(0, angleBetweenQ(rotateX90, rotateX90), EPS_ROT);

		assertEquals(Math.PI / 2, angleBetweenQ(IDENTITY, rotateX90), EPS_ROT);

		assertEquals(Math.PI / 2, angleBetweenQ(IDENTITY, rotateY90), EPS_ROT);

		assertEquals(Math.PI / 2, angleBetweenQ(IDENTITY, rotateZ90), EPS_ROT);

		assertEquals(Math.PI, angleBetweenQ(rotateZ90, conjugateQ(rotateZ90)),
				EPS_ROT);

		// TODO: add more complex tests
	}

	public static float ROOT2 = (float) Math.sqrt(2);

	@Test
	public void testMoveTowardRotation() {
		float[] orient = rotateZ90;
		float[] targetOrient = rotateX90;
		float[] angVel = new float[] { 1, 0, 0 }; // Rotating magnitude
													// radians/second around the
													// axis (normalized)
		float timeStep = 0.1f;
		float maxAngAcc = (float) Math.PI * 2;
		System.out.println("orient: " + Arrays.toString(orient));
		System.out.println("vel: " + Arrays.toString(angVel));
		float[] angAcc = new float[3];

		for (int i = 0; i < 100; i++) {
			float[] targetRotation = rotationalDifference(orient, targetOrient);
			ThrustControl.moveTowardRotation(angAcc, targetRotation, angVel,
					maxAngAcc, timeStep);

			// Update position
			float[] angVelAxis = VectorUtil.copy(angVel);
			float speed = VectorUtil.normalize(angVelAxis);
			float[] velQ = versor(angVelAxis, speed * timeStep / 2);
			// System.out.println("test vel: "+parAngSpeed*timeStep+", q: "+angleQ(velQ));
			orient = rotateQuaternion(orient, velQ);

			// 1/2at^2 part
			float[] angAccAxis = VectorUtil.copy(angAcc);
			float angAccMag = VectorUtil.normalize(angAccAxis);
			float[] accQ = versor(angAccAxis, 0.5f * angAccMag * timeStep
					* timeStep / 2);
			orient = rotateQuaternion(orient, accQ);

			// Update velocity
			angVel = VectorUtil.addMultiplied(angVel, angAcc, timeStep);

			System.out.println("acc: " + Arrays.toString(angAcc));
			System.out.println("vel: " + Arrays.toString(angVel));
			System.out.println("orient: " + Arrays.toString(orient));
			System.out.println();

			if (Quaternion.isSame(targetOrient, orient, EPS_ROT)) {
				break;
			}
		}

		assertArrayEquals(targetOrient, orient, EPS_ROT);
	}

	// rechecking your test
	@Test
	public void testMoveTowardRotationRevise2() {
		// Preparing data
		float[] orient = rotateZ90;
		float[] targetOrient = rotateX90;
		float[] angVel = new float[] { 1, 0, 0 }; // Rotating magnitude
													// radians/second around the
													// axis (normalized)
		float timeStep = 0.1f;
		float maxAngAcc = (float) Math.PI * 2;
		// Working process
		workingBodyBase(orient, targetOrient, angVel, maxAngAcc, timeStep);
	}

	// new added my tests
	@Test
	public void testRotationXYPlaneFixedTarget() {
		// Preparing data
		float[] orient = versor(new float[] { 0, 0, 1 }, -(float) Math.PI / 8);
		float[] targetOrient = versor(new float[] { 0, 0, 1 },
				+(float) Math.PI / 8);
		float[] angVel = new float[] { 0, 0, 0 }; // Rotating magnitude
													// radians/second around the
													// axis (normalized)
		float timeStep = 0.1f;
		float maxAngAcc = (float) Math.PI * 2;
		// Working process
		workingBodyBase(orient, targetOrient, angVel, maxAngAcc, timeStep);
	}

	@Test
	public void testRotationInitAVDiffNormal() {
		// Preparing data
		float[] orient = versor(new float[] { 0, 0, 1 }, -(float) Math.PI / 8);
		float[] targetOrient = versor(new float[] { 0, 0, 1 },
				+(float) Math.PI / 8);
		float[] angVel = new float[] { 0.3f, -0.3f, 0.2f }; // Rotating
															// magnitude
															// radians/second
															// around the axis
															// (normalized)
		float timeStep = 0.1f;
		float maxAngAcc = (float) Math.PI * 2;
		// Working process
		workingBodyBase(orient, targetOrient, angVel, maxAngAcc, timeStep);
	}

	@Test
	public void testRandomDatas() {
		final int NUM_OF_TESTS = 100;
		for (int i = 1; i <= NUM_OF_TESTS; i++) {
			float[] axis = VectorUtil.random3DVectorUnit();
			double angle = (Math.random() - 0.5) * Math.PI * 2;
			float[] orient = versor(axis, (float) angle / 2);
			axis = VectorUtil.random3DVectorUnit();
			angle = (Math.random() - 0.5) * Math.PI * 2;
			float[] targetOrient = versor(axis, (float) angle / 2);
			float[] angVel = VectorUtil.random3DVectorUnit();
			float timeStep = 0.1f;
			float maxAngAcc = (float) Math.PI * 2;

			System.out.println("--------------------- TEST " + i
					+ " ---------------------------\n");
			workingBodyBase(orient, targetOrient, angVel, maxAngAcc, timeStep);
		}
	}

	@Test
	public void testFailv1() {
		// Preparing data
		float[] orient = { 0.97939676f, -0.17024203f, -0.022837836f,
				-0.10619848f };
		float[] targetOrient = { 0.9986411f, -0.034314416f, -0.025978606f,
				-0.029387126f };
		float[] angVel = { 0.054313175f, 0.20127088f, 0.97802866f }; // Rotating
																		// magnitude
																		// radians/second
																		// around
																		// the
																		// axis
																		// (normalized)
		float timeStep = 0.1f;
		float maxAngAcc = (float) Math.PI * 2;
		// Working process
		workingBodyBase(orient, targetOrient, angVel, maxAngAcc, timeStep);
	}

	public static void workingBodyBase(float[] orient, float[] targetOrient,
			float[] angVel, float maxAngAcc, float timeStep) {
		System.out.println("target: " + Arrays.toString(targetOrient));
		System.out.println("orient: " + Arrays.toString(orient));
		System.out.println("vel: " + Arrays.toString(angVel));
		String failMessage = "Don't converged with initial datas "
				+ "init orient " + Arrays.toString(orient) + " targ orient "
				+ Arrays.toString(targetOrient) + " ang vel relative "
				+ Arrays.toString(angVel) + " max Ang Acc " + maxAngAcc
				+ " time step " + timeStep;

		float[] angAcc = new float[3];
		final float NUM_ITER = 100;
        float[] targetRotation = null;
        for (int i = 0; i < NUM_ITER; i++) {
            targetRotation = rotationalDifference(orient, targetOrient);
			normalizeQ(targetRotation); // modification
			ThrustControl.moveTowardRotation(angAcc, targetRotation, angVel,
					maxAngAcc, timeStep);
			ItemUtil.updatePhysicsRotational(orient, angVel, angAcc, timeStep);
			System.out.println("acc: " + Arrays.toString(angAcc));
			System.out.println("vel: " + Arrays.toString(angVel));
			System.out.println("orient: " + Arrays.toString(orient)
					+ " at ITER " + i);
			System.out.println();
			if ((Quaternion.isSame(targetOrient, orient, EPS_ROT))
			// && (VectorUtil.isZero(angVel, EPS_ANG_VEL))
			) {
				break;
			}
		}
		assertArrayEquals(failMessage, targetOrient, orient, EPS_ROT);
		// 

        assertArrayEquals(failMessage, angVel, VectorUtil.ZERO, EPS_ANG_VEL);
	}

	@Test
	public void testTrackingGENERAL_OnePlain() {
		// Init Data
		float[] camPos = { 10, 2, 0 };
		float[] camVel = { 1, -1, 0 };
		float[] camVelAng = { 0, 0, 0 };
		float[] camOrQ = { 1, 0, 0, 0 };
		float[] targPos = { 2, 7, 0 };
		float[] targVel = { 2, 3, 0 };

		float timeStep = 0.1f;
		// variables
		// loop
		System.out.println("START");
		for (int i = 0; i < 200; i++) {
			// relative ang velocity
			float[] relVelLin = subtract(targVel, camVel);
			float[] relPos = subtract(targPos, camPos);
			float[] angVelTarg = cross(relPos, relVelLin);
			float dist = magnitude(relPos);
			multiplyInto(angVelTarg, 1 / (dist * dist));
			float[] angVelRel = subtract(angVelTarg, camVelAng);
			// will tracking x axis
			// float[] curX = Quaternion.rotateVector(new float[] { 1, 0, 0 },
			// camOrQ);
			// float[] targetRotation = Quaternion.from2VectorsQuaternion(curX,
			// relPos);
			// tracking quaternion
			VectorUtil.normalize(relPos);
			float angle = (float) Math.atan2(relPos[1], relPos[0]);
			float[] targOrQ = Quaternion.versor(new float[] { 0, 0, 1 },
					angle / 2f);
			float[] targetRotation = Quaternion.rotationalDifference(camOrQ,
					targOrQ);
			System.out.println("Angle Cam " + Quaternion.angleQ(camOrQ)
					+ " Angle Trg " + Quaternion.angleQ(targOrQ));
			float[] applAngAcc = new float[3];
			ThrustControl.moveTowardRotation(applAngAcc, targetRotation,
					angVelRel, AutoCameraCommand.MAX_CAMERA_EPS, timeStep);
			ItemUtil.updatePhysicsRotational(camOrQ, camVelAng, applAngAcc,
					timeStep);

			ItemUtil.updatePhysics(camPos, camVel, new float[] { 0, 0, 0 },
					timeStep);
			ItemUtil.updatePhysics(targPos, targVel, new float[] { 0, 0, 0 },
					timeStep);

			// System.out.println("Before update curX "
			// + Arrays.toString(curX)
			// + "\ntarget direction "
			// + Arrays.toString(VectorUtil.multiply(relPos,
			// 1 / magnitude(relPos))));
			System.out.println("Src " + Arrays.toString(camOrQ) + "\nTrg "
					+ Arrays.toString(targOrQ));
		}
		System.out.println("END");
	}

}
