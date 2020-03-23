package com.mentics.qd.triggers;

import java.util.ArrayList;
import java.util.List;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.Command;
import com.mentics.qd.items.Quip;

public class TimeTrigger extends Trigger {

	private int timeStamp;
	
	public TimeTrigger(List<Object> params, Quip owner) {
		oneUse = true;
		commands = new ArrayList<Command>();
		this.owner = owner;
		
		timeStamp = (int) params.get(0);
	}
	
	@Override
	public boolean check() {
		if(timeStamp < QuipNebula.time)
			return true;
		return false;
	}

}
