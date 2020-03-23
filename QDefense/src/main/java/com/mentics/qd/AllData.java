package com.mentics.qd;

import static com.mentics.math.vector.VectorUtil.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.GraphWorkData;
import com.mentics.parallel.WorkData;
import com.mentics.qd.datastructures.ArrayTreeUtil;
import com.mentics.qd.datastructures.Response;
import com.mentics.qd.items.Camera;
import com.mentics.qd.items.Explosion;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.MovingThing;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.items.Shot;
import com.mentics.qd.triggers.Trigger;
import com.mentics.util.Value;


/**
 * Just threw in a simple single target thing here until we build out a proper implementation.
 */
public class AllData {

	private Map<String, List<Trigger>> script;
	public String currentState;	
	
	public String chatQuip, chatKeyword;
    public List<Response> responseQueue = new ArrayList<>();
    public List<String> gKeywords = new ArrayList<>();

    public List<String> getKnownGKeywords(String qname) {
        List<String> temp = new ArrayList();
        for (int i = 0; i < gKeywords.size(); i++) {
            //TODO: check what are possible keywords for this npc
        }
        return temp;
    }
    
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
    
    public void sendChatReply(String keyword, String qname) {
        if (!gKeywords.contains(keyword)) gKeywords.add(keyword);
        chatQuip = qname;
        chatKeyword = keyword;
        QuipNebula.togglePause();
        QuipNebula.allData.allQuips.forEach(qp -> qp.checkTriggers());
        QuipNebula.togglePause();
        chatQuip = null;
        chatKeyword = null;
    }    

    private GraphWorkData[] workDatas;
    public List<Quip> allQuips = new ArrayList<>();
    public List<Explosion> explosions = new ArrayList<>();

    public Camera camera = new Camera();
    private List<int[]> links = new ArrayList<>();
    private transient ConcurrentLinkedQueue<Item> itemQueue = new ConcurrentLinkedQueue<>(),
            removeItemQueue = new ConcurrentLinkedQueue<>();
    private transient ConcurrentLinkedQueue<Shot> shotQueue = new ConcurrentLinkedQueue<>(),
            removeShotQueue = new ConcurrentLinkedQueue<>();
    private long nextNodeId;

    private volatile List<Shot> shots = new ArrayList<>();

    public List<Shot> getShots() {
        return shots;
    }

    public AllData() {
        assert QuipNebula.numWorkers > 0;
        nextNodeId = 1;
        workDatas = new GraphWorkData[QuipNebula.numWorkers];
        for (int i = 0; i < QuipNebula.numWorkers; i++) {
            workDatas[i] = new GraphWorkData();
        }
        workDatas[0].addItem(camera);
        // workDatas[workDatas.length > 1 ? 1 : 0].addItem(n);
        script = new HashMap<String, List<Trigger>>();
    }

    /**
     * Called from worker threads.
     */
    public WorkData getWorkFor(int workerNum) {
        // TODO: properly maintain work divided up
        if (workerNum == 0) {
            workDatas[0].commands = QuipNebula.commandMgr.commands;
        }
        return workDatas[workerNum];
    }

    /**
     * Called from worker threads.
     */
    public Node queueNewNode(Quip quip) {
        Node n = new Node(nextNodeId, quip);
        nextNodeId++;
        itemQueue.add(n);
        return n;
    }

    public Quip queueNewQuip(String name) {
        Quip q = new Quip(nextNodeId, name);
        nextNodeId++;
        itemQueue.add(q);
        return q;
    }

    public Explosion queueItemExplosion(Item item) {
        Explosion explosion = new Explosion(nextNodeId, item);
        nextNodeId++;
        itemQueue.add(explosion);
        return explosion;
    }

    public Shot queueNewShot(float[] initialPosition, float initialEnergy) {
        Shot s = new Shot(initialPosition, initialEnergy);
        shotQueue.add(s);
        return s;
    }

    public void removeItem(Item item) {
        removeItemQueue.add(item);
    }

    public void removeShot(Shot shot) {
        removeShotQueue.add(shot);
    }

