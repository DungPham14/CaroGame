package com.ducdung.carogame.data.model;

import com.ducdung.carogame.data.enums.BoardCellState;

import java.io.Serializable;

/**
 * Created by framgia on 17/08/2016.
 */
public class ItemCaro implements Serializable{
    private int mPosX;
    private int mPosY;
    private BoardCellState mBoardCellState;

    public ItemCaro(int posX, int posY, BoardCellState boardCellState) {
        mPosX = posX;
        mPosY = posY;
        mBoardCellState = boardCellState;
    }

    public int getPosX() {
        return mPosX;
    }

    public int getPosY() {
        return mPosY;
    }

    public BoardCellState getBoardCellState() {
        return mBoardCellState;
    }

    public void setBoardCellState(BoardCellState boardCellState) {
        mBoardCellState = boardCellState;
    }
}
