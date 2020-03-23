package com.mentics.qd.model;

import com.mentics.qd.items.MovingThing;

import static com.mentics.math.vector.VectorUtil.*;


public class PlaneMoveTarget implements MoveTarget {
    public float[] normal = new float[] { 0, 0, 1 },
            point = new float[3],
            velocity = new float[3];

    @Override
    public void shortestPath(float[] result, float[] position) {
        set(result, point);
        subtractInto(result, position);
        float projection = dot(result, normal);
        set(result, normal);
        multiplyInto(result, projection);
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
