package com.mentics.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;


/**
 * Created by Fatma on 30/12/13.
 */
public class ItemSelection {
    public static final int VIEW_Persp = 0;
    public static final int VIEW_Axon = 1;
    public static final int VIEW_Plan = 2;
    public static final int VIEW_Front = 3;
    public static final int VIEW_Right = 4;
    public static final int VIEW_Rear = 5;
    public static final int VIEW_Left = 6;

    public static float[] ptStartPos = new float[3];
    public static float[] ptEndPos = new float[3];

    // Internal matrices and projection type.
    protected static int m_iProjection = VIEW_Persp;
    protected static double[] m_adModelview = new double[16];
    protected static double[] m_adProjection = new double[16];
    public static int[] m_aiViewport = new int[4];

    public static int winXCoord;
    public static int winYCoord;

    static public void captureViewMatrix(GL2 gl) {
        // Call this to capture the selection matrix after
        // you have called perspective() or ortho() and applied your
        // pan, zoom and camera angles - but before you start drawing
        // or playing with the matrices any further.
        if (gl != null) {
            // m_iProjection = projection;
            gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, m_adModelview, 0);
            gl.glGetDoublev(GLMatrixFunc.GL_PROJECTION_MATRIX, m_adProjection, 0);
            gl.glGetIntegerv(GL.GL_VIEWPORT, m_aiViewport, 0);
        }
    }

    static public boolean calculatePickPoints(int x, int y, GLU glu) {
        // Calculate positions on the near and far 3D frustum planes.
        if (glu != null) {
            double[] out = new double[4];
            glu.gluUnProject(x, m_aiViewport[3] - (double)y, 0.0, m_adModelview, 0, m_adProjection, 0, m_aiViewport, 0,
                    out, 0);
            ptStartPos[0] = (float)out[0];
            ptStartPos[1] = (float)out[1];
            ptStartPos[2] = (float)out[2];
            glu.gluUnProject(x, m_aiViewport[3] - (double)y, 1.0, m_adModelview, 0, m_adProjection, 0, m_aiViewport, 0,
                    out, 0);
            ptEndPos[0] = (float)out[0];
            ptEndPos[1] = (float)out[1];
            ptEndPos[2] = (float)out[2];
            return true;
        }
        return false;
    }

    static public void calculatePointOnScreen(float[] point, GLU glu) {
        double[] outputCoords = new double[3];
        glu.gluProject(point[0], point[1], point[2], m_adModelview, 0, m_adProjection, 0, m_aiViewport, 0,
                outputCoords, 0);
        winXCoord = (int)outputCoords[0];
        winYCoord = (int)-outputCoords[1] + m_aiViewport[3];
    }
}
