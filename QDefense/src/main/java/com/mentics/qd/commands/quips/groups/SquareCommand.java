package com.mentics.qd.commands.quips.groups;

import static com.mentics.math.vector.VectorUtil.*;
import static com.mentics.qd.datastructures.ArrayTreeUtil.*;

import java.util.Arrays;

import com.mentics.math.vector.VectorUtil;
import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.ItemProcessor;
import com.mentics.qd.commands.quips.MotionCommand;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.MoveTarget;
import com.mentics.qd.model.PlaneMoveTarget;
import com.mentics.qd.model.PointMoveTarget;
import com.mentics.qd.model.RadiusMoveTarget;


public class SquareCommand extends GroupCommandBase implements MotionCommand {
    MoveTarget moveTarget;
    PointMoveTarget squareCenter = new PointMoveTarget();
    PlaneMoveTarget plane = new PlaneMoveTarget();
    boolean initialized = false;

    float formationSize, itemSeparation;
    Item[] items;

    float[] relativePosition = new float[3], normal = new float[3], basisX = new float[3], basisY = new float[3],
            defaultBasisX = new float[] { 1, 0, 0 }, temp = new float[3];

    public ItemProcessor processor = new ItemProcessor() {
        boolean itemRests;

        @Override
        public void process(AllData allData, CommandMgr cmds, float duration, Item item) {
            set(relativePosition, item.position);
            subtractInto(relativePosition, squareCenter.position);
            itemRests = true;
            // if the item is outside of the prism, it needs to move towards the center
            if (Math.abs(dot(relativePosition, basisX)) > formationSize / 2 ||
                Math.abs(dot(relativePosition, basisY)) > formationSize / 2) {
                ItemUtil.moveToward(item.initiatedForce, item.position, item.velocity, ItemUtil.MAX_FORCE, item.mass,
                        squareCenter, duration);
                itemRests = false;
            } else {
                // the item is inside the prism
                if (Math.abs(dot(relativePosition, normal)) > item.getRadius() / 2) {
                    // the item needs to get closer to the plane
                    ItemUtil.moveToward(item.initiatedForce, item.position, item.velocity, ItemUtil.MAX_FORCE,
                            item.mass, plane, duration);
                    itemRests = false;
                }
                // repulsion between items
                float k = item.mass / duration * duration;
                Arrays.stream(items).filter(i -> i.id != item.id).map(i -> i.position)
                        .filter(p -> distance(item.position, p) < itemSeparation).forEach(p -> {
                            set(temp, item.position);
                            subtractInto(temp, p);
                            normalize(temp);
                            float dx = itemSeparation - distance(item.position, p);
                            multiplyInto(temp, dx * k);
                            addInto(item.initiatedForce, temp);
                            itemRests = false;
                        });
                // interaction with borders;
                // float x = dot(relativePosition, basisX),
                // y = dot(relativePosition, basisY),
                // range = itemSeparation / 2,
                // dx = range - (formationSize / 2 - Math.abs(x)),
                // dy = range - (formationSize / 2 - Math.abs(y));
                // if (dx > 0) {
                // set(temp, basisX);
                // multiply(temp, -Math.signum(x) * dx * 2 * k);
                // add(item.initiatedForce, temp);
                // itemRests = false;
                // }
                // if (dy > 0) {
                // set(temp, basisX);
                // multiply(temp, -Math.signum(y) * dy * 2 * k);
                // add(item.initiatedForce, temp);
                // itemRests = false;
                // }
                if (itemRests) {
                    ItemUtil.brake(item, duration);
                }
            }
        }
    };

    public SquareCommand(Quip quip, short[] group, MoveTarget moveTarget, float formationSize) {
        super(CommandMgrQueue.MOVEMENT, quip, group);
        this.moveTarget = moveTarget;
        this.formationSize = formationSize;
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        Object[] tree = quip.nodes;
        Object mbSubtree = getSubTree(tree, group);
        items = ArrayTreeUtil.getNodes(mbSubtree);
        itemSeparation = getSeparation(items.length);

        if (!initialized) {
            ItemUtil.centroid(squareCenter.position, Arrays.asList(items));
            zero(normal);
            normal[2] = 1;// Default normal is along z axis
            updateBasis();
            initialized = true;
        }

        moveTarget.shortestPath(temp, squareCenter.position);
        if (magnitude(temp) > 0) {
            addInto(squareCenter.position, temp); // update for moving target
            set(normal, temp); // if the relative movement of the target wasn't zero, update normal
            normalize(normal);
            updateBasis(); // and update basis
        }

        moveTarget.relativeVelocity(squareCenter.velocity, VectorUtil.ZERO);
        multiplyInto(squareCenter.velocity, -1);

        set(plane.normal, normal);
        set(plane.point, squareCenter.position);
        set(plane.velocity, squareCenter.velocity);

        // System.out.println("Square command running");
        allData.processGroup(quip, group, cmds, duration, processor);
        // Move square center
        set(temp, squareCenter.velocity);
        multiplyInto(temp, duration);
        addInto(squareCenter.position, temp);
    }

    private float getSeparation(int numItems) {
        if (numItems <= 1) {
            return formationSize;
        } else {
            return (float)(formationSize / (Math.sqrt(numItems) - 1));
        }
    }

    private void updateBasis() {
        set(temp, defaultBasisX);
        crossInto(temp, normal);
        if (magnitude(temp) == 0) {
            // the normal is along {1, 0, 0}
            basisX[0] = 0;
            basisX[1] = 0;
            basisX[2] = 1;
            basisY[0] = 0;
            basisY[1] = normal[0] > 0 ? 1 : -1;
            basisY[2] = 0;
        } else {
            // We select basisX along projection of defaultBasisX onto the plain of the square
            set(basisX, defaultBasisX);
            set(temp, normal);
            multiplyInto(temp, dot(basisX, normal));
            subtractInto(basisX, temp);
            normalize(basisX);
            set(basisY, normal);
            crossInto(basisY, basisX);
        }
    }

    @Override
    public String getShape() {
        return "Square";
    }

    @Override
    public float getParam() {
        return formationSize;
    }

    @Override
    public String getLandmarkName() {
        return ItemUtil.getName(moveTarget.getLandmark());
    }

    @Override
    public float getDistance() {
        return ((RadiusMoveTarget)moveTarget).getRadius();
    }

}