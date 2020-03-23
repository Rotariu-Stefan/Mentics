package com.mentics.qd.model;

import com.mentics.qd.items.MovingThing;


/**
 * Defines a relative location (probably motion)
 */
public interface MoveTarget {
    void shortestPath(float[] result, float[] position);

    float relativeSpeed(float[] velocity);

    void relativeVelocity(float[] result, float[] velocity);

    MovingThing getLandmark();
}
