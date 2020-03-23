package com.mentics.qd.commands;

import com.mentics.parallel.CommandMgr;
import com.mentics.qd.AllData;


/**
 * Make sure that you call cmds.remove(this) when your command is finished so it will be removed.
 */
public interface Command extends Comparable<CommandBase> {
    void run(AllData allData, CommandMgr cmds, float duration);

    int getType();

    void setId(long id);
}
