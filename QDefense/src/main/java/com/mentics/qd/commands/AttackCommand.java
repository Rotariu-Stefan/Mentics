package com.mentics.qd.commands;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.quips.CreateNodesCommand;
import com.mentics.qd.commands.quips.groups.GroupSizeCommand;
import com.mentics.qd.commands.quips.groups.ShellCommand;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.RadiusMoveTarget;


public class AttackCommand extends CommandBase {

    private Quip attackingQuip;
    private Quip attackedQuip;
    private boolean done = false;

    public AttackCommand(Quip attackingQuip, Quip attackedQuip) {
        super(CommandMgrQueue.STORY);
        this.attackingQuip = attackingQuip;
        this.attackedQuip = attackedQuip;
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        if (!done) {
            System.out.println("QUIP: " + attackingQuip.id + " is going to attack QUIP: " + attackedQuip.id);
            // Create nodes and send them to target.
            cmds.queueCommand(new CreateNodesCommand(attackingQuip, attackingQuip.getEnergy() / 2, 10000));
            // TODO Needs to fix this group numbering somehow so that each group has unique number.
            attackingQuip.cmds.queueCommand(new GroupSizeCommand(attackingQuip, new short[] { 1 }, 7000));
            // Now I want this group to move towards the player quip.(and attack afterwards)
            RadiusMoveTarget move = new RadiusMoveTarget(attackedQuip, 0f);// Set the target to move to
            attackingQuip.cmds.queueCommand(new ShellCommand(attackingQuip, new short[] { 1 }, move, 1f));
            cmds.remove(this);
            done = true;
        }
    }

    /*if (path.length > 0 && path[0] != ArrayTreeUtil.DIMENSION - 1) {
        try {
            int size = Integer.parseInt(getText(screen, "contains-text"));
            if (size >= 0) {
                GroupSizeCommand containsCommand = new GroupSizeCommand(path, size);

                QCommands commandMgr = (QCommands) uiState.getCommandMgr();
                GroupCommandBase[] groupCommands = commandMgr.getCommandsForGroup(path);
                for (GroupCommandBase command : groupCommands) {
                    if (command instanceof GroupSizeCommand) {
                        commandMgr.remove(command);
                    }
                }

                commandMgr.queueCommand(containsCommand);
            }
        } catch (NumberFormatException e) {
            //Do nothing
        }
    }*/

}
