package com.mentics.qd.commands.global;

import static com.mentics.math.vector.VectorUtil.addInto;
import static com.mentics.math.vector.VectorUtil.cross;
import static com.mentics.math.vector.VectorUtil.crossInto;
import static com.mentics.math.vector.VectorUtil.dot;
import static com.mentics.math.vector.VectorUtil.isSame;
import static com.mentics.math.vector.VectorUtil.isZero;
import static com.mentics.math.vector.VectorUtil.magnitude;
import static com.mentics.math.vector.VectorUtil.multiplyInto;
import static com.mentics.math.vector.VectorUtil.normalize;
import static com.mentics.math.vector.VectorUtil.set;
import static com.mentics.math.vector.VectorUtil.subtract;
import static com.mentics.math.vector.VectorUtil.subtractInto;
import static com.mentics.math.vector.VectorUtil.zero;

import java.util.Arrays;
import java.util.function.Supplier;

import com.mentics.math.matrix.Matrix3;
import com.mentics.math.quaternion.Quaternion;
import com.mentics.math.vector.VectorUtil;
import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.physics.ThrustControl;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.items.Camera;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.items.Node;
import com.mentics.qd.model.MutableRadiusMoveTarget;

public class AutoCameraCommand extends CommandBase {

	// Constants //

	public final float MIN_CAMERA_ROTATION_SPEED = 0.1f; // radian per second
	public final float CAMERA_ANGULAR_ACCELERATION = 0.1f; // radian per
															// second^2

	public final float ANGLE_EPS = 0.01f; // radian
	public final float MIN_DISTANCE = 0.1f;

	float rotationSpeed = MIN_CAMERA_ROTATION_SPEED;
	public static final float MAX_CAMERA_EPS = (float) Math.PI * 2 / 40;
	// max // ang // acc // ch // to // static

	// Instance Fields //

	Camera camera;
	private Supplier<Item[]> selection;

	private float[] temp = new float[3];
	private float[] temp1 = new float[3];
	private float[] temp2 = new float[3];

	private float[] versorPlus = new float[4];
	private float[] versorMinus = new float[4];
	private float[] quaternion1 = new float[4];
	private float[] quaternion2 = new float[4];

	private MovingThing centroid = new MovingThing();

	private float[] projectionMatrix = new float[9];

	private MutableRadiusMoveTarget moveTarget = new MutableRadiusMoveTarget(
			centroid, MIN_DISTANCE);

	// EXPERIMENTS
	private boolean isOrbMove = true;
	private boolean isOrbMoveInit = false;
	private OrbitalMovementTarget omt;
	private float vtang = 1f;

	// Constructors //

	public AutoCameraCommand(Camera camera, Supplier<Item[]> selection) {
		super(CommandMgrQueue.MOVEMENT);
		this.camera = camera;
		this.selection = selection;
	}

	// orbital move methods

	private void initOM() {
		Item[] selectedItems = selection.get();
		zero(centroid.position);
		if (selectedItems.length == 0) {
			// set player position
		} else {
			for (Item item : selectedItems) {
				addInto(centroid.position, item.position);
			}
			multiplyInto(centroid.position, 1f / selectedItems.length);
		}

		// float radius = 10f;
		// for (Item item : selectedItems) {
		// float d = distance(centroid.position, item.position);
		// if(d > radius) radius = d;
		// }
		// radius *= 3;

		// checking
		float radius = 7f;
		// centroid.position = new float[]{10, 0, 10}; //prev player pos J !?!

		omt = new OrbitalMovementTarget(centroid, radius, vtang,
				camera.position, camera.velocity);
	}

