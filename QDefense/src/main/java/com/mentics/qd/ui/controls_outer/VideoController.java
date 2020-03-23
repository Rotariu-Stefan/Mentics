package com.mentics.qd.ui.controls_outer;

import java.util.Properties;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.jogl.VideoValues;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIController;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.reusable.SliderController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;

public class VideoController extends UIController {
	public int[] wList;
	public int[] hList;
	private SliderController slider;
	private int respos;
	private CheckBox fullscreen;
	
	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        getCurrentValues();
        slider = element.findControl("resolution-slider", SliderController.class);
        slider.setMax(wList.length - 1);
        fullscreen = element.findNiftyControl("fullscreen-checkbox", CheckBox.class);
        reset();
    }
    
    public void getCurrentValues(){
    	int len = 0;
    	for(int i = 0; i < QuipNebula.oglCanvas.wListAll.length; i++)
    		if(QuipNebula.oglCanvas.deskResW >= QuipNebula.oglCanvas.wListAll[i]){
    			if(QuipNebula.oglCanvas.deskResH >= QuipNebula.oglCanvas.hListAll[i])
    				len++;
    		}
    		else break;
    	wList = new int[len];
    	hList = new int[len];
    	for(int i = 0; i < len; i++){
    		wList[i] = QuipNebula.oglCanvas.wListAll[i];
    		hList[i] = QuipNebula.oglCanvas.hListAll[i];
    	}
    }
    
	public void reset() {
		for(int i=0; i < wList.length; i++)
    		if(QuipNebula.window.getWidth() == wList[i] && QuipNebula.window.getHeight() == hList[i]) {
    			respos=i;
    			updateRes();
    		}
		fullscreen.setChecked(QuipNebula.window.isFullscreen());
	}
	
    public void back(){
        System.out.println("Video - BACK");

    	reset();
        element.hide();
        screen.findElementById("options_menu").show();
        screen.findElementById("options_menu").setFocus();
    }
    
    public void apply(){
    	System.out.println("Video - APPLY");
    	
    	QuipNebula.oglCanvas.saveNewVideoValues(new VideoValues(wList[respos], hList[respos], fullscreen.isChecked()));
    	QuipNebula.oglCanvas.updateVideoSettings();
    }
    
	private void decrease(){
		if(respos > 0) respos--;
		updateRes();
	}
	
	private void increase(){
		if(respos < wList.length - 1) respos++;
		updateRes();
	}
	
	private void updateRes(){
		NiftyUtil.displayText(element, "Resolution", "Resolution:   " + wList[respos] + "x" + hList[respos]);
		slider.setValue(respos);
	}
    
    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.navRow[2]) decrease();
        else if (input.getCharacter() == UIMapping.navRow[3]) increase();

        return super.inputEvent(input);
    }
}
