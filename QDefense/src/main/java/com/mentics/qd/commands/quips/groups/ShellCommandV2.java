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
import static com.mentics.qd.datastructures.ArrayTreeUtil.pathToString;
import static com.mentics.qd.items.ItemUtil.MAX_FORCE;
import static com.mentics.qd.items.ItemUtil.capForce;
import static com.mentics.qd.items.ItemUtil.moveToward;
import static com.mentics.qd.items.ItemUtil.updatePhysics;

import java.util.Arrays;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.ItemProcessor;
import com.mentics.qd.commands.quips.MotionCommand;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.MoveTarget;
import com.mentics.qd.model.RadiusMoveTarget;

public class ShellCommandV2 extends GroupCommandBase implements MotionCommand {

	// Instance Fields //
	ShellFormation sf;

	private ItemProcessor processor = new ItemProcessor() {
		@Override
		public void process(AllData allData, CommandMgr cmds, float duration,
				Item item) {
			Item[] neighbors = quip.getClosestOwnedNodes(item.id,
					item.position, 3);
			int groupSize = quip.groupSize(group);
			sf.processItem(duration, item, neighbors, groupSize);
		}
	};

	// Constructors //

	public ShellCommandV2(Quip quip, short[] group, MoveTarget moveTarget,
			float formationRadius) {
		super(CommandMgrQueue.MOVEMENT, quip, group);
		sf = new ShellFormation(moveTarget, formationRadius, quip.groupSize(group));
		System.out.println("Shell Formation is being made by group "
				+ pathToString(group) + " from " + quip.name);
	}

	// Public Methods //

	@Override
	public void run(AllData allData, CommandMgr cmds, float duration) {
		sf.run(allData, cmds, duration, processor); // !!! HERE WARN DUE
													// !!! OVERRIDE REQ
	}

	@Override
	public String getShape() {
		return "Shell";
	}

	@Override
	public float getParam() {
		return sf.getParam();
	}

	@Override
	public String getLandmarkName() {
		return sf.getLandmarkName();
	}

	@Override
	public float getDistance() {
		return sf.getDistance();
	}

}

class ShellFormation {

	private final MoveTarget moveTarget; // Intended center of the formation
	private final float formationRadius;
	final MovingThing moving = new MovingThing(); // Moving center of the
	// formation
	private transient MoveTarget shellTarget; // Moving target of formation
	final float minDist;
	private boolean initialized = false;

	public ShellFormation(MoveTarget moveTarget, float formationRadius, int x) {
		this.moveTarget = moveTarget;
		this.formationRadius = formationRadius;
		shellTarget = new RadiusMoveTarget(moving, formationRadius);
		final float BRUTE_COEFF = 0.9f;
		minDist = (0.314572f + 17.4927f / x / x / x - 19.8667f / x / x + 8.93104f / x)
				* formationRadius * BRUTE_COEFF;

		System.out.println("minDist is " + minDist);
	}

	public void run(Datable allData, CommandMgr cmds, float duration,
			ItemProcessor processor, Quip quip, short[] group) {
		if (!initialized) {
			if (quip.groupSize(group) > 0) {
				init(allData, quip, group);
			}
		}

		zero(moving.force);
		moveToward(moving.force, moving.position, moving.velocity, MAX_FORCE,
				moving.mass, moveTarget, duration);
		capForce(moving.force);

		allData.processGroup(quip, group, cmds, duration, processor);

		// Run physics on it because it's not an item, so won't run separately.
		// This way we can move the formation
		// center.
		updatePhysics(moving.position, moving.velocity, moving.force,
				moving.mass, duration);
	}

	public void init(Datable allData, Quip quip, short[] group) {
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
		moving.mass /= moving.mass / 2;
		System.out.println("AFTER INITIALIZATION");
		System.out.println("com pos ___ " + Arrays.toString(moving.position));
		System.out.println("com vel ___ " + Arrays.toString(moving.velocity));
	}

	public void processItem(float duration, Item item, Item[] neighbors,
			int groupSize) {
		zero(item.force);
		moveToward(item.force, item.position, item.velocity, MAX_FORCE,
				item.mass, shellTarget, duration);

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

		for (int i = 0; i < neighbors.length; i++) {
			Item neighbor = neighbors[i];
			if (neighbor == null) {
				continue;
			}
			float[] neighborDir = new float[3];
			set(neighborDir, neighbor.position);
			subtractInto(neighborDir, moving.position);
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

	public float getParam() {
		return formationRadius;
	}

	public String getLandmarkName() {
		return ItemUtil.getName(moveTarget.getLandmark());
	}

	public float getDistance() {
		return ((RadiusMoveTarget) moveTarget).getRadius();
	}
}

interface Datable {
	void processGroup(Quip quip, short[] group, CommandMgr cmds,
			float duration, ItemProcessor processor);
}
