package com.mentics.qd.jogl;

import static javax.media.opengl.GL.*;

import java.nio.Buffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.mentics.qd.items.Quip;


public class QuipRenderer implements ItemRenderer<Quip> {
    public static int MAX_PARTICLES = 300;
    public static float MAX_SPEED = Quip.QUIP_RADIUS / 1.0f;
    public static float[] particleVelocities = makeParticleVelocities();

    private static float[] makeParticleVelocities() {
        float[] particleVelocities = new float[3 * MAX_PARTICLES];
        int n = 0;
        float vx, vy, vz;
        while (n < MAX_PARTICLES) {
            vx = (-1 + 2 * (float)Math.random()) * MAX_SPEED;
            vy = (-1 + 2 * (float)Math.random()) * MAX_SPEED;
            vz = (-1 + 2 * (float)Math.random()) * MAX_SPEED;
            if ((vx * vx + vy * vy + vz * vz) <= MAX_SPEED * MAX_SPEED) {
                particleVelocities[3 * n] = vx;
                particleVelocities[3 * n + 1] = vy;
                particleVelocities[3 * n + 2] = vz;
                n++;
            }
        }
        return particleVelocities;
    }

    int particleVelocitiesVBO;
    ShaderProgram shaderProg;

    int centerLocation;
    int timeLocation;
    int attributeVelocityLocation;
    int projectionMatrixLocation;
    int modelViewMatrixLocation;
    int radiusLocation;

    @Override
    public void initialize(GL2 gl) {
        // Quip: Particle rendering. VBO initialization
        int[] buffers = new int[1];
        gl.glGenBuffers(buffers.length, buffers, 0);
        particleVelocitiesVBO = buffers[0];
        // Push data to GPU
        Buffer fbVelocities = Buffers.newDirectFloatBuffer(particleVelocities);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, particleVelocitiesVBO);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, particleVelocities.length * Buffers.SIZEOF_FLOAT, fbVelocities,
                GL.GL_STATIC_DRAW);
        // Shaders
        ShaderCode vertShader = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(), "", "", "quip", true);
        ShaderCode fragShader = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(), "", "", "quip", true);

        vertShader.compile(gl, System.err);
        fragShader.compile(gl, System.err);

        shaderProg = new ShaderProgram();
        shaderProg.add(gl, vertShader, System.err);
        shaderProg.add(gl, fragShader, System.err);
        shaderProg.link(gl, System.err);
        shaderProg.validateProgram(gl, System.err);

        centerLocation = gl.glGetUniformLocation(shaderProg.program(), "center");
        timeLocation = gl.glGetUniformLocation(shaderProg.program(), "time");
        attributeVelocityLocation = gl.glGetAttribLocation(shaderProg.program(), "velocity");
        projectionMatrixLocation = gl.glGetUniformLocation(shaderProg.program(), "m_projection");
        modelViewMatrixLocation = gl.glGetUniformLocation(shaderProg.program(), "m_camera");
        radiusLocation = gl.glGetUniformLocation(shaderProg.program(), "radius");
    }

    // TODO rewrite using instancing as in NodeRenderer
    @Override
    public void render(GL2 gl, List<Quip> quips) {
        float[] modelViewMatrix = new float[16];
        gl.glGetFloatv(GLMatrixFunc.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
        float[] projectionMatrix = new float[16];
        gl.glGetFloatv(GLMatrixFunc.GL_PROJECTION_MATRIX, projectionMatrix, 0);

        shaderProg.useProgram(gl, true);

        gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);
        gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);

        gl.glEnableVertexAttribArray(attributeVelocityLocation);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, particleVelocitiesVBO);
        gl.glVertexAttribPointer(attributeVelocityLocation, 3, GL.GL_FLOAT, false, 0, 0);

        gl.glDepthMask(false);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);

        gl.glUniform1f(radiusLocation, Quip.QUIP_RADIUS);
        long timeMillis = System.currentTimeMillis();
        gl.glPointSize(2);
        for (Quip quip : quips) {
            gl.glUniform3fv(centerLocation, 1, quip.position, 0);
            gl.glUniform1f(timeLocation, (timeMillis - quip.creationTimeMillis) / 1000.0f);
            // Actual drawing
            gl.glDrawArrays(GL_POINTS, 0, MAX_PARTICLES);
        }
        gl.glPointSize(1);
        gl.glDepthMask(true);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glDisableVertexAttribArray(attributeVelocityLocation);
        shaderProg.useProgram(gl, false);

        for (Quip quip : quips) {
            renderTentacles(gl, quip);
        }
    }

    public void renderTentacles(GL2 gl, Quip quip) {
        for (QuipTentacle tentacle : quip.tentacles) {
            tentacle.draw(gl, quip);
        }
    }
}
