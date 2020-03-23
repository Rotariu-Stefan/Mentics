package testing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.CMD;
import com.mentics.qd.MainThread;
import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.global.SkirmishWincheckCommand;
import com.mentics.qd.items.Quip;
import com.mentics.qd.triggers.Trigger;

public class WorldTest {
    
	@Test
	public void testWorld() {
        QuipNebula.allData = new AllData();
        QuipNebula.commandMgr = new CommandMgrQueue();
        
		try {
			gameData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		QuipNebula.paused = false;
		new MainThread().runWorldTest();
	}
	private void gameData() throws FileNotFoundException {
		initGameData(new FileInputStream("lib/QData/Data1.yml"));
	}
	
    @SuppressWarnings("unchecked")
	public static void initGameData(FileInputStream fileStream) throws FileNotFoundException {
		Map<String, Object> data = (Map<String, Object>)new Yaml().load(fileStream);
		Map<String, Object> world = (Map<String, Object>)data.get("World");
		Map<String, Object> npcs = (Map<String, Object>)data.get("Npcs");
		Map<String, Object> player = (Map<String, Object>)data.get("Player");		
		
    	Map<String, Object> sData = (Map<String, Object>)new Yaml().load(new FileInputStream("lib/QData/Script.yml"));
		Map<String, Object> sWorld = (Map<String, Object>)sData.get("World");
		Map<String, Object> sNpcs = (Map<String, Object>)sData.get("Npcs");
		
		Quip quip;
		ArrayList<Integer> location;
		Map<String, Object> sNpc;
		Map<String, Object> sNpcStates;
		
		// setting world values
		QuipNebula.allData.currentState = (String) world.get("State");
		QuipNebula.time = (int) world.get("Time");
		
		// setting player values
		location = (ArrayList<Integer>) player.get("Location");
		quip = CMD.addQuipInit(new float[] {location.get(0).floatValue(), location.get(1).floatValue(),
				location.get(2).floatValue()}, (String)player.get("Name"));
		
		//setting npc values
		for(Entry<String, Object> npc : npcs.entrySet()) {
			// create npc quip
			location = (ArrayList<Integer>) ((Map<String, Object>)npc.getValue()).get("Location");
			quip = CMD.addQuipInit(new float[] {location.get(0).floatValue(), location.get(1).floatValue(),
					location.get(2).floatValue()}, npc.getKey());
			
			// set npc states/triggers
			sNpc = (Map<String, Object>)sNpcs.get(npc.getKey());
			sNpcStates = (Map<String, Object>)sNpc.get("States");
			for(Entry<String, Object> sNpcState : sNpcStates.entrySet())
				for(Object trInfo : (List<Object>)sNpcState.getValue())
					quip.addTrigger(sNpcState.getKey(), Trigger.createTrigger(trInfo, quip));
					
			quip.changeState((String) ((Map<String, Object>)npc.getValue()).get("State"));
		}
    }
	
	private void newSkirmish() throws FileNotFoundException {        
        Quip playerQuip = CMD.addQuipInit(new FileInputStream("lib/QData/Player1.yml"));
        Quip e1 = CMD.addQuipInit(new FileInputStream("lib/QData/Enemy1.yml"));
        Quip e2 = CMD.addQuipInit(new FileInputStream("lib/QData/Enemy2.yml"));

        CMD.setWinCondition(playerQuip, e1, e2);
        //QuipNebula.commandMgr.queueCommand(new AutoCameraCommand(QuipNebula.allData.camera, () -> ArrayTreeUtil
        //        .getNodes(ArrayTreeUtil.getSubTree(playerQuip.nodes, QuipNebula.oglCanvas.getSelectedGroup()))));	//camera? pb?
	}
}
