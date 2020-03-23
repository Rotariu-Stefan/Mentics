package com.mentics.qd.commands.global;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.items.Quip;


public class SkirmishWincheckCommand extends CommandBase {

    private Quip pQuip;
    private Quip eQuip1;
    private Quip eQuip2;

    public SkirmishWincheckCommand(Quip pq, Quip eq1, Quip eq2) {
        super(CommandMgrQueue.STORY);

        pQuip = pq;
        eQuip1 = eq1;
        eQuip2 = eq2;
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        int detectWinLoose = detectWinLoose();
        if (1 == detectWinLoose) {// Player has won
            cmds.remove(this);
        } else if (0 == detectWinLoose) {// Player has lost
            cmds.remove(this);
        }
    }

    /**
     * @return 1- player wins 0- player looses -1- still competing
     */
    public int detectWinLoose() {
        // System.out.println("Trying to detect WIN and LOOSE.");

        if (isQuipCaptured(eQuip1) && isQuipCaptured(eQuip2)) return 1;// WIN
        else if (isQuipCaptured(pQuip)) return 0;// LOSS
        return -1;// Still competing
    }

    public boolean isQuipCaptured(Quip quip) {
        if ((quip.maxEnergy * 2) <= quip.getEnergy()) return true;
        return false;
    }
}
