package com.mentics.qd.model;

import static com.mentics.math.vector.VectorUtil.*;

import com.mentics.qd.items.MovingThing;
import com.mentics.qd.items.Quip;


public class MutableRadiusMoveTarget implements MoveTarget {
    public MovingThing target;
    public float radius;

    public String getLandmarkName() {
        return ((Quip)target).name;
    }

    public float getRadius() {
        return radius;
    }

    public MutableRadiusMoveTarget(MovingThing target, float radius) {
        this.target = target;
        this.radius = radius;
    }

    @Override
    public void shortestPath(float[] result, float[] position) {
        set(result, target.position);
        subtractInto(result, position);
        float mag = magnitude(result);
        normalize(result);
        multiplyInto(result, mag - radius);
    }

    @Override
    public void relativeVelocity(float[] result, float[] velocity) {
        set(result, velocity);
        subtractInto(result, target.velocity);
    }

    float[] temp = new float[3];

    @Override
    public float relativeSpeed(float[] velocity) {
        relativeVelocity(temp, velocity);
        return magnitude(temp);
    }

    @Override
    public MovingThing getLandmark() {
        return target;
    }
}
