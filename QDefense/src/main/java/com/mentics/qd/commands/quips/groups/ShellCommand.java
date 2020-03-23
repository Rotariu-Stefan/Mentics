package com.mentics.qd.commands.quips.groups;

import static com.mentics.math.vector.VectorUtil.addInto;
import static com.mentics.math.vector.VectorUtil.crossInto;
import static com.mentics.math.vector.VectorUtil.dot;
import static com.mentics.math.vector.VectorUtil.isSame;
import static com.mentics.math.vector.VectorUtil.isZero;
import static com.mentics.math.vector.VectorUtil.multiply;
import static com.mentics.math.vector.VectorUtil.multiplyInto;
import static com.mentics.math.vector.VectorUtil.normalize;
import static com.mentics.math.vector.VectorUtil.randomUnitVecInPlaneNormToCurrVec;
import static com.mentics.math.vector.VectorUtil.set;
import static com.mentics.math.vector.VectorUtil.subtract;
import static com.mentics.math.vector.VectorUtil.subtractInto;
import static com.mentics.math.vector.VectorUtil.zero;
import static com.mentics.qd.datastructures.ArrayTreeUtil.createGroup;
import static com.mentics.qd.datastructures.ArrayTreeUtil.pathToString;
import static com.mentics.qd.items.ItemUtil.MAX_FORCE;
import static com.mentics.qd.items.ItemUtil.capForce;
import static com.mentics.qd.items.ItemUtil.moveToward;
import static com.mentics.qd.items.ItemUtil.updatePhysics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mentics.math.vector.VectorUtil;
import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.ItemProcessor;
import com.mentics.qd.commands.quips.MotionCommand;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.Link;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.MoveTarget;
import com.mentics.qd.model.RadiusMoveTarget;

public class ShellCommand extends GroupCommandBase implements MotionCommand {

	private static final int numOfItems = 5; // Number of items to be linked.
	final float minDist;
	private static final float pushFRatio = 0.25f;
	private static final float outplaneFRatioToPush = 2 * 0.5f;

	// Instance Fields //
	private boolean initialized = false;

	private final MoveTarget moveTarget; // Intended center of the formation
	private final float formationRadius;

	final MovingThing moving = new MovingThing(); // Moving center of the
													// formation
	private transient MoveTarget shellTarget; // Moving target of formation

	private transient float[] temp1 = new float[3];
	private transient float[] temp2 = new float[3];

