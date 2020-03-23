package com.mentics.qd.ui;

import java.util.Properties;

import com.mentics.qd.QuipNebula;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;

//controller for the "fake" panels in the Config screen used to customize the Hud
//elements that use this controller can be move&resized freely within certain limits
public class UIController_Config extends UIController {

    private int minWidth, maxWidth;		//width size limits
    private int minHeight, maxHeight;	//height size limits

    // panel movement & resize
    private int difX, difY;				//the difference when moving a panel between the current&old position
    private int onBorderX, onBorderY;	//checks if the mouse cursor is the left/up border, right/down border, bot on border (values -1,1,0 resp)
    private int temp;					//reusing this to save memory

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        minWidth = Integer.valueOf(arg3.getWithDefault("minW", "20"));
        maxWidth = Integer.valueOf(arg3.getWithDefault("maxW", "1000"));
        minHeight = Integer.valueOf(arg3.getWithDefault("minH", "20"));
        maxHeight = Integer.valueOf(arg3.getWithDefault("maxH", "700"));
    }
    
    //when you click on a panel
    public void clickPanel(int X, int Y) {
        difX = X - element.getX();
        difY = Y - element.getY();

        if (element.getX() <= X && X < element.getX() + 5) onBorderX = -1;
        else if (element.getX() + element.getWidth() - 5 < X && X <= element.getX() + element.getWidth()) onBorderX = 1;
        else onBorderX = 0;

        if (element.getY() <= Y && Y < element.getY() + 5) onBorderY = -1;
        else if (element.getY() + element.getHeight() - 5 < Y && Y <= element.getY() + element.getHeight()) onBorderY =
                1;
        else onBorderY = 0;
    }

    //when you move the mouse after clicking on a panel and HOLDING the button down
    public void movePanel(int x, int y) {
        if (onBorderX == 0 && onBorderY == 0) {// move
            temp = x - difX;
            if (temp >= 5 && temp + element.getWidth() <= QuipNebula.resolutionW - 5)
                element.setConstraintX(new SizeValue(temp + "px"));

            temp = y - difY;
            if (temp >= 5 && temp + element.getHeight() <= QuipNebula.resolutionH - 5)
                element.setConstraintY(new SizeValue(temp + "px"));

        } else {// resize
            if (onBorderX == -1 && x >= 5) {
                temp = element.getWidth() + (element.getX() - x);
                if (temp > minWidth && temp < maxWidth) {
                    element.setConstraintWidth(new SizeValue(temp + "px"));
                    element.setConstraintX(new SizeValue(x + "px"));
                }
            } else if (onBorderX == 1 && x <= QuipNebula.resolutionW - 5) {
                temp = (element.getWidth() + (x - (element.getX() + element.getWidth())));
                if (temp > minWidth && temp < maxWidth) element.setConstraintWidth(new SizeValue(temp + "px"));
            }

            if (onBorderY == -1 && y >= 5) {
                temp = element.getHeight() + (element.getY() - y);
                if (temp > minHeight && temp < maxHeight) {
                    element.setConstraintHeight(new SizeValue(temp + "px"));
                    element.setConstraintY(new SizeValue(y + "px"));
                }
            } else if (onBorderY == 1 && y <= QuipNebula.resolutionH - 5) {
                temp = element.getHeight() + (y - (element.getY() + element.getHeight()));
                if (temp > minHeight && temp < maxHeight) element.setConstraintHeight(new SizeValue(temp + "px"));
            }
        }

        element.getParent().layoutElements();
    }

    // aligns quip/groups and add/grouping panels
    public void releasePanel() {
        if (element.getId().equals("groups")) {
            screen.findElementById("quip").setConstraintX(new SizeValue(element.getX() + "px"));
            screen.findElementById("quip").setConstraintY(new SizeValue(element.getY() + "px"));
        }

        else if (element.getId().equals("quip")) {
            screen.findElementById("groups").setConstraintX(new SizeValue(element.getX() + "px"));
            screen.findElementById("groups").setConstraintY(new SizeValue(element.getY() + "px"));
        }

        else if (element.getId().equals("grouping")) {
            screen.findElementById("addnodes").setConstraintX(new SizeValue(element.getX() + "px"));
            screen.findElementById("addnodes").setConstraintY(new SizeValue(element.getY() + "px"));
        }

        else if (element.getId().equals("addnodes")) {
            screen.findElementById("grouping").setConstraintX(new SizeValue(element.getX() + "px"));
            screen.findElementById("grouping").setConstraintY(new SizeValue(element.getY() + "px"));
        }

        element.getParent().layoutElements();
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;
        
        if (input.getCharacter() == UIMapping.pause) {
            screen.findElementById("config_menu").setVisible(!screen.findElementById("config_menu").isVisible());
        }

        return super.inputEvent(input);
    }
}