	private void runOM(float duration) {
		if (!isOrbMoveInit) {
			initOM();
			if (omt == null)
				return;
			isOrbMoveInit = true;// true
			// if(omt == null) return;
		}
		zero(camera.force);

		// Trick for check
		Item[] selectedItems = selection.get();
		zero(centroid.position);
		zero(centroid.velocity);
		if (selectedItems.length == 0) {
			// set player position
		} else {
			for (Item item : selectedItems) {
				addInto(centroid.position, item.position);
				addInto(centroid.velocity, item.velocity);
			}
			multiplyInto(centroid.position, 1f / selectedItems.length);
			multiplyInto(centroid.velocity, 1f / selectedItems.length);
		}
		float radius = 7f;
		omt.getAcceleration(centroid, camera.position, camera.velocity,
				duration);
		// new OrbitalMovementTarget(centroid, radius, vtang, camera.position,
		// camera.velocity);

		// camera rotation

		boolean newWay = false;
		if (newWay) {
			// NEW WAY USING ROTATION TRACKING GENERAL
			// relative ang velocity
			float[] relVelLin = subtract(centroid.velocity, camera.velocity);
			float[] relPos = subtract(centroid.position, camera.position);
			float[] angVelTarg = cross(relPos, relVelLin);
			float dist = magnitude(relPos);
			multiplyInto(angVelTarg, 1 / (dist * dist));
			float[] angVelSrc = camera.angVel;
			float[] angVelRel = subtract(angVelTarg, angVelSrc);
			// relative quaternion
			// float[] qT = Quaternion.from2VectorsQuaternion(from, to);
			// float[] qS = camera.orientationQ;
			// float[] targetRotation = Quaternion.newRotationalDifference(qS,
			// qT);
			float[] targetRotation = Quaternion.from2VectorsQuaternion(
					camera.lookingDirection, relPos);
			float[] applAngAcc = new float[3];
			ThrustControl.moveTowardRotation(applAngAcc, targetRotation,
					angVelRel, MAX_CAMERA_EPS, duration);
			ItemUtil.updatePhysicsRotational(camera.orientationQ,
					camera.angVel, applAngAcc, duration);
			System.out.println("a a a " + Arrays.toString(applAngAcc));
			System.out.println("CAMERA ORIENT Q4 : "
					+ Arrays.toString(camera.orientationQ));
			camera.lookingDirection = new float[] { 1, 0, 0 };
			camera.lookingDirection = Quaternion.rotateVector(
					camera.lookingDirection, camera.orientationQ);
			System.out.println("CAMERA LD : "
					+ Arrays.toString(camera.lookingDirection)
					+ " RP "
					+ Arrays.toString(VectorUtil.multiply(relPos,
							1 / magnitude(relPos))));
			System.out
					.println("CAMERA POS " + Arrays.toString(camera.position));
			camera.up = new float[] { 0, 1, 0 };
			camera.up = Quaternion.rotateVector(camera.up, camera.orientationQ);

		} else {
			// PREVIOUS WAY OF CALCULATION WORKING
			// set(temp, centroid.position);
			set(temp, omt.pmtE.position);
			subtractInto(temp, camera.position);
			// camera.distanceToCenter =
			// magnitude(temp);////////////////////////
			normalize(temp); // now temp is the unit vector towards centroid
			// System.out.println("temp as look to center must be " +
			// java.util.Arrays.toString(temp));
			set(temp1, camera.lookingDirection);
			normalize(temp1);// now temp1 is the unit lookingTo vector
			// System.out.println("temp1 as look to center is " +
			// java.util.Arrays.toString(temp1));
			float cosAngle = dot(temp, temp1);
			crossInto(temp, temp1);
			float sinAngle = magnitude(temp);
			float angle = (float) Math.atan2(sinAngle, cosAngle);
			// float zeroOrOne = normalize(temp); // now temp is the unit
			// rotation axis;
			normalize(temp);
			// System.out.println("temp as axis of rotation " +
			// java.util.Arrays.toString(temp));
			// float rotationAngle = -Math.signum(angle) *
			// Math.min(Math.abs(angle), rotationSpeed * duration);
			float rotationAngle = -Math.signum(angle) * Math.abs(angle);
			set(camera.lookingDirection,
					Quaternion.rotatePoint(temp1, temp, rotationAngle));
			// System.out.println("Camera LD after rotation " +
			// java.util.Arrays.toString(camera.lookingDirection));
			set(temp1, camera.up);
			normalize(temp1);
			if (!isSame(temp1, temp)) {
				set(camera.up,
						Quaternion.rotatePoint(temp1, temp, rotationAngle));
			}
		}

		// set(camera.lookingDirection, newSubtract(centroid.position,
		// camera.position));

		set(camera.force, omt.getAcceleration(centroid, camera.position,
				camera.velocity, duration));

		// System.out.println("Camera velocity " +
		// java.util.Arrays.toString(camera.velocity));
		// System.out.println("Camera force " +
		// java.util.Arrays.toString(camera.force));
		// System.out.println("Camera look dir " +
		// java.util.Arrays.toString(camera.lookingDirection));
		// System.out.println("Camera pos " +
		// java.util.Arrays.toString(camera.position));
		// System.out.println("Centroid pos " +
		// java.util.Arrays.toString(centroid.position));
		// System.out.println("Centroid vel " +
		// java.util.Arrays.toString(centroid.velocity));
	}

	// Public Methods //

