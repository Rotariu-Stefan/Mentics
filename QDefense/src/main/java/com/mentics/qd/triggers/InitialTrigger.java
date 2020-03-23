package com.mentics.qd.triggers;

import java.util.ArrayList;

import com.mentics.qd.commands.Command;
import com.mentics.qd.items.Quip;

public class InitialTrigger extends Trigger {

	public InitialTrigger(Quip owner) {
		oneUse = true;
		commands = new ArrayList<Command>();
		this.owner = owner;
	}
	
	@Override
	public boolean check() {
		return true;
	}

}
