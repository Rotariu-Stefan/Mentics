package com.mentics.qd.jogl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3ES3;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.mentics.qd.items.Node;


public class NodeRenderer implements ItemRenderer<Node> {
    public final static int DETAIL_LEVEL = 3, MAX_NUMBER_OF_NODES = 20000;
    private int vertexBuffer, elementBuffer, centerPositionBuffer, projectionMatrixLocation, modelViewMatrixLocation,
            centerPositionLocation, vertexPositionLocation;

    private ShaderProgram shaderProgram;
    private int vertexCount, indexCount;
    FloatBuffer fbCenters = Buffers.newDirectFloatBuffer(3 * MAX_NUMBER_OF_NODES);

    @Override
    public void initialize(GL2 gl2) {
        final GL3ES3 gl = (GL3ES3)gl2;
        final Sphere sphere = Sphere.makeIcosphere(Node.RADIUS, DETAIL_LEVEL);
        final FloatBuffer fbVertices = sphere.vertexCoordinateBuffer;
        final IntBuffer ibElements = sphere.vertexIndexBuffer;
        vertexCount = sphere.vertexCount;
        indexCount = sphere.indexCount;

        final int[] buffers = new int[3];
        gl.glGenBuffers(3, buffers, 0);// 0 is offset. We want to start with index 0.
        vertexBuffer = buffers[0];
        elementBuffer = buffers[1];
        centerPositionBuffer = buffers[2];

        final ShaderCode vertShader =
                ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, Sphere.class, "", "", "node", true);
        final ShaderCode fragShader =
                ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, Sphere.class, "", "", "node", true);
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

        final int programId = shaderProgram.program();
        projectionMatrixLocation = gl.glGetUniformLocation(programId, "m_projection");
        modelViewMatrixLocation = gl.glGetUniformLocation(programId, "m_camera");
        centerPositionLocation = gl.glGetAttribLocation(programId, "center_position");
        vertexPositionLocation = gl.glGetAttribLocation(programId, "position");

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, centerPositionBuffer);
        gl.glVertexAttribPointer(centerPositionLocation, 3, GL.GL_FLOAT, false, 0, 0);// 3 components per instance,
        // 0 stride = tightly packed
        // 0 offset
        gl.glEnableVertexAttribArray(centerPositionLocation);
        gl.glVertexAttribDivisor(centerPositionLocation, 1);// 1 means 1 value per instance
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);// unbind
        gl.glDisableVertexAttribArray(centerPositionLocation);

        shaderProgram.useProgram(gl, false);
        int error;
        if ((error = gl.glGetError()) != 0) {
            System.out.println(error);
        }
    }

    private final float[] modelViewMatrix = new float[16], projectionMatrix = new float[16];

    @Override
    public void render(GL2 gl2, List<Node> nodes) {
        final GL3ES3 gl = (GL3ES3)gl2;
        final int instanceCount = nodes.size();

        fbCenters.rewind();
        for (final Node node : nodes) {
            for (final float f : node.position) {
                fbCenters.put(f);
            }
        }
        fbCenters.rewind();

        shaderProgram.useProgram(gl, true);

        gl.glGetFloatv(GLMatrixFunc.GL_MODELVIEW_MATRIX, modelViewMatrix, 0); // 0 is offset
        gl.glGetFloatv(GLMatrixFunc.GL_PROJECTION_MATRIX, projectionMatrix, 0);

        gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);// count=1 - 1 matrix,
        // offset=0
        gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, centerPositionBuffer);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 3 * instanceCount, fbCenters, GL.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);// unbind

        gl.glEnableVertexAttribArray(centerPositionLocation);
        gl.glEnableVertexAttribArray(vertexPositionLocation);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
        gl.glVertexAttribPointer(vertexPositionLocation, 3, GL.GL_FLOAT, false, 0, 0); // 3 components per instance,
        // 0 stride = tightly packed
        // 0 offset
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);

        gl.glDrawElementsInstanced(GL.GL_TRIANGLES, indexCount, GL.GL_UNSIGNED_INT, 0, instanceCount);// 0 is offset

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);// unbind
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glDisableVertexAttribArray(centerPositionLocation);
        gl.glDisableVertexAttribArray(vertexPositionLocation);
        shaderProgram.useProgram(gl, false);
    }
}
