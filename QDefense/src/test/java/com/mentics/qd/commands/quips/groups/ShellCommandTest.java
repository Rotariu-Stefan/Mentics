package com.mentics.qd.commands.quips.groups;

import static org.junit.Assert.*;
import static com.mentics.math.vector.VectorUtil.distance;

import java.util.Arrays;

import org.junit.Test;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.PointMoveTarget;


public class ShellCommandTest {

    private static final int MAX_ITERATIONS = 5000;

    @Test
    public void testSomeHow() {
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = new float[] { -12f, 0f, 0f };
        Node n2 = new Node(3, qp);
        n2.position = new float[] { 0f, -12f, 0f };
        Node n3 = new Node(4, qp);
        n3.position = new float[] { 12f, 0f, 0f };
        Node n4 = new Node(5, qp);
        n4.position = new float[] { 0f, 12f, 0f };
        PointMoveTarget pmt = new PointMoveTarget();
        pmt.position = new float[] { 0f, 0f, 0f };
        pmt.velocity = new float[] { 0f, 0f, 0f };
        float fR = 5f;
        float dt = 0.11f;
        int numIt = 10 + 90;
        testUniverseMovingView(qp, pmt, fR, dt, numIt, n1, n2, n3, n4);
    }

    @Test
    public void testSomeHowMovingCOM() {
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = new float[] { -12f, 10f, 0f };
        Node n2 = new Node(3, qp);
        n2.position = new float[] { 0f, -2f, 0f };
        Node n3 = new Node(4, qp);
        n3.position = new float[] { 12f, 10f, 0f };
        Node n4 = new Node(5, qp);
        n4.position = new float[] { 0f, 22f, 0f };
        PointMoveTarget pmt = new PointMoveTarget();
        pmt.position = new float[] { 0f, 0f, 0f };
        pmt.velocity = new float[] { 0f, 0f, 0f };
        float fR = 2f;
        float dt = 0.2f;
        int numIt = 100;
        testUniverseMovingView(qp, pmt, fR, dt, numIt, n1, n2, n3, n4);
    }

    @Test
    public void testMovingFixedRelPosition() {
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = new float[] { -12f, 0f + 20f, 0f };
        Node n2 = new Node(3, qp);
        n2.position = new float[] { 0f, -12f + 20f, 0f };
        Node n3 = new Node(4, qp);
        n3.position = new float[] { 12f, 0f + 20f, 0f };
        Node n4 = new Node(5, qp);
        n4.position = new float[] { 0f, 12f + 20f, 0f };
        PointMoveTarget pmt = new PointMoveTarget();
        pmt.position = new float[] { 0f, -20f + 20f, 0f };
        pmt.velocity = new float[] { 0f, 0f + 0f, 0f };
        float fR = 12f;
        float dt = 0.2f;
        int numIt = 100 + 4440; // + 444
        testUniverseMovingView(qp, pmt, fR, dt, numIt, n1, n2, n3, n4);
    }

    @Test
    public void testInteractionsSimple() {
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = new float[] { 1f, -11.958260743101398021129840756196f, 0f };
        Node n2 = new Node(3, qp);
        n2.position = new float[] { -1f, -11.958260743101398021129840756196f, 0f };
        Node n3 = new Node(4, qp);
        n3.position = new float[] { 1f, 11.958260743101398021129840756196f, 0f };
        Node n4 = new Node(5, qp);
        n4.position = new float[] { -1f, 11.958260743101398021129840756196f, 0f };
        Node n5 = new Node(6, qp);
        n5.position = new float[] { 0f, -15, 0f };
        Node n6 = new Node(7, qp);
        n6.position = new float[] { 0f, 15, 0f };
        PointMoveTarget pmt = new PointMoveTarget();
        pmt.position = new float[] { 0f, 0f, 0f };
        pmt.velocity = new float[] { 0f, 0f, 0f };
        float formationRadius = 12f;
        float dt = 0.2f;
        int numIt = 100 + 300;
        testUniverseMovingView(qp, pmt, formationRadius, dt, numIt, n1, n2, n3, n4, n5, n6);
    }

    @Test
    public void testLittleCloudRombCase() {
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = new float[] { -1f, 20, 0f };
        Node n2 = new Node(3, qp);
        n2.position = new float[] { 1f, 20, 0f };
        Node n3 = new Node(4, qp);
        n3.position = new float[] { 0f, 20, 0f };
        Node n4 = new Node(5, qp);
        n4.position = new float[] { 0f, 19, 0f };
        Node n5 = new Node(6, qp);
        n5.position = new float[] { 0f, 21, 0f };
        PointMoveTarget pmt = new PointMoveTarget();
        pmt.position = new float[] { 0f, 0f, 0f };
        pmt.velocity = new float[] { 0f, 0f, 0f };
        float fR = 5f;
        float dt = 0.2f;
        int numIt = 110;
        testUniverseMovingView(qp, pmt, fR, dt, numIt, n1, n2, n3, n4, n5);
    }

    @Test
    public void test2PtsCenter1Line() {
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = new float[] { 0f, 20, 0f };
        Node n2 = new Node(3, qp);
        n2.position = new float[] { 0f, 30, 0f };
        PointMoveTarget pmt = new PointMoveTarget();
        pmt.position = new float[] { 0f, 0f, 0f };
        pmt.velocity = new float[] { 0f, 0f, 0f };
        float fR = 1f;
        float dt = 0.2f;
        int numIt = 125;
        testUniverseMovingView(qp, pmt, fR, dt, numIt, n1, n2);
    }

    public static void testUniverseMovingView(Quip quip, PointMoveTarget target, float formationRadius, float dt,
            int numIt, Node... nodes) {
        for (Node n : nodes) {
            quip.addNode(n);
        }
        short[] party = new short[0];// new short[]{2, 3, 4};
        ShellCommand sc = new ShellCommand(quip, party, target, formationRadius);
        AllData ad = new AllData();
        CommandMgr cmr = new CommandMgrQueue();
        boolean converged = false;
        for (int i = 0; i < numIt; i++) {
            sc.run(ad, cmr, dt);

            System.out.println("after iteration " + i + " values");
            for (Node n : nodes) {
                n.runPhysics(dt);
                System.out.println("In processor item id " + n.id + " mass " + n.mass + "\n pos" +
                                   Arrays.toString(n.position) + " vel" + Arrays.toString(n.velocity) + " force " +
                                   Arrays.toString(n.force));
            }

            System.out.print("com pos " + Arrays.toString(sc.moving.position));
            System.out.print(" com vel " + Arrays.toString(sc.moving.velocity));
            System.out.println(" com frc " + Arrays.toString(sc.moving.force));

            int numInForm = 0;
            for (Node n : nodes) {
                if (sc.isInFormation(n)) {
                    numInForm++;
                }
            }
            if (numInForm == nodes.length) {
                System.out.println("CONVERGED AFTER ITERATION " + i);
                converged = true;
                break;
            }
        }

        assertTrue(converged);
        
        for(int i = 0; i < nodes.length; i++) {
        	for(int j = i + 1; i < nodes.length; i++) {
        		float dst = distance(nodes[i].position, nodes[j].position);
            	assertTrue("Too close between nodes " + i + " " + j, dst < sc.minDist);
            }
        }
    }
}
