package com.mentics.qd.ui.controls_custom;

import static com.mentics.qd.datastructures.ArrayTreeUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mentics.qd.items.Energetic;
import com.mentics.qd.items.Node;
import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIController_Hud;
import com.mentics.qd.ui.controls_custom.reusable.ProgressbarController;


public class NavigationController extends UIController_Hud {

    // updates the size constraint in the UI
    public void updateMaxSizes() {
        short[] subpath = Arrays.copyOf(uiMgr.gPath, uiMgr.gPath.length + 1);
        for (int i2 = 0; i2 < DIMENSION; i2++) {
            subpath[subpath.length - 1] = (short)i2;
            Optional<Integer> freeNodes = uiMgr.activeQuip.sizeConstraint(subpath);
            NiftyUtil.displayText(screen, "navigation-group-" + (i2 + 1) + "-max",
                    "/" + freeNodes.map(size -> size.toString()).orElse("0"));
        }
    }

    // update the UI values in the Navigation panel based on the selection
    public void updateNavigation() {
        Object subGroups = getSubTree(uiMgr.activeQuip.nodes, uiMgr.gPath);
        if (subGroups != null) {
            int nodeCount;

            for (int i = 0; i < DIMENSION; i++)
                try {
                    nodeCount = itemCount(((Object[])subGroups)[i]);
                    NiftyUtil.displayText(element, "navigation-group-" + (i + 1) + "-current", "" + nodeCount);
                    updateProgressbar(((Object[])subGroups)[i], i + 1);
                } catch (ClassCastException e) {
                    for (int i2 = 0; i2 < DIMENSION; i2++) {
                        NiftyUtil.displayText(element, "navigation-group-" + (i2 + 1) + "-current", "n/a");
                        NiftyUtil.displayText(element, "navigation-group-" + (i2 + 1) + "-max", "");
                        updateProgressbar(null, i2 + 1);
                    }
                }

        } else {
            for (int i = 0; i < DIMENSION; i++) {
                NiftyUtil.displayText(element, "navigation-group-" + (i + 1) + "-current", "n/a");
                NiftyUtil.displayText(element, "navigation-group-" + (i + 1) + "-max", "");
                updateProgressbar(null, i + 1);
            }
        }
    }

    // update the progressbar(of index navIndex) with the energy in group
    private void updateProgressbar(Object group, int navIndex) {
        ProgressbarController progressbar =
                element.findControl("navigation-energy-" + navIndex, ProgressbarController.class);

        if (group == null) {
            progressbar.setProgress(0);
            return;
        }

        List<Float> energies = new ArrayList<>();
        traverse(group, (item) -> {
            if (item instanceof Energetic) {
                energies.add(((Energetic)item).getEnergy());
            }
        });
        if (energies.isEmpty()) {
            progressbar.setProgress(0);
        } else {
            float totalEnergy = energies.stream().collect(Collectors.reducing(0f, Float::sum));
            float maxEnergy = energies.size() * Node.NODE_MAX_ENERGY;
            progressbar.setProgress(totalEnergy / maxEnergy);
        }
    }

}
