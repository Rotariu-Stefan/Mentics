package com.mentics.qd.items;

import com.mentics.parallel.CommandMgrQueue;


public abstract class GraphItem extends Item {
    private boolean selected;
    private boolean reference;
    private boolean active;

    private transient boolean[] cmdTypeHasRun = new boolean[CommandMgrQueue.NUM_TYPES];


    public GraphItem(long id) {
        super(id);
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean isReference) {
        this.reference = isReference;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public boolean cmdTypeHasRun(int type) {
        return cmdTypeHasRun[type];
    }

    public void cmdHasRunFor(int type) {
        cmdTypeHasRun[type] = true;
    }

    @Override
    public void beginStep() {
        super.beginStep();
        for (int i = 0; i < CommandMgrQueue.NUM_TYPES; i++) {
            cmdTypeHasRun[i] = false;
        }
    }
}
