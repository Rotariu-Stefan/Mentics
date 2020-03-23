package com.mentics.qd.ui.controls_custom.reusable;

import java.util.Properties;

import com.mentics.qd.ui.UIMapping;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.AbstractController;
import de.lessvoid.nifty.controls.NextPrevHelper;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyMouseInputEvent;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;


public class SliderController extends AbstractController implements Slider {
    private Nifty nifty;
    private Element element;
    private Element elementPosition;
    private Element elementBackground;
    private NextPrevHelper nextPrevHelper;
    private float min;
    private float max;
    private float initial;
    private float stepSize;
    private float buttonStepSize;
    private float value;
    private float oldValue;
    private boolean vertical;
    private Element mainControl;

    // GETTERS/SETTERS
    @Override
    public void setValue(final float value) {
        changeValue(value);
        viewUpdate();
    }

    @Override
    public float getValue() {
        return value;
    }

    @Override
    public void setMin(final float min) {
        this.min = min;
        changeValue(value);
        viewUpdate();
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public void setMax(final float max) {
        this.max = max;
        changeValue(value);
        viewUpdate();
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public void setStepSize(final float stepSize) {
        this.stepSize = stepSize;
        changeValue(value);
        viewUpdate();
    }

    @Override
    public float getStepSize() {
        return stepSize;
    }

    @Override
    public void setButtonStepSize(final float buttonStepSize) {
        this.buttonStepSize = buttonStepSize;
        changeValue(value);
        viewUpdate();
    }

    @Override
    public float getButtonStepSize() {
        return buttonStepSize;
    }

    // CONSTRUCTORS
	@Override
	public void bind(Nifty nifty, Screen screen, Element element, Parameters parameter) {
        super.bind(element);

        this.nifty = nifty;
        this.element = element;
        elementBackground = element.findElementById("#background");
        elementPosition = element.findElementById("#position");
        nextPrevHelper = new NextPrevHelper(element, screen.getFocusHandler());

        if ("QNverticalSlider".equals(parameter.get("name"))) vertical = true;
        else if ("QNhorizontalSlider".equals(parameter.get("name"))) vertical = false;

        min = Float.valueOf(parameter.getWithDefault("min", "0.0"));
        max = Float.valueOf(parameter.getWithDefault("max", "100.0"));
        initial = Float.valueOf(parameter.getWithDefault("initial", "0.0"));
        stepSize = Float.valueOf(parameter.getWithDefault("stepSize", "1.0"));
        buttonStepSize = Float.valueOf(parameter.getWithDefault("buttonStepSize", "25.0"));
        
        String maincstr=parameter.getWithDefault("mainControl", "");
        if(!maincstr.isEmpty())
        	mainControl=screen.findElementById(maincstr);
        
        viewUpdate();
        changeValue(0.f);
        setValue(initial);
    }

    @Override
    public void onStartScreen() {}

    @Override
    public boolean inputEvent(final NiftyInputEvent inputEvent) {
        if (nextPrevHelper.handleNextPrev(inputEvent)) {
            return true;
        }
        if (inputEvent == NiftyStandardInputEvent.MoveCursorUp || inputEvent == NiftyStandardInputEvent.MoveCursorLeft) {
            changeValue(value - buttonStepSize);
            viewUpdate();
            return true;
        } else if (inputEvent == NiftyStandardInputEvent.MoveCursorDown || inputEvent == NiftyStandardInputEvent.MoveCursorRight) {
            changeValue(value + buttonStepSize);
            viewUpdate();
            return true;
        }
        return false;
    }

    @Override
    public void layoutCallback() {
        viewUpdate();
    }

    public void upClick() {
    	if(mainControl != null) {
    		mainControl.setFocus();
    		if(vertical)
    			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[0], (char) 0, true, false, false));
    		else
    			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[2], (char) 0, true, false, false));
    	}
    	else {
	        changeValue(value - buttonStepSize);
	        viewUpdate();
    	}
    }

    public void downClick() {
    	if(mainControl != null) {
    		mainControl.setFocus();
    		if(vertical)
    			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[1], (char) 0, true, false, false));
    		else
    			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[3], (char) 0, true, false, false));    		
    	}
    	else {
	        changeValue(value + buttonStepSize);
	        viewUpdate();
    	}
    }

    public void mouseClick(final int mouseX, final int mouseY) {
    	if(mainControl != null) {
    		mainControl.setFocus();
    		float newPos = ensureStepSize(viewToWorld(viewFilter(mouseX - elementBackground.getX() - elementPosition.getWidth() / 2,
    				mouseY - elementBackground.getY() - elementPosition.getHeight() / 2)));
    		if(newPos < value) {
        		if(vertical)
        			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[0], (char) 0, true, false, false));
        		else
        			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[2], (char) 0, true, false, false));
    		}
    		else if(newPos > value) {
        		if(vertical)
        			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[1], (char) 0, true, false, false));
        		else
        			mainControl.keyEvent(new KeyboardInputEvent(UIMapping.navRow[3], (char) 0, true, false, false));
    		}
    	}
    	else {
    	//size difference between the starting (left/top) position and current mouse position(accounts for position size)
        setValueFromPosition(mouseX - elementBackground.getX() - elementPosition.getWidth() / 2,
                mouseY - elementBackground.getY() - elementPosition.getHeight() / 2);
    	}
    }

    public void mouseWheel(final Element element, final NiftyMouseInputEvent inputEvent) {
        int mouseWheel = inputEvent.getMouseWheel();
        float currentValue = value;
        if (mouseWheel < 0) {
            setValue(currentValue - buttonStepSize * mouseWheel);
        } else if (mouseWheel > 0) {
            setValue(currentValue - buttonStepSize * mouseWheel);
        }
    }

    @Override
    public void setup(final float min, final float max, final float current, final float stepSize,
            final float buttonStepSize) {
        this.min = min;
        this.max = max;
        this.value = current;
        this.stepSize = stepSize;
        this.buttonStepSize = buttonStepSize;
        changeValue(value);
        viewUpdate();
    }

    //rounds up a new position value to an exact value to be set
    private float ensureStepSize(final float value) {
        return Math.round(value / stepSize) * stepSize;
    }

    //returns a new position value based on the mouse offset/background size and (number of) possible values
    private float viewToWorld(final float viewValue) {
        return (viewValue / viewGetSize() * (max - min)) + min;
    }

    private float worldToView(final float worldValue) {
        return (worldValue - min) / (max - min) * viewGetSize();
    }

    public void changeValue(final float newValue) {
        value = newValue;
        if (value > max) {
            value = max;
        } else if (newValue < min) {
            value = min;
        }
        value = ensureStepSize(value);
        if (value != oldValue) {
            oldValue = value;
            if (element.getId() != null) nifty.publishEvent(element.getId(), new SliderChangedEvent(this, value));
        }
    }

    public void setValueFromPosition(final int pixelX, final int pixelY) {
        setValue(ensureStepSize(viewToWorld(viewFilter(pixelX, pixelY))));
    }

    public void viewUpdate() {
        if (vertical) elementPosition.setConstraintY(new SizeValue((int)worldToView(value) + "px"));
        else elementPosition.setConstraintX(new SizeValue((int)worldToView(value) + "px"));
        elementBackground.layoutElements();
    }

    //difference between background size and position size(empty background size)
    private int viewGetSize() {
        if (vertical) return elementBackground.getHeight() - elementPosition.getHeight();
        else return elementBackground.getWidth() - elementPosition.getWidth();
    }

    //returns which coord type is used(vertical or horizontal) depending on the slider type
    private int viewFilter(final int pixelX, final int pixelY) {
        if (vertical) return pixelY;
        else return pixelX;
    }
}
