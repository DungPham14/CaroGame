package com.ducdung.carogame.ui.listener;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Created by framgia on 22/08/2016.
 */
public interface OnGetSingleBoardInfo {
    void onFinishGame();
    void updateWinLose();
    void setPlayerTurnState(@StringRes int turnState);
    void setPlayerBackground(@DrawableRes int drawableRes1, @DrawableRes int drawableRes2);
}
