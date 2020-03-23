package com.mentics.qd.jogl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.mentics.qd.items.Explosion;
import com.mentics.qd.items.Node;


public class ExplosionSequenceRenderer implements ItemRenderer<Explosion> {
    public final static int DETAIL_LEVEL = 3;
    private int vertexBuffer, elementBuffer, projectionMatrixLocation, modelViewMatrixLocation, centerPositionLocation,
            vertexPositionLocation;

    private ShaderProgram shaderProgram;
    private int vertexCount, indexCount;

    @Override
    public void initialize(GL2 gl) {
        Sphere sphere = Sphere.makeIcosphere(Node.RADIUS, DETAIL_LEVEL);
        FloatBuffer fbVertices = sphere.vertexCoordinateBuffer;
        IntBuffer ibElements = sphere.vertexIndexBuffer;
        vertexCount = sphere.vertexCount;
        indexCount = sphere.indexCount;

        int[] buffers = new int[2];
        gl.glGenBuffers(2, buffers, 0);// 0 is offset. We want to start with index 0.
        vertexBuffer = buffers[0];
        elementBuffer = buffers[1];

        ShaderCode vertShader =
                ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, Sphere.class, "", "", "explosion_s", true);
        ShaderCode fragShader =
                ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, Sphere.class, "", "", "explosion_s", true);
        vertShader.compile(gl, System.err);
        fragShader.compile(gl, System.err);

        shaderProgram = new ShaderProgram();
        shaderProgram.add(gl, vertShader, System.err);
        shaderProgram.add(gl, fragShader, System.err);
        shaderProgram.link(gl, System.err);
        shaderProgram.validateProgram(gl, System.err);

        shaderProgram.useProgram(gl, true);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount * Buffers.SIZEOF_FLOAT, fbVertices, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0); // unbind buffer

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indexCount * Buffers.SIZEOF_INT, ibElements, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0); // unbind buffer

        int programId = shaderProgram.program();
        projectionMatrixLocation = gl.glGetUniformLocation(programId, "m_projection");
        modelViewMatrixLocation = gl.glGetUniformLocation(programId, "m_camera");
        centerPositionLocation = gl.glGetUniformLocation(programId, "center_position");
        vertexPositionLocation = gl.glGetAttribLocation(programId, "position");

        shaderProgram.useProgram(gl, false);
        int error;
        if ((error = gl.glGetError()) != 0) {
            System.out.println(error);
        }
    }

    private float[] modelViewMatrix = new float[16], projectionMatrix = new float[16];

    @Override
    public void render(GL2 gl, List<Explosion> explosions) {
        shaderProgram.useProgram(gl, true);

        gl.glGetFloatv(GLMatrixFunc.GL_MODELVIEW_MATRIX, modelViewMatrix, 0); // 0 is offset
        gl.glGetFloatv(GLMatrixFunc.GL_PROJECTION_MATRIX, projectionMatrix, 0);

        gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);// count=1 - 1 matrix,
        gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);

        gl.glEnableVertexAttribArray(vertexPositionLocation);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
        gl.glVertexAttribPointer(vertexPositionLocation, 3, GL.GL_FLOAT, false, 0, 0); // 3 components per instance,

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        for (Explosion ex : explosions) {
            gl.glUniform3fv(centerPositionLocation, 1, ex.position, 0);
            gl.glDrawElements(GL.GL_TRIANGLES, indexCount, GL.GL_UNSIGNED_INT, 0);
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);// unbind
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glDisableVertexAttribArray(vertexPositionLocation);
        shaderProgram.useProgram(gl, false);
    }
}
