package com.mentics.qd.ui;

import java.util.Properties;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;

//base abstract Nifty Controller for ALL panels that receive input in the application
//isn't related to the reusable custom nifty controls
public abstract class UIController implements Controller {
	
    protected static UIManager uiMgr;	//reference to the UI Manager from OpenGLCanvas for easy access
    protected static Screen screen;		//current screen
    
    protected Element element;			//this element
	
    //sets the UI Manager for the whole UI to have easy access to it
    public static void initUIMgr(UIManager uim) {
        uiMgr = uim;
    }

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        // System.out.println(arg2.getId() + " - Bind.");

        screen = arg1;
        element = arg2;
    }

	@Override
	public void init(Parameters arg0) {
        // System.out.println(element.getId() + " - Init.");
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        // System.out.println(element.getId() + " - Input Event: " + inputEvent + " " + inputEvent.getCharacter());
    	
        return false;
    }

    @Override
    public void onFocus(boolean focus) {
        // if (focus) System.out.println(element.getId() + " - Focused.");
        // else System.out.println(element.getId() + " - Focused.");
    	
    	if(focus) uiMgr.focusedElement = element;
    }

    @Override
    public void onStartScreen() {
        // System.out.println(element.getId() + " - Start Screen.");
    }
}
