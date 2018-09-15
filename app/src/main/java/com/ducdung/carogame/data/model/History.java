package com.ducdung.carogame.data.model;

/**
 * Created by framgia on 31/08/2016.
 */
public class History {
    private int mId;
    private String mHumanStatus;
    private String mComputerStatus;
    private String mLevel;
    private String mTimeGame;

    public History(int id, String humanStatus, String computerStatus, String level,
                   String timeGame) {
        mId = id;
        mHumanStatus = humanStatus;
        mComputerStatus = computerStatus;
        mLevel = level;
        mTimeGame = timeGame;
    }

    public History(String humanStatus, String computerStatus, String level, String timeGame) {
        mHumanStatus = humanStatus;
        mComputerStatus = computerStatus;
        mLevel = level;
        mTimeGame = timeGame;
    }

    public int getId() {
        return mId;
    }

    public String getHumanStatus() {
        return mHumanStatus;
    }

    public String getComputerStatus() {
        return mComputerStatus;
    }

    public String getLevel() {
        return mLevel;
    }

    public String getTimeGame() {
        return mTimeGame;
    }
}
