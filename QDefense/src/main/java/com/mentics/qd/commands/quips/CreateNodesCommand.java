package com.mentics.qd.commands.quips;

import java.util.List;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.QuipNebula;
import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.ui.controls_custom.NotifyController;


public class CreateNodesCommand extends CommandBase {
    private final static float RADIUS = Quip.QUIP_RADIUS + Node.RADIUS + 0.01f;
    private final float minEnergyToKeep;
    private int number;

    private final Quip quip;

    public CreateNodesCommand(Quip quip, float minEnergyToKeep, int number) {
        super(CommandMgrQueue.PLAYER);
        this.quip = quip;
        this.minEnergyToKeep = minEnergyToKeep;
        this.number = number;
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        if (quip.currentEnergy < minEnergyToKeep + Node.NODE_MAX_ENERGY / 2 || number <= 0) {
            return;
        }

        float epsilon = 0.02f;
        List<Item> itemsNearby = allData.getItemsInRadius(quip, RADIUS + epsilon);

        float X = 0, Y = 0, Z = 0;
        boolean freePosition = true;
        float separation = 2 * (float)Math.toDegrees(Math.asin(1.5f * Node.RADIUS / RADIUS));
        for (float theta = 0.0f; theta <= 360.0f - separation; theta += separation) {
            freePosition = true;
            X = quip.position[0] + RADIUS * (float)Math.cos(Math.toRadians(theta));
            Y = quip.position[1] + RADIUS * (float)Math.sin(Math.toRadians(theta));
            Z = quip.position[2];
            for (Item currentItem : itemsNearby) {
                if ((Math.abs(X - currentItem.position[0]) < epsilon) &&
                    (Math.abs(Y - currentItem.position[1]) < epsilon) &&
                    (Math.abs(Z - currentItem.position[2]) < epsilon)) {
                    freePosition = false;
                    break;
                }
            }
            if (freePosition) break;
        }
        if (freePosition) {
            Node node = allData.queueNewNode(quip);
            node.position[0] = X;
            node.position[1] = Y;
            node.position[2] = Z;
            quip.setEnergy(quip.getEnergy() - Node.NODE_MAX_ENERGY / 2);

            number--;
            if(QuipNebula.oglCanvas != null)		//checking to avoid errors if world testing
            	QuipNebula.oglCanvas.addNotification("Created 1 node for: Quip: " + quip.id + "; nodes left in this command: " + number,
                    NotifyController.MSG_MADENODE);
            // Remove self
            if (number <= 0) {
            	if(QuipNebula.oglCanvas != null)	//checking to avoid errors if world testing
            		QuipNebula.oglCanvas.addNotification("All nodes created.", NotifyController.MSG_ADDNODES);
                cmds.remove(this);
            }
        }
    }
}