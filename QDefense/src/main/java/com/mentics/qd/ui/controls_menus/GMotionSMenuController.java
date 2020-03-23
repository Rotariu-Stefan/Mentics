package com.mentics.qd.ui.controls_menus;

import java.util.Optional;
import java.util.Properties;

import testing.WorldTest;

import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.CMD;
import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.quips.MotionCommand;
import com.mentics.qd.commands.quips.groups.GroupCommandBase;
import com.mentics.qd.commands.quips.groups.ShellCommand;
import com.mentics.qd.commands.quips.groups.SquareCommand;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.model.RadiusMoveTarget;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.NotifyController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;


// handles(shows) the movement values set in the dialogmenus and the motion command pushing and retrieval
public class GMotionSMenuController extends SubMenuController {

    private boolean fromTop;	// check if the panel is accessed(newly focused) from a Top menu or from a Dialog
                            	// (by activating/canceling it to return 1level)

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        fromTop = true;
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.cRow2[0]) {		//a is pressed
            element.hide();
            screen.findElementById("move").show();
            screen.findElementById("move").setFocus();
            fromTop = false;
        }

        else if (input.getCharacter() == UIMapping.cRow2[1]) {	//s is pressed
            element.hide();
            screen.findElementById("gformation").show();
            screen.findElementById("gformation").onStartScreen();
            fromTop = false;
        }

        else if (input.getCharacter() == UIMapping.activate) {	//space is pressed            
        	CMD.setGroupMotion(uiMgr.activeQuip, uiMgr.gPath,
            		QuipNebula.allData.getTargetByName(NiftyUtil.getText(element, "move-target-text")),
            		Float.parseFloat(NiftyUtil.getText(element, "move-distance-text")),
            		NiftyUtil.getText(element, "formation-type-text"),
            		Float.parseFloat(NiftyUtil.getText(element, "formation-param-text")));
            
            uiMgr.addNotification(
                    "Formation on group " + ArrayTreeUtil.pathToString(uiMgr.gPath) + " set to " +
                            NiftyUtil.getText(element, "formation-type-text") + " - " +
                            NiftyUtil.getText(element, "formation-param-text"), NotifyController.MSG_MOTION);
            uiMgr.addNotification(
                    "Movement on group " + ArrayTreeUtil.pathToString(uiMgr.gPath) + " set to " +
                            NiftyUtil.getText(element, "move-distance-text") + " distance from " +
                            NiftyUtil.getText(element, "move-target-text"), NotifyController.MSG_MOTION);
        }

        return super.inputEvent(input);
    }

    @Override
    public void onFocus(boolean focus) {
        if (focus) {
            if (fromTop) {
                Optional<MotionCommand> mc = uiMgr.activeQuip.motionConstraint(uiMgr.gPath);

                NiftyUtil.displayText(element, "move-target-text",
                        mc.map(shell -> shell.getLandmarkName()).orElse("Player"));
                NiftyUtil.displayText(element, "move-distance-text", mc.map(shell -> "" + (int)shell.getDistance())
                        .orElse("" + 1));
                NiftyUtil
                .displayText(element, "formation-type-text", mc.map(shell -> shell.getShape()).orElse("Shell"));                
                NiftyUtil.displayText(element, "formation-param-text", mc.map(shell -> "" + (int)shell.getParam())
                        .orElse("" + 1));
            } else fromTop = true;
        }

        super.onFocus(focus);
    }
}
