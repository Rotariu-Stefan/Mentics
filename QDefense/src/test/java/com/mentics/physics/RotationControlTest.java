package com.mentics.physics;
import java.util.Arrays;

import org.junit.Test;

import static com.mentics.math.quaternion.Quaternion.*;
import static com.mentics.math.vector.VectorUtil.*;

public class RotationControlTest {
	
	public void q_next(float[] qnext, float[] qprev, float[] wQprev, float timestep) {
		float[] qtmp = addQtoScalar(multiplyQbyScalar(wQprev, timestep / 2f), 1);
		multiplyQbyQInto(qnext, qtmp, qprev);
		normalizeQ(qnext);
	}
	
	public float[] ang_acc_calc(float[] wnext, float[] wprev, float epsmax, float timestep) {
		// angular accs calcs
		float[] eps_t  = multiply(subtract(wnext, wprev), 1f / timestep);
		// scaling if eps is greater eps_max
		float norm_et = magnitude(eps_t);
		if(norm_et > epsmax) {
			multiplyInto(eps_t, epsmax / norm_et);
		}
		return eps_t;
	}
	
	@Test
	public void generalRotationTrackingCalculations() {
		
		// PREPARING DATA
		
		//Reviewing case when target and src pos fixed 
		//Rotations performed in one plane
		// Looking direction is rotating from src ld to target pos
		// Target data
		float[] posT = {10, 10, 0};
		// Source data
		float[] posS = {20, 10, 0};
		float phiS = (float) (0);
		float[] qS = {(float) Math.cos(phiS / 2f), 0, 0, 1f * (float) Math.sin(phiS / 2f)};
		float[] omegaS = {0, 0, 0};
		// Calculate q for end of rotation
		float phiT = (float) (3f / 4f * Math.PI);
		float[] qT = {(float) Math.cos(phiT / 2f), 0, 0, 1f * (float) Math.sin(phiT / 2f)};
		float[] omegaT = {0, 0, 1};
		// Physics data
		float dt = 0.1f;
		float dt1 = dt / 2;
		float eps_max = 5f * 10;
		// loop items
		float[] q_t0 = new float[4];
		float[] q_t1 = new float[4];
		float[] q_t2 = new float[4];
		copyToOtherQ(q_t0, qS);
		copyToOtherQ(q_t2, qT);
		float omega_t0[] = new float[3];
		float omega_t1[] = new float[3];
		float omega_t2[] = new float[3];
		set(omega_t0, omegaS);
		set(omega_t2, omegaT);
		float[] omega_q_t0 = pointAsQuaternion(omega_t0);
		float[] omega_q_t1;
		float[] omega_q_t2 = pointAsQuaternion(omega_t2);
		// temps
		float[] qtmp = new float[4];
		
		// WORKING LOOP
		
		for(int i = 0; i < 500; i++) {
			// PRELIMINARY CALCULATIONS of torque (angular accel as eps)
			// Let t0 = t, t1 = t + dt / 2, t2 = t + dt . (dt in sense dt1)
			// float[] qtmp = addNewQtoScalar(multiplyNewQbyScalar(omega_q_t0, dt1 / 2f), 1);multiplyQbyQ(q_t1, qtmp, q_t0);normalizeQ(q_t1);
			q_next(q_t1, q_t0, omega_q_t0, dt1);
			multiplyQbyQInto(qtmp, q_t2, inverseQ(q_t1));
			omega_q_t1 = multiplyQbyScalar(addQtoScalar(qtmp, -1f), 2f / dt1);
			copyToVector(omega_t1, omega_q_t1);
			// angular accs calcs & scaling if eps is greater eps_max () in function
			float[] eps_t0 = ang_acc_calc(omega_t1, omega_t0, eps_max, dt1);
			float[] eps_t1 = ang_acc_calc(omega_t2, omega_t1, eps_max, dt1);
			// ACTUAL CALCULATIONS
			omega_t1 = add(omega_t0, multiply(eps_t0, dt1));
			omega_q_t1 = pointAsQuaternion(omega_t1);
			// qtmp = addNewQtoScalar(multiplyNewQbyScalar(omega_q_t1, dt1 / 2f), 1);multiplyQbyQ(q_t2, qtmp, q_t1);normalizeQ(q_t2);
			q_next(q_t2, q_t1, omega_q_t1, dt1);
			omega_t2 = add(omega_t1, multiply(eps_t1, dt1));
			omega_q_t2 = pointAsQuaternion(omega_t2);
			// copy end(2) to start(0) 
			copyToOtherQ(omega_q_t0, omega_q_t2);
			copyToVector(omega_t0, omega_q_t0);
			copyToOtherQ(q_t0, q_t2);
			
			// update target
			q_next(qtmp, qT, pointAsQuaternion(omegaT), dt1);
			copyToOtherQ(qT, qtmp);
			
			// copy target(T) to end(2)
			set(omega_t2, omegaT);
			omega_q_t2 = pointAsQuaternion(omega_t2);
			copyToOtherQ(q_t2, qT);
			
			// Trace
			System.out.println("i = " + i + " T 1st " + Arrays.toString(eps_t0) + " T 2nd " + Arrays.toString(eps_t1) + " orient " + Arrays.toString(q_t0));
			System.out.println("qT orient " + Arrays.toString(qT) + " diff q's " + Arrays.toString(subtractQfromQ(qT, q_t0)));
		}
	}
}
