package com.mentics.qd.ui.controls_outer;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.items.Item;
import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIManager;
import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class ScreenGuiController implements ScreenController, KeyInputHandler {
	private boolean hidden;				//if the Hud is hidden or not
	
    private static UIManager uiMgr;		//reference to the UI Manager from OpenGLCanvas for easy access
    private static Screen screen;		//current screen
	
    //sets the UI Manager for the whole UI to have easy access to it
    public static void initUIMgr(UIManager uim) {
        uiMgr = uim;
    }
    
	@Override
	public void bind(Nifty arg0, Screen arg1) {
		screen = arg1;
		hidden = false;
	}

	@Override
	public void onEndScreen() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartScreen() {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean keyEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;
		
        if (input.getCharacter() == UIMapping.pause) {		//when the game is (un)paused
            boolean pause = QuipNebula.togglePause();
            screen.findElementById("pause_layer").setVisible(pause);
        }
        else if (input.getCharacter() == UIMapping.hide) {	//when the hud is set to (un)hide
        	if(hidden) {
            	screen.findElementById("top").show();
            	screen.findElementById("chat").onStartScreen();
            	if(UIController_Hud.isOnQuip())	screen.findElementById("groups").hide();
            	else screen.findElementById("quip").hide();
            	uiMgr.focusedElement.show();
            	uiMgr.focusedElement.setFocus();
        	}
        	else {
	        	screen.findElementById("top").hide();
	        	uiMgr.focusedElement.hide();
        	}
        	hidden = !hidden;
        }
        else if (input.getCharacter() == UIMapping.TTest) {
        	
        }
        
		return false;
	}
}
