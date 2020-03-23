package com.mentics.physics;

import static com.mentics.math.vector.VectorUtil.ZERO;
import static com.mentics.math.vector.VectorUtil.isSame;
import static com.mentics.math.vector.VectorUtil.set;
import static com.mentics.math.vector.VectorUtil.subtractInto;
import static com.mentics.qd.items.ItemUtil.EPS_CALC;
import static com.mentics.qd.items.ItemUtil.EPS_COMPARE;
import static com.mentics.qd.items.ItemUtil.updatePhysics;
import static com.mentics.physics.ThrustControl.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.mentics.math.vector.VectorUtil;

public class ThrustControlTest {
	
	private static final int NUM_TESTS = 5000;
    private static final int MAX_ITERATIONS = 5000;
    Random r = new Random(1);
	
	@Test
    public void testRandom3DCase() {
        final float timeStep = 0.30316028f;
        final float maxAcc = 105.28079f;
        final float[] target = new float[] { -18.766922f, 47.91671f, 38.84202f };
        final float[] v0 = new float[] { -0.65870583f, 2.2176862f, 2.7735167f };

        // final float timeStep = 0.18375902f;
        // final float maxAcc = 65.93395f;
        // final float[] target = new float[] { -33.89567f, -1.374042f, -32.19452f };
        // final float[] v0 = new float[] { -0.12485027f, 0.40397048f, -3.7342505f };

        final float[] pos = new float[3];
        final float[] vel = new float[3];
        set(vel, v0);
        System.out.println("{ " + Arrays.toString(pos) + " , " + Arrays.toString(vel) + " }");
        for (int i = 0; i < 1000; i++) {
            final float[] relPos = new float[3];
            set(relPos, target);
            subtractInto(relPos, pos);

            if (VectorUtil.isSame(relPos, ZERO) && isSame(vel, ZERO)) {
                break;
            }

            final float[] acc = moveToward3D(new float[3], relPos, vel, maxAcc, timeStep);
            updatePhysics(pos, vel, acc, timeStep);
            System.out.println("{ " + Arrays.toString(pos) + " , " + Arrays.toString(vel) + " , " +
                               Arrays.toString(acc) + "  }");
        }

        final String msg =
                " failed test: " + timeStep + ", " + maxAcc + ", " + Arrays.toString(target) + ", " +
                        Arrays.toString(v0);
        Assert.assertTrue("position " + msg, isSame(target, pos));
        Assert.assertTrue("velocity " + msg, isSame(vel, ZERO, EPS_COMPARE));
    }

    @Test
    public void testRandom3D() {
        int i = 0;
        for (int test = 0; test < NUM_TESTS; test++) {
            final float timeStep = 0.01f + r.nextFloat() * .5f;
            final float maxAcc = 50f + r.nextFloat() * 100;
            final float[] target = VectorUtil.randomize(new float[3], 100f);
            final float[] v0 = VectorUtil.randomize(new float[3], 10f);

            final float[] pos = new float[3];
            final float[] vel = new float[3];
            set(vel, v0);

            for (i = 0; i < MAX_ITERATIONS; i++) {
                final float[] relPos = new float[3];
                set(relPos, target);
                subtractInto(relPos, pos);

                if (isSame(target, pos, EPS_COMPARE) && isSame(relPos, ZERO, EPS_COMPARE) &&
                    isSame(vel, ZERO, EPS_COMPARE)) {
                    break;
                }

                final float[] acc = moveToward3D(new float[3], relPos, vel, maxAcc, timeStep);
                updatePhysics(pos, vel, acc, timeStep);
                if (test == 25) {
                    System.out.println("{ " + Arrays.toString(pos) + " , " + Arrays.toString(vel) + " , " +
                                       Arrays.toString(acc) + "  }");
                }
                // if (isSame(acc, ZERO, EPS_CALC)) {
                // break;
                // }
            }

            final String msg =
                    "Test " + test + " failed after " + i + " iterations: " + timeStep + ", " + maxAcc + ", " +
                            Arrays.toString(target) + ", " + Arrays.toString(v0);
            Assert.assertTrue(" position " + msg, isSame(target, pos, EPS_COMPARE));
            Assert.assertTrue("velocity " + msg, isSame(vel, ZERO, EPS_COMPARE));
        }
    }

    @Test
    public void testSimple0RelVel() {
        final float duration = 0.1f;
        final float maxAcc = 10;
        final float targetPos = 10;

        float pos = 0;
        float vel = 0f;
        for (int i = 0; i < 50; i++) {
            final float relPos = targetPos - pos;
            final float acc = moveToward1D(relPos, vel, maxAcc, duration);
            // System.out.println("[ " + pos + " , " + vel + " , " + acc + " ]");
            pos += vel * duration + 0.5f * acc * duration * duration;
            vel += acc * duration;
        }
    }

