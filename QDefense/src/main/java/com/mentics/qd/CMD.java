package com.mentics.qd;

import static com.mentics.qd.datastructures.ArrayTreeUtil.getSubTree;
import static com.mentics.qd.datastructures.ArrayTreeUtil.itemCount;

import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.commands.global.AddQuipCommand;
import com.mentics.qd.commands.global.AutoCameraCommand;
import com.mentics.qd.commands.global.SkirmishWincheckCommand;
import com.mentics.qd.commands.quips.CreateNodesCommand;
import com.mentics.qd.commands.quips.MotionCommand;
import com.mentics.qd.commands.quips.groups.GroupCommandBase;
import com.mentics.qd.commands.quips.groups.GroupSizeCommand;
import com.mentics.qd.commands.quips.groups.ShellCommand;
import com.mentics.qd.commands.quips.groups.SquareCommand;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.RadiusMoveTarget;

//utility class used to perform most actions(commands) within the game and modify the game state
//has one-line methods similar to a game console
//it's not necessary to run anything but makes things easier/more consistent
public class CMD {
	
	//shows information about a quip
	public static void showInfo(MovingThing thing) {
		String info;
		
		System.out.println(thing.getClass().getSimpleName());
		switch(thing.getClass().getSimpleName()) {
		case "Quip":
			info = "Quip: " + ((Quip)thing).name + ".\n";
			info += "Energy: " + ((Quip)thing).currentEnergy + "\n";
			info += "State: " +  ((Quip)thing).currentState + "\n";
			break;
		case "Node":
			info = "Node.\n";
			info += "Energy: " + ((Node)thing).currentEnergy + "\n";
			break;
		case "Camera":
			info = "Camera.\n";
			break;
		default:
			info = "Temporary thing.\n";
		}
		
		info += "Position: " + thing.position[0] + " , " + thing.position[1] + " , " + thing.position[2] + "\n";
		info += "Velocity: " + thing.velocity[0] + " , " + thing.velocity[1] + " , " + thing.velocity[2] + "\n";
		info += "Force: " + thing.force[0] + " , " + thing.force[1] + " , " + thing.force[2] + "\n";
		
		System.out.println(info);
	}
	
	//shows information about a quip
	public static void showInfo(String qname) {
		showInfo(QuipNebula.allData.getTargetByName(qname));
	}
	
	//shows information about a group
	public static void showInfo(Quip quip, short[] gpath) {
		String info;
		
		info = "Group: " + gpath + ". (from " + quip.name + ")\n";
		
		Object group = getSubTree(quip.nodes, gpath);
		info += "Nodes: " + itemCount(group) + " / " + quip.sizeConstraint(gpath).map(size -> size.toString()).orElse("-");
		//TODO: energy too ?
		
		Optional<MotionCommand> mc = quip.motionConstraint(gpath);
		info += "Target: " + mc.map(shell -> shell.getLandmarkName()).orElse("None");
		info += " in " + mc.map(shell -> "" + (int)shell.getDistance()).orElse("-");
		info += "\n Formation: " + mc.map(shell -> shell.getShape()).orElse("None");
		info += " in " + mc.map(shell -> "" + (int)shell.getParam()).orElse("-");
		
		System.out.println(info);
	}
	
	//shows information about a quip
	public static void showInfo(String qname, short[] gpath) {
		showInfo(((Quip)QuipNebula.allData.getTargetByName(qname)), gpath);
	}
	
	//shows information about all game objects
	public static void showInfo() {
		System.out.println("World State: " +  QuipNebula.allData.currentState);
		System.out.println(QuipNebula.allData.allQuips.size() + " Quips:");
		for(Quip q : QuipNebula.allData.allQuips)
			showInfo(q);
	}
	
	//adds a quip to the game world(instantly without needing to queue the command)
	public static Quip addQuipInit(float[] coord, String name) {
		AddQuipCommand addQuip = new AddQuipCommand(coord, name);
		addQuip.run(QuipNebula.allData, QuipNebula.commandMgr, 1);
		return addQuip.getCreatedQuip();
	}
	
	//adds a quip to the game world(instantly without needing to queue the command)
	public static Quip addQuipInit(FileInputStream fs) {
		AddQuipCommand addQuip = new AddQuipCommand(fs);
		addQuip.run(QuipNebula.allData, QuipNebula.commandMgr, 1);
		return addQuip.getCreatedQuip();
	}
	
	//adds a quip to the game world
	public static void addQuip(float[] coord, String name) {
		AddQuipCommand addQuip = new AddQuipCommand(coord, name);
		QuipNebula.commandMgr.queueCommand(addQuip);
	}
	
	//adds a quip to the game world
	public static void addQuip(FileInputStream fs) {
		AddQuipCommand addQuip = new AddQuipCommand(fs);
		QuipNebula.commandMgr.queueCommand(addQuip);
	}
	
	//adds nodes to a quip(instantly without needing to queue the command)
	public static void addNodesInit(Quip quip, int nodeNr) {
		CreateNodesCommand cNodes = new CreateNodesCommand(quip, 0, nodeNr);
		cNodes.run(QuipNebula.allData, QuipNebula.commandMgr, 1);
	}
	
