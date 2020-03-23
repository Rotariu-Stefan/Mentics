package com.mentics.qd.ui.controls_menus;

import static com.mentics.qd.datastructures.ArrayTreeUtil.*;

import java.util.Arrays;

import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.NavigationController;
import com.mentics.qd.ui.controls_custom.SelectionController;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;


// handles topmenu common inputs for quip,groups
public abstract class TopMenuController extends UIController_Hud {

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.selback && onQuip == false) {		//if back is pressed(and there is a "back")
            if (uiMgr.gPath.length == 1) {
                uiMgr.gPath = new short[0];
                updateSN();
            } else if (uiMgr.gPath.length != 0) {
                uiMgr.gPath = Arrays.copyOf(uiMgr.gPath, uiMgr.gPath.length - 1);
                updateSN();
            }
        }

        else if (getGroup(input) != -1) {			//is a group key is pressed
            short i = getGroup(input);
            gotoGroupsMenu();
            uiMgr.gPath = Arrays.copyOf(uiMgr.gPath, uiMgr.gPath.length + 1);
            uiMgr.gPath[uiMgr.gPath.length - 1] = i;
            updateSN();
        }

        else if (input.getCharacter() == UIMapping.seltop) {	//if select top is pressed
            gotoGroupsMenu();
            uiMgr.gPath = new short[0];
            updateSN();
        }

        return super.inputEvent(input);
    }

    // calls selection and navigation to update their values
    private void updateSN() {
        screen.findControl("selection", SelectionController.class).updateSelection(false);
        screen.findControl("navigation", NavigationController.class).updateMaxSizes();
    }

    // (possibly)switches to groups menu if group command key is typed
    private void gotoGroupsMenu() {
        if (onQuip) {
            element.hide();
            screen.findElementById("groups").show();
            screen.findElementById("groups").setFocus();
            onQuip = false;
        }
    }

    // translated group command key to (sub)group index
    public short getGroup(NiftyStandardInputEvent input) {
        for (short i = 0; i < DIMENSION; i++)
            if (input.getCharacter() == UIMapping.gRow[i]) return i;
        return -1;
    }
}
