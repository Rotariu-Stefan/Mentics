package com.mentics.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mentics.qd.AllData;
import com.mentics.qd.commands.Command;
import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.commands.quips.groups.GroupCommandBase;


/**
 * Quip implementation of Commands interface.
 * <p>
 * Must be optimized to receive commands (queueCommand call) from multiple threads.
 */
public class CommandMgrQueue implements CommandMgr {
    public static final int NUM_TYPES = 7;
    // TODO: order these by priority
    public static final int STORY = 0;
    public static final int MOVEMENT = 1;
    public static final int PLAYER = 2;
    public static final int SHOOTING = 3;
    public static final int ENERGY_USAGE = 4;
    public static final int GROUPING = 5;
    public static final int WINLOOSE = 6;

    public final TreeSet<Command>[] commands = new TreeSet[NUM_TYPES];

    private ConcurrentLinkedQueue<Command> addQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Command> removeQueue = new ConcurrentLinkedQueue<>();

    private volatile long nextId = 0;

    public GroupCommandBase[] getCommandsForGroup(short[] group) {
        ArrayList<GroupCommandBase> result = new ArrayList<>();
        for (int i = 0; i < NUM_TYPES; i++) {
            for (Command c : commands[i]) {
                if (c instanceof GroupCommandBase) {
                    GroupCommandBase gc = (GroupCommandBase)c;
                    if (Arrays.equals(gc.group, group)) {
                        result.add(gc);
                    }
                }
            }
        }
        return result.toArray(new GroupCommandBase[result.size()]);
    }
    
    public CommandMgrQueue() {
        for (int i = 0; i < NUM_TYPES; i++) {
            commands[i] = new TreeSet<>((x, y) -> (y.compareTo((CommandBase)x)));
        }
    }

    /**
     * Does not immediately remove the command. This is called from multiple threads so must be very fast and thread
     * safe. Commands passed to this method are removed for real on the call to processQueue.
     */
    @Override
    public void remove(Command command) {
        removeQueue.add(command);
    }

    /**
     * Does not immediately add the command. This is called from multiple threads so must be very fast and thread safe.
     * Commands added here are added for real on the call to processQueue.
     */
    @Override
    public void queueCommand(Command command) {
        // TODO: add an assertion to confirm that if this mgr is owned by a quip, then it can only receive quip specific
        // commands and if it's the global cmg mgr, then it cannot receive quip specific commands.
        addQueue.add(command);
    }

    /**
     * Always called only from the main thread so no synchronization needed in here.
     */
    @Override
    public void processQueue() {
        while (!removeQueue.isEmpty()) {
            Command cmd = removeQueue.remove();
            commands[cmd.getType()].remove(cmd);
        }
        while (!addQueue.isEmpty()) {
            Command cmd = addQueue.remove();
            cmd.setId(nextId++); // Put this here so it doesn't need synchronized
            commands[cmd.getType()].add(cmd);
        }
    }

    @Override
    public void run(AllData allData, float duration) {
        for (int type = 0; type < commands.length; type++) {
            commands[type].forEach(cmd -> cmd.run(allData, this, duration));
        }
    }
}
