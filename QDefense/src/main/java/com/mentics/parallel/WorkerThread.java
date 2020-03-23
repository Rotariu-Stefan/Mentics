package com.mentics.parallel;

import com.mentics.qd.AllData;
import com.mentics.qd.MainThread;


public class WorkerThread extends Thread {
    private final MainThread caller;
    private final int workerNum;
    public long accumulatedWaitTime = 0;
    private AllData allData;
    private CommandMgr commandMgr;
    private WorkData work;
    private float duration;
    private long stepNanoTime;
    volatile boolean ready = true;

    // Constructors //
    public WorkerThread(MainThread caller, int workerNum) {
        this.workerNum = workerNum;
        this.caller = caller;
    }

    // Public Methods //
    /**
     * Called from other thread
     */
    public void newStep(AllData allData, CommandMgr commandMgr, WorkData work, float duration, long stepNanoTime) {
        // This unsynched access is ok because we only get called here when we already finished last step and main is
        // ready for new step work.
        // So we're guaranteed to be idle at this point. Unless we implement some type of ahead processing but... that's
        // later, maybe.
        this.allData = allData;
        this.commandMgr = commandMgr;
        this.work = work;
        this.duration = duration;
        this.stepNanoTime = stepNanoTime;
        synchronized (this) {
            this.notify();
        }
        ready = true;
    }

    public void waitForNextStep() {
        if (ready) {
            ready = false;
            return;
        }
        long startWait = System.nanoTime();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endWait = System.nanoTime();
        accumulatedWaitTime += (endWait - startWait);
    }

    @Override
    public void run() {
        while (true) {
            try {
                // System.out.println(getName() + " OK");
                waitForNextStep();
                // TODO: this if shouldn't be necessary. Maybe we need to do a memory fence or something
                if (work != null) {
                    work.run(allData, commandMgr, this.duration);
                    caller.stepDone();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}