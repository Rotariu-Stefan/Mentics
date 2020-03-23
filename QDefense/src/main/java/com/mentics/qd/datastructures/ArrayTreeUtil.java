package com.mentics.qd.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.commands.quips.groups.GroupSizeCommand;
import com.mentics.qd.items.Node;


public class ArrayTreeUtil {
    public static final short DIMENSION = 6, UNCONSTRAINED = DIMENSION - 1;

    public static Object getSubTree(Object tree, short[] path) {
        if (path == null) {
            return tree;
        }
        Object subtree = tree;
        for (short s : path) {
            if (subtree == null || subtree instanceof Node) {
                return null;
            } else /*if (subtree instanceof Object[]) */{
                subtree = ((Object[])subtree)[s];
            }
        }
        return subtree;
    }

    public static void traverse(Object tree, Consumer<Node> consumer) {
        if (tree == null) {
            // do nothing
        } else if (tree instanceof Node) {
            consumer.accept((Node)tree);
        } else /* if (tree instanceof Object[] */{
            Object[] array = (Object[])tree;
            for (Object subtree : array) {
                traverse(subtree, consumer);
            }
        }
    }

    public static Node[] getNodes(Object tree) {
        final ArrayList<Node> nodes = new ArrayList<>();
        traverse(tree, nodes::add);
        return nodes.toArray(new Node[nodes.size()]);
    }

    public static void addChild(Object[] tree, Object child) {
        if (child == null) return;
        // Simple child addition
        short immediateChildrenCount = immediateChildrenCount(tree);
        if (immediateChildrenCount < DIMENSION) {
            for (int i = 0; i < DIMENSION; i++) {
                if (tree[i] == null) {
                    tree[i] = child;
                    return;
                }
            }
        }
        // If maximum number of children is present already
        // If we have leafs, then we will change first of those to the tree if we are adding tree, and put the leaf
        // being changed
        // into it
        // If we are adding a leaf we will create a new subtree and place both leaves there
        for (int i = 0; i < DIMENSION; i++) {
            Object c = tree[i];
            if (c instanceof Node) {
                if (child instanceof Object[]) {
                    tree[i] = child;
                    addChild((Object[])child, c);
                } else {
                    Object[] subtree = new Object[DIMENSION];
                    tree[i] = subtree;
                    subtree[0] = c;
                    subtree[1] = child;
                }
                return;
            }
        }
        // If there are no leaves, we will put new child into the subtree with least leaves
        Object[] minSubtree = (Object[])getImmediateChildWithMinLeaves(tree);
        addChild(minSubtree, child);
    }

    public static short immediateChildrenCount(Object tree) {
        if (tree == null) {
            return 0;
        } else if (tree instanceof Node) {
            return 1;
        } else /* if (tree instanceof Object[]) */{
            short n = 0;
            for (Object subtree : (Object[])tree) {
                if (subtree != null) {
                    n++;
                }
            }
            return n;
        }
    }

    public static int itemCount(Object tree) {
        if (tree == null) {
            return 0;
        } else if (tree instanceof Node) {
            return 1;
        } else /* if (tree instanceof Object[] */{
            int result = 0;
            for (Object subtree : (Object[])tree) {
                result += itemCount(subtree);
            }
            return result;
        }
    }

    public static Object getImmediateChildWithMinLeaves(Object[] tree) {
        Object result = null;
        int minLeaves = 0;
        for (Object subtree : tree) {
            int ic = itemCount(subtree);
            if (ic < minLeaves || result == null) {
                minLeaves = ic;
                result = subtree;
            }
        }
        return result;
    }

    public static long idFromPath(short[] path) {
        long result = 0;
        long power = 1;
        for (short s : path) {
            result += s * power;
            power *= DIMENSION;
        }
        return result;
    }

    public static Object[] createGroup(Object[] tree, short[] path) {
        Object[] parent = tree;
        Object child;
        for (short s : path) {
            // if(null == parent) return null;
            child = parent[s];
            if (!(child instanceof Object[])) {
                Object[] branches = new Object[DIMENSION];
                parent[s] = branches;
                branches[UNCONSTRAINED] = child;
                parent = branches;
            } else {
                parent = (Object[])child;
            }
        }
        return parent;
    }

    public static void removeChild(Object[] tree, Object child) {
        for (int i = 0; i < DIMENSION; i++) {
            Object c = tree[i];
            if (c == child) {
                tree[i] = null;
                return;
            } else if (c instanceof Object[]) {
                removeChild((Object[])c, child);
            }
        }
    }

    public static Optional<Integer> sizeConstraint(short[] path, CommandMgrQueue cmdMgr) {
        return Arrays.stream(cmdMgr.getCommandsForGroup(path)).filter(c -> c instanceof GroupSizeCommand)
                .map(c -> ((GroupSizeCommand)c).getSize()).findFirst();
    }

    public static void remove(Object[] objects, Node item) {
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object != null && Node.class == object.getClass() && ((Node)object) == item) {
                objects[i] = null;
            }
        }
    }

    public static Object[] newTree() {
        return new Object[DIMENSION];
    }

    public static String pathToString(short[] path) {
        if (path.length == 0) return "Top";
        else {
            String s = "";
            for (short gnr : path)
                s += (gnr + 1) + "-";
            return s.substring(0, s.length() - 1);
        }
    }
}
