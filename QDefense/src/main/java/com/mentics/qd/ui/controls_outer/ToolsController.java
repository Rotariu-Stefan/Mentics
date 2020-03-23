package com.mentics.qd.ui.controls_outer;


import com.mentics.qd.ui.UIController;

import de.lessvoid.nifty.elements.Element;

//am outer menu with tools to help customize the HUD in the config screen
//it is an always visible outer menu and cannot itself be changed(is Not part of the HUD)
//can switch between the HUD layers to bring them to front
public class ToolsController extends UIController {
	
    @Override
    public void onStartScreen() {
        super.onStartScreen();
        
    	top();
    }

    //brings top HUD layer to front, fades all the others
    public void top() {
        unfade("top");
        fade("sub");
        fade("dialog");
    }

    //brings sub HUD layer to front, fades all the others
    public void sub() {
        fade("top");
        unfade("sub");
        fade("dialog");
    }

    //brings dialog HUD layer to front, fades all the others
    public void dialog() {
        fade("top");
        fade("sub");
        unfade("dialog");
    }

    //brings a layer with the "layer" ID to front
    private void unfade(String layer) {
    	screen.findElementById(layer).setRenderOrder(2);
        for (Element p : screen.findElementById(layer).getChildren()) {
            p.setVisibleToMouseEvents(true);
            p.getChildren().get(0).show();
            p.getChildren().get(1).hide();
        }
    }

    //fades a layer with the "layer" ID in the background
    //shows a faded transparent panel with the same dimensions that takes no input in the background instead of the regular one
    private void fade(String layer) {
    	screen.findElementById(layer).setRenderOrder(1);
        for (Element p : screen.findElementById(layer).getChildren()) {
            p.setVisibleToMouseEvents(false);
            p.getChildren().get(0).hide();
            p.getChildren().get(1).show();
        }
    }
}
