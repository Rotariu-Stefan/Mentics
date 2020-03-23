package com.mentics.qd.ui.controls_menus;

import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;


// handles dialogmenu common inputs for more, gformation
public abstract class DialogMenuController extends UIController_Hud {

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

    	//is selback or space is pressed(when this needs to close and move on the next thing)
        if (input.getCharacter() == UIMapping.selback || input.getCharacter() == UIMapping.activate) {
            element.hide();

            if (onQuip) {
                screen.findElementById("quip").enable();
                screen.findElementById("quip").setFocus();
            } else {
                screen.findElementById("groups").enable();
                screen.findElementById("gmotion").show();
                screen.findElementById("gmotion").setFocus();
            }
        }

        return super.inputEvent(input);
    }

    public void onFocus(boolean focus) {
        if (focus) {
            if (onQuip) screen.findElementById("quip").disable();
            else screen.findElementById("groups").disable();
        }

        super.onFocus(focus);
    }
}
