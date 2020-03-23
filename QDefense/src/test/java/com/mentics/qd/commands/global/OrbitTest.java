package com.mentics.qd.commands.global;
import static com.mentics.qd.items.ItemUtil.*;
import static com.mentics.math.vector.VectorUtil.*;
import static com.mentics.math.quaternion.Quaternion.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.mentics.math._float.FloatUtil;
import com.mentics.math.vector.VectorUtil;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.model.PointMoveTarget;
import com.mentics.qd.model.RadiusMoveTarget;

public class OrbitTest {
	
	@Test
	public void constantRotCheckXYPlane() {
		float[] center = {0, 0, 0};
		float radius = 10f;
		float[] point = {0, radius, 0};
		float velAbs = 10f;
		float omega = velAbs / radius;
		float[] velocity = {velAbs, 0, 0};
		float dt = 0.1f;
		int numIter = 100;
		float[] acc = new float[3];
		for (int i = 0; i < numIter; i++) {
			float theta = omega * dt;
			float[] radDir = subtract(center, point);
			normalize(radDir);
			multiplyInto(radDir, velAbs * (float)Math.sin(theta) / dt); // - removed cause already to center directed
			float[] tanDir = new float[3];
			set(tanDir, velocity);
			normalize(tanDir);
			multiplyInto(tanDir, -velAbs * (1 - (float)Math.cos(theta)) / dt); // - keep cause slowing in this dir discretly
			zero(acc);
			addInto(acc, radDir);
			addInto(acc, tanDir);
			updatePhysics(point, velocity, acc, dt);
			System.out.print("iter " + i + " Point pos is " + Arrays.toString(point));
			System.out.println(" at radius " + distance(center, point));
			System.out.println(" velocity " + Arrays.toString(velocity) + " |v| " + magnitude(velocity));
		}
	}
	
	@Test
	public void constantRotCheckGeneralPlane() {
		float[] center = {0, 0, 0};
		float radius = 10f;
		final float sqrt3rev = (float)(1.0/Math.sqrt(3));
		final float rx0 = sqrt3rev * radius;
		float[] point = {rx0, rx0, rx0};
		float velAbs = 10f;
		final float sqrt6rev = (float)(1.0/Math.sqrt(6));
		float omega = velAbs / radius;
		final float vx0 = sqrt6rev * velAbs;
		float[] velocity = {-vx0, -vx0, 2 * vx0};
		float dt = 0.1f;
		int numIter = 100;
		float[] acc = new float[3];
		for (int i = 0; i < numIter; i++) {
			float theta = omega * dt;
			float[] radDir = subtract(center, point);
			normalize(radDir);
			multiplyInto(radDir, velAbs * (float)Math.sin(theta) / dt); // - removed cause already to center directed
			float[] tanDir = new float[3];
			set(tanDir, velocity);
			normalize(tanDir);
			multiplyInto(tanDir, -velAbs * (1 - (float)Math.cos(theta)) / dt); // - keep cause slowing in this dir discretly
			zero(acc);
			addInto(acc, radDir);
			addInto(acc, tanDir);
			updatePhysics(point, velocity, acc, dt);
			System.out.print("iter " + i + " Point pos is " + Arrays.toString(point));
			System.out.print(" at radius " + distance(center, point));
			System.out.println(" velocity " + Arrays.toString(velocity) + " |v| " + magnitude(velocity));
		}
	}
	
