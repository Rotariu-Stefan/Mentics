package com.mentics.qd.ui.controls_custom.reusable;

import java.util.Properties;

import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;


public class TextFieldController implements Controller {
    private Element element;
    public static final int TF_MAX = 999999;
    private boolean isHighlight;

    // types a digit in the textfield
    public void typeDigit(NiftyStandardInputEvent input) {
        tryReset();
        float textnr = Float.parseFloat(getText() + getDigit(input));
        if (textnr > TF_MAX) setText("" + TF_MAX);
        else setText("" + (int)textnr);
        setCursorPos();
    }

    // deletes the last character in the textfield
    public void deleteChar() {
        tryReset();
        if (getText().length() <= 1) setText("0");
        else setText(getText().substring(0, getText().length() - 1));
        setCursorPos();
    }

    // adjust cursor position after typed text in the textfield
    public void setCursorPos() {
        Element cp = element.findElementById("#cursor-panel");
        cp.setPaddingLeft(new SizeValue((getText().length() * 10) + "px"));
        cp.layoutElements();
    }

    // makes the textfield text selected(next key will delete the value)
    public void tryReset() {
        if (isHighlight) {
            element.findElementById("#text").setStyle("subcommands-text");
            setText("");
            isHighlight = false;
        }
    }

    // translated key presses to digits(or -1 if not)
    public int getDigit(NiftyStandardInputEvent input) {
        for (int i = 0; i <= 9; i++)
            if (input.getCharacter() == UIMapping.numbers[i]) return i;
        return -1;
    }

    // sets the text
    public void setText(String text) {
        TextRenderer textRenderer = element.findElementById("#text").getRenderer(TextRenderer.class);
        textRenderer.setText(text);
    }

    // gets the text
    public String getText() {
        TextRenderer textRenderer = element.findElementById("#text").getRenderer(TextRenderer.class);
        return textRenderer.getOriginalText();
    }

	@Override
	public void bind(Nifty arg0, Screen arg1, Element arg2, Parameters arg3) {
        element = arg2;
        isHighlight = true;
    }

    @Override
    public void onFocus(boolean focus) {
        if (focus) {
            element.findElementById("#text").setStyle("highlight-text");
            setCursorPos();
            isHighlight = true;
        }
    }

    @Override
    public void init(Parameters arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean inputEvent(NiftyInputEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onStartScreen() {
        // TODO Auto-generated method stub

    }
}
