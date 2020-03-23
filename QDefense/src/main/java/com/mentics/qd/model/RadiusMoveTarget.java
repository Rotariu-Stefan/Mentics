package com.mentics.qd.model;

import static com.mentics.math.vector.VectorUtil.*;

import java.util.Random;

import com.mentics.math._float.FloatUtil;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.MovingThing;


public class RadiusMoveTarget implements MoveTarget {
    protected final MovingThing target;
    protected final float radius;

    public String getLandmarkName() {
        return ItemUtil.getName(target);
    }

    public float getRadius() {
        return radius;
    }

    public RadiusMoveTarget(final MovingThing target, final float radius) {
        this.target = target;
        this.radius = radius;
        System.out.println("Movement towards " + ItemUtil.getName(target) + " in radius " + radius);
    }

    /**
     * May set result to all zeroes if we're already there.
     */
    @Override
    public void shortestPath(float[] result, float[] position) {
        if (isSame(position, target.position)) {
            // TODO: change this
            final Random random = new Random();
            // Move the position to radius distance away arbitrarily.
            final int val = random.nextInt(3);
            result[random.nextInt(3)] = radius;
        } else {
            set(result, target.position);
            subtractInto(result, position);
            final float mag = magnitude(result);
            if (FloatUtil.isSame(mag, radius)) {
                // We're already there
                zero(result);
                return;
            }
            normalize(result);
            multiplyInto(result, mag - radius);
            // multiply(result, Math.abs(mag - radius));
        }
    }

    @Override
    public void relativeVelocity(float[] result, float[] velocity) {
        set(result, velocity);
        subtractInto(result, target.velocity);
    }

    @Override
    public float relativeSpeed(float[] velocity) {
        final float[] temp = new float[3]; // TODO: reuse?
        relativeVelocity(temp, velocity);
        return magnitude(temp);
    }

    @Override
    public MovingThing getLandmark() {
        return target;
    }
}
