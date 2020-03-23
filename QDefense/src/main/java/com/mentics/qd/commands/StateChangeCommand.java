package com.mentics.qd.commands;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.QuipNebula;
import com.mentics.qd.items.Quip;

public class StateChangeCommand extends CommandBase{

	private Quip quip;
	private String state;
	
	public StateChangeCommand(Quip quip, String state) {
		super(CommandMgrQueue.STORY);

		this.quip = quip;
		this.state = state;
	}

	@Override
	public void run(AllData allData, CommandMgr cmds, float duration) {
		if(quip == null)	//execute initial commands here?
			QuipNebula.allData.currentState = state;
		else
			quip.currentState = state;
		cmds.remove(this);
	}

}
