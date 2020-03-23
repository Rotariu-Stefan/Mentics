package com.mentics.qd.ui.controls_outer;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.ui.UIController;

//menu containing all the user options/preferences you can set for the game
public class OptionsController extends UIController {

	//shows video options
    public void video() {
        System.out.println("Options - VIDEO");
        
        element.hide();
        screen.findElementById("video_menu").show();
        screen.findElementById("video_menu").setFocus();
    }

    //shows audio options
    public void audio() {
        System.out.println("Options - AUDIO");
    }

    //shows key rebinding menu/possibilities
    public void rebind() {
        System.out.println("Options - REBIND");
    }

    //goes to the ui customizable Hud screen (config screen)
    public void config() {
        System.out.println("Options - CONFIG");

        uiMgr.setScreen("config");
    }

    //goes back to main menu
    public void back() {
        System.out.println("Options - BACK");

        element.hide();
        screen.findElementById("main_menu").show();
        screen.findElementById("main_menu").setFocus();
    }
}
