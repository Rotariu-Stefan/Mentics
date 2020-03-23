package com.mentics.qd.ui;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;


// maps keyboard events to nifty events to be handled by the controllers
public class UIMapping implements NiftyInputMapping {

    // navigation keys
    public static char navRow[] = { KeyboardInputEvent.KEY_UP, KeyboardInputEvent.KEY_DOWN,
                                   KeyboardInputEvent.KEY_LEFT, KeyboardInputEvent.KEY_RIGHT };

    // 1st control key row
    public static char cRow1[] = { KeyboardInputEvent.KEY_Q, KeyboardInputEvent.KEY_W, KeyboardInputEvent.KEY_E,
                                  KeyboardInputEvent.KEY_R, KeyboardInputEvent.KEY_T };

    // 2nd control key row
    public static char cRow2[] = { KeyboardInputEvent.KEY_A, KeyboardInputEvent.KEY_S, KeyboardInputEvent.KEY_D,
                                  KeyboardInputEvent.KEY_F, KeyboardInputEvent.KEY_G };

    // 3rd control key row
    public static char cRow3[] = { KeyboardInputEvent.KEY_Z, KeyboardInputEvent.KEY_X, KeyboardInputEvent.KEY_C,
                                  KeyboardInputEvent.KEY_V, KeyboardInputEvent.KEY_B };

    // group control key row
    public static char gRow[] = { KeyboardInputEvent.KEY_1, KeyboardInputEvent.KEY_2, KeyboardInputEvent.KEY_3,
                                 KeyboardInputEvent.KEY_4, KeyboardInputEvent.KEY_5, KeyboardInputEvent.KEY_6 };

    public static char pause = KeyboardInputEvent.KEY_ESCAPE;	// pause
    public static char hide = KeyboardInputEvent.KEY_F11;		// hide the Hud
    public static char seltop = KeyboardInputEvent.KEY_F1;		// select top group
    public static char selback = 0;								// select a menu back 1 level or a (parent)group back 1 level
    public static char activate = KeyboardInputEvent.KEY_SPACE;	// confirm operation/queue a command
    public static char TTest = KeyboardInputEvent.KEY_F5;		// a random Key to test stuff

    public static char tab = KeyboardInputEvent.KEY_TAB;		// tab?
    public static char backspace = KeyboardInputEvent.KEY_BACK;	// normal backspace for texts
    
    // normal number for text (might change)
    public static char numbers[] = { KeyboardInputEvent.KEY_0, KeyboardInputEvent.KEY_1, KeyboardInputEvent.KEY_2,
                                    KeyboardInputEvent.KEY_3, KeyboardInputEvent.KEY_4, KeyboardInputEvent.KEY_5,
                                    KeyboardInputEvent.KEY_6, KeyboardInputEvent.KEY_7, KeyboardInputEvent.KEY_8,
                                    KeyboardInputEvent.KEY_9 };

    // conversion method. doesn't use the constants itself just provides the char to be checked against them
    @Override
    public NiftyInputEvent convert(final KeyboardInputEvent inputEvent) {
        if (inputEvent.isKeyDown()) {
            NiftyStandardInputEvent.Character.setCharacter((char)inputEvent.getKey());
            return NiftyStandardInputEvent.Character;
        } else return null;
    }
}
