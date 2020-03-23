package com.mentics.parallel;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import com.mentics.math.vector.VectorUtil;
import com.mentics.qd.AllData;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.Command;
import com.mentics.qd.items.Energetic;
import com.mentics.qd.items.Explosion;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.ItemUtil;
import com.mentics.qd.items.Quip;
import com.mentics.qd.items.Shot;


public class GraphWorkData implements WorkData {
    public List<Item> items;
    public List<Shot> shots;
    public TreeSet<Command>[] commands;
    public int workLoad = 0;

    public GraphWorkData() {
        items = new ArrayList<>();
        shots = new ArrayList<>();
        commands = new TreeSet[0];
    }

    @Override
    public void run(AllData allData, CommandMgr commandMgr, float duration) {
        int num = items.size();
        items.forEach(item -> {
            item.beginStep();
            // Generate energy
            if (item instanceof Energetic) {
                ((Energetic)item).generateEnergy(duration);
            }
        });
        List<Quip> quips = ((AllData)allData).allQuips;
        for (Quip quip : quips)
            quip.run(allData, duration);
        for (int type = 0; type < commands.length; type++) {
            TreeSet<Command> cmds = commands[type];
            cmds.forEach(cmd -> {
                cmd.run(allData, commandMgr, duration);
            });
            // Execute Quip commands.
            /*Map<Quip,List<Node>> map = ((GraphAllData) allData).quipNodes;
            Iterator it = map.entrySet().iterator();
            while(it.hasNext()){
            	Map.Entry pairs = (Map.Entry)it.next();
                cmds.forEach(cmd -> {
                    cmd.run(allData, ((Quip) pairs.getKey()).cmds, duration);
                });
            }*/
        }
        items.forEach(item -> {
            VectorUtil.addInto(item.force, item.initiatedForce);
        });

        float remainingTime = duration;

        if (remainingTime > 0) {
            runPhysics(allData, remainingTime);
            // removeShotsOutsideOfShootingRange(allData, collisions);
        }
        removeExplosionsExceededLifeTime(allData);
    }

    private void runPhysics(AllData allData, float duration) {
        items.forEach(item -> item.endStep(allData, duration));
        shots.forEach(shot -> ItemUtil.updatePhysics(shot.position, shot.velocity, VectorUtil.ZERO, 1f, duration));
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void addShot(Shot newShot) {
        shots.add(newShot);
    }

    private void destroyItem(Item item, AllData allData) {
        items.remove(item);
        allData.removeItem(item);
        ((AllData)allData).queueItemExplosion(item);
    }

    private void destroyItems(AllData allData) {
        ListIterator<Item> li = items.listIterator();
        while (li.hasNext()) {
            Item item = li.next();
            if (item instanceof Energetic && ((Energetic)item).getEnergy() < 0) {
                li.remove();
                allData.removeItem(item);
            }
        }
    }

    private void removeExplosionsExceededLifeTime(AllData allData) {
        ListIterator<Item> li = items.listIterator();
        while (li.hasNext()) {
            Item item = li.next();
            if (item instanceof Explosion && ((Explosion)item).timeLeft < 0) {
                li.remove();
                allData.removeItem(item);
            }
        }
    }
}