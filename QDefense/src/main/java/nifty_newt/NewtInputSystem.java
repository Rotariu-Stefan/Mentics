package nifty_newt;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import de.lessvoid.nifty.NiftyInputConsumer;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;


/**
 * Copy of InputSystemAwtImpl
 */
public class NewtInputSystem implements InputSystem, MouseListener, KeyListener {

    private ConcurrentLinkedQueue<MouseEventData> mouseEvents = new ConcurrentLinkedQueue<MouseEventData>();

    private ConcurrentLinkedQueue<KeyboardInputEvent> keyboardEvents = new ConcurrentLinkedQueue<KeyboardInputEvent>();

    @Override
    public void forwardEvents(final NiftyInputConsumer inputEventConsumer) {
        MouseEventData mouseEvent = mouseEvents.poll();
        while (mouseEvent != null) {
            mouseEvent.processMouseEvents(inputEventConsumer);
            mouseEvent = mouseEvents.poll();
        }

        KeyboardInputEvent keyEvent = keyboardEvents.poll();
        while (keyEvent != null) {
            inputEventConsumer.processKeyboardEvent(keyEvent);
            keyEvent = keyboardEvents.poll();
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        // at the moment we only care about the BUTTON1
        if (mouseEvent.getButton() == MouseEvent.BUTTON1 || mouseEvent.getModifiers() == InputEvent.BUTTON1_MASK) {
            mouseEvents.add(new MouseEventData(mouseEvent.getX(), mouseEvent.getY(), true, 0));
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        mouseEvents.add(new MouseEventData(mouseEvent.getX(), mouseEvent.getY(), false, 0));
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // at the moment we only care about the BUTTON1
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            mouseEvents.add(new MouseEventData(mouseEvent.getX(), mouseEvent.getY(), true, 0));
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        // at the moment we only care about the BUTTON1
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            mouseEvents.add(new MouseEventData(mouseEvent.getX(), mouseEvent.getY(), false, 0));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKeyEvent(e, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handleKeyEvent(e, false);
    }

    private void handleKeyEvent(KeyEvent e, boolean isKeyDown) {
        int newKeyCode = keyCodeConverter.convertToNiftyKeyCode(e.getKeyCode());
        keyboardEvents.add(new KeyboardInputEvent(newKeyCode, e.getKeyChar(), isKeyDown, e.isShiftDown(), e
                .isControlDown()));
    }

    NewtToNiftyKeyCodeConverter keyCodeConverter = new NewtToNiftyKeyCodeConverter();

    /*@Override
    public void keyTyped(KeyEvent e) {

    }*/

    @Override
    public void setMousePosition(int x, int y) {
        // TODO implement this method later
    }


    private class MouseEventData {
        private int mouseX;
        private int mouseY;
        private int mouseWheel;
        private int button;
        private boolean buttonDown;

        public MouseEventData(final int mouseX, final int mouseY, final boolean mouseDown, final int mouseButton) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.buttonDown = mouseDown;
            this.button = mouseButton;
            this.mouseWheel = 0;
        }

        public void processMouseEvents(final NiftyInputConsumer inputEventConsumer) {
            inputEventConsumer.processMouseEvent(mouseX, mouseY, mouseWheel, button, buttonDown);
        }
    }

    @Override
    public void setResourceLoader(final NiftyResourceLoader niftyResourceLoader) {}

    @Override
    public void mouseWheelMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }
}
