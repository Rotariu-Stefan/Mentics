package com.mentics.qd.ui;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.newt.opengl.GLWindow;

import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;

//contains some utility general methods for Nifty stuff
public class NiftyUtil {

    // displays a string text in a nifty text element
    public static void displayText(Screen screen, String elementName, String text) {
        Element cell = screen.findElementById(elementName);
        TextRenderer textRenderer = cell.getRenderer(TextRenderer.class);
        textRenderer.setText(text);
    }

    // displays a string text in a nifty text element
    public static void displayText(Element root, String elementName, String text) {
        Element cell = root.findElementById(elementName);
        TextRenderer textRenderer = cell.getRenderer(TextRenderer.class);
        textRenderer.setText(text);
    }

    // displays a string text in a nifty text element
    public static void displayText(Element cell, String text) {
        TextRenderer textRenderer = cell.getRenderer(TextRenderer.class);
        textRenderer.setText(text);
    }

    // retrieves a string text from a nifty text element
    public static String getText(Screen screen, String elementName) {
        Element cell = screen.findElementById(elementName);
        TextRenderer textRenderer = cell.getRenderer(TextRenderer.class);
        return textRenderer.getOriginalText();
    }

    // retrieves a string text from a nifty text element
    public static String getText(Element root, String elementName) {
        Element cell = root.findElementById(elementName);
        TextRenderer textRenderer = cell.getRenderer(TextRenderer.class);
        return textRenderer.getOriginalText();
    }

    // retrieves a string text from a nifty text element
    public static String getText(Element cell) {
        TextRenderer textRenderer = cell.getRenderer(TextRenderer.class);
        return textRenderer.getOriginalText();
    }

    // used to render nifty
    public static void switchTo2D(GL2 gl, GLWindow window) {
        // Enable 2D
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);                 // Select projection
        gl.glPushMatrix();                          // Push the matrix
        gl.glLoadIdentity();           // Reset the matrix
        gl.glOrtho(0, window.getWidth(), window.getHeight(), 0, -1, 1);            // Select ortho mode
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);                  // Select Modelview matrix
        gl.glPushMatrix();                            // Push the Matrix
        gl.glLoadIdentity();                   // Reset the Matrix

        gl.glDisable(GL.GL_DEPTH_TEST);                    // Cure z-fighting
    }

    // used to render nifty
    public static void switchTo3D(GL2 gl, GLWindow window) {
        // Disable 2D
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);// Select Modelview
        gl.glPopMatrix();// Pop the matrix
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);// Select projection
        gl.glPopMatrix();// Pop the matrix

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    // translated key presses to digits(or -1 if not)
    public static int getDigit(NiftyStandardInputEvent input) {
        for (int i = 0; i <= 9; i++)
            if (input.getCharacter() == UIMapping.numbers[i]) return i;
        return -1;
    }
}
