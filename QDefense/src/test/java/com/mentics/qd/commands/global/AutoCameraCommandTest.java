package com.mentics.qd.commands.global;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.Test;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.quips.groups.ShellCommand;
import com.mentics.qd.items.Camera;
import com.mentics.qd.items.Item;
import com.mentics.qd.items.Node;
import com.mentics.qd.items.Quip;
import com.mentics.qd.model.PointMoveTarget;

public class AutoCameraCommandTest {

//	public static float formRad = 10.f;
	public static float[][] ptsAxial = {
		{-1, 0, 0}, {0, -1, 0}, {0, 0, -1},
		{1, 0, 0}, {0, 1, 0}, {0, 0, 1}
		};
	public static final float revSqr2 = (float)(1.0 / Math.sqrt(2.0));
	public static float[][] ptsInCPlane = {
		{+revSqr2, +revSqr2, 0}, {+revSqr2, 0, +revSqr2}, {0, +revSqr2, +revSqr2}, 
		{-revSqr2, -revSqr2, 0}, {-revSqr2, 0, -revSqr2}, {0, -revSqr2, -revSqr2},
		{+revSqr2, -revSqr2, 0}, {+revSqr2, 0, -revSqr2}, {0, +revSqr2, -revSqr2},
		{-revSqr2, +revSqr2, 0}, {-revSqr2, 0, +revSqr2}, {0, -revSqr2, +revSqr2}
	};
	public static final float revSqr3 = (float)(1.0 / Math.sqrt(3.0));
	public static float[][] ptsInQuadrants = {
		{+revSqr3, +revSqr3, +revSqr3}, {-revSqr3, -revSqr3, -revSqr3}, 
		{+revSqr3, -revSqr3, -revSqr3}, {-revSqr3, +revSqr3, +revSqr3}, 
		{-revSqr3, +revSqr3, -revSqr3}, {+revSqr3, -revSqr3, +revSqr3}, 
		{-revSqr3, -revSqr3, +revSqr3}, {+revSqr3, +revSqr3, -revSqr3}
	};
	
	public static float[][] copySetOfPts(float[][] sp) {
		float[][] res =  new float[sp.length][];
		for(int i = 0; i < sp.length; i++) res[i] = sp[i].clone();
		return res;
	}
	
	public static float[][] moveSetOfPts(float[][] sp, float dx, float dy, float dz) {
		float[][] res = copySetOfPts(sp);
		for (float[] fa : res) {
			fa[0] += dx;
			fa[1] += dy;
			fa[2] += dz;
		}
		return res;
	}
	
	public static float[][] scaleSetOfPts(float[][] sp, float radius) {
		float[][] res = copySetOfPts(sp);
		for(int i = 0; i < res.length; i++) {
			for(int j = 0; j < res[i].length; j++) {
				res[i][j] *= radius;
			}
		}
		return res;
	}
	
	public static float[][] groupcom1stScl2ndMvSetOfPts(float[][] sp, float dx, float dy, float dz, float radius) {
		return moveSetOfPts(scaleSetOfPts(sp, radius), dx, dy, dz);
	}
	
	@Test
	public void testMvSOP() {
		float[][] ofPTA = moveSetOfPts(ptsAxial, 1, 1, 1);
		System.out.println("Base " + Arrays.deepToString(ptsAxial) + " offseted " + Arrays.deepToString(ofPTA));
	}
	
	@Test
	public void testScSOP() {
		float[][] scPTA = scaleSetOfPts(ptsAxial, 10);
		System.out.println("Base " + Arrays.deepToString(ptsAxial) + " scaled " + Arrays.deepToString(scPTA));
	}
	
	@Test
	public void testGroupComm() {
		float[][] scPTA = groupcom1stScl2ndMvSetOfPts(ptsAxial, 2, 2, 2, 2);
		System.out.println("Base " + Arrays.deepToString(ptsAxial) + " moved and scaled " + Arrays.deepToString(scPTA));
	}
	
	@Test
    public void testGeneral() {
//      System.out.println(Arrays.toString(n1.position));
//      System.out.println(Arrays.toString(ptsAxial[0]));
        Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = ptsAxial[0].clone();
        Node n2 = new Node(3, qp);
        n2.position = ptsAxial[1].clone();
        Node n3 = new Node(4, qp);
        n3.position = ptsAxial[2].clone();
        Node n4 = new Node(5, qp);
        n4.position = ptsAxial[3].clone();
        Node n5 = new Node(6, qp);
        n5.position = ptsAxial[4].clone();
        Node n6 = new Node(7, qp);
        n6.position = ptsAxial[5].clone();
        float dt = 0.1f;
        int numIt = 33;
        testAutoCameraMovingView(dt, numIt, n1, n2, n3, n4, n5, n6);
    }
	
