package com.mentics.qd.ui.controls_custom;

import static com.mentics.qd.datastructures.ArrayTreeUtil.*;

import com.mentics.qd.ui.NiftyUtil;
import com.mentics.qd.ui.UIController_Hud;


public class SelectionController extends UIController_Hud {

    public void updateSelection(boolean quipSelected) {
        if (quipSelected == true) NiftyUtil.displayText(screen, "current-group", "Quip Selected");
        else if (uiMgr.gPath.length == 0) NiftyUtil.displayText(screen, "current-group", "Group: Top");
        else NiftyUtil.displayText(element, "current-group", "Group: " + pathToString(uiMgr.gPath));
    }

}
