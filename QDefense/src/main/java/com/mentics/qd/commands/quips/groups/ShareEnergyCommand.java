package com.mentics.qd.commands.quips.groups;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.util.AppConstant;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.mentics.qd.datastructures.ArrayTreeUtil.getNodes;
import static com.mentics.qd.datastructures.ArrayTreeUtil.getSubTree;


public class ShareEnergyCommand extends GroupCommandBase {
    public ShareEnergyCommand(Quip quip, short[] group) {
        super(CommandMgrQueue.ENERGY_USAGE, quip, group);
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        float maxEnergy = duration * AppConstant.NODE_MAX_ENERGY_TRANSFER_RATE;
        Node[] nodes = getNodes(getSubTree(quip.nodes, group));
        // Sort nodes by energy in descending order
        // I think this will result in better energy distribution (though I don't have a proof)
        // But in principle this is not necessary
        Arrays.sort(nodes, (a, b) -> Float.compare(b.currentEnergy, a.currentEnergy));
        for (Node donor : nodes) {
            List<Node> linkedNodes = allData.getLinkedWith(donor);
            List<Node> linkedEnergyAcceptors = linkedNodes.stream()
                    .filter(other -> other.currentEnergy < donor.currentEnergy)
                    .sorted((a, b) -> Float.compare(a.currentEnergy, b.currentEnergy))
                    .collect(Collectors.toList());
            if (linkedEnergyAcceptors.isEmpty()) {
                continue;
            }
            // Determining which nodes we will share energy with
            // If nodes are sorted by energy in ascending order
            // then we are interested in a sublist such that following holds:
            // E[i] <= MeanEnergy(linked nodes 0...i, donor) <= E[i+1]
            float sumOfEnergies = donor.currentEnergy;
            int i;
            for (i = 0; i < linkedEnergyAcceptors.size(); i++) {
                Node ni = linkedEnergyAcceptors.get(i);
                float currentMean = (sumOfEnergies + ni.currentEnergy) / (2 + i);
                if (ni.currentEnergy < currentMean) {
                    sumOfEnergies += ni.currentEnergy;
                } else if (ni.currentEnergy == currentMean) {
                    sumOfEnergies += ni.currentEnergy;
                    i++;
                    break;
                } else if (ni.currentEnergy > currentMean) {
                    break;
                }
            }
            List<Node> acceptors = linkedEnergyAcceptors.subList(0, i);
            float meanEnergy = sumOfEnergies / (1 + acceptors.size());
            // Coefficient will be 1 if the node can give away all the required energy and will be less than 1 otherwise
            float coefficient = Math.min(1, (donor.currentEnergy - meanEnergy) / maxEnergy);
            for (Node acceptor : acceptors) {
                float dE = coefficient * (meanEnergy - acceptor.currentEnergy);
                acceptor.currentEnergy += dE;
                donor.currentEnergy -= dE;
            }
        }
    }
}