	private ItemProcessor processor = new ItemProcessor() {
		@Override
		public void process(AllData allData, CommandMgr cmds, float duration,
				Item item) {
			zero(item.force);
			// This isInFormation check might work if it also checked to ensure
			// angular distance between each was a
			// certain minimum, but at that point, we might as well just run the
			// below calculation maybe.
			// if (isInFormation(item)) {
			// set(item.force, moving.force);
			// return;
			// } else {
			moveToward(item.force, item.position, item.velocity, MAX_FORCE,
					item.mass, shellTarget, duration);
			// }

			int groupSize = quip.groupSize(group);
			if (groupSize < 2) {
				return;
			}

			float[] thisDir = new float[3];
			set(thisDir, item.position);
			subtractInto(thisDir, moving.position);
			float thisDistance = normalize(thisDir);
			float fromRadius = (formationRadius + Math.abs(formationRadius
					- thisDistance))
					/ formationRadius;

			Item[] neighbors = quip.getClosestOwnedNodes(item.id,
					item.position, 3);
			for (int i = 0; i < neighbors.length; i++) {
				Item neighbor = neighbors[i];
				if (neighbor == null) {
					continue;
				}
				float[] neighborDir = new float[3];
				set(neighborDir, neighbor.position);
				subtractInto(neighborDir, moving.position);
				float neighborDistance = normalize(neighborDir);
				float angularDistance = 1 - dot(thisDir, neighborDir);
				float minAngularDistance = 2f / (groupSize / 1.5f);
				if (angularDistance < minAngularDistance) {
					float[] between;
					if (angularDistance > 0) {
						between = subtract(item.position, neighbor.position);
						float parallel = dot(between, thisDir);
						subtractInto(between, multiply(thisDir, parallel));
						normalize(between);
					} else {
						between = randomUnitVecInPlaneNormToCurrVec(thisDir);
					}
					float scale = ((minAngularDistance - angularDistance) / minAngularDistance)
							* (MAX_FORCE / fromRadius);
					addInto(item.force, multiply(between, scale));
				}
			}

			if (groupSize > 3) {
				float[] neighborLine = ItemUtil.newLineForCoplanarWithFirstTwo(
						item.position, neighbors);
				if (neighborLine != null) {
					crossInto(neighborLine, thisDir);
					float dist = normalize(neighborLine);
					if (dist == 0) {
						neighborLine = randomUnitVecInPlaneNormToCurrVec(thisDir);
					}
					addInto(item.force,
							multiply(neighborLine, MAX_FORCE / fromRadius));
				}
			}

			capForce(item.force);

			if (true)
				return;

			// Adding interactions between items
			// I think 12 because we can construct from 20 tetrahedral 1
			// icosahedron with 12 vertices with item in the
			// center

			//
			// Item[] neighbors = quip.getClosestOwnedNodes(item.id,
			// item.position, 3);
			// int neighb1st = -1;
			// boolean isPopFound = false;
			// float[] popMF = new float[3];
			// for (int i = 0; i < neighbors.length; i++) {
			// if (neighbors[i] != null) {
			// // System.out.println(i + " " + neighbors[i]);
			// float[] relPos = new float[3];
			// set(relPos, item.position);
			// subtract(relPos, neighbors[i].position);
			// float mutDist = magnitude(relPos);
			// // sure will be need to refine but not bad for begin
			// if (mutDist < minDist) {
			// // Calculation of force acting on line between items
			// if (neighb1st == -1) neighb1st = i;
			// float[] pushMF = new float[3];
			// set(pushMF, relPos);
			// normalize(pushMF);
			// multiply(pushMF, pushFRatio * MAX_FORCE * item.mass);
			// add(item.force, pushMF);
			// // Calculation of out of plane force
			// if (!isPopFound) {
			// float[] v1 = new float[3];
			// float[] v2 = new float[3];
			// shellTarget.shortestPath(v1, item.position);
			// shellTarget.shortestPath(v2, neighbors[i].position);
			// // Case 2 item and center on 1 line
			// if (isParallelVectors(v1, v2)) {
			// set(popMF, randomUnitVecInPlaneNormToCurrVec(relPos));
			// isPopFound = true;
			// // Case 3 item and center on same plane
			// } else if (neighb1st != i) {
			// float[] v3 = new float[3];
			// shellTarget.shortestPath(v3, neighbors[i].position);
			// if (isCoplanar3Vectors(v1, v2, v3)) {
			// set(popMF, v1);
			// cross(popMF, v2);
			// // randomizing for a while which side of normal to choose
			// normalize(popMF);
			// multiply(popMF, (Math.random() < 0.5f) ? -1 : 1);
			// isPopFound = true;
			// }
			// }
			// // prev method for calculation of pop
			// // set(popMF, pushMF);
			// // float[] dirTarg = new float[3];
			// // shellTarget.shortestPath(dirTarg, item.position);
			// // cross(popMF, dirTarg);
			// // normalize(popMF);
			// // if (isSame(popMF, ZERO)) {
			// // popMF = random3DVectorUnit();
			// // }
			//
			// }
			// }
			// }
			// }
			// multiply(popMF, outplaneFRatioToPush * pushFRatio * MAX_FORCE);
			// add(item.force, popMF);
			// // This is far not exact but not bad for begin
			// capForce(item.force);
		}
	};

	// Constructors //

	public ShellCommand(Quip quip, short[] group, MoveTarget moveTarget,
			float formationRadius) {
		super(CommandMgrQueue.MOVEMENT, quip, group);
		this.moveTarget = moveTarget;
		this.formationRadius = formationRadius;
		shellTarget = new RadiusMoveTarget(moving, formationRadius);
		int x = quip.groupSize(group);
		final float BRUTE_COEFF = 0.9f;
		minDist = (0.314572f + 17.4927f / x / x / x - 19.8667f / x / x + 8.93104f / x)
				* formationRadius * BRUTE_COEFF;
		System.out.println("Shell Formation is being made by group "
				+ pathToString(group) + " from " + quip.name);

		System.out.println("minDist is " + minDist);
	}

	// Public Methods //

