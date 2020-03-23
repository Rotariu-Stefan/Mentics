package com.mentics.qd.ui.controls_outer;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.ui.UIController;


// handles the possible inputs from the paused menu
public class PauseMenuController extends UIController {

    // exists the application
    public void exit() {
        System.out.println("Pause Menu - EXIT");
        System.exit(0);
    }

    // resumes the game
    public void resume() {
        System.out.println("Pause Menu - RESUME");
        boolean pause = QuipNebula.togglePause();
        screen.findElementById("pause_layer").setVisible(pause);
    }

    // quits to main menu
    public void quit() {
        System.out.println("Pause Menu - QUIT");
        QuipNebula.initMainMenu();
    }
}
