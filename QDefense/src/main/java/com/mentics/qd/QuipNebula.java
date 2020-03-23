package com.mentics.qd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import org.yaml.snakeyaml.Yaml;

import com.jogamp.newt.opengl.GLWindow;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.items.Quip;
import com.mentics.qd.jogl.OpenGLCanvas;
import com.mentics.qd.triggers.MoveTrigger;
import com.mentics.qd.triggers.ResponseTrigger;
import com.mentics.qd.triggers.Trigger;


public class QuipNebula {

    // current window resolution.Used to adjust sizes on reshaping/resolution changes
    public static int resolutionW = 1024;
    public static int resolutionH = 768;

    public static int numWorkers = 1;	// number of threads running game interactions<- to corespond to Cpu cores
    public static int gameState = 0;	// overall top-most level game state 0=just started app, 1=in main menu, 2=running
                                     // skirmish mode
    public static boolean paused = true;		// game is running or is paused
    public static float timeControl = 1.2f;		// speed at which the game runs
    public static int time = 0;					// time elapsed since play session started(or a state change?)

    public static GLWindow window;				// visible openGL window
    public static OpenGLCanvas oglCanvas;		// the canvas where everything is drawn. Handles overall rendering
    public static AllData allData;				// All the game-related values/objects
    public static CommandMgrQueue commandMgr;	// has top-level commands for the game mode

    // creates the canvas and attaches it to new window, starts the main thread(game loop)
    public static void main(String[] args) throws Exception {
        window = GLWindow.create(new GLCapabilities(GLProfile.getDefault()));
        window.setTitle("Quip Nebula");
        window.setVisible(true);

        oglCanvas = new OpenGLCanvas();
        window.addGLEventListener(oglCanvas);
        oglCanvas.updateVideoSettings();

        new MainThread().run();
    }
    
    // (un)pauses the game
    public static boolean togglePause() {
        paused = !paused;
        return paused;
    }

    // initiates needed values/objects, cleans up/creates resources when entering the Main Menu
    public static void initMainMenu() {
        allData = null;
        commandMgr = null;

        oglCanvas.initMainMenuRendering();

        gameState = 1;
        paused = true;
    }

    // initiates needed values/objects, cleans up/creates resources when entering Skirmish Mode(New)
    public static void initSkirmishNew() throws FileNotFoundException {
        allData = new AllData();
        commandMgr = new CommandMgrQueue();
        timeControl = 1.2f;
        time = 0;

        oglCanvas.initSkirmishNewRendering();
        initGameData(new FileInputStream("lib/QData/Data1.yml"));

        gameState = 2;
        paused = false;
    }
    
    @SuppressWarnings("unchecked")
    // loads the game data from a save or default file (and the static scripting file)
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
		
		// set world triggers
		for(Entry<String, Object> sWState : ((Map<String, Object>)sWorld.get("States")).entrySet())
			for(Object trInfo : (List<Object>)sWState.getValue())
				allData.addTrigger(sWState.getKey(), Trigger.createTrigger(trInfo, null));
		// set world values
		QuipNebula.allData.changeState((String) world.get("State"));
		QuipNebula.time = (int) world.get("Time");
		
		// set player values
		location = (ArrayList<Integer>) player.get("Location");
		Quip playerQuip = CMD.addQuipInit(new float[] {location.get(0).floatValue(), location.get(1).floatValue(),
				location.get(2).floatValue()}, "Player");
		oglCanvas.setActiveQuip(playerQuip);
		//CMD.setWinCondition(playerQuip, e1, e2);
		CMD.setAutoCamera(() -> ArrayTreeUtil.getNodes(ArrayTreeUtil.getSubTree(playerQuip.nodes, oglCanvas.getSelectedGroup())));
		
		// set npc values
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
}