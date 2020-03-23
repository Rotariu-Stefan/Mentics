package com.mentics.qd.items;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.mentics.qd.ItemVisitor;


/**
 * TODO: Probably don't need Link to be an Item.
 */
public class Link extends GraphItem {
    public long startId;
    public long endId;
    public float endPosition[] = new float[3];


    public Link(long id) {
        super(id);
    }


    @Override
    public void runPhysics(float duration) {
        endPosition[0] += velocity[0] * duration;
        endPosition[1] += velocity[1] * duration;
        endPosition[2] += velocity[2] * duration;
        position[0] += velocity[0] * duration;
        position[1] += velocity[1] * duration;
        position[2] += velocity[2] * duration;
        velocity[0] += force[0] / mass * duration;
        velocity[1] += force[1] / mass * duration;
        velocity[2] += force[2] / mass * duration;
    }

    @Override
    public void renderItem(GL2 gl, GLU glu) {
        gl.glPushMatrix();
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(1, 1, 0);
        gl.glVertex3f(position[0], position[1], position[2]);
        gl.glVertex3f(endPosition[0], endPosition[1], endPosition[2]);
        gl.glEnd();
        gl.glPopMatrix();
    }

    @Override
    public void acceptItemVisitor(ItemVisitor v) {
        v.visit(this);
    }


    @Override
    public float getRadius() {
        // TODO Auto-generated method stub
        return 0;
    }
}