	@Override
	public void run(AllData allData, CommandMgr cmds, float duration) {

		if (isOrbMove) {
			runOM(duration);
			return;
		}

		Item[] selectedItems = selection.get();
		if (selectedItems.length == 0 || camera.movedManually) {
			camera.movedManually = false;
			return;
		}

		zero(camera.force);

		zero(centroid.position);
		zero(centroid.velocity);
		for (Item item : selectedItems) {
			addInto(centroid.position, item.position);
			addInto(centroid.velocity, item.velocity);
		}
		multiplyInto(centroid.position, 1f / selectedItems.length);
		multiplyInto(centroid.velocity, 1f / selectedItems.length);

		set(temp, centroid.position);
		subtractInto(temp, camera.position);
		camera.distanceToCenter = magnitude(temp);
		normalize(temp); // now temp is the unit vector towards centroid

		set(temp1, camera.lookingDirection);
		normalize(temp1);// now temp1 is the unit lookingTo vector

		float cosAngle = dot(temp, temp1);
		crossInto(temp, temp1);
		float sinAngle = magnitude(temp);
		float angle = (float) Math.atan2(sinAngle, cosAngle);
		float zeroOrOne = normalize(temp); // now temp is the unit rotation
											// axis;
		if (isZero(temp)) {
			set(temp, camera.up);
			normalize(temp);
		}
		// Rotation
		int accelerationSign = 1;
		if (angle - rotationSpeed * duration < rotationSpeed * rotationSpeed
				/ (2 * CAMERA_ANGULAR_ACCELERATION)) {
			accelerationSign = -1;
		}
		if (Math.abs(angle) > ANGLE_EPS) {
			float rotationAngle = -Math.signum(angle)
					* Math.min(Math.abs(angle), rotationSpeed * duration);

			// PREVIOUS IMPLEMENTATION
			// Quaternion.versor(versorPlus, temp, rotationAngle / 2);
			// Quaternion.versor(versorMinus, temp, -rotationAngle / 2);
			// Rotate looking at vector;
			// Quaternion.multiplyVbyQ(quaternion1, temp1, versorMinus);
			// Quaternion.multiplyQbyQ(quaternion2, versorPlus, quaternion1);
			// Quaternion.copyToVector(temp1, quaternion2);
			// set(camera.lookingDirection, temp1);
			// Rotate up vector
			// Quaternion.multiplyVbyQ(quaternion1, camera.up, versorMinus);
			// Quaternion.multiplyQbyQ(quaternion2, versorPlus, quaternion1);
			// Quaternion.copyToVector(camera.up, quaternion2);
			// Here we compensate for possible rounding errors to keep up vector
			// orthogonal to looking direction
			// multiply(temp1, dot(temp1, camera.up));
			// subtract(camera.up, temp1);
			// normalize(camera.up);

			// NEW IMPLEMENTATION
			// System.out.println("DEBUG Rotational angle is " + rotationAngle);
			set(camera.lookingDirection,
					Quaternion.rotatePoint(temp1, temp, rotationAngle));
			set(temp1, camera.up);
			normalize(temp1);
			if (!isSame(temp1, temp)) {
				set(camera.up,
						Quaternion.rotatePoint(temp1, temp, rotationAngle));
			}

			rotationSpeed += CAMERA_ANGULAR_ACCELERATION * accelerationSign
					* duration;
		} else {
			rotationSpeed = MIN_CAMERA_ROTATION_SPEED;
		}

		moveTarget.radius = Math.max(MIN_DISTANCE,
				viewDistance(selectedItems, centroid.position));
		// Camera, so we use force for movement instead of initiatedForce
		ItemUtil.moveToward(camera.force, camera.position, camera.velocity,
				ItemUtil.MAX_FORCE, camera.mass, moveTarget, duration);
	}

	// Local Methods //

	/**
	 * Finds distance such that all items are in FOV when camera is looking at
	 * viewPoint
	 * 
	 * @param items
	 * @param viewPoint
	 * @return
	 */
	float viewDistance(Item[] items, float[] viewPoint) {
		float maxDistance = -1;
		normalize(camera.up);
		float tanHalfFovX = camera.getTanHalfFovX();
		float tanHalfFovY = (float) Math.tan(camera.fovY / 2);

		set(temp, viewPoint);
		subtractInto(temp, camera.position);
		normalize(temp);// temp is a unit vector towards viewPoint;

		Matrix3.projectionMat3(projectionMatrix, temp);

		Matrix3.multiplyMbyV(temp2, projectionMatrix, camera.up);
		normalize(temp2);// temp2 is unit projection of camera.up

		for (Item item : items) {
			set(temp, item.position);
			subtractInto(temp, camera.position);// temp is pointing from camera
												// towards item

			Matrix3.multiplyMbyV(temp1, projectionMatrix, temp); // temp1
																	// contains
																	// projection
																	// of temp

			float y = Math.abs(dot(temp2, temp1));
			float distanceY = (y + 2 * Node.RADIUS) / tanHalfFovY;

			float x = (float) Math.sqrt(dot(temp1, temp1) - y * y);
			float distanceX = (x + 2 * Node.RADIUS) / tanHalfFovX;

			float distance = Math.max(distanceX, distanceY);
			if (distance > maxDistance) {
				maxDistance = distance;
			}
		}
		return maxDistance;
	}
}