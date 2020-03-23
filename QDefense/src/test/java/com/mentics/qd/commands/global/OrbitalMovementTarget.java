package com.mentics.qd.commands.global;

import static com.mentics.math.quaternion.Quaternion.rotatePoint;
import static com.mentics.math.vector.VectorUtil.ZERO;
import static com.mentics.math.vector.VectorUtil.addInto;
import static com.mentics.math.vector.VectorUtil.isSame;
import static com.mentics.math.vector.VectorUtil.multiplyInto;
import static com.mentics.math.vector.VectorUtil.add;
import static com.mentics.math.vector.VectorUtil.newArbitraryPerpendicular;
import static com.mentics.math.vector.VectorUtil.cross;
import static com.mentics.math.vector.VectorUtil.subtract;
import static com.mentics.math.vector.VectorUtil.normalize;
import static com.mentics.math.vector.VectorUtil.projectVectorOnAnotherV;
import static com.mentics.math.vector.VectorUtil.random3DVectorUnit;
import static com.mentics.math.vector.VectorUtil.set;
import static com.mentics.math.vector.VectorUtil.zero;
import static com.mentics.qd.items.ItemUtil.updatePhysics;

import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.model.PointMoveTarget;

public class OrbitalMovementTarget {
	
	float radius;
	float velAbs;
	float w;
	// TARGET
	// Portable motion = motion of wagon where rotor stands. Index e.
	PointMoveTarget pmtE = new PointMoveTarget();
	// Relative motion = rotation around some axis moving with wagon relative to wagon motion. Index r. Base ZERO.
	PointMoveTarget pmtR = new PointMoveTarget();
	// Absolute motion will be our target = sum of previous two
	PointMoveTarget pmtA = new PointMoveTarget();
	// Accelerations
	float[] accE = new float[3];
	float[] accR = new float[3];
	float[] accA = new float[3];
	float[] accS = new float[3];
	
	public OrbitalMovementTarget(MovingThing center, float radius, float velAbs, float[] pointS, float[] pvelocityS) {
		// Rotation properties
		this.radius = radius;
		this.velAbs = velAbs;
		w = velAbs / radius;
		set(pmtE.position, center.position);
		set(pmtE.velocity, center.velocity);
		//INITIALIZATION OF ROTATION
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
		
		pmtA.position = add(pmtE.position, pmtR.position);
		pmtA.velocity = new float[]{0, 0, 0};
	}
	
	public float[] getAcceleration(MovingThing mthCom, float[] pointS, float[] pvelocityS, float dt) {
		// calculating absolute target velocity
		zero(pmtA.velocity);
		addInto(pmtA.velocity, pmtR.velocity);
		addInto(pmtA.velocity, pmtE.velocity);
		// calculating portable target acceleration
		zero(accE);
		PointMoveTarget targECom = new PointMoveTarget();
		set(targECom.position, mthCom.position);
		set(targECom.velocity, mthCom.velocity);
		ItemUtil.moveToward(accE, pmtE.position, pmtE.velocity, 10, 1, targECom, dt);
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
		// calculating absolute target acceleration
		zero(accA);
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
//		updatePhysics(pointS, pvelocityS, accS, dt);
		return accS;
	}

}
