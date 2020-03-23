package com.mentics.util;

import com.mentics.qd.CMD;
import com.mentics.qd.QuipNebula;

import testing.WorldTest;

/**
 * FPS calcuation stuff. Irrelevant to processing
 */
public class FPS {
    public long lastCheckpoint;
    public long lastFrameNumber;
    public long workerAccumulatedWaitTime;
    public long frameNumber;
    public long mainAccumulatedWaitTime;

    public void calculateFPS() {
        if (frameNumber % 100 == 0) {
            long newTime = System.nanoTime();
            long duration = newTime - lastCheckpoint;
            lastCheckpoint = newTime;
            long numFrames = frameNumber - lastFrameNumber;
            /*System.out.println("checkpoint duration: " + (duration / 1000000000.0) + ", main avg wait: " +
                               ((double)mainAccumulatedWaitTime / numFrames) + ", worker avg wait: " +
                               ((double)workerAccumulatedWaitTime / numFrames) + ", frame #" + frameNumber +
                               ", frames per second: " + ((double)numFrames / (double)duration) * 1000000000.0);*/
            lastFrameNumber = frameNumber;
            CMD.showInfo();
            System.out.println(++QuipNebula.time);
        }
        frameNumber++;
    }
}
