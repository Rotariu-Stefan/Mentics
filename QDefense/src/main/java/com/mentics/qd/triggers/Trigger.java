package com.mentics.qd.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.AttackCommand;
import com.mentics.qd.commands.Command;
import com.mentics.qd.commands.StateChangeCommand;
import com.mentics.qd.commands.quips.ChatCommand;
import com.mentics.qd.items.Quip;

public abstract class Trigger {
	
	protected boolean marked;
	protected boolean oneUse;
	protected Quip owner;
	protected List<Command> commands;
	
	public boolean isMarked() {
		return marked;
	}
	
	public static Trigger createTrigger(Object trInfo, Quip owner) {
		Trigger tr = null;		
		
		if(!trInfo.getClass().getSimpleName().equals("String")) {	//must implement this for initial commands(or other stuff like behaviors)
			boolean line1 = true;
			for(Entry<String, Object> trLine : ((Map<String, Object>)trInfo).entrySet()) {
				if(line1) {
					switch(trLine.getKey()) {
					case "Initial":
						tr = new InitialTrigger(owner);
						break;
					case "Moved":
						tr = new MoveTrigger((List<Object>) trLine.getValue(), owner);
						break;
					case "Response":
						tr = new ResponseTrigger((List<Object>) trLine.getValue(), owner);
						break;
					case "TimeStamp":
						tr = new TimeTrigger((List<Object>) trLine.getValue(), owner);
						break;
					}
					line1 = false;
				}
				else
					tr.setCommand(trLine);
			}
		}
		
		return tr;
	}
	
	private void setCommand(Entry<String, Object> cmdSet) {
		switch(cmdSet.getKey()){
		case "Chat":
			setChatCommand(cmdSet.getValue());
			break;
		case "Attack":
			setAttackCommand(cmdSet.getValue());
			break;
		case "ChangeState":
			setChangeState(cmdSet.getValue());
			break;			
		}
	}
	
	private void setChangeState(Object params) {
		commands.add(new StateChangeCommand(owner, (String) ((List<Object>)params).get(0)));		
	}

	private void setChatCommand(Object params) {
		List<String> keywords = (List<String>) ((List<Object>)params).get(1);
		commands.add(new ChatCommand(owner.name, (String) ((List<Object>)params).get(0), keywords.toArray(new String[0])));
	}
	
	private void setAttackCommand(Object params) {
		String attackTarget = (String) ((List<Object>)params).get(0);
		commands.add(new AttackCommand(owner, (Quip) QuipNebula.allData.getTargetByName(attackTarget)));
	}
	
	public void queueCommands() {
		if(oneUse) marked = true;
		if(owner == null) {
			System.out.println("Triggered: " + this.getClass().getSimpleName() + " for World");
			for(Command cmd : commands) {
				if(cmd.getClass().getSimpleName().equals("ChatCommand"))
					((ChatCommand)cmd).reset();
				QuipNebula.commandMgr.queueCommand(cmd);
			}
		}
		else {
			System.out.println("Triggered: " + this.getClass().getSimpleName() + " for " + owner.name);			
			for(Command cmd : commands) {
				if(cmd.getClass().getSimpleName().equals("ChatCommand"))
						((ChatCommand)cmd).reset();
				owner.cmds.queueCommand(cmd);
			}
		}
		for(Command cmd : commands)
			System.out.println("  --" + cmd.getClass().getSimpleName());
	}
	
	public abstract boolean check();
}
