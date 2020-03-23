package com.mentics.qd.items;

import static com.mentics.math.vector.VectorUtil.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.mentics.qd.AllData;
import com.mentics.qd.ItemVisitor;


public class Node extends GraphItem implements Energetic {
    public static final Node[] EMPTY_ARRAY = new Node[0];

    public static final float RADIUS = 0.02f;
    public static final float NODE_MAX_ENERGY = 1.0f;// mJ
    public static final float NODE_ENERGY_GENERATION = 0.1f;// mW

    private final Quip quip;

    public float currentEnergy = NODE_MAX_ENERGY / 2;

    public String color;

    @Override
    public float getEnergy() {
        return currentEnergy;
    }

    @Override
    public void setEnergy(float value) {
        currentEnergy = value;
    }

    public Node(long itemId, Quip quip) {
        super(itemId);
        this.quip = quip;
    }

    @Override
    public void generateEnergy(float duration) {
        currentEnergy += duration * NODE_ENERGY_GENERATION;
        if (currentEnergy > NODE_MAX_ENERGY) {
            currentEnergy = NODE_MAX_ENERGY;
        }
    }

    @Override
    public float getRadius() {
        return RADIUS;
    }

    @Override
    public void acceptItemVisitor(ItemVisitor v) {
        v.visit(this);
    }

    public void shoot(float[] target, float energy, AllData allData) {
        if (currentEnergy > energy) {
            float[] initialPosition = new float[3];
            set(initialPosition, target);
            subtractInto(initialPosition, this.position);
            normalize(initialPosition);
            multiplyInto(initialPosition, RADIUS);
            addInto(initialPosition, this.position);
            this.currentEnergy -= energy;
            Shot shot = allData.queueNewShot(initialPosition, energy);
            set(shot.velocity, target);
            subtractInto(shot.velocity, initialPosition);
            normalize(shot.velocity);
            multiplyInto(shot.velocity, Shot.SPEED);
        }
    }

    @Override
    public void renderItem(GL2 gl, GLU glu) {}

    public Quip getQuip() {
        return quip;
    }
}
