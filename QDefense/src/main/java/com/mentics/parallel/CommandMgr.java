package com.mentics.parallel;


import com.mentics.qd.AllData;
import com.mentics.qd.commands.Command;


/**
 * Interface for querying and manipulating Commands.
 * <p>
 * This must be thread safe and extremely optimized for pushing new commands to it from multiple threads.
 */
public interface CommandMgr {
    void remove(Command command);

    void queueCommand(Command command);

    void processQueue();

    void run(AllData allData, float duration);
}
