package com.mentics.qd;


import com.mentics.qd.items.Item;


public interface LinkVisitor {
    void visit(Item from, Item to);
}