	@Test
	public void transitionToOrbMoveSimple() {
		// Target
		float[] centerSph = {0, 0, 0};
		float[] centerSphVel = {0, 0, 0};
		// Subject
		float[] point = {0, 20, 0};
		float[] pvelocity = {1, 0, 0};//{1, 0, 0}
		float[] acc = {0, 0, 0};
		// Rotation properties
		float radius = 10f;
		float velAbs = 10f * 1;
		float omega = velAbs / radius;
		// Physical properties
		float dt = 0.1f;
		int numIter = 100;
		// Targeting roots
		// Center of sphere of possible rotation
		MovingThing center = new MovingThing();
		center.position = centerSph;
		center.velocity = centerSphVel;
		RadiusMoveTarget rotSphere = new RadiusMoveTarget(center, radius);
		// Rotating point on sphere as target
		PointMoveTarget trgOnSphr = new PointMoveTarget();
		rotSphere.shortestPath(trgOnSphr.position, point);
		addInto(trgOnSphr.position, point);
		float[] normal = cross(point, pvelocity);
		if(isSame(normal, ZERO)) {
			normal = newArbitraryPerpendicular(point);
		}
		normalize(normal);
		float[] tangDir = rotatePoint(point, normal, (float) (Math.PI / 2f));
		// Calculating tangent direction of velocity to find plane of rotation
		float[] vtang = projectVectorOnAnotherV(pvelocity, tangDir);
		if(isSame(vtang, ZERO)) {
			set(vtang, tangDir);
		}
		normalize(vtang);
		multiplyInto(vtang, velAbs);
		set(trgOnSphr.velocity, vtang);
		
		// Loop
		final float EPSILON_ROT = 0.001f;
		for (int i = 0; i < numIter; i++) {
			zero(acc);
			// Calculating accel for target to perform close to rotational move
			float theta = omega * dt;
			float[] radDirTarg = subtract(center.position, trgOnSphr.position);
			normalize(radDirTarg);
			multiplyInto(radDirTarg, velAbs * (float)Math.sin(theta) / dt); // - removed cause already to center directed
			float[] tangDirTarg = new float[3];//rotatePoint(radDirTarg, normal, (float) (Math.PI / 2f));
			set(tangDirTarg, trgOnSphr.velocity);
			normalize(tangDirTarg);
			multiplyInto(tangDirTarg, -velAbs * (1 - (float)Math.cos(theta)) / dt); // - keep cause slowing in this dir discretly
			addInto(acc, radDirTarg);
			addInto(acc, tangDirTarg);
			updatePhysics(trgOnSphr.position, trgOnSphr.velocity, acc, dt);
			System.out.println("targ pos " + Arrays.toString(trgOnSphr.position) + " targ vel " + Arrays.toString(trgOnSphr.velocity));
			System.out.println("Checking dot product " + dot(normal, tangDir));
			// Additional acceleration for subject to reach target
			ItemUtil.moveToward(acc, point, pvelocity, 10, 1, trgOnSphr, dt);
			updatePhysics(point, pvelocity, acc, dt);
			System.out.print("iter " + i + " Point pos is " + Arrays.toString(point));
			System.out.print(" at radius " + distance(center.position, point));
			System.out.println(" velocity " + Arrays.toString(pvelocity) + " |v| " + magnitude(pvelocity));
			if(VectorUtil.isSame(point, trgOnSphr.position, EPSILON_ROT) && VectorUtil.isSame(pvelocity, trgOnSphr.velocity, EPSILON_ROT)) break;
		}
	}
	
	@Test
	public void checkPMTConstantVelocity() {
		float[] point = {0, 20, 0};
		float[] pvelocity = {1, 0, 0};
		float[] acc = {0, 0, 0};
		float dt = 0.1f;
		int numIter = 100;
		PointMoveTarget trgOnSphr = new PointMoveTarget();
		trgOnSphr.velocity = new float[]{1, 0, 0};
		trgOnSphr.position = new float[]{4, 4, 0};
		for (int i = 0; i < numIter; i++) {
			if(isSame(point, trgOnSphr.position) && isSame(pvelocity, trgOnSphr.velocity)) break;
			zero(acc);
			ItemUtil.moveToward(acc, point, pvelocity, 10, 1, trgOnSphr, dt);
			updatePhysics(point, pvelocity, acc, dt);
			updatePhysics(trgOnSphr.position, trgOnSphr.velocity, new float[]{0, 0, 0}, dt);
			System.out.print("iter " + i + " Point pos is " + Arrays.toString(point));
			System.out.println(" velocity " + Arrays.toString(pvelocity) + " |v| " + magnitude(pvelocity));
			System.out.print("target " + " pos is " + Arrays.toString(trgOnSphr.position));
			System.out.println(" velocity " + Arrays.toString(trgOnSphr.velocity) + " |v| " + magnitude(trgOnSphr.velocity));
		}
	}
	
