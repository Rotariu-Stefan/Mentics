package com.mentics.qd.ui.controls_menus;

import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;


// handles submenu common inputs for addnodes,grouping,motion
public abstract class SubMenuController extends UIController_Hud {

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

    	//when a command is executed or cancelled(when this should close and move on to other)
        if (input.getCharacter() == UIMapping.selback || input.getCharacter() == UIMapping.activate) {
            element.hide();
            screen.findElementById(onQuip ? "quip" : "groups").setFocus();

        } else if (!onQuip) {		//while on groups
            if (input.getCharacter() == UIMapping.cRow1[0]) gotoSubMenu("gmotion");			//q is pressed
            else if (input.getCharacter() == UIMapping.cRow1[2]) gotoSubMenu("grouping");	//e is pressed
            else if (input.getCharacter() == UIMapping.cRow1[4]) {							//t is pressed
                element.hide();
                screen.findElementById("groups").hide();
                screen.findElementById("quip").show();
                screen.findElementById("quip").setFocus();
                uiMgr.gPath = new short[0];
                onQuip = true;
            }
        }

        else if (onQuip) {			//while on quip
            if (input.getCharacter() == UIMapping.cRow1[0]) gotoSubMenu("move");			//q is pressed
            else if (input.getCharacter() == UIMapping.cRow1[2]) gotoSubMenu("addnodes");	//e is pressed
        }

        return super.inputEvent(input);
    }

    // goes to another submenu if command key is typed
    private void gotoSubMenu(String id) {
        element.hide();
        screen.findElementById(id).show();
        screen.findElementById(id).setFocus();
    }
}
