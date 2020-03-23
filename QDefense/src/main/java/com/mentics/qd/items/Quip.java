package com.mentics.qd.items;

import static com.mentics.math.vector.VectorUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.ItemVisitor;
import com.mentics.qd.commands.Command;
import com.mentics.qd.commands.quips.MotionCommand;
import com.mentics.qd.commands.quips.groups.GroupSizeCommand;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.datastructures.Response;
import com.mentics.qd.jogl.QuipTentacle;
import com.mentics.qd.triggers.Trigger;


public class Quip extends GraphItem implements Energetic {

    // Constants //
    public final static float QUIP_RADIUS = 0.1f;
    public final static float QUIP_ENERGY_GENERATION = 0.1f;

    // Instance Fields //
    public float currentEnergy = Node.NODE_MAX_ENERGY;
    public float maxEnergy = Node.NODE_MAX_ENERGY;

    public final long creationTimeMillis;

    public final String name;
    public final CommandMgr cmds = new CommandMgrQueue();

    public final Object[] nodes = ArrayTreeUtil.newTree();

    public QuipTentacle[] tentacles = new QuipTentacle[] { new QuipTentacle(new float[] { 1, 0, 0 }),
                                                          new QuipTentacle(new float[] { -1, 0, 0 }),
                                                          new QuipTentacle(new float[] { 0, 1, 0 }),
                                                          new QuipTentacle(new float[] { 0, -1, 0 }),
                                                          new QuipTentacle(new float[] { 0, 0, 1 }),
                                                          new QuipTentacle(new float[] { 0, 0, -1 }) };
    
    private Map<String, List<Trigger>> script;
    public String currentState;
    
    public void addTrigger(String state, Trigger tr) {
    	if(script.keySet().contains(state))
    		script.get(state).add(tr);
    	else {
    		script.put(state, new ArrayList<Trigger>());
    		script.get(state).add(tr);
    	}
    }
    
    public void checkTriggers() {
    	if(script.get(currentState) != null)
	    	for(Trigger tr : script.get(currentState))
	    		if(!tr.isMarked() && tr.check())
	    			tr.queueCommands();
    	if(script.get("All") != null)
	    	for(Trigger tr : script.get("All"))
	    		if(!tr.isMarked() && tr.check())
	    			tr.queueCommands();
    }
    
    public void changeState(String newState) {
    	currentState = newState;
    	for(Trigger tr : script.get(currentState))
    		if(tr.getClass().getSimpleName().equals("InitialTrigger")) {
    			tr.queueCommands();
    			return;
    		}
    }

    // Constructors //
    public Quip(long id, final String name) {
        super(id);
        
        this.name = name;
        creationTimeMillis = System.currentTimeMillis();
        script = new HashMap<String, List<Trigger>>();
    }

    // Public Methods //
    @Override
    public float getEnergy() {
        return currentEnergy;
    }

    @Override
    public void setEnergy(float value) {
        currentEnergy = value;
    }

    @Override
    public float getRadius() {
        return QUIP_RADIUS;
    }

    public void run(AllData allData, float duration) {
        cmds.run(allData, duration);
    }

    @Override
    public void generateEnergy(float duration) {
        if (currentEnergy < maxEnergy) {
            currentEnergy = Math.min(currentEnergy + QUIP_ENERGY_GENERATION * duration, maxEnergy);
        }
    }

    public Optional<Integer> sizeConstraint(short[] path) {
        return Arrays.stream(((CommandMgrQueue)cmds).getCommandsForGroup(path))
                .filter(c -> c instanceof GroupSizeCommand).map(c -> ((GroupSizeCommand)c).getSize()).findFirst();
    }

    public Optional<MotionCommand> motionConstraint(short[] path) {
        return Arrays.stream(((CommandMgrQueue)cmds).getCommandsForGroup(path)).filter(c -> c instanceof MotionCommand)
                .map(c -> ((MotionCommand)c)).findFirst();
    }

    @Override
    public void renderItem(GL2 gl, GLU glu) {
        // we render this in a different way for now
    }

    @Override
    public void acceptItemVisitor(ItemVisitor v) {
        v.visit(this);
    }

    public void deleteNode(Node node) {
        ArrayTreeUtil.remove(nodes, node);
    }

    public void addNode(Node newNode) {
        // ArrayTreeUtil.addChild(nodes, newNode);
        // New nodes should go to "unconstrained" subtree
        Object unconstrained = nodes[ArrayTreeUtil.UNCONSTRAINED];
        if (unconstrained instanceof Object[]) {
            ArrayTreeUtil.addChild((Object[])unconstrained, newNode);
        } else {
            Object[] newUnconstrained = ArrayTreeUtil.newTree();

            // SV
            // short[] pathUncons = { 5 };
            // ShellCommand shellComm = new ShellCommand(this, pathUncons, new RadiusMoveTarget(this, 0.0f), 1.0f);
            // cmds.queueCommand(shellComm);
            // SVend

            if (unconstrained instanceof Node) {
                ArrayTreeUtil.addChild(newUnconstrained, unconstrained);
            }
            ArrayTreeUtil.addChild(newUnconstrained, newNode);
            nodes[ArrayTreeUtil.UNCONSTRAINED] = newUnconstrained;
        }
    }

    public void visitNodes(ItemVisitor visitor) {
        ArrayTreeUtil.traverse(nodes, visitor::visit);
    }

    public int groupSize(short[] group) {
        return ArrayTreeUtil.itemCount(ArrayTreeUtil.getSubTree(nodes, group));
    }

    /**
     * This will return an array that is sized at numberOfItems, but some tail elements may be null if fewer than that
     * were found.
     */
    public Item[] getClosestOwnedNodes(long id, float[] position, int numberOfItems) {
        // TODO: once we have spatial index, use it for this
        Item[] result = new Item[numberOfItems];
        float[] distances = new float[numberOfItems];
        visitNodes(item -> {
            if (item.id != id) { // Don't consider itself
                for (int j = 0; j < numberOfItems; j++) {
                    float distance = distance(position, item.position);
                    if (result[j] == null || distance < distances[j]) {
                        AllData.insert(result, item, j);
                        AllData.insert(distances, distance, j);
                        break;
                    }
                }
            }
        });
        return result;
    }
}
