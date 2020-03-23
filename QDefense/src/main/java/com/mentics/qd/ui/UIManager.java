package com.mentics.qd.ui;

import static com.mentics.math.vector.VectorUtil.*;
import static javax.media.opengl.GL.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import nifty_newt.NewtInputSystem;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.mentics.qd.QuipNebula;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.items.Camera;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.Quip;
import com.mentics.qd.ui.controls_custom.ChatController;
import com.mentics.qd.ui.controls_custom.NavigationController;
import com.mentics.qd.ui.controls_custom.NotifyController;
import com.mentics.qd.ui.controls_outer.ScreenGuiController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.jogl.render.JoglRenderDevice;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.TimeProvider;


// contains data needed by controllers
// handles frame-by-frame UI changes
// provides methods to game-level UI changes
// renders the nifty UI
public class UIManager {

    // FIELDS
    //public static final float HALF_ACTIVATION_ANGLE = (float)(110.0 / 2.0 * Math.PI / 180.0);
    //public static final float HALF_PI = (float)(Math.PI / 2.0);
    private final float[] cyan = new float[] { 0, 1, 1 };
    private float[] itemDirection = new float[3], up = new float[3], right = new float[3];

    public Nifty nifty;			//nifty instance
    public Screen screen;		//current screen
    public List<PanelValues> panelsValues;	//customizable values for the Hud(saved to transfer between screens without repeated access to files)
    public Quip activeQuip;		// the active player quip
    public short[] gPath;		// retains the tree path of the currently selected group
    public Element focusedElement;		//the current nifty element with keyboard focus

