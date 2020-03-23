package com.mentics.qd.commands.quips.groups;

import static com.mentics.qd.datastructures.ArrayTreeUtil.*;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;


public class GroupSizeCommand extends GroupCommandBase {
    private boolean flag = false;

    public int getSize() {
        return size;
    }

    private int size;

    public GroupSizeCommand(Quip quip, short[] group, int size) {
        super(CommandMgrQueue.GROUPING, quip, group);
        this.size = size;
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        // Check if the group path contains 9 (DIMENSION - 1) - these are groups that cannot be constrained
        // Go from the top level to the bottom starting from the group 9 motion additional items to the group
        // Or remove unnecessary items from the group
        // System.out.println("Maintaining Group.");

        Object mbSubtree = getSubTree(quip.nodes, group);
        Object[] subtree;
        if (!(mbSubtree instanceof Object[])) {
            subtree = createGroup(quip.nodes, group);
        } else {
            subtree = (Object[])mbSubtree;
        }

        int currentSize = itemCount(subtree);
        if (currentSize < size) {
            Object ungroupedItemsTree = quip.nodes[UNCONSTRAINED];
            if (ungroupedItemsTree instanceof Object[]) {
                Node[] ungroupedItems = getNodes(ungroupedItemsTree);
                for (Node i : ungroupedItems) {
                    addChild(subtree, i);
                    removeChild((Object[])ungroupedItemsTree, i);
                    currentSize++;
                    if (currentSize >= size) {
                        break;
                    }
                }
            } else if (ungroupedItemsTree instanceof Item) {
                quip.nodes[UNCONSTRAINED] = null;
                addChild(subtree, ungroupedItemsTree);
                currentSize++;
            }
        } else if (currentSize > size) {
            Object ungroupedItemsTree = quip.nodes[UNCONSTRAINED];
            if (ungroupedItemsTree instanceof Object[]) {
                Node[] items = getNodes(subtree);
                for (Node i : items) {
                    addChild((Object[])ungroupedItemsTree, i);
                    removeChild(subtree, i);
                    currentSize--;
                    if (currentSize <= size) {
                        break;
                    }
                }
            } else if (ungroupedItemsTree instanceof Node) {
                quip.nodes[UNCONSTRAINED] = null;
                addChild(subtree, ungroupedItemsTree);
                currentSize--;
            }
        }
    }
}