	@Override
	public void run(AllData allData, CommandMgr cmds, float duration) {
		if (!initialized) {
			if (quip.groupSize(group) > 0) {
				init(allData);
			}
		}

		// Creating Links between group of Nodes. Each node is linked to maximum
		// of 'numOfItems'.
		// TODO: create links should only happen in init and then when a new
		// node is added to group
		// List<List<Link>> linksList = createLinks(getSubTree(quip.nodes,
		// group));

		zero(moving.force);
		moveToward(moving.force, moving.position, moving.velocity, MAX_FORCE,
				moving.mass, moveTarget, duration);
		capForce(moving.force);

		allData.processGroup(quip, group, cmds, duration, processor);

		// moveTarget.relativeVelocity(temp1, motion.velocity);
		// float mag = normalize(temp1);
		// if (validAboveZero(mag)) {
		// multiply(temp1, mag*motion.mass / duration);
		// add(motion.force, temp1);
		// }

		// System.out.println("formation center: " +
		// Arrays.toString(motion.position));

		// Run physics on it because it's not an item, so won't run separately.
		// This way we can move the formation
		// center.
		updatePhysics(moving.position, moving.velocity, moving.force,
				moving.mass, duration);
	}

	private void init(AllData allData) {
		assert isZero(moving.position);
		moving.mass = 0;

		float[] tmp = { 0f, 0f, 0f };

		allData.processGroup(quip, group, null, 0, (allData1, cmds, duration,
				item) -> {
			// Centroid calc
				addInto(moving.position, item.position);
				// Mass calc
				moving.mass += item.mass;

				// CoM velocity calc
				set(tmp, item.velocity);
				multiplyInto(tmp, item.mass);
				addInto(moving.velocity, tmp);
				zero(tmp);
			});

		// moving.mass /= 10f;
		multiplyInto(moving.position, 1f / quip.groupSize(group));
		initialized = true;

		multiplyInto(moving.velocity, 1f / moving.mass);
		// TODO: possibly have moving.mass dependent on average distance of
		// items from CoM and maybe update over time
		moving.mass /= moving.mass / 2;
		System.out.println("AFTER INITIALIZATION");
		System.out.println("com pos ___ " + Arrays.toString(moving.position));
		System.out.println("com vel ___ " + Arrays.toString(moving.velocity));
	}

	private List<List<Link>> createLinks(Object subtree) {
		Object[] tree = null;
		if (!(subtree instanceof Object[])) {
			Object[] ob = new Object[2];
			ob[0] = subtree;
			tree = createGroup(ob, group);
		} else {
			tree = (Object[]) subtree;
		}
		List<List<Link>> linkList = new ArrayList<List<Link>>(tree.length);
		for (int i = 0; i < (tree.length - 1); i++) {
			int counter = 0;
			List<Link> arr = new ArrayList<>(numOfItems);
			for (int j = i + 1; j < tree.length; j++) {
				if (counter >= numOfItems)
					break;
				// Find the distance between these two nodes.
				if (null != tree[i] && null != tree[j]) {
					if (tree[i] instanceof Node && tree[j] instanceof Node) {
						float distance = VectorUtil.distance(
								((Node) tree[i]).position,
								((Node) tree[j]).position);
						if (1 >= distance) {
							// Create Link between these nodes
							Link link = new Link(counter++);
							link.startId = ((Node) tree[i]).id;// From this node
							link.endId = ((Node) tree[j]).id;// To this node
							link.endPosition = moving.position;// I am not sure
																// if
																// link.endPosition
																// means to move
																// till
							// this position with this link
							arr.add(link);
						}
					}
				}
			}
			linkList.add(arr);
		}
		return linkList;
	}

	@Override
	public String getShape() {
		return "Shell";
	}

	@Override
	public float getParam() {
		return formationRadius;
	}

	@Override
	public String getLandmarkName() {
		return ItemUtil.getName(moveTarget.getLandmark());
	}

	@Override
	public float getDistance() {
		return ((RadiusMoveTarget) moveTarget).getRadius();
	}

	public boolean isInFormation(Item i) {
		float[] relPos = new float[3];
		shellTarget.shortestPath(relPos, i.position);
		float[] relVel = new float[3];
		shellTarget.relativeVelocity(relVel, i.velocity);
		boolean posInForm = isSame(relPos, new float[] { 0f, 0f, 0f });
		boolean velInForm = isSame(relVel, new float[] { 0f, 0f, 0f });
		return posInForm && velInForm;
	}
}
