package com.mentics.qd.commands.quips.groups;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.ItemProcessor;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.items.Shot;

import java.util.List;
import java.util.Optional;

import static com.mentics.math.vector.VectorUtil.distance;


public class ShootingCommand extends GroupCommandBase {
    final float maintainEnergyLevel, shotEnergy, targetDistance, targetDamage, shootingRange;

    private ItemProcessor itemProcessor;

    public ShootingCommand(Quip quip, short[] group, float maintainEnergyLevel, float shotEnergy, float targetDistance,
            float targetDamage) {
        super(CommandMgrQueue.SHOOTING, quip, group);
        this.maintainEnergyLevel = maintainEnergyLevel;
        this.shotEnergy = shotEnergy;
        this.shootingRange = Shot.getShootingRange(shotEnergy);
        this.targetDistance = targetDistance;
        this.targetDamage = targetDamage;

        itemProcessor =
                (allData, cmds, duration, item) -> {
                    if (!(item instanceof Node && ((Node)item).currentEnergy > maintainEnergyLevel)) {
                        return;
                    }
                    float actualShotEnergy = Math.min(shotEnergy, ((Node)item).currentEnergy - maintainEnergyLevel);
                    List<Item> itemsWithingRange = allData.getItemsInRadius(item, shootingRange);
                    // Check for targets to which we can do targetDamage first
                    Optional<Item> target1 =
                            itemsWithingRange
                                    .stream()
                                    .filter(i -> {
                                        float d = distance(item.position, i.position);
                                        float possibleDamageToTarget =
                                                Shot.getDamageAtDistance(shotEnergy,
                                                        d - (item.getRadius() + i.getRadius()));
                                        return (d <= targetDistance || possibleDamageToTarget >= targetDamage);
                                    })
                                    .min((item1, item2) -> Float.compare(distance(item.position, item1.position),
                                            distance(item.position, item2.position)));
                    target1.ifPresent(target -> {
                        float energy = Shot.getEnergy(targetDamage, distance(target.position, item.position));
                        ((Node)item).shoot(target.position, energy, allData);
                    });
                    // Then check for targets within targetDistance
                    if (!target1.isPresent() && ((Node)item).currentEnergy > maintainEnergyLevel + shotEnergy) {
                        Optional<Item> target2 =
                                itemsWithingRange
                                        .stream()
                                        .filter(i -> distance(item.position, i.position) <= targetDistance)
                                        .min((item1, item2) -> Float.compare(distance(item.position, item1.position),
                                                distance(item.position, item2.position)));
                        target2.ifPresent(target -> ((Node)item).shoot(target.position, actualShotEnergy, allData));
                    }
                };
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        allData.processGroup(quip, group, cmds, duration, itemProcessor);
    }
}