	@Test
	public void checkPMTWithAccel() {
		float[] point = {0, 20, 0};
		float[] pvelocity = {1, 0, 0};
		float[] acc = {0, 0, 0};
		float dt = 0.1f;
		int numIter = 100;
		PointMoveTarget trgOnSphr = new PointMoveTarget();
		trgOnSphr.velocity = new float[]{1, 0, 0};
		trgOnSphr.position = new float[]{4, 4, 0};
		float[] trgAcc = new float[]{1, 1, 1};
		for (int i = 0; i < numIter; i++) {
			if(isSame(point, trgOnSphr.position) && isSame(pvelocity, trgOnSphr.velocity)) break;
			zero(acc);
			ItemUtil.moveToward(acc, point, pvelocity, 10, 1, trgOnSphr, dt);
			addInto(acc, trgAcc);
			updatePhysics(point, pvelocity, acc, dt);
			updatePhysics(trgOnSphr.position, trgOnSphr.velocity, trgAcc, dt);
			System.out.print("iter " + i + " Point pos is " + Arrays.toString(point));
			System.out.println(" velocity " + Arrays.toString(pvelocity) + " |v| " + magnitude(pvelocity));
			System.out.print("target " + " pos is " + Arrays.toString(trgOnSphr.position));
			System.out.println(" velocity " + Arrays.toString(trgOnSphr.velocity) + " |v| " + magnitude(trgOnSphr.velocity));
		}
	}
	
	
	
	@Test
	public void testToFindRootOfEvil() {
		PointMoveTarget pmt = new PointMoveTarget();
		pmt.position = new float[] {5.272235f, -6.6564684f, 5.272235f};
		pmt.velocity = new float[] {-4.7095222f, -7.465585f, -4.7095222f};
		float[] point = {-25.094198f, 3.3435276f, -25.094198f};
		float[] pvelocity = {-26.044048f, -7.465589f, -26.044048f};//{1, 0, 0}
		float[] acc = {0, 0, 0};
		float dt = 0.1f;
		for (int i = 0; i < 100; i++) {
			zero(acc);
			ItemUtil.moveToward(acc, point, pvelocity, 10, 1, pmt, dt);
//			ItemUtil.moveToward(acc, point, pvelocity, 10, 1, pmt.position, pmt.velocity, dt); AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA NOOOOOOOOOOOOO !!!
			updatePhysics(point, pvelocity, acc, dt);
			updatePhysics(pmt.position, pmt.velocity, new float[]{0, 0, 0}, dt);
			System.out.println("iter " + i + " Point pos is " + Arrays.toString(point) + " PMT is " + Arrays.toString(pmt.position));
			System.out.println(" Point vel is " + Arrays.toString(pvelocity) + " PMT vel is " + Arrays.toString(pmt.velocity));
			System.out.println("Accel is " + Arrays.toString(acc));
		}
	}
	
	@Test
	public void test3BodyFollowEO() {
		PointMoveTarget pmt1 = new PointMoveTarget();
		pmt1.position = new float[]{8, 8, 8};
		pmt1.velocity = new float[]{1, 1, 1};
		PointMoveTarget pmt2 = new PointMoveTarget();
		pmt2.position = new float[]{4, 4, 4};
		pmt2.velocity = new float[]{0, 0, 0};
		PointMoveTarget pmt3 = new PointMoveTarget();
		pmt3.position = new float[]{0, 0, 0};
		pmt3.velocity = new float[]{0, 0, 0};
		float[] acc1 = {0, 0, 0};
		float[] acc2 = new float[3];
		float[] acc3 = new float[3];
		float dt = 0.1f;
		for(int i = 0; i < 100; i++) {
			zero(acc2);
			ItemUtil.moveToward(acc2, pmt2.position, pmt2.velocity, 10, 1, pmt1, dt);
			zero(acc3);
			ItemUtil.moveToward(acc3, pmt3.position, pmt3.velocity, 10, 1, pmt2, dt);
			addInto(acc3, acc2);
			updatePhysics(pmt1.position, pmt1.velocity, acc1, dt);
			updatePhysics(pmt2.position, pmt2.velocity, acc2, dt);
			updatePhysics(pmt3.position, pmt3.velocity, acc3, dt);
			System.out.println("iter " + i);
			System.out.println(" pmt1 pos is " + Arrays.toString(pmt1.position) + " vel is " + Arrays.toString(pmt1.velocity));
			System.out.println(" pmt2 pos is " + Arrays.toString(pmt2.position) + " vel is " + Arrays.toString(pmt2.velocity));
			System.out.println(" pmt3 pos is " + Arrays.toString(pmt3.position) + " vel is " + Arrays.toString(pmt3.velocity));
		}
	}
	
