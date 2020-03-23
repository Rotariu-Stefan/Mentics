package com.mentics.qd.ui.controls_menus;

import java.util.List;
import java.util.Properties;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.Quip;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.reusable.TextFieldController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;


// handles the move dialogmenu specific inputs(target/distance)
public class MoveDMenuController extends DialogMenuController {

    private TextFieldController tfDistance;	// distance to maintain from target textfield

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        tfDistance = element.findControl("move-distance-input", TextFieldController.class);
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.backspace) tfDistance.deleteChar();	//delete is pressed

        else if (NiftyUtil.getDigit(input) >= 0) tfDistance.typeDigit(input);		//a digit is pressed

        else if (getRecent(input) >= 0) {			//a "recent" landmark key is pressed
            String landmark = NiftyUtil.getText(element, "move-target-recentLandmarks-" + (getRecent(input) + 1));
            NiftyUtil.displayText(element, "move-target-titletext", landmark);
        }

        else if (getNearby(input) >= 0) {			//a "nearby" landmark key is pressed
            String landmark = NiftyUtil.getText(element, "move-target-nearbyLandmarks-" + (getNearby(input) + 1));
            NiftyUtil.displayText(element, "move-target-titletext", landmark);
        }

        else if (input.getCharacter() == UIMapping.activate) {		//space is pressed
            if (onQuip) {
                // TODO: quip movement
            } else {
                NiftyUtil.displayText(screen, "move-target-text", NiftyUtil.getText(element, "move-target-titletext"));
                NiftyUtil.displayText(screen, "move-distance-text", tfDistance.getText());
            }
        }

        return super.inputEvent(input);
    }

    @Override
    public void onFocus(boolean focus) {
        if (focus) {
            if (onQuip) {
                // TODO:get quip movement current values
            } else {
                NiftyUtil.displayText(element, "move-target-titletext", NiftyUtil.getText(screen, "move-target-text"));
                tfDistance.setText(NiftyUtil.getText(screen, "move-distance-text"));
            }

            updateLandmarks();
        }

        super.onFocus(focus);
    }

    // populates the target landmarks UI list from the game data
    private void updateLandmarks() {
        List<Quip> landmarks = QuipNebula.allData.allQuips;
        int lmlen = landmarks.size() < 10 ? landmarks.size() : 10;

        for (int i = 0; i < lmlen; i++)
            NiftyUtil
                    .displayText(element, "move-target-nearbyLandmarks-" + (i + 1), ItemUtil.getName(landmarks.get(i)));
    }

    // translates key pressed into recent landmarks indexes
    private int getRecent(NiftyStandardInputEvent input) {
        for (int i = 0; i < 5; i++)
            if (input.getCharacter() == UIMapping.cRow1[i]) return i;
        return -1;
    }

    // translates key pressed into nearby landmarks indexes
    private int getNearby(NiftyStandardInputEvent input) {
        for (int i = 0; i < 5; i++)
            if (input.getCharacter() == UIMapping.cRow2[i]) return i;
        for (int i = 0; i < 5; i++)
            if (input.getCharacter() == UIMapping.cRow3[i]) return i + 5;
        return -1;
    }
}
