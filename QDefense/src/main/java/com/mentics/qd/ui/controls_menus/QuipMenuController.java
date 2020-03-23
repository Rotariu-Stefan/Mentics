package com.mentics.qd.ui.controls_menus;

import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;


// handles quip topmenu specific inputs
public class QuipMenuController extends TopMenuController {

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.cRow1[0]) {		//q is pressed
            onQuip = true;
            screen.findElementById("move").show();
            screen.findElementById("move").setFocus();
        }

        else if (input.getCharacter() == UIMapping.cRow1[1]) {	//w is pressed
            // TODO:quip behaviour
        }

        else if (input.getCharacter() == UIMapping.cRow1[2]) {	//e is pressed
            screen.findElementById("addnodes").show();
            screen.findElementById("addnodes").setFocus();
        }

        return super.inputEvent(input);
    }

    @Override
    public void onStartScreen() {
    	super.onStartScreen();
    	
        element.setFocus();
        onQuip = true;
        
        //test controls
        
    }
}
