package com.mentics.qd.items;

import static com.mentics.math.vector.VectorUtil.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.mentics.qd.ItemVisitor;


public class Explosion extends Item {
    public static final float LIFE_TIME = 5;
    public float timeLeft = LIFE_TIME;

    public Explosion(long id) {
        super(id);
    }

    public Explosion(long id, Item item) {
        super(id);
        set(position, item.position);
        set(velocity, item.velocity);
    }

    @Override
    public void renderItem(GL2 gl, GLU glu) {

    }

    @Override
    public void acceptItemVisitor(ItemVisitor v) {
        v.visit(this);
    }

    @Override
    public float getRadius() {
        return 0;
    }

    @Override
    public void runPhysics(float duration) {
        zero(force);
        zero(initiatedForce); // should be zero anyway
        super.runPhysics(duration);
        timeLeft -= duration;
    }
}
