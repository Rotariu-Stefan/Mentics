package com.mentics.qd.commands.quips;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.datastructures.Response;

public class ChatCommand extends CommandBase {
	private Response resp;
	private boolean done;
	
	public void reset() {
		done = false;
	}
	
	public ChatCommand(Response resp) {
		super(CommandMgrQueue.STORY);
		
		this.resp = resp;
		done = false;
	}
	
	public ChatCommand(String qname, String text, String[] keywords) {
		super(CommandMgrQueue.STORY);
		
		resp = new Response(qname, text, keywords);
	}
	
	@Override
	public void run(AllData allData, CommandMgr cmds, float duration) {
		if(done) return;

		allData.responseQueue.add(resp);
		done = true;
		cmds.remove(this);
	}
}