	@Test
	public void testTargetingHelixFromScratch() {
		// Rotation properties
		float radius = 10f;
		float velAbs = 5f;
		float w = velAbs / radius;
		// Physical properties
		float dt = 0.1f;
		int numIter = 100;
		// SOURCE
		// Source item which will perform targeting
		float[] pointS = {10, -10, 10};//{0, 20, 0}
		float[] pvelocityS = {2, 2, -2};
		float[] accS = {0, 0, 0};
		// TARGET
		// Portable motion = motion of wagon where rotor stands. Index e.
		PointMoveTarget pmtE = new PointMoveTarget();
		pmtE.position = new float[]{10, 3, 5};
		pmtE.velocity = new float[]{2, -2, 2};// {2, 0, 0}
		float[] accE = new float[]{0, 0, 0};//new float[]{1, 1, 1};
		// Relative motion = rotation around some axis moving with wagon relative to wagon motion. Index r. Base ZERO.
		PointMoveTarget pmtR = new PointMoveTarget();
//		pmtR.position = new float[]{0, radius, 0};
//		pmtR.velocity = new float[]{0, 0, velAbs};
		float[] accR = new float[]{0, 0, 0};
		
		//PROPERLY INITIALIZATION OF ROTATION
		// Find vector relative speed point to orb center
		float[] velPtRelOrb = subtract(pvelocityS, pmtE.velocity);
		// Point rel to center of sphere
		pmtR.position = subtract(pointS, ZERO);
		normalize(pmtR.position);//random3DVectorUnit;
		if(isSame(pmtR.position, ZERO)) {
			pmtR.position = random3DVectorUnit();
		}
		multiplyInto(pmtR.position, radius);
		// Find normal to plane of rotation
		float[] normal = cross(pmtR.position, velPtRelOrb);// EEEEEEEEEEE (point,)
		if(isSame(normal, ZERO)) {
			normal = newArbitraryPerpendicular(pmtR.position);
		}
		normalize(normal);
		// Find tangent direction of move
		float[] tangDir0 = rotatePoint(pmtR.position, normal, (float) (Math.PI / 2f));
		// Find initial rotation of target orb
		float[] vtang = projectVectorOnAnotherV(velPtRelOrb, tangDir0);
		if(isSame(vtang, ZERO)) {
			set(vtang, tangDir0);
		}
		normalize(vtang);
		multiplyInto(vtang, velAbs);
		set(pmtR.velocity, vtang);
		
		// Absolute motion will be our target = sum of previous two
		PointMoveTarget pmtA = new PointMoveTarget();
		pmtA.position = add(pmtE.position, pmtR.position);
		pmtA.velocity = new float[]{0, 0, 0};
		float[] accA = new float[]{0, 0, 0};
		// run loop
		for(int i = 0; i < numIter; i++) {
			// calculating absolute target velocity
			zero(pmtA.velocity);
			addInto(pmtA.velocity, pmtR.velocity);
			addInto(pmtA.velocity, pmtE.velocity);
			// calculating absolute target acceleration
			zero(accA);
			// here will be IU for E UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU
			zero(accE);//accE = new float[]{1, 1, 1};
			//...................
			// centripetal accel for R
			zero(accR);
			float theta = w * dt;
			float[] radDirToCenter = subtract(ZERO, pmtR.position);
			normalize(radDirToCenter);
			multiplyInto(radDirToCenter, velAbs * (float)Math.sin(theta) / dt); // - removed cause already to center directed
			float[] tangDir = new float[3];//rotatePoint(radDirTarg, normal, (float) (Math.PI / 2f));
			set(tangDir, pmtR.velocity);
			normalize(tangDir);
			multiplyInto(tangDir, -velAbs * (1 - (float)Math.cos(theta)) / dt); // - keep cause slowing in this dir discreetly
			addInto(accR, radDirToCenter);
			addInto(accR, tangDir);
			// add ER
			addInto(accA, accE);
			addInto(accA, accR);
			
			// SOURCE PART
			zero(accS);
			ItemUtil.moveToward(accS, pointS, pvelocityS, 10, 1, pmtA, dt);
			addInto(accS, accA);
			
			//Update physics parts
			updatePhysics(pmtE.position, pmtE.velocity, accE, dt);
			updatePhysics(pmtR.position, pmtR.velocity, accR, dt);
			updatePhysics(pmtA.position, pmtA.velocity, accA, dt);
			updatePhysics(pointS, pvelocityS, accS, dt);
			
			//OUTPUT
			System.out.println("iter " + i);
			System.out.println("NEWEST RECHEKS");
			System.out.println("1) Distance to center of rotation is " + distance(pointS, pmtE.position) + " must be " + radius);
			System.out.println("2) Distance to target is " + distance(pointS, pmtA.position) + " must be 0 ?!!");
			System.out.println("3) Velocity of point vector " + Arrays.toString(pvelocityS) + " Abs velocity of targ on sphere " + Arrays.toString(pmtA.velocity));
			System.out.println("4) rel vel pt to center " + Arrays.toString(subtract(pvelocityS, pmtE.velocity)) + " ?= rotator rel vel " + Arrays.toString(pmtR.velocity));
			System.out.println("5) Orbital speed " + magnitude(subtract(pvelocityS, pmtE.velocity)) + " must be " + velAbs);
			System.out.println("1.1.1. PMTE pos is " + Arrays.toString(pmtE.position));
			System.out.println("1.1.2. PMTR pos is " + Arrays.toString(pmtR.position));
			System.out.println("1.1.3. PMTA pos is " + Arrays.toString(pmtA.position));
			System.out.println("1.1.4. PMTS pos is " + Arrays.toString(pointS));
			System.out.println("___________________________________________________________________________________________________");
			
		}
	}
	
