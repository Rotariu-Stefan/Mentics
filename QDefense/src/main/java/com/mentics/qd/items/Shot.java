package com.mentics.qd.items;

import static com.mentics.math.vector.VectorUtil.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.mentics.math.equation.AlgebraicEquation;
import com.mentics.qd.ItemVisitor;


public class Shot extends Item {
    public static final float SPEED = 1f;
    public float[] color = { 1, 1, 1 };
    public final float[] initialPosition;
    float initialEnergy;
    public final float shootingRange;

    public Shot(float[] initialPosition, float initialEnergy) {
        super(-1);
        this.initialPosition = initialPosition;
        set(position, initialPosition);
        this.initialEnergy = initialEnergy;
        this.shootingRange = getShootingRange(initialEnergy);
    }

    public static float getShootingRange(float initialEnergy) {
        return (float)Math.pow(initialEnergy, 0.75);
    }

    public float getDamage() {
        return initialEnergy * (shootingRange - distance(initialPosition, position)) / shootingRange;
    }

    public static float getDamageAtDistance(float energy, float distance) {
        float shootingRange = getShootingRange(energy);
        return energy * (shootingRange - distance) / shootingRange;
    }

    public static float getEnergy(float damage, float distance) {
        // the equation is
        // E - d E ^ 1/4 - g = 0
        // where d is distance and g is damage
        float[] roots = AlgebraicEquation.solveQuartic(1, 0, 0, -distance, -damage);
        float minRoot4OfEnergy = 0;
        for (float x : roots) {
            if (x > 0 && x < minRoot4OfEnergy || minRoot4OfEnergy == 0) {
                minRoot4OfEnergy = x;
            }
        }
        // Should always return some positive value if both parameters are non-negative
        return (float)Math.pow(minRoot4OfEnergy, 4);
    }

    @Override
    public void renderItem(GL2 gl, GLU glu) {
        gl.glColor3fv(color, 0);
        gl.glVertex3fv(position, 0);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor v) {
        v.visit(this);
    }

    @Override
    public float getRadius() {
        return 0;
    }
}
