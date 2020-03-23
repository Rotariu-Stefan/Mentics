package com.mentics.qd.ui.controls_custom.reusable;

import java.util.Properties;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;


// handles the current energy of items in the UI(in navigation)
public class ProgressbarController implements Controller {
    private Element progressBarElement;

	@Override
	public void bind(Nifty arg0, Screen arg1, Element element, Parameters arg3) {
        progressBarElement = element.findElementById("#progress");
    }

    @Override
    public void init(Parameters arg3) {}

    @Override
    public void onStartScreen() {}

    @Override
    public void onFocus(final boolean getFocus) {}

    @Override
    public boolean inputEvent(final NiftyInputEvent inputEvent) {
        return false;
    }

    // (re)sets the current energy values
    public void setProgress(final float progressValue) {
        float progress = progressValue;
        if (progress < 0.0f) {
            progress = 0.0f;
        } else if (progress > 1.0f) {
            progress = 1.0f;
        }
        final int MIN_WIDTH = 0;
        int pixelWidth = (int)(MIN_WIDTH + (progressBarElement.getParent().getWidth() - MIN_WIDTH) * progress);
        progressBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
        progressBarElement.getParent().layoutElements();
    }
}
