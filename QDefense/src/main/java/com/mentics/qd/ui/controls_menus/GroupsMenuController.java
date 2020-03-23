package com.mentics.qd.ui.controls_menus;

import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.NavigationController;
import com.mentics.qd.ui.controls_custom.SelectionController;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;


// handles the groups topmenu specific inputs
public class GroupsMenuController extends TopMenuController {

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.cRow1[0]) {			//q is pressed
            screen.findElementById("gmotion").show();
            screen.findElementById("gmotion").setFocus();
        }

        else if (input.getCharacter() == UIMapping.cRow1[1]) {		//w is pressed
            // TODO:group behaviour
        }

        else if (input.getCharacter() == UIMapping.cRow1[2]) {		//e is pressed
            screen.findElementById("grouping").show();
            screen.findElementById("grouping").setFocus();
        }

        else if (input.getCharacter() == UIMapping.cRow1[3]) {		//r is pressed
            // TODO:group shoot
        }

        else if (input.getCharacter() == UIMapping.cRow1[4]) {		//t is pressed
            element.hide();
            screen.findElementById("quip").show();
            screen.findElementById("quip").setFocus();
            uiMgr.gPath = new short[0];
            onQuip = true;

            screen.findControl("selection", SelectionController.class).updateSelection(true);
            screen.findControl("navigation", NavigationController.class).updateMaxSizes();
        }

        return super.inputEvent(input);
    }

}
