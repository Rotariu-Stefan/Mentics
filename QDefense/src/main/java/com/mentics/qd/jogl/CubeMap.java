package com.mentics.qd.jogl;

import java.io.IOException;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class CubeMap {
    private Texture cubeMapTex = null;
    private URL posX, posY, posZ, negX, negY, negZ;

    /**
     * Creates a new cube map with the specified six textures.
     * 
     * @param posX
     *            path to posX texture
     * @param negX
     *            path to negX texture
     * @param posY
     *            path to posY texture
     * @param negY
     *            path to negY texture
     * @param posZ
     *            path to posZ texture
     * @param negZ
     *            path to negZ texture //@param width texture width in pixels //@param height texture height in pixels
     */
    public CubeMap(GL2 gl, URL posX, URL negX, URL posY, URL negY, URL posZ, URL negZ /*, int width, int height */) {
        this.posX = posX;
        this.negX = negX;
        this.posY = posY;
        this.negY = negY;
        this.posZ = posZ;
        this.negZ = negZ;

        this.init(gl); // init the cube map data
    }

    /**
     * Initializes the cube map data.
     * 
     * @param gl
     */
    private void init(GL2 gl) {
        this.cubeMapTex = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
        GLProfile defaultProfile = GLProfile.getDefault();

        try {
            this.cubeMapTex.updateImage(gl, TextureIO.newTextureData(defaultProfile, this.posX, true, null),
                    GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
            this.cubeMapTex.updateImage(gl, TextureIO.newTextureData(defaultProfile, this.negX, true, null),
                    GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
            this.cubeMapTex.updateImage(gl, TextureIO.newTextureData(defaultProfile, this.posY, true, null),
                    GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
            this.cubeMapTex.updateImage(gl, TextureIO.newTextureData(defaultProfile, this.negY, true, null),
                    GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
            this.cubeMapTex.updateImage(gl, TextureIO.newTextureData(defaultProfile, this.posZ, true, null),
                    GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
            this.cubeMapTex.updateImage(gl, TextureIO.newTextureData(defaultProfile, this.negZ, true, null),
                    GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
        } catch (GLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cubeMapTex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        cubeMapTex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        cubeMapTex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        cubeMapTex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        cubeMapTex.setTexParameteri(gl, GL2ES2.GL_TEXTURE_WRAP_R, GL.GL_CLAMP_TO_EDGE);

    }

    public void bind(GL2 gl) {
        gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        this.cubeMapTex.enable(gl);
        this.cubeMapTex.bind(gl);

    }

    public void release(GL2 gl) {
        gl.glActiveTexture(GL.GL_TEXTURE0);
        this.cubeMapTex.disable(gl);
        gl.glPopAttrib();
    }
}
