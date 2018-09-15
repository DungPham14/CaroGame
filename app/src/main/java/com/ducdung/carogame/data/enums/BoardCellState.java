package com.ducdung.carogame.data.enums;

import java.io.Serializable;

/**
 * Created by framgia on 17/08/2016.
 */
public enum BoardCellState implements Serializable {
    EMPTY(0),
    PLAYER_X(1),
    PLAYER_O(2),
    HUMAN(3),
    MACHINE(4);
    private int mValues;

    BoardCellState(int values) {
        mValues = values;
    }

    public int getValues() {
        return mValues;
    }
}
