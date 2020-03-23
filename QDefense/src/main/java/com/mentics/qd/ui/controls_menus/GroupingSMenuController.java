package com.mentics.qd.ui.controls_menus;

import java.util.Optional;
import java.util.Properties;

import testing.WorldTest;

import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.CMD;
import com.mentics.qd.commands.quips.groups.GroupCommandBase;
import com.mentics.qd.commands.quips.groups.GroupSizeCommand;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.NotifyController;
import com.mentics.qd.ui.controls_custom.reusable.TextFieldController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;


// handles the grouping submenu specific commands
public class GroupingSMenuController extends SubMenuController {

    private TextFieldController tfContains;		// grouping contains input textfield

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        tfContains = element.findControl("grouping-input", TextFieldController.class);
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (NiftyUtil.getDigit(input) >= 0) tfContains.typeDigit(input);	//a digit is pressed

        else if (input.getCharacter() == UIMapping.backspace) tfContains.deleteChar();	//delete is pressed

        else if (input.getCharacter() == UIMapping.activate) {				//space is pressed
            int size = Integer.parseInt(tfContains.getText());

            CMD.setGroupSize(uiMgr.activeQuip, uiMgr.gPath, size);

            element.hide();
            screen.findElementById("groups").setFocus();
            uiMgr.addNotification("Group " + ArrayTreeUtil.pathToString(uiMgr.gPath) + " max size set to " + size +
                                    " nodes", NotifyController.MSG_GROUPNODES);
        }

        return super.inputEvent(input);
    }

    @Override
    public void onFocus(boolean focus) {
        if (focus) {
            Optional<Integer> cSize = uiMgr.activeQuip.sizeConstraint(uiMgr.gPath);
            tfContains.setText(cSize.map(size -> size.toString()).orElse("1"));
        }
        
        super.onFocus(focus);
    }
}
