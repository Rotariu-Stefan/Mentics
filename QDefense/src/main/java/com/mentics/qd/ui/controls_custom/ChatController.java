package com.mentics.qd.ui.controls_custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.datastructures.Response;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;

//controller that handles events and changes to the chat panel
//also handles operations related to NPC chat
public class ChatController extends UIController_Hud {
    private int keywordNrMax = 4;		//number of keywords on the control for the current response
    private int gkeywordNrMax = 4;		//max number of global keywords visible on the control
    private int page = 0;				//current visible global list page number 
    private Response resp;				//response on screen show
    private boolean showkeywords;			//whether to show the keywords on UI(or continue)
    private List<String> gkeywords;		//list of available keywords that can be selected/sent

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);
    	
    	gkeywords = new ArrayList<>();
    	showkeywords = true;
    }
    
    @Override
    public void onStartScreen() {
        super.onStartScreen();

        displayResponse();
    }

    //displays the stored sent response(resp) on the panel
    //if [showkeywords] shows keywords for the response else a "more" type message
    public void displayResponse() {
        if (resp == null) {
            NiftyUtil.displayText(element, "message-text", "--");
            for (int i = 0; i < keywordNrMax; i++)
                element.findElementById("keyword-" + (i + 1)).hide();
            for (int i = 0; i < gkeywordNrMax; i++)
                element.findElementById("gkeyword-" + (i + 1)).hide();
            return;
        }

        NiftyUtil.displayText(element, "message-text", resp.qname + ": " + resp.text);
        if (showkeywords) {
            for (int i = 0; i < resp.keywords.length; i++) {
                NiftyUtil.displayText(element, "keyword-text-" + (i + 1), resp.keywords[i]);
                element.findElementById("keyword-" + (i + 1)).show();
            }
            for (int i = resp.keywords.length; i < keywordNrMax; i++)
                element.findElementById("keyword-" + (i + 1)).hide();

            gkeywords = QuipNebula.allData.getKnownGKeywords(resp.qname);
            for (int i = page * gkeywordNrMax; i < (page + 1) * gkeywordNrMax; i++) {
                if (i >= gkeywords.size()) element.findElementById("gkeyword-" + (i - page * gkeywordNrMax + 1))
                        .hide();
                else {
                    NiftyUtil.displayText(element, "gkeyword-text-" + (i - page * gkeywordNrMax + 1), gkeywords.get(i));
                    element.findElementById("gkeyword-" + (i - page * gkeywordNrMax + 1)).show();
                }
            }
        }

        else {
            NiftyUtil.displayText(element, "keyword-text-1", "...");
            element.findElementById("keyword-1").show();
            for (int i = 1; i < keywordNrMax; i++)
                element.findElementById("keyword-" + (i + 1)).hide();
            for (int i = 0; i < keywordNrMax; i++)
                element.findElementById("gkeyword-" + (i + 1)).hide();            
        }
    }

    @Override
    //TODO: decribe this thing
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.navRow[0]) {			//down is pressed
            if (page > 0) {
                page--;
                showkeywords = true;
                displayResponse();
            }
        }

        else if (input.getCharacter() == UIMapping.navRow[1]) {		//up is pressed
            if ((page + 1) * gkeywordNrMax < gkeywords.size()) {
                page++;
                showkeywords = true;
                displayResponse();
            }
        }

        else for (int i = 0; i < keywordNrMax; i++) {
        	//a number is pressed that has a keyword option
            if (input.getCharacter() == UIMapping.numbers[i + 1] &&
                element.findElementById("keyword-text-" + (i + 1)).isVisible() && resp != null) {

            	if(showkeywords)
            		QuipNebula.allData.sendChatReply(resp.keywords[i], resp.qname);
                resp = null;
                break;
            }

            //a number is pressed that has a global keyword option
            if (input.getCharacter() == UIMapping.cRow3[i] &&
                element.findElementById("gkeyword-text-" + (i + 1)).isVisible() && resp != null) {

                if(showkeywords)
                	QuipNebula.allData.sendChatReply(gkeywords.get(i + page * gkeywordNrMax), resp.qname);
                resp = null;
                break;
            }
        }

        return super.inputEvent(input);
    }

    //gets the next response in the Alldata queue
    public void getNextResponse() {
    	if(resp == null) {
	        if (!QuipNebula.allData.responseQueue.isEmpty()) {
	            resp = QuipNebula.allData.responseQueue.get(0);
	            QuipNebula.allData.responseQueue.remove(0);
	            if (QuipNebula.allData.responseQueue.isEmpty()) {
	            	showkeywords = true;
	            	displayResponse();
	            }
	            else {
	            	showkeywords = false;
	            	displayResponse();
	            }
	        }
    	}
    }
}