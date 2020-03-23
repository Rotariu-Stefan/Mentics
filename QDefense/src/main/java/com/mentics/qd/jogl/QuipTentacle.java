package com.mentics.qd.jogl;

import static com.mentics.math.vector.VectorUtil.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.mentics.qd.items.Quip;


public class QuipTentacle {
    public final float[] direction;
    public final float K = 0.003f, L = 0.01f, M = 0.01f;

    public final float k = K * L * L / M; // This is a^2 from string equation, the characteristic of rigidity and mass;

    public final int MAX_POINTS = 10;
    public final float[][] points = new float[MAX_POINTS][3];
    public final float pointSeparation = L / MAX_POINTS;

    public QuipTentacle(float[] direction) {
        normalize(direction);
        this.direction = direction;
    }

    public final float maxForcePerUnitOfMass = 0.01f;

    public float[][] getPoints(Quip quip) {
        // The equation for the shape of the string
        // will be k * u'' = f
        // So u = ax^2 + bx
        // Where x axis is perpendicular to force, and u is opposite to force
        // f is the force acting on a quip;
        // a = f / (2k)
        // b is a tangent of the angle between direction and x axis
        float[] forcePerUnitOfMass = new float[3];

        set(forcePerUnitOfMass, quip.force);
        multiplyInto(forcePerUnitOfMass, -1 / quip.mass); // inertia force

        float[] r0 = quip.position;
        float f = magnitude(forcePerUnitOfMass);

        if (f > maxForcePerUnitOfMass) {
            multiplyInto(forcePerUnitOfMass, maxForcePerUnitOfMass / f);
            f = maxForcePerUnitOfMass;
        }

        if (f != 0) {
            if (dot(direction, forcePerUnitOfMass) < 0) {
                f = -f; // f is going to be negative if it is opposite to direction;
            }
            float a = f / 2 / k;
            float[] e = new float[3]; // this will be unit vector along x axis, unit vector along u is
                                      // -force/magnitude(force)
            set(e, forcePerUnitOfMass);
            multiplyInto(e, dot(direction, e) / (f * f)); // now it contains a projection of the direction vector on the
                                                      // direction of the force
            addInto(e, direction);// now e contains projection of the direction on the x axis
            normalize(e);// now e is a unit vector along x axis
            if (dot(direction, e) != 0) {
                float b = dot(direction, forcePerUnitOfMass) / f / dot(direction, e);

                float x = 0, u = 0;
                float[] temp = new float[3];
                for (int i = 0; i < MAX_POINTS; i++) {
                    u = a * x * x + b * x;
                    set(points[i], e);
                    multiplyInto(points[i], x);
                    set(temp, forcePerUnitOfMass);
                    multiplyInto(temp, u / f);
                    addInto(points[i], temp);
                    addInto(points[i], r0);
                    x += pointSeparation;
                }
            } else {
                resetPoints(quip);
            }
        } else {
            resetPoints(quip);
        }
        return points;
    }

    public void resetPoints(Quip quip) {
        float x = 0;
        float[] r0 = quip.position;
        for (int i = 0; i < MAX_POINTS; i++) {
            set(points[i], direction);
            multiplyInto(points[i], x);
            addInto(points[i], r0);
            x += pointSeparation;
        }
    }

    public void draw(GL2 gl, Quip quip) {
        float[][] points = getPoints(quip);
        gl.glLineWidth(1.5f);
        gl.glColor3f(1, 1, 0);
        gl.glBegin(GL.GL_LINE_STRIP);
        for (float[] p : points) {
            gl.glVertex3fv(p, 0);
        }
        gl.glEnd();
    }
}
