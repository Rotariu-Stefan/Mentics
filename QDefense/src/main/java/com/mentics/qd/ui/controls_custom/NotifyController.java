package com.mentics.qd.ui.controls_custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.UIMapping;
import com.mentics.qd.ui.controls_custom.reusable.SliderController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.controls.dynamic.TextCreator;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;


// handles the notification box in the bottom left of the screen
public class NotifyController extends UIController_Hud {
    public static final int MSG_SYSTEM = 0, MSG_ADDNODES = 1, MSG_MADENODE = 2, MSG_MOTION = 3, MSG_GROUPNODES = 4,
            MSG_DIALOG = 5;		// message types
    private static int linelenMax;			// max number of chars per line
    private static int lineNrMax = 10;		// max number of lines of text
    private static int msgMax = -1;			// max number of messages memorized in the list(NOT notifications)

    private SliderController slider;
    private List<NoteLine> noteLines;		// list of memorized messages
    private int startPos;					// start position from the list to shown in the UI

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        super.bind(arg0, arg1, arg2, arg3);

        linelenMax = (int)((float)element.getWidth() * 0.96 / 10);
        slider = element.findControl("notify-slider", SliderController.class);
        noteLines = new ArrayList<NoteLine>();
        startPos = 0;
        
        TextCreator line;
        for(int i = 0; i < 10; i++) {
            line = new TextCreator("notify-" + (i + 1), "-");
            line.setStyle("commands-text");
            line.setController("com.mentics.qd.ui.controls_custom.LineController");
            line.setInteractOnClick("mouseClick()");
            line.create(arg0, screen, element.findElementById("textarea"));
        }
    };

    @Override
    public boolean inputEvent(NiftyInputEvent input1) {
        if (input1 == null) return false;
        NiftyStandardInputEvent input = (NiftyStandardInputEvent) input1;

        if (input.getCharacter() == UIMapping.navRow[0]) goUp();			//up is pressed
        else if (input.getCharacter() == UIMapping.navRow[1]) goDown();		//down is pressed

        return super.inputEvent(input);
    }

    // adds note lines to list and adjust value/UI if necessary
    public void addNotification(String msg, int type) {
        int newcutoff = 0, oldcutoff = 0;

        while (newcutoff + linelenMax < msg.length()) {        	
            if (msgMax != -1 && msgMax < noteLines.size()) {
                noteLines.remove(0);
            }

            oldcutoff = newcutoff;
            newcutoff = oldcutoff + msg.substring(oldcutoff, oldcutoff + linelenMax).lastIndexOf(' ');
            if (newcutoff == -1) newcutoff += linelenMax;
            noteLines.add(new NoteLine(msg.substring(oldcutoff, newcutoff), type));
            if (noteLines.size() > lineNrMax && startPos + 1 == noteLines.size() - lineNrMax) startPos++;
        }
        
        noteLines.add(new NoteLine(msg.substring(newcutoff, msg.length()), type));
        if (noteLines.size() > lineNrMax && startPos + 1 == noteLines.size() - lineNrMax) startPos++;
        updateLines();
    }

    // scroll text up
    public void goUp() {
        if (startPos != 0) {
            startPos--;
            updateLines();
        }
    }

    // scroll text down
    public void goDown() {
        if (noteLines.size() > lineNrMax && startPos + 1 <= noteLines.size() - lineNrMax) {
            startPos++;
            updateLines();
        }
    }

    // populates texts with note lines
    private void updateLines() {
        for (int i = 0; i < (noteLines.size() < lineNrMax ? noteLines.size() : lineNrMax); i++)
            noteLines.get(startPos + i).showMessage(element.findElementById("notify-" + (i + 1)));
        slider.setValue(noteLines.size() <= lineNrMax ? 0 : startPos * 100 / (noteLines.size() - lineNrMax));
    }

    private NoteLine getNoteByLine(int lineNr) {
        if (lineNr > noteLines.size()) return null;
        if (1 <= lineNr && lineNr <= 7) return noteLines.get(startPos + lineNr - 1);
        else return null;
    }

    // single line message to be shown in the textpanel
    // multiple messages can have similar fields and be part of the same notification
    // (and so have the same results if inputs are performed on them)
    private class NoteLine {
        private String line;	// message text(1 single line)
        private int noteype;	// message type(what kind of notification it is)

        public NoteLine(String line, int type) {
            this.noteype = type;
            this.line = line;
        }

        // displays the message in the text element(elem)
        public void showMessage(Element elem) {
            switch (noteype) {
            case MSG_SYSTEM:
                elem.setStyle("notify-text-system");
                break;
            case MSG_ADDNODES:
                elem.setStyle("notify-text-addnodes");
                break;
            case MSG_MADENODE:
                elem.setStyle("notify-text-madenode");
                break;
            case MSG_MOTION:
                elem.setStyle("notify-text-motion");
                break;
            case MSG_GROUPNODES:
                elem.setStyle("notify-text-groupnodes");
                break;
            case MSG_DIALOG:
                elem.setStyle("notify-text-dialog");
                break;
            default:
                elem.setStyle("commands-text");
            }
            NiftyUtil.displayText(elem, line);
        }

    }
}
