package com.mentics.qd.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.Command;
import com.mentics.qd.items.Quip;

public class ResponseTrigger extends Trigger {
	
	private String qname;
	private String keyword;
	
	public ResponseTrigger(List<Object> params, Quip owner) {
		oneUse = false;
		commands = new ArrayList<Command>();
		this.owner = owner;
		
		qname = (String) params.get(0);
		keyword = (String) params.get(1);
	}
	
	@Override
	public boolean check() {
		if(QuipNebula.allData.chatQuip != null && QuipNebula.allData.chatKeyword !=null)
			if(QuipNebula.allData.chatQuip.equals(qname) && QuipNebula.allData.chatKeyword.equals(keyword))
				return true;
		return false;
	}
}
