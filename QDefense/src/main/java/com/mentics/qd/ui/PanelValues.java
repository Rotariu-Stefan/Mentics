package com.mentics.qd.ui;

import java.io.Serializable;

//used for persistently storing and retrieving customizable values for a Hud panel
public class PanelValues implements Serializable {
    public String id;
    public int x;
    public int y;
    public int w;
    public int h;

    public PanelValues(String id, int x, int y, int w, int h) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}
