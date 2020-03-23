package com.mentics.qd.commands.quips.groups;

import com.mentics.qd.commands.CommandBase;
import com.mentics.qd.items.Quip;

import static com.mentics.qd.datastructures.ArrayTreeUtil.idFromPath;


public abstract class GroupCommandBase extends CommandBase {
    public final short[] group;
    protected final Quip quip;

    public GroupCommandBase(int type, Quip quip, short[] group) {
        super(type);
        this.quip = quip;
        this.group = group;
    }

    @Override
    public int compareTo(CommandBase other) {
        if (other instanceof GroupCommandBase) {
            long id1 = idFromPath(this.group);
            long id2 = idFromPath(((GroupCommandBase)other).group);
            if (id1 > id2) {
                return 1;
            } else if (id1 < id2) {
                return -1;
            } else /* if (id1 == id2) */ {
                return super.compareTo(other);
            }
        } else {
            return 1;
        }
    }
}