    /**
     * Called from main thread.
     */
    public void processQueue() {
        // Remove first
        while (!removeItemQueue.isEmpty()) {
            Item item = removeItemQueue.remove();
            // Has to remove node from the quipNodes map here too.
            if (item instanceof Node) {
                Node node = (Node)item;
                node.getQuip().deleteNode(node);
            } else if (item instanceof Quip) {
                allQuips.remove(item);
            } else if (item instanceof Explosion) {
                explosions.remove(item);
            }
        }
        while (!removeShotQueue.isEmpty()) {
            shots.remove(removeShotQueue.remove());
        }
        while (!itemQueue.isEmpty()) {
            Item newItem = itemQueue.remove();

            if (newItem instanceof Node) {
                Node newNode = (Node)newItem;
                Quip q = newNode.getQuip();
                q.addNode(newNode);
            } else if (newItem instanceof Quip) {
                allQuips.add((Quip)newItem);
            } else if (newItem instanceof Explosion) {
                explosions.add((Explosion)newItem);
            }

            // TODO: optimize this: don't need to loop through for every target in queue
            int maxLoad = -1;
            int found = -1;
            for (int i = 0; i < workDatas.length; i++) {
                int newLoad = workDatas[i].workLoad;
                if (newLoad > maxLoad) {
                    found = i;
                    maxLoad = newLoad;
                }
            }

            if (found >= 0) {
                workDatas[found].addItem(newItem);
            }
        }
        while (!shotQueue.isEmpty()) {
            Shot newShot = shotQueue.remove();
            shots.add(newShot);
            // TODO: optimize this: don't need to loop through for every target in shotQueue
            int maxLoad = -1;
            int found = -1;
            for (int i = 0; i < workDatas.length; i++) {
                int newLoad = workDatas[i].workLoad;
                if (newLoad > maxLoad) {
                    found = i;
                    maxLoad = newLoad;
                }
            }

            if (found >= 0) {
                workDatas[found].addShot(newShot);
            }
        }
    }

    public void visitItems(ItemVisitor visitor) {
        allQuips.forEach(quip -> {
            visitor.visit(quip);
            quip.visitNodes(visitor);
        });
    }

