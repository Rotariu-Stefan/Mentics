package com.mentics.qd;


import com.mentics.parallel.CommandMgr;
import com.mentics.qd.items.Item;


/**
 * Handle processing items during command execution.
 */
public interface ItemProcessor {
    void process(AllData allData, CommandMgr cmds, float duration, Item item);
}