	private static final float EPS_POS = 0.1f;
	private static final float EPS_RAD = 0.1f;
	private static final float EPS_VTAN = 0.1f;
	private static final int HIT_COUNT_STOP = 5;
	
	@Test
	public void testGeneralForNewClassVersion() {
		MovingThing mt = new MovingThing();
		mt.position = new float[] {0, 0, 0};
		mt.velocity = new float[] {1, 0, 0};
		// Subject
		float[] pointS = {10, 0, 0};//{5, 11, 14};
		float[] pvelocityS = {-1, 0, 0};//{3, 5, 7};
		float[] acc = {0, 0, 0};
		// Rotation properties
		float radius = 8f;
		float velAbs = 3f * 1;
		// Physical properties
		float dt = 0.1f;
		int numIter = 200;
		// Orbit Object
		OrbitalMovementTarget omt = new OrbitalMovementTarget(mt, radius, velAbs, pointS, pvelocityS);
		// Counter to stop
		int hitCount = 0;
		for (int i = 1; i <= numIter; i++) {
			acc = omt.getAcceleration(mt, pointS, pvelocityS, dt);
			updatePhysics(pointS, pvelocityS, acc, dt);
			updatePhysics(mt.position, mt.velocity, new float[]{0, 0, 0}, dt);
			//OUTPUT
			System.out.println("iter " + i);
			System.out.println("NEWEST RECHEKS");
			System.out.println("1) Distance to center of rotation is " + distance(pointS, omt.pmtE.position) + " must be " + radius);
			System.out.println("2) Distance to target is " + distance(pointS, omt.pmtA.position) + " must be 0 ?!!");
			System.out.println("3) Velocity of point vector " + Arrays.toString(pvelocityS) + " Abs velocity of targ on sphere " + Arrays.toString(omt.pmtA.velocity));
			System.out.println("4) rel vel pt to center " + Arrays.toString(subtract(pvelocityS, omt.pmtE.velocity)) + " ?= rotator rel vel " + Arrays.toString(omt.pmtR.velocity));
			System.out.println("5) Orbital speed " + magnitude(subtract(pvelocityS, omt.pmtE.velocity)) + " must be " + velAbs);
			System.out.println("1.1.1. PMTE pos is " + Arrays.toString(omt.pmtE.position));
			System.out.println("1.1.2. PMTR pos is " + Arrays.toString(omt.pmtR.position));
			System.out.println("1.1.3. PMTA pos is " + Arrays.toString(omt.pmtA.position));
			System.out.println("1.1.4. PMTS pos is " + Arrays.toString(pointS));
			System.out.println("___________________________________________________________________________________________________");
			// CHECKING FOR CONVERGENCE
			boolean isConv = FloatUtil.isSame(distance(pointS, omt.pmtE.position), radius, EPS_RAD);
			isConv = isConv && FloatUtil.isSame(distance(pointS, omt.pmtA.position), 0, EPS_POS);
			isConv = isConv && VectorUtil.isSame(omt.pmtA.position, pointS, EPS_POS);
			isConv = isConv && FloatUtil.isSame(magnitude(subtract(pvelocityS, omt.pmtE.velocity)), velAbs, EPS_VTAN);
			if(isConv) {
				hitCount++;
			} else {
				hitCount = 0;
			}
			if(hitCount == HIT_COUNT_STOP) {
				System.out.println("Converged after iteration " + i);
				break;
			}
		}
	}
	
