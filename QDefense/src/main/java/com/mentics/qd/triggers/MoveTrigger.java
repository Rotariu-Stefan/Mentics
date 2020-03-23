package com.mentics.qd.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.Command;
import com.mentics.qd.items.Quip;

public class MoveTrigger extends Trigger {
	
	private String target;
	private int distance;

	public MoveTrigger(List<Object> params, Quip owner) {
		oneUse = true;
		commands = new ArrayList<Command>();
		this.owner = owner;
		
		target = (String) params.get(0);
		distance = (int) params.get(1);
	}
	
	@Override
	public boolean check() {
		float[] tpos = QuipNebula.allData.getTargetByName(target).position;
		if(owner.position[0] - distance < tpos[0] && tpos[0] < owner.position[0] + distance
				&& owner.position[1] - distance < tpos[1] && tpos[1] < owner.position[1] + distance
				&& owner.position[2] - distance < tpos[2] && tpos[2] < owner.position[2] + distance) {
			return true;
		}
		return false;
	}
}