    public void visitLinks(LinkVisitor visitor) {
        int len = links.size();
        Map<Quip, List<Node>> map = new HashMap<>();
        map.putAll(map);
        Iterator it1 = map.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry pairs = (Map.Entry)it1.next();
            List<Node> items = (List<Node>)pairs.getValue();
            for (int i = 0; i < len; i++) {
                int[] link = links.get(i);
                visitor.visit(items.get(link[0]), items.get(link[1]));
            }
            it1.remove();
        }
    }

    public List<Item> getItemsInRadius(Item search, float radius) {
        List<Item> result = new ArrayList<>();
        visitItems(item -> {
            if ((item != search) && (distance(item.position, search.position) <= radius)) {
                result.add(item);
            }
        });
        return result;
    }

    public void processGroup(Quip quip, short[] group, CommandMgr cmds, float duration, ItemProcessor processor) {
        ArrayTreeUtil.traverse(ArrayTreeUtil.getSubTree(quip.nodes, group), item -> {
            processor.process(this, cmds, duration, item);
        });
    }

    /**
     * This will return an array that is sized at numberOfItems, but some tail elements may be null if fewer than that
     * were found.
     */
    public Item[] getClosestItems(long id, float[] position, int numberOfItems) {
        // TODO: once we have spatial index, use it for this
        Item[] result = new Item[numberOfItems];
        float[] distances = new float[numberOfItems];
        visitItems(item -> {
            if (item.id != id) { // Don't consider itself
                for (int j = 0; j < numberOfItems; j++) {
                    float distance = distance(position, item.position);
                    if (result[j] == null || distance < distances[j]) {
                        insert(result, item, j);
                        insert(distances, distance, j);
                        break;
                    }
                }
            }
        });
        return result;
    }


    // TODO: move this to array util class
    public static <A> void insert(A[] result, A value, int index) {
        for (int i = result.length - 1; i > index; i--) {
            result[i] = result[i - 1];
        }
        result[index] = value;
    }

    // TODO: move this to array util class
    public static void insert(float[] result, float value, int index) {
        for (int i = result.length - 1; i > index; i--) {
            result[i] = result[i - 1];
        }
        result[index] = value;
    }


    public Item getNearestItemWithinSpherical(Item activeItem, float minTheta, float maxTheta, float minPhi,
            float maxPhi) {
        // Camera camera = (Camera) items.get(0);
        float[] r0 = activeItem == null ? camera.position : activeItem.position;

        float[] k = camera.up; // Unit vector showing theta=0

        float[] j = new float[3];// Unit "in" vector showing theta = pi/2, phi = pi/2;
        set(j, camera.lookingDirection);
        normalize(j);

        float[] i = new float[3];// Will contain unit "right" vector (theta = pi/2, phi = 0);
        set(i, j);
        crossInto(i, k);


        float[] temp = new float[3];
        Value<Float> rmin = new Value<>();
        Value<Item> result = new Value<>();

        visitItems(item -> {
            set(temp, item.position);
            subtractInto(temp, r0);

            float r = magnitude(temp);
            float theta = (float)Math.acos(dot(k, temp) / r);
            float phi = (float)Math.acos(dot(i, temp) / r);
            if (dot(j, temp) < 0) {
                phi = (float)(2 * Math.PI - phi);
            }

            if (minTheta <= theta && theta <= maxTheta && minPhi <= phi && phi <= maxPhi) {
                if (r < rmin.value || result == null) {
                    rmin.value = r;
                    result.value = item;
                }
            }
        });
        return result.value;
    }


    public List<Item> getItemsWithingSpherical(Item activeItem, float minTheta, float maxTheta, float minPhi,
            float maxPhi, float minR, float maxR) {
        List<Item> result = new ArrayList<>();
        // Camera camera = (Camera) items.get(0);
        float[] r0 = activeItem == null ? camera.position : activeItem.position;

        float[] k = camera.up; // Unit vector showing theta=0

        float[] j = new float[3];// Unit "in" vector showing theta = pi/2, phi = pi/2;
        set(j, camera.lookingDirection);
        normalize(j);

        float[] i = new float[3];// Will contain unit "right" vector (theta = pi/2, phi = 0);
        set(i, j);
        crossInto(i, k);

        float[] temp = new float[3];

        visitItems(item -> {
            set(temp, item.position);
            subtractInto(temp, r0);

            float r = magnitude(temp);
            float theta = (float)Math.acos(dot(k, temp) / r);
            float phi = (float)Math.acos(dot(i, temp) / r);
            if (dot(j, temp) < 0) {
                phi = (float)(2 * Math.PI - phi);
            }

            if (minTheta <= theta && theta <= maxTheta && minPhi <= phi && phi <= maxPhi && minR <= r && r <= maxR) {
                result.add(item);
            }
        });
        return result;
    }

    /**
     * Will only return items with greater id!
     */
    public List<Item> getAllPossiblyCollidingItems(Item item, float duration) {
        List<Item> result = new ArrayList<>();
        float[] x = new float[3], v = new float[3], a = new float[3];

        visitItems(other -> {
            set(a, item.force);
            multiplyInto(a, 1f / item.mass);
            set(x, other.force);
            multiplyInto(x, 1f / other.mass);
            subtractInto(a, x);

            set(x, item.position);
            subtractInto(x, other.position);
            set(v, item.velocity);
            subtractInto(v, other.velocity);

            if (dot(x, v) < 0 || dot(x, a) < 0) {
                float speed = magnitude(v);
                float acceleration = magnitude(a);
                float r = item.getRadius() + other.getRadius();
                float distance = magnitude(x);

                if (r + speed * duration + acceleration * duration * duration / 2 >= distance) {
                    result.add(other);
                }
            }
        });
        return result;
    }

    /**
     * Will only return items with greater id!
     */
    public List<Item> getPossiblyCollidingItemsWithGreaterId(Item item, float duration) {
        List<Item> result = new ArrayList<>();
        float[] x = new float[3], v = new float[3], a = new float[3];

        visitItems(other -> {
            if (other.id > item.id) {
                set(a, item.force);
                multiplyInto(a, 1f / item.mass);
                set(x, other.force);
                multiplyInto(x, 1f / other.mass);
                subtractInto(a, x);

                set(x, item.position);
                subtractInto(x, other.position);
                set(v, item.velocity);
                subtractInto(v, other.velocity);

                if (dot(x, v) < 0 || dot(x, a) < 0) {
                    float speed = magnitude(v);
                    float acceleration = magnitude(a);
                    float r = item.getRadius() + other.getRadius();
                    float distance = magnitude(x);

                    if (r + speed * duration + acceleration * duration * duration / 2 >= distance) {
                        result.add(other);
                    }
                }
            }
        });
        return result;
    }

    public List<Node> getLinkedWith(Node node) {
        return null;
    }

    public MovingThing getTargetByName(String name) {
        for (int i = 0; i < allQuips.size(); i++)
            if (allQuips.get(i).name.equals(name)) return (MovingThing)allQuips.get(i);
        return null;
    }
}
