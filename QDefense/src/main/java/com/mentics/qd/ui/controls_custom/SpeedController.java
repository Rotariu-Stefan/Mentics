package com.mentics.qd.ui.controls_custom;

import java.util.Properties;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.reusable.SliderController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;


public class SpeedController extends UIController_Hud {
    SliderController slider;
    float speedValue;

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        slider = element.findControl("speed-slider", SliderController.class);
        speedValue=11;
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.navRow[2]) decrease();	//left is pressed
        else if (input.getCharacter() == UIMapping.navRow[3]) increase();	//right is pressed

        return super.inputEvent(input);
    }

    public void decrease() {
        if(speedValue!=0) speedValue--;
        slider.setValue(speedValue);
        updateSpeedValue(speedValue);
    }

    public void increase() {
        if(speedValue!=20) speedValue++;
        slider.setValue(speedValue);
        updateSpeedValue(speedValue);
    }

    private void updateSpeedValue(float val) {
        float timeControl;
        if (val <= 10) timeControl = val / 10f;
        else if (val <= 15) timeControl = val / 5f - 1f;
        else timeControl = 1.6f * val - 22f;

        NiftyUtil.displayText(element, "speed-header-value", "" + timeControl);
        QuipNebula.timeControl = timeControl;
    }
}