    @Test
    public void testRandom() {
        for (int test = 0; test < NUM_TESTS; test++) {
            final float timeStep = 0.01f + r.nextFloat();
            final float maxAcc = 5f + r.nextFloat() * 100;
            final float target = r.nextFloat() * 100f - 10f;
            final float v0 = r.nextFloat() * 100 - 20f;// - 100;

            if (test == 310) {
                testFail(test, timeStep, maxAcc, target, v0);
            }
            //
            // float pos = 0;
            // float vel = v0;
            // for (int i = 0; i < MAX_ITERATIONS; i++) {
            // final float relPos = target - pos;
            // if (Math.abs(relPos) < EPS_CALC && Math.abs(vel) < EPS_CALC) {
            // break;
            // }
            // final float direction = relPos == 0f ? 1f : Math.signum(relPos);
            // // Convert direction so moveToward is always positive
            // final float acc = moveToward1D(relPos * direction, vel * direction, maxAcc, timeStep) * direction;
            // System.out.println("[ " + pos + " , " + vel + " , " + acc + " ]");
            // pos += vel * timeStep + 0.5f * acc * timeStep * timeStep;
            // vel += acc * timeStep;
            // }
            //
            // final String msg = " failed test: " + timeStep + ", " + maxAcc + ", " + target + ", " + v0;
            // Assert.assertEquals("position " + msg, target, pos, EPS_CALC);
            // Assert.assertEquals("velocity " + msg, 0f, vel, EPS_CALC);
        }
    }

    // in failed tests 1-3 was line final float direction = Math.signum(relPos);
    // not like in general case final float direction = relPos == 0f ? 1f : Math.signum(relPos);
    // but data in trace are same almost

    @Test
    public void testFailedCase1() {
        testFail(0, 0.81994194f, 50.175858f, 5.5106115f, 7.835233f);
    }

    @Test
    public void testFailedCase2() {
        testFail(0, 0.06697577f, 96.053024f, 4.277205f, 79.116295f);
    }

    @Test
    public void testFailedCase3() {
        testFail(0, 0.014212796f, 50.683563f, 71.35579f, 61.245255f);
    }

    @Test
    public void testFailedCase4() {
        testFail(0, 0.014302203f, 53.27833f, 81.83223f, 66.36492f);
    }

    @Test
    public void testFailedCase5() {
        testFail(0, 0.010757634f, 6.5847025f, 77.73468f, 63.552826f);
    }

    @Test
    public void testFailedCase6() {
        testFail(0, 1f, 1f, 0.5f, 1f);
    }

    @Test
    public void testFailedCase7() {
        testFail(0, 1f, 1f, 11.5f, 3f);
    }

    @Test
    public void testFailedCase8() {
        testFail(0, 1f, 1f, 0.5f, 3f);
    }

    private static void testFail(int testNum, float timeStep, float maxAcc, float target, float v0) {
        float pos = 0;
        float vel = v0;
        int i = 0;
        for (; i < MAX_ITERATIONS; i++) {
            final float relPos = target - pos;
            if (Math.abs(relPos) < EPS_CALC && Math.abs(vel) < EPS_CALC) {
                break;
            }
            final float direction = relPos == 0f ? 1f : Math.signum(relPos);
            // Convert direction so moveToward is always positive
            final float acc = moveToward1D(relPos * direction, vel * direction, maxAcc, timeStep) * direction;
            pos += vel * timeStep + 0.5f * acc * timeStep * timeStep;
            vel += acc * timeStep;
            System.out.println("[ " + pos + " , " + vel + " , " + acc + " ]");
        }
        final String msg =
                " failed test #" + testNum + " at iteration " + i + ": " + timeStep + ", " + maxAcc + ", " + target +
                        ", " + v0;
        Assert.assertEquals("position " + msg, target, pos, EPS_CALC);
        Assert.assertEquals("velocity " + msg, 0f, vel, EPS_CALC);
    }

    // help test for Shell Command
    @Test
    public void testFailedCaseSC1() {
        testFail(0, 1f, 10f, 10f, 0f);
    }

    @Test
    public void test3DFailedCaseSC() {
        float[] target = { 22, 0, 0 };
        float[] pos = { 0, 0, 0 };
        float[] vel = { 0, 0, 0 };
        float[] relPos = { 0, 0, 0 };
        float[] acc = { 0, 0, 0 };
        float dt = 0.1f;
        for (int i = 0; i < 100; i++) {
            for (int axis = 0; axis < 3; axis++) {
                relPos[axis] = target[axis] - pos[axis];
            }
            moveToward3D(acc, relPos, vel, 10, dt);
            for (int axis = 0; axis < 3; axis++) {
                pos[axis] += vel[axis] * dt + 0.5f * acc[axis] * dt * dt;
                vel[axis] += acc[axis] * dt;
            }
            System.out.println(" pos " + Arrays.toString(pos) + " vel " + Arrays.toString(vel) + " acc " +
                               Arrays.toString(acc));
        }
        System.out.println("result accel" + "[ " + acc[0] + " , " + acc[1] + " , " + acc[2] + " ]");
    }

