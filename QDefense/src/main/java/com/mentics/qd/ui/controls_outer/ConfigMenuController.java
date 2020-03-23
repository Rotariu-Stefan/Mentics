package com.mentics.qd.ui.controls_outer;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.ui.UIController;

//menu that (re)sets the changes done in the config screen and returns to main menu
//accessed when escape key is pressed in the config screen
public class ConfigMenuController extends UIController {

	//closes this and goes back to config screen
    public void resume() {
        element.hide();
    }

    //restores all default values <- overrides all changes made
    public void defaults() {
        uiMgr.resetDefaults();
        uiMgr.setPanelValues();
        uiMgr.repositionUI(1024, 768, QuipNebula.window.getWidth(), QuipNebula.window.getHeight());
    }

    //save current changes and goes back to main menu
    public void save() {
    	uiMgr.saveNewUIConfig();
    	uiMgr.setScreen("opening-menu");
    }

    //cancel current changes(back to last save) and goes back to main menu
    public void cancel() {
    	uiMgr.setScreen("opening-menu");
    }
}
