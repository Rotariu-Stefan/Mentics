package com.mentics.qd.items;

public interface Energetic {
    float getEnergy();

    void setEnergy(float value);

    void generateEnergy(float duration);
}
