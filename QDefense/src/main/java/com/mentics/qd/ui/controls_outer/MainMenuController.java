package com.mentics.qd.ui.controls_outer;

import java.io.FileNotFoundException;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.ui.UIController;


// handles the Main menu (the screen level menu shown to the player when 1st starting the game)
public class MainMenuController extends UIController {

    // starts the story playthrough
    public void story() {
        System.out.println("Main Menu - STORY");
    }

    // sets all the needed stuff required for a new skirmish and switches gameState
    public void skirmish() {
        System.out.println("Main Menu - SKIRMISH");

        try {
            QuipNebula.initSkirmishNew();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // user options for the game
    public void options() {
        System.out.println("Main Menu - OPTIONS");

        element.hide();
        screen.findElementById("options_menu").show();
        screen.findElementById("options_menu").setFocus();
    }

    // exit the application
    public void exit() {
        System.out.println("Main Menu - EXIT");

        System.exit(0);
    }
}
