package com.mentics.qd.jogl;

import java.util.List;

import javax.media.opengl.GL2;

import com.mentics.qd.items.Item;


public interface ItemRenderer<ItemType extends Item> {
    void initialize(GL2 gl);

    void render(GL2 gl, List<ItemType> items);
}
