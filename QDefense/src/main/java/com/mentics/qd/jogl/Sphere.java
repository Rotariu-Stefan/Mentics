package com.mentics.qd.jogl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jogamp.common.nio.Buffers;


public class Sphere {
    public float radius;
    public FloatBuffer vertexCoordinateBuffer;
    public IntBuffer vertexIndexBuffer;
    public int vertexCount, indexCount;

    public Sphere(float radius, FloatBuffer vertexCoordinateBuffer, IntBuffer vertexIndexBuffer, int vertexNumber,
            int indexNumber) {
        this.radius = radius;
        this.vertexCoordinateBuffer = vertexCoordinateBuffer;
        this.vertexIndexBuffer = vertexIndexBuffer;
        this.vertexCount = vertexNumber;
        this.indexCount = indexNumber;
    }

    static final float t = (float)((Math.sqrt(5) - 1) / 2);
    static final float[][] icosahedronVertices = new float[][] { new float[] { -1, -t, 0 }, new float[] { 0, 1, t },
                                                                new float[] { 0, 1, -t }, new float[] { 1, t, 0 },
                                                                new float[] { 1, -t, 0 }, new float[] { 0, -1, -t },
                                                                new float[] { 0, -1, t }, new float[] { t, 0, 1 },
                                                                new float[] { -t, 0, 1 }, new float[] { t, 0, -1 },
                                                                new float[] { -t, 0, -1 }, new float[] { -1, t, 0 }, };
    static final float icosahedronRadius = (float)Math.sqrt(1 + t * t);

    static final int[][] icosahedronFaces = new int[][] { { 3, 7, 1 }, { 4, 7, 3 }, { 6, 7, 4 }, { 8, 7, 6 },
                                                         { 7, 8, 1 }, { 9, 4, 3 }, { 2, 9, 3 }, { 2, 3, 1 },
                                                         { 11, 2, 1 }, { 10, 2, 11 }, { 10, 9, 2 }, { 9, 5, 4 },
                                                         { 6, 4, 5 }, { 0, 6, 5 }, { 0, 11, 8 }, { 11, 1, 8 },
                                                         { 10, 0, 5 }, { 10, 5, 9 }, { 0, 8, 6 }, { 0, 10, 11 }, };

    /**
     * Create an isosphere, based on an icosahedron. The number of times that the faces of the icosahedron are to be
     * subdivided is given by the level parameters.
     */
    public static Sphere makeIcosphere(float radius, int level) {
        ArrayList<Float> verts = new ArrayList<Float>();         // Vertex data.
        ArrayList<Integer> faceIndx = new ArrayList<Integer>();  // Face data.
        for (float[] v : icosahedronVertices) { // Add icosahedron vertex to vertex data.
            verts.add(v[0] / icosahedronRadius);
            verts.add(v[1] / icosahedronRadius);
            verts.add(v[2] / icosahedronRadius);
        }
        for (int[] f : icosahedronFaces) {
            // Subdivide each face of the icosahedron the given number of times.
            subdivide(f[0], f[1], f[2], verts, faceIndx, level);
        }
        int vertexCount = verts.size();
        int indexCount = faceIndx.size();
        // FloatBuffer fbVertices = BufferUtil.newFloatBuffer(verts.size());
        FloatBuffer fbVertices = Buffers.newDirectFloatBuffer(verts.size());
        for (float x : verts) {// Copy vertex data into the vertex buffer.
            fbVertices.put(x * radius);
        }
        fbVertices.rewind();
        // IntBuffer ibElements = BufferUtil.newIntBuffer(faceIndx.size());
        IntBuffer ibElements = Buffers.newDirectIntBuffer(faceIndx.size());
        for (int i : faceIndx)
            // Copy the face data into the face index buffer.
            ibElements.put(i);
        ibElements.rewind();

        return new Sphere(radius, fbVertices, ibElements, vertexCount, indexCount);
    }

    /**
     * Subdivides a triangular face on the unit sphere and stores the data for all the (sub-)faces that are generated
     * into the list of vertex coordinates and the list of vertex indices for faces. (Note that a given vertex will
     * actually be generated twice, and that no attempt is made to eliminate this redundancy.)
     * 
     * @param v1
     *            Index in vertex list of the first vertex of the face.
     * @param v2
     *            Index in vertex list of the second vertex of the face.
     * @param v3
     *            Index in vertex list of the third vertex of the face.
     * @param vertices
     *            The vertex list.
     * @param faces
     *            The list of vertex indices for each face that is generated.
     * @param level
     *            The number of times the face is to be subdivided.
     */
    private static void
            subdivide(int v1, int v2, int v3, ArrayList<Float> vertices, ArrayList<Integer> faces, int level) {
        if (level == 0) {
            // For level 0, add the vertex indices for this face to the vertex data.
            faces.add(v1);
            faces.add(v2);
            faces.add(v3);
        } else {  // Subdivide the face into 4 triangles, and then subdivide
            // each of those triangles (level-1) times. The new vertices
            // that are generated are placed in the vertex list. There
            // is a new vertex half-way between each pair of vertices
            // of the original face.
            float a1 = (vertices.get(3 * v1) + vertices.get(3 * v2));
            float a2 = (vertices.get(3 * v1 + 1) + vertices.get(3 * v2 + 1));
            float a3 = (vertices.get(3 * v1 + 2) + vertices.get(3 * v2 + 2));
            float length = (float)Math.sqrt(a1 * a1 + a2 * a2 + a3 * a3);
            a1 /= length;
            a2 /= length;
            a3 /= length;
            int indexA = vertices.size() / 3;
            vertices.add(a1);
            vertices.add(a2);
            vertices.add(a3);

            float b1 = (vertices.get(3 * v3) + vertices.get(3 * v2));
            float b2 = (vertices.get(3 * v3 + 1) + vertices.get(3 * v2 + 1));
            float b3 = (vertices.get(3 * v3 + 2) + vertices.get(3 * v2 + 2));
            length = (float)Math.sqrt(b1 * b1 + b2 * b2 + b3 * b3);
            b1 /= length;
            b2 /= length;
            b3 /= length;
            int indexB = vertices.size() / 3;
            vertices.add(b1);
            vertices.add(b2);
            vertices.add(b3);

            float c1 = (vertices.get(3 * v1) + vertices.get(3 * v3));
            float c2 = (vertices.get(3 * v1 + 1) + vertices.get(3 * v3 + 1));
            float c3 = (vertices.get(3 * v1 + 2) + vertices.get(3 * v3 + 2));
            length = (float)Math.sqrt(c1 * c1 + c2 * c2 + c3 * c3);
            c1 /= length;
            c2 /= length;
            c3 /= length;
            int indexC = vertices.size() / 3;
            vertices.add(c1);
            vertices.add(c2);
            vertices.add(c3);

            subdivide(v1, indexA, indexC, vertices, faces, level - 1);
            subdivide(indexA, v2, indexB, vertices, faces, level - 1);
            subdivide(indexC, indexB, v3, vertices, faces, level - 1);
            subdivide(indexA, indexB, indexC, vertices, faces, level - 1);
        }
    }
}