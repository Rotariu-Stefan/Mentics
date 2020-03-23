package com.mentics.qd.jogl;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES1.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.*;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.glu.GLU;

import com.mentics.qd.ItemVisitor;
import com.mentics.qd.LinkVisitor;
import com.mentics.qd.QuipNebula;
import com.mentics.qd.items.Explosion;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.items.Shot;
import com.mentics.qd.ui.PanelValues;
import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIManager;


public class OpenGLCanvas implements GLEventListener, ItemVisitor, LinkVisitor {
    public final int[] wListAll = new int[] {1024, 1280, 1280, 1280, 1366, 1440, 1600, 1920};
    public final int[] hListAll = new int[] {768, 800, 960, 1024, 768, 900, 900, 1080};
    public final int deskResW = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public final int deskResH = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    
    /**
     * Warning, this should only be considered valid during call flow of display method
     */
    private GL2 gl;
    private GLU glu;    
    private SkyBox skyBox;    	//the background sky
    
    private UIManager uiMgr;			//manages ui processes/changes
    private VideoValues videoValues;	//game video settings    

    // for rendering objects
    List<Quip> quips;
    List<Node> nodes;
    ItemRenderer<Quip> quipRenderer;
    ItemRenderer<Node> nodeRenderer;
    ItemRenderer<Explosion> explosionRenderer;

    public OpenGLCanvas() {
        glu = new GLU();    // get GL Utilities
        
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("lib/QData/cfg_video_values.ser"));
            videoValues = (VideoValues)in.readObject();
            in.close();
        } catch (Exception e) {
            System.out.println("NO VIDEO PROFILE FOUND.");
        }
    }
    
    //sets the player quip when it is created
    public void setActiveQuip(Quip quip) {
        uiMgr.activeQuip = quip;
    }
    
    //retreive selected group to keep in view
    public short[] getSelectedGroup() {
        return uiMgr.gPath;
    }
    
    //saves video settings values on drive for the next time
    public void saveNewVideoValues(VideoValues vv){
    	videoValues=vv;
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("lib/QData/cfg_video_values.ser"));
            out.writeObject(videoValues);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //applies the video settings to the current window
    public void updateVideoSettings(){
    	if(videoValues != null){
    		if(videoValues.fullscr)
				QuipNebula.window.setFullscreen(true);
    		else {
    			QuipNebula.window.setFullscreen(false);
    			QuipNebula.window.setSize(videoValues.resW, videoValues.resH);
    			QuipNebula.window.setPosition(4, 23);
    		}
    	}
		else {
    		QuipNebula.window.setSize(1024, 768);
    		QuipNebula.window.setPosition(4, 23);
		}
    }

    //initiates needed values/objects, cleans up/creates resources when entering Skirmish Mode(New) <- for rendering
    public void initSkirmishNewRendering() {
        quips = new ArrayList<>();
        nodes = new ArrayList<>();
        quipRenderer = new QuipRenderer();
        quipRenderer.initialize(gl);
        nodeRenderer = new NodeRenderer();
        nodeRenderer.initialize(gl);
        explosionRenderer = new ExplosionSequenceRenderer();
        explosionRenderer.initialize(gl);
        initSkyBox(gl);

        uiMgr.setScreen("gui");
    }

    //initiates needed values/objects, cleans up/creates resources when entering the Main Menu <- for rendering
    public void initMainMenuRendering() {
        quips = null;
        nodes = null;
        quipRenderer = null;
        nodeRenderer = null;
        explosionRenderer = null;
        skyBox = null;

        uiMgr.setScreen("opening-menu");
    }

    //adds a new notification message to the notify panel on the UI
    public void addNotification(String notification, int noteType) {
        uiMgr.addNotification(notification, noteType);
    }

    @Override
    public void init(GLAutoDrawable arg0) {
        gl = arg0.getGL().getGL2();      // get the OpenGL graphics context

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      	// set clear depth value to farthest
        gl.glEnable(GL_DEPTH_TEST); 	// enables depth testing
        gl.glDepthFunc(GL_LEQUAL);  	// the type of depth test to do
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
        gl.glShadeModel(GL_SMOOTH); 	// blends colors nicely, and smooths out lighting
        // Disable vsync
        gl.setSwapInterval(0);
        gl.glShadeModel(GLLightingFunc.GL_FLAT);

        uiMgr = new UIManager("opening-menu");
        QuipNebula.gameState = 1;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        if (QuipNebula.gameState == 2) {
            nodes.clear();
            QuipNebula.allData.visitItems(item -> {
                if (item instanceof Node) {
                    nodes.add((Node)item);
                }
            });

            QuipNebula.allData.camera.renderItem(gl, glu);

            QuipNebula.allData.visitLinks(this);
            // Render Items
            quipRenderer.render(gl, QuipNebula.allData.allQuips);
            nodeRenderer.render(gl, nodes);
            explosionRenderer.render(gl, QuipNebula.allData.explosions);
            renderShots(gl, glu);

            skyBox.draw(gl);
        }

        uiMgr.renderUI(gl);

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        gl = drawable.getGL().getGL2();	// get the OpenGL 2 graphics context

        if (height == 0) height = 1;	// prevent divide by zero
        float aspect = (float)width / height;

        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
        gl.glLoadIdentity();             // reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.01, 100000.0); // fovy, aspect, zNear, zFar
        // gl.glFrustum( 500, 500, 500, 500, 0.1, 100000.0 );

        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset

        uiMgr.repositionUI(QuipNebula.resolutionW, QuipNebula.resolutionH, width, height);
        QuipNebula.resolutionW = width;
        QuipNebula.resolutionH = height;
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        System.out.println("Window - EXIT");
        System.exit(0);
    }

    @Override
    public void visit(Item item) {
        item.renderItem(gl, glu);
    }

    @Override
    public void visit(Item from, Item to) {
        // TODO
    }

    private void initSkyBox(GL2 gl) {
        URL picURL = getClass().getResource("/com/mentics/qd/res/starfield_1.png");
        CubeMap skyBoxCubeMap = new CubeMap(gl, picURL, picURL, picURL, picURL, picURL, picURL);
        skyBox = new SkyBox(gl, skyBoxCubeMap);
    }

    private void renderShots(GL2 gl, GLU glu) {
        gl.glPointSize(2);
        gl.glBegin(GL.GL_POINTS);
        List<Shot> shots = QuipNebula.allData.getShots();
        shots.forEach(shot -> shot.renderItem(gl, glu));
        gl.glEnd();
        gl.glPointSize(1);
    }
}
