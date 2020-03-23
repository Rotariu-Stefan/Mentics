package com.mentics.qd.commands.global;

import static com.mentics.math.vector.VectorUtil.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.mentics.parallel.CommandMgr;
import com.mentics.parallel.CommandMgrQueue;
import com.mentics.qd.AllData;
import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.datastructures.Response;
import com.mentics.qd.items.Quip;


public class AddQuipCommand extends CommandBase {
    private String quipName = null;
    private float[] position = new float[3];
    private List<Object> responses = null;

    private Quip createdQuip;
    private boolean done = false;

    public Quip getCreatedQuip() {
        return createdQuip;
    }

    public AddQuipCommand(InputStream input) {
        super(CommandMgrQueue.STORY);
        Map<String, Object> quipData = (Map<String, Object>)new Yaml().load(input);

        quipName = (String)quipData.get("name");
        List<Object> poslist = (List<Object>)quipData.get("position");
        for (int i = 0; i < 3; i++)
            position[i] = ((Integer)poslist.get(i)).floatValue();
        responses = (List<Object>)quipData.get("responses");
        if (responses == null) responses = new ArrayList<>();
    }

    public AddQuipCommand(float[] position, String quipName) {
        super(CommandMgrQueue.STORY);

        set(this.position, position);
        this.quipName = quipName;
        responses = new ArrayList<>();
    }

    @Override
    public void run(AllData allData, CommandMgr cmds, float duration) {
        if (done) return;
        Quip quip = allData.queueNewQuip(quipName);
        set(quip.position, this.position);

        createdQuip = quip;
        done = true;
        System.out.println("Created Quip: " + createdQuip.id);
        if (cmds != null) cmds.remove(this);
    }
}
