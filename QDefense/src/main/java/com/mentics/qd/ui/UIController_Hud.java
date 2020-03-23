package com.mentics.qd.ui;

import java.util.Properties;

import com.mentics.qd.QuipNebula;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;


// abstract controller for UI elements (in-game HUD)
// saves fields necessary for UI elements (in-game HUD)
// contains outputs to track events and common events for UI elements (in-game HUD)
// similar to a screen controller
public abstract class UIController_Hud extends UIController {

    protected static boolean onQuip;	//checks if the quip(or groups if not) is selected
    
    public static boolean isOnQuip(){
    	return onQuip;
    }
}