package com.mentics.qd.model;

import com.mentics.qd.items.MovingThing;

import static com.mentics.math.vector.VectorUtil.*;


public class PointMoveTarget implements MoveTarget {
    public float[] position = new float[3],
            velocity = new float[3];

    @Override
    public void shortestPath(float[] result, float[] position) {
        set(result, this.position);
        subtractInto(result, position);
    }

    @Override
    public float relativeSpeed(float[] velocity) {
        float[] temp = new float[3];
        relativeVelocity(temp, velocity);
        return magnitude(temp);
    }

    @Override
    public void relativeVelocity(float[] result, float[] velocity) {
        set(result, velocity);
        subtractInto(result, this.velocity);
    }

    @Override
    public MovingThing getLandmark() {
        return null;
    }
}
