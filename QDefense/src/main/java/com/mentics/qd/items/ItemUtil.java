package com.mentics.qd.items;

import static com.mentics.math.quaternion.Quaternion.copyToOtherQ;
import static com.mentics.math.quaternion.Quaternion.rotateQuaternion;
import static com.mentics.math.quaternion.Quaternion.versor;
import static com.mentics.math.vector.VectorUtil.addInto;
import static com.mentics.math.vector.VectorUtil.isCoplanar3Vectors;
import static com.mentics.math.vector.VectorUtil.isZero;
import static com.mentics.math.vector.VectorUtil.magnitude;
import static com.mentics.math.vector.VectorUtil.multiplyInto;
import static com.mentics.math.vector.VectorUtil.normalize;
import static com.mentics.math.vector.VectorUtil.set;
import static com.mentics.math.vector.VectorUtil.subtract;
import static com.mentics.math.vector.VectorUtil.subtractInto;
import static com.mentics.math.vector.VectorUtil.zero;

import java.util.List;

import com.mentics.math.vector.VectorUtil;
import com.mentics.physics.ThrustControl;
import com.mentics.qd.model.MoveTarget;

/**
 * Useful little static things for Items.
 */
public class ItemUtil {
	public static final float DISTANCE_EPS = 1e-4f;
	public static final float SPEED_EPS = 1e-5f;
	public static final float EPS_CALC = 1e-6f;
	public static final float EPS_COMPARE = 1e-3f;
	public static float MAX_FORCE = 10f; // 10 g m/s^2
	public static float ENERGY_CONSUMPTION_PER_UNIT_MOMENTUM = Quip.QUIP_ENERGY_GENERATION
			/ MAX_FORCE;

	public static float moveToward(float[] resultForce, float[] position,
			float[] velocity, float maxForce, float mass, float[] target,
			float[] targetVelocity, float duration) {
		final float[] temp = new float[3]; // temp will be direction. TODO:
											// reuse? or unroll calcs?

		// Relative velocity
		float[] relVel = new float[3];
		set(relVel, targetVelocity);
		subtractInto(relVel, velocity);

		float[] result = new float[3];
		ThrustControl.moveToward3D(result, temp, relVel, maxForce / mass,
				duration);

		multiplyInto(result, mass);
		addInto(resultForce, result);

		return 1f;
	}

	/**
	 * This does not do max_force capping.
	 *
	 * @param resultForce
	 *            the resultant force is added to this array
	 * @return calculated ratio
	 */
	public static float moveToward(float[] resultForce, float[] position,
			float[] velocity, float maxForce, float mass, MoveTarget target,
			float duration) {
		final float[] temp = new float[3]; // temp will be direction. TODO:
											// reuse? or unroll calcs?

		target.shortestPath(temp, position);

		// Relative velocity ??? GGGGGGGGGGGGGGGG
		float[] relVel = new float[3];
		target.relativeVelocity(relVel, velocity);
		// set(relVel, velocity);
		// subtract(relVel, relVel);

		float[] result = new float[3];
		// ThrustControl.moveToward3D(result, temp, velocity, maxForce / mass,
		// duration);
		ThrustControl.moveToward3D(result, temp, relVel, maxForce / mass,
				duration);

		multiplyInto(result, mass);
		addInto(resultForce, result);

		return 1f;
	}

	public static void updatePhysics(float[] position, float[] velocity,
			float[] acc, float duration) {
		// TODO: I don't like doing this here since it's modifying values in the
		// wrong place and possibly from wrong
		// thread, but... want to do the concept of it for now.
		if (isZero(velocity)) {
			zero(velocity);
		}
		final float[] temp = new float[3];
		set(temp, velocity);
		multiplyInto(temp, duration);
		addInto(position, temp);
		set(temp, acc);
		multiplyInto(temp, duration);
		addInto(velocity, temp);
		// Add the 1/2 a t^2 term
		multiplyInto(temp, duration / 2f);
		addInto(position, temp);
	}

