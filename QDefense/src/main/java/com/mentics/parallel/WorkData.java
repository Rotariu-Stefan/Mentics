package com.mentics.parallel;

import com.mentics.qd.AllData;


public interface WorkData {
    WorkData NOOP = new WorkData() {
        @Override
        public void run(AllData allData, CommandMgr commandMgr, float duration) {}
    };

    void run(AllData allData, CommandMgr commandMgr, float duration);
}
