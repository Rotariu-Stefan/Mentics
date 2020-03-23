package com.mentics.qd.ui.controls_menus;

import java.util.Properties;

import testing.WorldTest;

import com.mentics.qd.CMD;
import com.mentics.qd.commands.quips.CreateNodesCommand;
import com.mentics.qd.items.Node;
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


// handles the addnodes submenu specific inputs
public class AddnodeSMenuController extends SubMenuController {

    private TextFieldController tfAdd;	// addnodes input textfield

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        tfAdd = element.findControl("addnodes-input", TextFieldController.class);
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (NiftyUtil.getDigit(input) >= 0) {						//a digit is pressed
            tfAdd.typeDigit(input);
        } else if (input.getCharacter() == UIMapping.backspace) {	//delete is pressed
            tfAdd.deleteChar();
        } else if (input.getCharacter() == UIMapping.activate) {	//space is pressed
            int size = Integer.parseInt(tfAdd.getText());

            CMD.addNodes(uiMgr.activeQuip, size);

            element.hide();
            screen.findElementById("quip").setFocus();
            uiMgr.addNotification("Starting to create nodes. " + size + " nodes to create.",
                    NotifyController.MSG_ADDNODES);
        }

        return super.inputEvent(input);
    }
}