	//adds nodes to a quip(instantly without needing to queue the command)
	public static void addNodesInit(String qname, int nodeNr) {
		addNodes((Quip) QuipNebula.allData.getTargetByName(qname), nodeNr);
	}
	
	//adds nodes to a quip
	public static void addNodes(Quip quip, int nodeNr) {
		CreateNodesCommand cNodes = new CreateNodesCommand(quip, Node.NODE_MAX_ENERGY / 2, nodeNr);
		quip.cmds.queueCommand(cNodes);
	}
	
	//adds nodes to a quip
	public static void addNodes(String qname, int nodeNr) {
		addNodesInit((Quip) QuipNebula.allData.getTargetByName(qname), nodeNr);
	}
	
	//sets the max nr of nodes in a group
	public static void setGroupSize(Quip quip, short[] gpath, int nodeNr) {
        CommandMgrQueue commandMgr = (CommandMgrQueue)quip.cmds;
        GroupCommandBase[] groupCommands = commandMgr.getCommandsForGroup(gpath);        
        for (GroupCommandBase command : groupCommands) {
            if (command instanceof GroupSizeCommand) {
                commandMgr.remove(command);
            }
        }

        GroupSizeCommand containsCommand = new GroupSizeCommand(quip, gpath, nodeNr);
        commandMgr.queueCommand(containsCommand);
	}
	
	//sets the max nr of nodes in a group
	public static void setGroupSize(String qname, short[] gpath, int nodeNr) {
		setGroupSize((Quip) QuipNebula.allData.getTargetByName(qname), gpath, nodeNr);
	}
	
	//sets a motion directive for a group(target + formation)
	public static void setGroupMotion(Quip quip, short[] gpath, MovingThing targetThing, float distance, String formationName, float param) {
        CommandMgrQueue commandMgr = (CommandMgrQueue)quip.cmds;
        GroupCommandBase[] groupCommands = commandMgr.getCommandsForGroup(gpath);
        for (GroupCommandBase command : groupCommands) {
            if (command instanceof MotionCommand) {
                commandMgr.remove(command);
            }
        }

        MovingThing landmark = targetThing;
        RadiusMoveTarget target =
                new RadiusMoveTarget(landmark, distance);

        switch (formationName) {
        case "Shell":
            ShellCommand shellCommand = new ShellCommand(quip, gpath, target, param);
            commandMgr.queueCommand(shellCommand);
            break;

        case "Square":
            SquareCommand squareCommand = new SquareCommand(quip, gpath, target, param);
            commandMgr.queueCommand(squareCommand);
            break;

        case "Somethin":
            break;
        }
	}
	
	//sets a motion directive for a group(target + formation)
	public static void setGroupMotion(String qname, short[] gpath, MovingThing targetThing, float distance, String formationName, float param) {
		setGroupMotion((Quip) QuipNebula.allData.getTargetByName(qname), gpath, targetThing, distance, formationName, param);
	}
	
	//sets a motion directive for a group(target + formation)
	public static void setGroupMotion(Quip quip, short[] gpath, String targetName, float distance, String formationName, float param) {
		setGroupMotion(quip, gpath, QuipNebula.allData.getTargetByName(targetName), distance, formationName, param);
	}
	
	//sets a motion directive for a group(target + formation)
	public static void setGroupMotion(String qname, short[] gpath, String targetName, float distance, String formationName, float param) {
		setGroupMotion((Quip) QuipNebula.allData.getTargetByName(qname), gpath, QuipNebula.allData.getTargetByName(targetName), distance, formationName, param);
	}
	
	//sets the items that the camera will automatically follow
	public static void setAutoCamera(Supplier<Item[]> selection) {
        QuipNebula.commandMgr.queueCommand(new AutoCameraCommand(QuipNebula.allData.camera, selection));
	}
	
	//sets the win condition for the current play <-- LIMITED, will change
	public static void setWinCondition(Quip p, Quip e1, Quip e2) {
		QuipNebula.commandMgr.queueCommand(new SkirmishWincheckCommand(p, e1, e2));
	}
	
	//removes a quip from the world state
	public static void removeQuip(Quip quip) {
		QuipNebula.allData.removeItem(quip);
	}
	
	//removes a quip from the world state
	public static void removeQuip(String qname) {
		QuipNebula.allData.removeItem((Quip) QuipNebula.allData.getTargetByName(qname));
	}
	
	//removes nodes from the world state
	public static void removeNodes(Node[] nodes) {
		for(Node n : nodes)
			QuipNebula.allData.removeItem(n);
	}
	
	//removes nodes from the world state
	public static void removeNodes(List<Node> nodes) {
		for(Node n : nodes)
			QuipNebula.allData.removeItem(n);
	}
	
	//removes nodes(all within a group) from the world state
	public static void removeNodes(Quip quip, short[] gPath) {
		Node[] nodes = ArrayTreeUtil.getNodes(ArrayTreeUtil.getSubTree(quip.nodes, gPath));
		removeNodes(nodes);
	}
}
