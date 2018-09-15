package com.ducdung.carogame.ui.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.Constants;
import com.ducdung.carogame.data.database.DBHelper;
import com.ducdung.carogame.data.enums.GameState;
import com.ducdung.carogame.data.model.History;
import com.ducdung.carogame.ui.customview.SingleBoardView;
import com.ducdung.carogame.ui.listener.OnGetSingleBoardInfo;
import com.ducdung.carogame.utility.ShareUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by framgia on 19/08/2016.
 */
public class SinglePlayerActivity extends AppCompatActivity
    implements OnGetSingleBoardInfo, Constants, View.OnClickListener {
    private SharedPreferences mSharedPreferences;
    private TextView mTextViewHumanWinLose, mTextViewMachineWinLose, mTextViewPlayerTurn;
    private LinearLayout mLinearLayoutHuman, mLinearLayoutMachine;
    private SingleBoardView mSingleBoardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        initViews();
        updateWinLose();
    }

    private void initViews() {
        findViewById(R.id.image_button_search).setVisibility(View.INVISIBLE);
        findViewById(R.id.image_button_visibility).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_play).setVisibility(View.GONE);
        findViewById(R.id.image_button_exit).setVisibility(View.GONE);
        findViewById(R.id.image_button_undo).setVisibility(View.GONE);
        mTextViewPlayerTurn = (TextView) findViewById(R.id.text_view_player_turn);
        mTextViewPlayerTurn.setVisibility(View.VISIBLE);
        findViewById(R.id.image_button_back).setOnClickListener(this);
        mLinearLayoutHuman = (LinearLayout) findViewById(R.id.layout_profile_player_left);
        mLinearLayoutMachine = (LinearLayout) findViewById(R.id.layout_profile_player_right);
        mTextViewHumanWinLose = (TextView) findViewById(R.id.layout_profile_player_left)
            .findViewById(R.id.text_player_win_lose);
        mTextViewMachineWinLose =
            (TextView) mLinearLayoutMachine.findViewById(R.id.text_player_win_lose);
        ((ImageView) mLinearLayoutMachine.findViewById(R.id.image_player))
            .setImageResource(R.drawable.img_o);
        ((TextView) mLinearLayoutHuman.findViewById(R.id.text_player_name))
            .setText(getString(R.string.you));
        ((TextView) mLinearLayoutMachine.findViewById(R.id.text_player_name))
            .setText(getString(R.string.computer_easy));
        mSingleBoardView = new SingleBoardView(this, getIntent());
        ((HorizontalScrollView) findViewById(R.id.horizontal_scroll_board))
            .addView(mSingleBoardView);
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        findViewById(R.id.image_button_share).setOnClickListener(this);
        if (mSharedPreferences.getBoolean(SCREEN_ORIENTATION_PORTRAIT, true))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onFinishGame() {
        finish();
    }

    @Override
    public void updateWinLose() {
        mTextViewHumanWinLose.setText(String.format(Locale.getDefault(), getString(R.string
                .win_lose_format), mSharedPreferences.getInt(WIN_HUMAN, WIN_LOSE_DEFAULT),
            mSharedPreferences.getInt(LOSE_HUMAN, WIN_LOSE_DEFAULT)));
        mTextViewMachineWinLose.setText(String.format(Locale.getDefault(), getString(R.string
                .win_lose_format), mSharedPreferences.getInt(LOSE_HUMAN, WIN_LOSE_DEFAULT),
            mSharedPreferences.getInt(WIN_HUMAN, WIN_LOSE_DEFAULT)));
    }

    @Override
    public void setPlayerTurnState(@StringRes int turnState) {
        mTextViewPlayerTurn.setText(turnState);
    }

    @Override
    public void setPlayerBackground(@DrawableRes int drawableRes1, @DrawableRes int drawableRes2) {
        mLinearLayoutHuman.setBackground(ContextCompat.getDrawable(this, drawableRes1));
        mLinearLayoutMachine.setBackground(ContextCompat.getDrawable(this, drawableRes2));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_button_back:
                onBackPressed();
                break;
            case R.id.image_button_share:
                ShareUtils.requestShareScreenShot(this);
                break;
        }
    }

    private void showBackGame() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.message_back_game_play)
            .setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        surrender();
                        finish();
                    }
                })
            .setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
            .show().setCanceledOnTouchOutside(false);
    }

    private void surrender() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(LOSE_HUMAN,
            mSharedPreferences.getInt(LOSE_HUMAN, WIN_LOSE_DEFAULT) + INCREASE_DEFAULT);
        editor.apply();
        mSingleBoardView.setTimeGameEnd(Calendar.getInstance().getTimeInMillis());
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.addHistory(
            new History(SURRENDER, WIN, getString(R.string.game_easy),
                mSingleBoardView.getTimeGame()));
        mSingleBoardView.saveDataGame();
    }

    @Override
    public void onBackPressed() {
        if (mSingleBoardView.getGameState() == GameState.PLAYING) showBackGame();
        else finish();
    }
}