	public static void updatePhysics(float[] position, float[] velocity,
			float[] force, float mass, float duration) {
		// TODO: I don't like doing this here since it's modifying values in the
		// wrong place and possibly from wrong
		// thread, but... want to do the concept of it for now.
		if (isZero(velocity)) {
			zero(velocity);
		}
		final float[] temp = new float[3]; // TODO: reuse?
		set(temp, velocity);
		multiplyInto(temp, duration);
		addInto(position, temp);
		set(temp, force);
		multiplyInto(temp, duration / mass);
		addInto(velocity, temp);
		// Add the 1/2 a t^2 term
		multiplyInto(temp, duration / 2f);
		addInto(position, temp);
	}

	public static void updatePhysicsRotational(float[] orient, float[] angVel,
			float[] angAcc, float timeStep) {
		// Update position
		float[] angVelAxis = VectorUtil.copy(angVel);
		float speed = VectorUtil.normalize(angVelAxis);
		float[] velQ = versor(angVelAxis, speed * timeStep / 2);
		// System.out.println("test vel: "+parAngSpeed*timeStep+", q: "+angleQ(velQ));////////////////////////
		copyToOtherQ(orient, rotateQuaternion(orient, velQ));
		// 1/2at^2 part
		float[] angAccAxis = VectorUtil.copy(angAcc);
		float angAccMag = VectorUtil.normalize(angAccAxis);
		float[] accQ = versor(angAccAxis, 0.5f * angAccMag * timeStep
				* timeStep / 2);
		copyToOtherQ(orient, rotateQuaternion(orient, accQ));
		// Update velocity
		VectorUtil.multiplyInto(angAcc, timeStep);
		VectorUtil.addInto(angVel, angAcc);
	}

	public static void brake(Item item, float duration) {
		// TODO: we could optimize avoiding heap allocation by doing math
		// directly here instead of calling vector
		// methods
		final float speed = magnitude(item.velocity);
		if (speed < SPEED_EPS) {
			return;
		}
		// float duration = (float)nanoDuration/1000000000;
		final float force = Math.min(speed / duration * item.mass, MAX_FORCE);

		final float[] temp = new float[3];
		set(temp, item.velocity);
		multiplyInto(temp, -1f);
		normalize(temp);
		multiplyInto(temp, force);
		addInto(item.initiatedForce, temp);
	}

	public static void relativeBrake(Item item, Item reference, float duration) {
		final float[] relativeVelocity = new float[3];
		set(relativeVelocity, item.velocity);
		subtractInto(relativeVelocity, reference.velocity);
		final float speed = magnitude(relativeVelocity);
		if (speed < SPEED_EPS) {
			return;
		}
		// float duration = (float)nanoDuration/1000000000;
		final float force = Math.min(speed / duration * item.mass, MAX_FORCE);

		final float[] temp = new float[3];
		set(temp, relativeVelocity);
		multiplyInto(temp, -1f);
		normalize(temp);
		multiplyInto(temp, force);
		addInto(item.initiatedForce, temp);
	}

	public static String getName(MovingThing movingThing) {
		if (movingThing instanceof Item) {
			final Item item = (Item) movingThing;
			String name = Long.toString(item.id);
			if (item instanceof Node) {
				name = "Node " + name;
			} else if (item instanceof Quip) {
				name = ((Quip) item).name;
			}
			return name;
		} else {
			return movingThing.toString();
		}
	}

	public static void capForce(float[] force) {
		final float magnitude = magnitude(force);
		if (magnitude > MAX_FORCE) {
			multiplyInto(force, MAX_FORCE / magnitude);
		}
	}

	public static void centroid(float[] result, List<Item> items) {
		for (final Item i : items) {
			VectorUtil.addInto(result, i.position);
		}
		VectorUtil.multiplyInto(result, 1f / items.size());
	}

	public static float[] newLineForCoplanarWithFirstTwo(float[] position,
			Item[] neighbors) {
		Item neighbor1 = null;
		Item neighbor2 = null;
		for (int i = 0; i < neighbors.length; i++) {
			if (neighbor1 == null) {
				neighbor1 = neighbors[i];
			}
			if (neighbor2 == null && neighbor1 != null) {
				neighbor2 = neighbors[i];
				break;
			}
		}

		if (neighbor2 != null
				&& isCoplanar3Vectors(position, neighbor1.position,
						neighbor2.position)) {
			return subtract(neighbor1.position, neighbor2.position);
		}

		return null;
	}
}
