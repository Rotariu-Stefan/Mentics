package com.mentics.qd;


import com.mentics.qd.items.Item;


public interface ItemVisitor {
    void visit(Item item);
}
