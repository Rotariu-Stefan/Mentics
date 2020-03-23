package com.mentics.qd;

import java.util.List;

import testing.WorldTest;

import com.mentics.parallel.WorkerThread;
import com.mentics.qd.items.Camera;
import com.mentics.qd.items.Quip;
import com.mentics.util.FPS;


public class MainThread {
    private FPS fps = new FPS();

    private WorkerThread[] workers;

    volatile int workerDoneCount = 0;
    volatile boolean readyForWorkerProcessing = false;

    float emaDur = 0.001f;
    private int dragX0, dragY0;
    private float mouseSensitivity = 1f;

    // Constructors //

    public MainThread() {

        workers = new WorkerThread[QuipNebula.numWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new WorkerThread(this, i);
            workers[i].setDaemon(true);
            workers[i].start();
        }
    }

    // Public Methods //

    public void run() {
        fps.lastCheckpoint = System.nanoTime();

        long prevStepNanoTime;
        long stepNanoTime = System.nanoTime() - 1000;
        float duration;
        float durationToUse;

        while (true) {
            // float timeControl = 0.8f;// moved to QuipNebula main
            prevStepNanoTime = stepNanoTime;
            stepNanoTime = System.nanoTime();
            duration = (stepNanoTime - prevStepNanoTime) / 1000000000f;
            emaDur += (duration - emaDur) * (2.0 / 61.0);
            durationToUse = emaDur * QuipNebula.timeControl;
            // TODO: we should put in a check or something to ensure duration
            // never gets above a certain amount
            // because then it might mess things up. Also, it would mean there
            // is a bug somewhere...
            // though we need to handle it if the user pauses the process or
            // something.
            // pausing the game needs to handle time appropriately, as well.
            // TODO resolve concurrency issues here

            if (QuipNebula.gameState < 2) {
            	
                //fps.calculateFPS();
                QuipNebula.window.display();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {

                if (!QuipNebula.paused) {
                    fps.workerAccumulatedWaitTime = 0;
                    for (int i = 0; i < workers.length; i++) {
                        fps.workerAccumulatedWaitTime += workers[i].accumulatedWaitTime;
                        workers[i].newStep(QuipNebula.allData, QuipNebula.commandMgr, QuipNebula.allData.getWorkFor(i),
                                durationToUse, stepNanoTime);
                    }
                }

                //fps.calculateFPS();                
                QuipNebula.window.display();

                if (!QuipNebula.paused) {
                    waitForWorkers();
                    
                    QuipNebula.allData.allQuips.forEach(qp -> qp.checkTriggers());		// check all Quip triggers                    
                    QuipNebula.allData.checkTriggers();									// check world triggers
                    
                    QuipNebula.allData.processQueue();									// process item management stuff
                    QuipNebula.commandMgr.processQueue();								// process world commands
                    QuipNebula.allData.allQuips.forEach(qp -> qp.cmds.processQueue());	// process all Quip commands
                }

                // TODO: maybe put in throttle?
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void runWorldTest() {
        fps.lastCheckpoint = System.nanoTime();

        long prevStepNanoTime;
        long stepNanoTime = System.nanoTime() - 1000;
        float duration;
        float durationToUse;

        while (true) {
            prevStepNanoTime = stepNanoTime;
            stepNanoTime = System.nanoTime();
            duration = (stepNanoTime - prevStepNanoTime) / 1000000000f;
            emaDur += (duration - emaDur) * (2.0 / 61.0);
            durationToUse = emaDur * QuipNebula.timeControl;

            fps.workerAccumulatedWaitTime = 0;
            for (int i = 0; i < workers.length; i++) {
                fps.workerAccumulatedWaitTime += workers[i].accumulatedWaitTime;
                workers[i].newStep(QuipNebula.allData, QuipNebula.commandMgr, QuipNebula.allData.getWorkFor(i),
                        durationToUse, stepNanoTime);
            }

            fps.calculateFPS();

            waitForWorkers();
            // Process all Quip commands.
            List<Quip> quips = QuipNebula.allData.allQuips;
            quips.forEach(qp -> qp.cmds.processQueue());
            QuipNebula.commandMgr.processQueue();
            QuipNebula.allData.processQueue();

            // TODO: maybe put in throttle?
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }        
    }

    /**
     * This is called from other threads. It's the callback from the workers when they have finished a step.
     */
    public void stepDone() {
        workerDoneCount++;
        if (workerDoneCount == workers.length) {
            if (readyForWorkerProcessing) {
                synchronized (this) {
                    this.notify();
                }
            }
        }
    }

    // Local Methods //

    private void waitForWorkers() {
        readyForWorkerProcessing = true;
        if (workerDoneCount < workers.length) {
            long start = System.nanoTime();
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace(); // To change body of catch statement
                    // use File | Settings | File
                    // Templates.
                }
            }
            fps.mainAccumulatedWaitTime += System.nanoTime() - start;
        }
    }

    /*public void exitThreads() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].destroy();
        }
    }

    public void startDrag(int x, int y) {
        dragX0 = x;
        dragY0 = y;
    }

    public void drag(int x, int y) {

        int dx = x - dragX0;
        int dy = -(y - dragY0);

        float ratioX = (float)dx / QuipNebula.window.getWidth();
        float ratioY = (float)dy / QuipNebula.window.getHeight();

        float angleTheta = (float)(ratioY * Math.PI / 2 * mouseSensitivity);
        float anglePhi = (float)(ratioX * Math.PI / 2 * mouseSensitivity);

        // Camera camera = (Camera) ((GraphAllData) allData).items.get(0);
        Camera camera = QuipNebula.allData.camera;
        camera.rotate(angleTheta, anglePhi);
        camera.movedManually = true;

        dragX0 = x;
        dragY0 = y;
    }*/
}