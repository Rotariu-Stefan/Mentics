package com.mentics.qd.jogl;

import static com.mentics.math.matrix.Matrix4.*;

import java.nio.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;


public class SkyBox {
    private CubeMap cubeMap;
    private ShaderProgram shaderProg;

    private int cubeVBO;
    private int positionLocation, modelViewMatrixLocation, projectionMatrixLocation, textureLocation,
            translationMatrixLocation;

    private float[] cubeVertices = new float[] {
                                                // Front face
                                                -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
                                                1.0f,
                                                1.0f,
                                                -1.0f,
                                                1.0f,
                                                1.0f,
                                                // Back Face
                                                -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
                                                -1.0f,
                                                1.0f,
                                                -1.0f,
                                                -1.0f,
                                                // Top Face
                                                -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                                                1.0f,
                                                1.0f,
                                                -1.0f,
                                                // Bottom Face
                                                -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f,
                                                -1.0f,
                                                1.0f,
                                                // Right face
                                                1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                                                1.0f,
                                                // Left Face
                                                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f,
                                                1.0f, -1.0f };

    public SkyBox(GL2 gl, CubeMap cubeMap) {
        this.cubeMap = cubeMap;
        init(gl);
    }

    public void init(GL2 gl) {
        int[] buffers = new int[1];
        gl.glGenBuffers(buffers.length, buffers, 0);
        cubeVBO = buffers[0];
        // Push data to GPU
        Buffer fbCubeVertices = Buffers.newDirectFloatBuffer(cubeVertices);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, cubeVBO);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * Buffers.SIZEOF_FLOAT, fbCubeVertices,
                GL.GL_STATIC_DRAW);
        // Shaders
        ShaderCode vertShader = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(), "", "", "skybox", true);
        ShaderCode fragShader =
                ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(), "", "", "skybox", true);

        vertShader.compile(gl, System.err);
        fragShader.compile(gl, System.err);

        shaderProg = new ShaderProgram();
        shaderProg.add(gl, vertShader, System.err);
        shaderProg.add(gl, fragShader, System.err);
        shaderProg.link(gl, System.err);
        shaderProg.validateProgram(gl, System.err);

        positionLocation = gl.glGetAttribLocation(shaderProg.program(), "Position");
        projectionMatrixLocation = gl.glGetUniformLocation(shaderProg.program(), "m_projection");
        modelViewMatrixLocation = gl.glGetUniformLocation(shaderProg.program(), "m_camera");
        textureLocation = gl.glGetUniformLocation(shaderProg.program(), "gCubemapTexture");
        translationMatrixLocation = gl.glGetUniformLocation(shaderProg.program(), "m_translation");
    }

    public void draw(GL2 gl) {
        // This uses an approach when z coordinate is set to value of w, so that, after
        // perspective division the Z distance will always be 1.0; maximum distance in normalised device space,
        // and therefore always in the background
        // This can boost performance if the sky box is rendered last

        // It may or may not cause bad rendering if depth testing is on.
        // Another approach is to disable depth masking when drawing sky box and draw it first. Described at
        // http://antongerdelan.net/opengl/cubemaps.html

        gl.glPushMatrix();
        // We need a big cube
        gl.glScalef(80, 80, 80);
        // saving params
        int[] oldParams = new int[2];
        gl.glGetIntegerv(GL.GL_CULL_FACE_MODE, oldParams, 0);
        gl.glGetIntegerv(GL.GL_DEPTH_FUNC, oldParams, 1);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glCullFace(GL.GL_FRONT);
        // Matrices
        float[] modelViewMatrix = new float[16];
        gl.glGetFloatv(GLMatrixFunc.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
        float[] projectionMatrix = new float[16];
        gl.glGetFloatv(GLMatrixFunc.GL_PROJECTION_MATRIX, projectionMatrix, 0);

        // For drawing sky box we need the camera in the center of the box.
        // this matrix translates the camera to zero;
        float[] translateBackMatrix = translationMat4(-modelViewMatrix[12], -modelViewMatrix[13], -modelViewMatrix[14]);

        shaderProg.useProgram(gl, true);

        gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);
        gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);
        gl.glUniformMatrix4fv(translationMatrixLocation, 1, false, translateBackMatrix, 0);

        gl.glUniform1i(textureLocation, 0);

        gl.glEnableVertexAttribArray(positionLocation);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, cubeVBO);
        gl.glVertexAttribPointer(positionLocation, 3, GL.GL_FLOAT, false, 0, 0);

        cubeMap.bind(gl);
        // Draw skybox
        gl.glDrawArrays(GL2GL3.GL_QUADS, 0, 24);

        gl.glDisableVertexAttribArray(positionLocation);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        shaderProg.useProgram(gl, false);
        cubeMap.release(gl);
        // Restore parameters
        gl.glCullFace(oldParams[0]);
        gl.glDepthFunc(oldParams[1]);
        gl.glPopMatrix();
    }
}
