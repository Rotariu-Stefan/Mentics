package com.mentics.qd.commands;


public abstract class CommandBase implements Command {
    protected final int type;

    /**
     * id is set when a command is queued, so it cannot be final.
     */
    protected long id;


    public CommandBase(int type) {
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(CommandBase other) {
        return id < other.id ? -1 : (id > other.id ? 1 : 0);
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof CommandBase) {
            return id == ((CommandBase)other).id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int)id;
    }
}