    @Test
    public void test3DFailedCaseSCTop() {
        float[] acc = new float[3];
        moveToward3D(acc, new float[] { 0, 0.6f, 0 }, new float[] { 0, -10f, 0 }, 10f, 0.2f);
        System.out.println(Arrays.toString(acc));
    }

    @Test
    public void test3DFailedCaseSCFTT() {
        float[] target = { 0, 0.6f, 0 };
        float[] pos = { 0, 0, 0 };
        float[] vel = { 0, -10, 0 };
        float[] relPos = { 0, 0, 0 };
        float[] acc = { 0, 0, 0 };
        float dt = 0.2f;
        for (int i = 0; i < 100; i++) {
            for (int axis = 0; axis < 3; axis++) {
                relPos[axis] = target[axis] - pos[axis];
            }
            moveToward3D(acc, relPos, vel, 10, dt);
            for (int axis = 0; axis < 3; axis++) {
                pos[axis] += vel[axis] * dt + 0.5f * acc[axis] * dt * dt;
                vel[axis] += acc[axis] * dt;
            }
            System.out.println(" pos " + Arrays.toString(pos) + " vel " + Arrays.toString(vel) + " acc " +
                               Arrays.toString(acc));
        }
        System.out.println("result accel" + "[ " + acc[0] + " , " + acc[1] + " , " + acc[2] + " ]");
    }

    @Test
    public void test3DFailedCaseSCTop2() {
        float[] acc = new float[3];
        moveToward3D(acc, new float[] { 0, -6f, 0 }, new float[] { 0, -10f, 0 }, 10f, 0.2f);
        System.out.println(Arrays.toString(acc));
    }

    @Test
    public void testFailedCaseFor3D1() {
        testFail(0, 0.2f, 10f, 0.6f, -10f);
    }

    @Test
    public void testFailedCaseFor3D2() {
        testFail(0, 0.2f, 10f, -6f, -10f);
    }

    @Test
    public void testFail3DSampleBrake() {
        testFail3D(new float[] { 0, 5, 0 }, new float[] { 0, 10, 0 }, 1, 10);
    }

    @Test
    public void testFail3DSampleBrake2D() {
        testFail3D(new float[] { 5f / (float)Math.sqrt(2f), 5f / (float)Math.sqrt(2f), 0 },
                new float[] { 10f / (float)Math.sqrt(2f), 10f / (float)Math.sqrt(2f), 0 }, 1, 10);
    }

    @Test
    public void testFail3DSampleBrake2DLitleOppositeVel() {
        testFail3D(new float[] { 5f / (float)Math.sqrt(2f), 5f / (float)Math.sqrt(2f), 0 },
                new float[] { -10f / (float)Math.sqrt(2f), -10f / (float)Math.sqrt(2f), 0 }, 1, 10);
    }

    @Test
    public void testFailedCaseForOpVel1Dfrom3D() {
        testFail(0, 0.2f, 10, 5, -10);
    }

    @Test
    public void testFail3DOverMaxAcc() {
        testFail3D(new float[] { 0, 0.80000377f, 0 }, new float[] { 0, 4f, 0 }, 0.2f, 10f);
    }

    @Test
    public void testFail1Dfrom3Dover() {
        testFail(0, 0.2f, 10, 0.80000377f, 4f);
    }

    @Test
    public void testFail1Dfrom3DoverQQQ() {
        testFail(0, 0.2f, 10, 0.80000f, 4f);
    }

    @Test
    public void testFail1Dfrom3DoverQQQ9() {
        testFail(0, 0.2f, 10, 0.90000f, 4f);
    }
    
    @Test
    public void testFail1Dfrom3DoverQQQ10() {
        testFail(0, 0.2f, 10, 1.00000f, 4f);
    }
    
    @Test
    public void testFail1Dfrom3DoverQQQ1Xcss() {
        testFail(0, 0.2f, 10, 1.20000f, 4f);
    }

    public static void testFail3D(float[] target, float[] vel, float dt, float amax) {
        float[] pos = { 0, 0, 0 };
        float[] relPos = { 0, 0, 0 };
        float[] acc = { 0, 0, 0 };
        for (int i = 0; i < 100; i++) {
            for (int axis = 0; axis < 3; axis++) {
                relPos[axis] = target[axis] - pos[axis];
            }
            moveToward3D(acc, relPos, vel, amax, dt);
            for (int axis = 0; axis < 3; axis++) {
                pos[axis] += vel[axis] * dt + 0.5f * acc[axis] * dt * dt;
                vel[axis] += acc[axis] * dt;
            }
            System.out.println(" pos " + Arrays.toString(pos) + " vel " + Arrays.toString(vel) + " acc " +
                               Arrays.toString(acc));
        }
    }
}
