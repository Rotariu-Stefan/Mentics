package com.mentics.qd.items;

import static com.mentics.math.vector.VectorUtil.*;

import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import com.mentics.math.quaternion.Quaternion;
import com.mentics.qd.ItemVisitor;


public class Camera extends Item {
    @Override
    public float getRadius() {
        return 0;// Maybe change later
    }

    public float[] lookingDirection = new float[3];
    public float distanceToCenter = 1;
    private float lookingAt[] = new float[3];
    public float up[] = new float[3];

    public boolean movedManually = false;

    // Projection parameters from:
    // glu.gluPerspective(45.0, aspect, 0.1, 100000.0); // fovy, aspect, zNear, zFar
    // TODO store the projection parameters and matrix in camera object
    public float fovY = (float)(45 * Math.PI / 180), aspect = 800f / 600f;

    float[] qThetaPlus = new float[4], qThetaMinus = new float[4], qPhiPlus = new float[4], qPhiMinus = new float[4];

    float[] q1 = new float[4];
    float[] q2 = new float[4];
    float[] temp = new float[3];
    
    // NEW PARAMETERS
    public float[] angVel = {0, 0, 0};
    public float[] orientationQ = {0, 0, 0, 0};
    
    public float getFovX() {
        return (float)(2 * Math.atan(aspect * Math.tan(fovY / 2)));
    }

    public float getTanHalfFovX() {
        return (float)(aspect * Math.tan(fovY / 2));
    }

    public Camera() {
        super(0);
        position[2] = 20f;
        lookingDirection[0] = lookingDirection[1] = 0;
        lookingDirection[2] = -1;
        lookingAt[0] = lookingAt[1] = lookingAt[2] = 0;
        up[0] = up[2] = 0;
        up[1] = 1;
        // 3rd axis is dont used here but any {1, 0, 0}
        // orient q for these will be
        orientationQ = new float[] {(float)Math.cos(Math.PI / 2 / 2), 0, (float)Math.sin(Math.PI / 2 / 2), 0};
    }
    
    public void rotate(float theta, float phi) {
        updateLookingAt();

        Quaternion.versorInto(qPhiMinus, up, -phi / 2);
        Quaternion.versorInto(qPhiPlus, up, phi / 2);

        set(temp, lookingDirection);
        crossInto(temp, up);
        normalize(temp);

        Quaternion.versorInto(qThetaMinus, temp, -theta / 2);
        Quaternion.versorInto(qThetaPlus, temp, theta / 2);

        Quaternion.multiplyVbyQInto(q1, lookingDirection, qThetaMinus);
        Quaternion.multiplyQbyQInto(q2, qThetaPlus, q1);
        q2[0] = 0;
        Quaternion.multiplyQbyQInto(q1, q2, qPhiMinus);
        Quaternion.multiplyQbyQInto(q2, qPhiPlus, q1);
        Quaternion.copyToVector(lookingDirection, q2);

        Quaternion.multiplyVbyQInto(q1, up, qThetaMinus);
        Quaternion.multiplyQbyQInto(q2, qThetaPlus, q1);
        q2[0] = 0;
        Quaternion.multiplyQbyQInto(q1, q2, qPhiMinus);
        Quaternion.multiplyQbyQInto(q2, qPhiPlus, q1);
        Quaternion.copyToVector(up, q2);

        set(position, lookingDirection);
        multiplyInto(position, -distanceToCenter);
        addInto(position, lookingAt);
    }

    private void updateLookingAt() {
        // Find lookingAt using lookingDirection and distanceToCenter
        set(lookingAt, lookingDirection);
        multiplyInto(lookingAt, distanceToCenter);
        addInto(lookingAt, position);
    }

    @Override
    public void renderItem(GL2 gl, GLU glu) {
        updateLookingAt();

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(position[0], position[1], position[2], lookingAt[0], lookingAt[1], lookingAt[2], up[0], up[1],
                up[2]);
    }
    
    @Override
    public void acceptItemVisitor(ItemVisitor v) {
        v.visit(this);
    }
}