    // CONSTRUCTORS
    public UIManager(String screenID) {
        NewtInputSystem inputSystem = new NewtInputSystem();
        QuipNebula.window.addMouseListener((MouseListener)inputSystem);
        QuipNebula.window.addKeyListener((KeyListener)inputSystem);

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("lib/QData/cfg_ui_values.ser"));
            panelsValues = (List<PanelValues>)in.readObject();
            in.close();
        } catch (Exception e) {
            resetDefaults();
        }

        nifty = new Nifty(new JoglRenderDevice(), new NullSoundDevice(), inputSystem, new TimeProvider());
        setScreen(screenID);
        UIController.initUIMgr(this);
        ScreenGuiController.initUIMgr(this);
    }

    //sets the current screen and other values related to the change
    public void setScreen(String screenID) {
        if (screenID.equals("config")) {
            nifty.fromXml("com/mentics/qd/res/ui_layouts/uiConfig.xml", screenID);
        } else {
            gPath = new short[0];
            nifty.fromXml("com/mentics/qd/res/ui_layouts/ui.xml", screenID);
        }
        screen = nifty.getScreen(screenID);
        setPanelValues();
    }

    // METHODS
    public void addNotification(String notification, int noteType) {
        screen.findControl("notify", NotifyController.class).addNotification(notification, noteType);
    }

    // renders/updates nifty UI
    public void renderUI(GL2 gl) {
        if (QuipNebula.gameState == 1) {
            NiftyUtil.switchTo2D(gl, QuipNebula.window);
            nifty.render(true);
            nifty.update();
            NiftyUtil.switchTo3D(gl, QuipNebula.window);

        } else if (QuipNebula.gameState == 2) {
            NiftyUtil.switchTo2D(gl, QuipNebula.window);
            nifty.render(false);
            NiftyUtil.switchTo3D(gl, QuipNebula.window);

            drawItemBorders(gl, QuipNebula.allData.camera);

            NiftyUtil.switchTo2D(gl, QuipNebula.window);
            nifty.render(false);
            nifty.update();
            if (QuipNebula.paused == false) {
                screen.findControl("navigation", NavigationController.class).updateNavigation();
                screen.findControl("chat", ChatController.class).getNextResponse();
            }
            NiftyUtil.switchTo3D(gl, QuipNebula.window);
        }
    }

    // draws the selection borders around items
    public void drawItemBorders(GL2 gl, Camera camera) {
        Object tree = ArrayTreeUtil.getSubTree(activeQuip.nodes, gPath);
        ArrayTreeUtil.traverse(tree, node -> drawBorder(gl, camera, node, 0.10f, cyan));
    }

    // draws 1 border
    private void drawBorder(GL2 gl, Camera camera, Item item, float size, float[] color) {
        set(itemDirection, item.position);
        subtractInto(itemDirection, camera.position);
        normalize(itemDirection);
        addInto(itemDirection, camera.position);

        set(up, camera.up);
        multiplyInto(up, size / 2);

        set(right, camera.lookingDirection);
        // set(right, camera.lookingAt);
        // subtract(right, camera.position);
        crossInto(right, camera.up);
        normalize(right);
        multiplyInto(right, size / 2);

        addInto(itemDirection, up);
        addInto(itemDirection, right);

        multiplyInto(up, 2);
        multiplyInto(right, 2);

        gl.glDisable(GLLightingFunc.GL_LIGHTING);
        gl.glClear(GL_DEPTH_BUFFER_BIT);
        gl.glColor3fv(color, 0);
        
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3fv(itemDirection, 0);
        subtractInto(itemDirection, up);
        gl.glVertex3fv(itemDirection, 0);
        subtractInto(itemDirection, right);
        gl.glVertex3fv(itemDirection, 0);
        addInto(itemDirection, up);
        gl.glVertex3fv(itemDirection, 0);
        gl.glEnd();
    }

    //repositions the Hud when the window resolution changes to keep the overall feel
    public void repositionUI(int oldResW, int oldResH, int newResW, int newResH) {
        nifty.resolutionChanged();

        repositionUIValues(oldResW, oldResH, newResW, newResH);
        setPanelValues();
    }
    
    private void repositionUIValues(int oldW, int oldH, int newW, int newH){
        for (PanelValues pv : panelsValues) {
            boolean marginX = pv.x + pv.w / 2 <= oldW / 2;
            boolean marginY = pv.y + pv.h / 2 <= oldH / 2;

            pv.x = marginX ? pv.x : newW - (oldW - pv.x);
            pv.y = marginY ? pv.y : newH - (oldH - pv.y);
        }
    }
    
    //(re)sets the Hud values from the list
    public void setPanelValues() {
        if (screen.getScreenId().equals("opening-menu")) return;

        Element temp;
        for (PanelValues pv : panelsValues) {
            temp = screen.findElementById(pv.id);
            temp.setConstraintX(new SizeValue(pv.x + "px"));
            temp.setConstraintY(new SizeValue(pv.y + "px"));
            temp.setConstraintWidth(new SizeValue(pv.w + "px"));
            temp.setConstraintHeight(new SizeValue(pv.h + "px"));
        }
        screen.layoutLayers();
    }

    //saves to Hud configuration from config into the list and files on drive
    public void saveNewUIConfig() {
        panelsValues = new ArrayList<PanelValues>();
        for (Element p : screen.findElementById("top").getElements())
            panelsValues.add(new PanelValues(p.getId(), p.getX(), p.getY(), p.getWidth(), p.getHeight()));
        for (Element p : screen.findElementById("sub").getElements())
            panelsValues.add(new PanelValues(p.getId(), p.getX(), p.getY(), p.getWidth(), p.getHeight()));
        for (Element p : screen.findElementById("dialog").getElements())
            panelsValues.add(new PanelValues(p.getId(), p.getX(), p.getY(), p.getWidth(), p.getHeight()));
        
        repositionUIValues(QuipNebula.resolutionW, QuipNebula.resolutionH, 1024, 768);        
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("lib/QData/cfg_ui_values.ser"));
            out.writeObject(panelsValues);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        repositionUIValues(1024, 768, QuipNebula.resolutionW, QuipNebula.resolutionH); 
    }

    //loads and resets all Default Hud values into the file(overrides the current profile) 
    public void resetDefaults() {
        panelsValues = new ArrayList<PanelValues>();
        panelsValues.add(new PanelValues("selection", 5, 5, 170, 62));
        panelsValues.add(new PanelValues("navigation", 5, 67, 170, 152));
        panelsValues.add(new PanelValues("quip", 5, 219, 170, 93));
        panelsValues.add(new PanelValues("groups", 5, 219, 170, 156));
        panelsValues.add(new PanelValues("speed", 5, 538, 170, 62));
        panelsValues.add(new PanelValues("notify", 5, 600, 400, 163));
        panelsValues.add(new PanelValues("chat", 620, 600, 400, 163));
        panelsValues.add(new PanelValues("addnodes", 190, 283, 160, 53));
        panelsValues.add(new PanelValues("grouping", 190, 283, 160, 53));
        panelsValues.add(new PanelValues("gmotion", 190, 223, 320, 80));
        panelsValues.add(new PanelValues("move", 190, 76, 600, 608));
        panelsValues.add(new PanelValues("gformation", 190, 228, 600, 304));
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("lib/QData/cfg_ui_values.ser"));
            out.writeObject(panelsValues);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}