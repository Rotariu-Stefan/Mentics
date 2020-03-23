package com.mentics.qd.ui.controls_menus;

import java.util.Properties;

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


// handles the gformation dialogmenu specific inputs
public class FormationDMenuController extends DialogMenuController {
	
    private boolean init;		// check if the panel was initiated(for StartScreen to be fine on 1st load)
    private boolean fromMotion;	// checks if the panel was accessed(newly focused) from gmotion or changed(newly focused)
                               	// from inside by selecting another formation type	
    private String type;		// current formation type
    
	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
        
        fromMotion = true;
        init = false;
    }
    
    //updates All the values related to formation
    private void update(String type) {
    	screen.findElementById("notify").setFocus();
    	
    	if(fromMotion) {
	    	for(Element e : element.findElementById("gformation-params-select").getElements())
	    		if(!e.getId().equals("gformation-params-" + type))
	    			e.hide();
	    	
	    	element.setFocus();
	    	
	    	if(type.equals("Shell")) {
	    		element.findControl("params-Shell-1", TextFieldController.class)
	    			.setText(NiftyUtil.getText(screen, "formation-param-text"));
	    	}
	    	else if(type.equals("Square")) {
	    		element.findControl("params-Square-1", TextFieldController.class)
	    			.setText(NiftyUtil.getText(screen, "formation-param-text"));
	    	}

	    	fromMotion = false;
    	}
    	
    	else {
    		element.findElementById("gformation-params-" + this.type).hide();
    		element.findElementById("gformation-params-" + type).show();
    		
    		element.setFocus();
    	}    	
    	
    	this.type = type;
    	updateTitles();
    }
    
    //updates the titles that show current values
    private void updateTitles() {
    	NiftyUtil.displayText(element, "gformation-type-hvalue", type);
    	
    	if(type.equals("Shell")) {
    		NiftyUtil.displayText(element, "gformation-params-hvalue", 
    				"Radius:" + element.findControl("params-Shell-1", TextFieldController.class).getText());    		
    	}
    	
    	else if(type.equals("Square")) {
    		NiftyUtil.displayText(element, "gformation-params-hvalue", 
    				"Square param:" + element.findControl("params-Square-1", TextFieldController.class).getText());    		
    	}
    	
    	else if(type.equals("Somethin")) {
    		NiftyUtil.displayText(element, "gformation-params-hvalue", "Somethin:?");
    	}
    }
    
    @Override
    public void onStartScreen() {
    	if(init) update(NiftyUtil.getText(screen, "formation-type-text"));
    	else init = true;
    }

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (NiftyUtil.getDigit(input) >= 0) {						//a digit is pressed
        	if(type == "Shell") {
        		element.findControl("params-Shell-1", TextFieldController.class).typeDigit(input);
        		updateTitles();
        	}
        	else if(type == "Square") {
        		element.findControl("params-Square-1", TextFieldController.class).typeDigit(input);
        		updateTitles();
        	}
        }

        else if (input.getCharacter() == UIMapping.backspace) {		//delete is pressed
        	if(type == "Shell") {
        		element.findControl("params-Shell-1", TextFieldController.class).deleteChar();
        		updateTitles();
        	}
        	else if(type == "Square") {
        		element.findControl("params-Square-1", TextFieldController.class).deleteChar();
        		updateTitles();
        	}
        }

        else if (input.getCharacter() == UIMapping.cRow1[0]) {		//q is pressed
        	if(!type.equals("Shell")) update("Shell");
        }
        
        else if (input.getCharacter() == UIMapping.cRow1[1]) {		//w is pressed
        	if(!type.equals("Square")) update("Square");
        }
        
        else if (input.getCharacter() == UIMapping.cRow1[2]) {		//e is pressed
        	if(!type.equals("Somethin")) update("Somethin");
        }

        else if (input.getCharacter() == UIMapping.activate) {		//space is pressed
            NiftyUtil.displayText(screen, "formation-type-text", NiftyUtil.getText(element, "gformation-type-hvalue"));
            NiftyUtil.displayText(screen, "formation-param-text", NiftyUtil
                    .getText(element, "gformation-params-hvalue").split(":")[1]);
            fromMotion = true;
        } 
        
        else if (input.getCharacter() == UIMapping.selback) {		//selback is pressed
        	fromMotion = true;
        }

        return super.inputEvent(input);
    }
}