	@Test
	public void testRotation() {
		float[][] basePts = moveSetOfPts(ptsAxial, 0, 0, 30);
		Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = basePts[0].clone();
        Node n2 = new Node(3, qp);
        n2.position = basePts[1].clone();
        Node n3 = new Node(4, qp);
        n3.position = basePts[2].clone();
        Node n4 = new Node(5, qp);
        n4.position = basePts[3].clone();
        Node n5 = new Node(6, qp);
        n5.position = basePts[4].clone();
        Node n6 = new Node(7, qp);
        n6.position = basePts[5].clone();
        float dt = 0.1f;
        int numIt = 100;
        testAutoCameraMovingView(dt, numIt, n1, n2, n3, n4, n5, n6);
	}
	
	@Test
	public void testRotation3D() {
		float[][] basePts = moveSetOfPts(ptsAxial, 10, 10, 30);
		Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = basePts[0].clone();
        Node n2 = new Node(3, qp);
        n2.position = basePts[1].clone();
        Node n3 = new Node(4, qp);
        n3.position = basePts[2].clone();
        Node n4 = new Node(5, qp);
        n4.position = basePts[3].clone();
        Node n5 = new Node(6, qp);
        n5.position = basePts[4].clone();
        Node n6 = new Node(7, qp);
        n6.position = basePts[5].clone();
        float dt = 0.1f;
        int numIt = 100;
        testAutoCameraMovingView(dt, numIt, n1, n2, n3, n4, n5, n6);
	}
	
	@Test
	public void testWithMovingItemsCoaxially() {
		float[][] basePts = copySetOfPts(ptsAxial);
		Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = basePts[0].clone();
        n1.velocity = new float[]{0 , 0, -1};
        Node n2 = new Node(3, qp);
        n2.position = basePts[1].clone();
        n2.velocity = new float[]{0 , 0, -1};
        Node n3 = new Node(4, qp);
        n3.position = basePts[2].clone();
        n3.velocity = new float[]{0 , 0, -1};
        Node n4 = new Node(5, qp);
        n4.position = basePts[3].clone();
        n4.velocity = new float[]{0 , 0, -1};
        Node n5 = new Node(6, qp);
        n5.position = basePts[4].clone();
        n5.velocity = new float[]{0 , 0, -1};
        Node n6 = new Node(7, qp);
        n6.position = basePts[5].clone();
        n6.velocity = new float[]{0 , 0, -1};
        float dt = 0.1f;
        int numIt = 100;
        testAutoCameraMovingView(dt, numIt, n1, n2, n3, n4, n5, n6);
	}
	
	@Test
	public void testWithMovingItemsInvolvedInRotationCamera() {
		float[][] basePts = groupcom1stScl2ndMvSetOfPts(ptsAxial, 0, -20, 0, 1);
		Quip qp = new Quip(11, "qqqq");
        Node n1 = new Node(2, qp);
        n1.position = basePts[0].clone();
        n1.velocity = new float[]{0 , 1, 0};
        Node n2 = new Node(3, qp);
        n2.position = basePts[1].clone();
        n2.velocity = new float[]{0 , 1, 0};
        Node n3 = new Node(4, qp);
        n3.position = basePts[2].clone();
        n3.velocity = new float[]{0 , 1, 0};
        Node n4 = new Node(5, qp);
        n4.position = basePts[3].clone();
        n4.velocity = new float[]{0 , 1, 0};
        Node n5 = new Node(6, qp);
        n5.position = basePts[4].clone();
        n5.velocity = new float[]{0 , 1, 0};
        Node n6 = new Node(7, qp);
        n6.position = basePts[5].clone();
        n6.velocity = new float[]{0 , 1, 0};
        float dt = 1f;
        int numIt = 100;
        testAutoCameraMovingView(dt, numIt, n1, n2, n3, n4, n5, n6);
	}
	
	public static void testAutoCameraMovingView(float dt, int numIt, Node... nodes) {
        Camera cam = new Camera();
        Supplier<Item[]> sup = new Supplier<Item[]>() {
			@Override
			public Item[] get() {
				return nodes;
			}
		};
        AutoCameraCommand acm = new AutoCameraCommand(cam, sup);
        AllData ad = new AllData();
        CommandMgr cmr = new CommandMgrQueue();
        for (int i = 0; i < numIt; i++) {
        	acm.run(ad, cmr, dt);
        	cam.runPhysics(dt);
        	System.out.println("Camera position " + Arrays.toString(cam.position) + " after iteration " + i);
        	System.out.println("Camera orientation up axis " + Arrays.toString(cam.up) + " looking axis " + Arrays.toString(cam.lookingDirection));
        	System.out.println("For debugging purposes " + "vel " + Arrays.toString(cam.velocity) + " force " + Arrays.toString(cam.force));
        	System.out.println("For debugging purposes " + "w " + acm.rotationSpeed);
        	for(Node n : nodes) {
                n.runPhysics(dt);
        		System.out.print(" npos = " + Arrays.toString(n.position));
        	};
    		System.out.println();
        }
   
    }
	
	public static void testOrbitalCommandAroundSelection(float dt, int numIt, Node... nodes) {
		
	}
	
	
}
