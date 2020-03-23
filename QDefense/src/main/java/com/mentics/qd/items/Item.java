package com.mentics.qd.items;

import static com.mentics.math.vector.VectorUtil.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.mentics.qd.AllData;
import com.mentics.qd.ItemVisitor;


public abstract class Item extends MovingThing {
    public static final Item[] EMPTY_ARRAY = new Item[0];

    public final long id;

    private transient float temp[] = new float[3];


    public Item(long id) {
        this.id = id;
        // Default just in case we don't set it elsewhere
        // TODO: remove this after confirming it's set properly elsewhere
        mass = 1.0f;
    }

    public abstract void renderItem(GL2 gl, GLU glu);

    public abstract void acceptItemVisitor(ItemVisitor v);

    public abstract float getRadius();

    public void runPhysics(float duration) {
        // TODO: Put in max force cap here so it only happens once per target processing
        // TODO: but we'll have to deal with max force along the way because of external factors that may be motion
        // things like explosions.
        // added for energy calculation the initial position & the target position
        float[] initialPosition = new float[3];
        float[] targetPosition = new float[3];

        initialPosition = position;

        ItemUtil.updatePhysics(position, velocity, force, mass, duration);

        targetPosition = position;

        // energy consumption
        if (this instanceof Energetic) {
            Energetic e = (Energetic)this;
            float energyUsed = ItemUtil.ENERGY_CONSUMPTION_PER_UNIT_MOMENTUM * magnitude(initiatedForce) * duration;
            e.setEnergy(e.getEnergy() - energyUsed);
        }
    }

    public void beginStep() {
        zero(force);
        zero(initiatedForce);
    }

    public void endStep(AllData allData, float duration) {
        // TODO: make this true: Physics doesn't need to run in separate because we don't modify what other things read
        // from
        runPhysics(duration);
    }
}