	@Test
	public void RandomDataTests() {
		Random r = new Random(1024);
		int numTests = 100;
		final float MT_POS_RANGE = 8;
		final float MT_VEL_RANGE = 4;
		final float PS_POS_RANGE = 30;
		final float PS_VEL_RANGE = 20;
		final float MAX_RADIUS = 9f;
		final float MAX_VABS = 9f;
		float dt = 0.1f;
		int numIter = 200;
		for(int n = 1; n <= numTests; n++) {
			MovingThing mt = new MovingThing();
			mt.position = new float[] {(r.nextFloat() - 0.5f) * MT_POS_RANGE, (r.nextFloat() - 0.5f) * MT_POS_RANGE, (r.nextFloat() - 0.5f) * MT_POS_RANGE};
			mt.velocity = new float[] {(r.nextFloat() - 0.5f) * MT_VEL_RANGE, (r.nextFloat() - 0.5f) * MT_VEL_RANGE, (r.nextFloat() - 0.5f) * MT_VEL_RANGE};
			float[] pointS = {(r.nextFloat() - 0.5f) * PS_POS_RANGE, (r.nextFloat() - 0.5f) * PS_POS_RANGE, (r.nextFloat() - 0.5f) * PS_POS_RANGE};
			float[] pvelocityS = {(r.nextFloat() - 0.5f) * PS_VEL_RANGE, (r.nextFloat() - 0.5f) * PS_VEL_RANGE, (r.nextFloat() - 0.5f) * PS_VEL_RANGE};
			float[] acc = {0, 0, 0};
			float radius = r.nextFloat() * MAX_RADIUS + 1;
			float velAbs = r.nextFloat() * MAX_VABS + 1;
			OrbitalMovementTarget omt = new OrbitalMovementTarget(mt, radius, velAbs, pointS, pvelocityS);
			int hitCount = 0;
			int i = 1;
			for (; i <= numIter; i++) {
				acc = omt.getAcceleration(mt, pointS, pvelocityS, dt);
				updatePhysics(pointS, pvelocityS, acc, dt);
				updatePhysics(mt.position, mt.velocity, new float[]{0, 0, 0}, dt);
				boolean isConv = FloatUtil.isSame(distance(pointS, omt.pmtE.position), radius, EPS_RAD);
				isConv = isConv && FloatUtil.isSame(distance(pointS, omt.pmtA.position), 0, EPS_POS);
				isConv = isConv && VectorUtil.isSame(omt.pmtA.position, pointS, EPS_POS);
				isConv = isConv && FloatUtil.isSame(magnitude(subtract(pvelocityS, omt.pmtE.velocity)), velAbs, EPS_VTAN);
				if(isConv) {
					hitCount++;
				} else {
					hitCount = 0;
				}
				if(hitCount == HIT_COUNT_STOP) {
					System.out.println("Converged after iteration " + i);
					break;
				}
			}
			Assert.assertTrue("dont converged ", i < numIter - 1);
		}
		
	}

}
