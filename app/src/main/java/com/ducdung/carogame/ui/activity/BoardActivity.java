package com.ducdung.carogame.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.Constants;
import com.ducdung.carogame.data.enums.GameState;
import com.ducdung.carogame.data.enums.TurnGame;
import com.ducdung.carogame.data.model.GameData;
import com.ducdung.carogame.service.BluetoothConnectionService;
import com.ducdung.carogame.ui.customview.BoardView;
import com.ducdung.carogame.ui.listener.OnGetBoardInfo;
import com.ducdung.carogame.utility.ShareUtils;
import com.ducdung.carogame.utility.ToastUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.ducdung.carogame.data.enums.GameState.UPDATE_INFO;
import static com.ducdung.carogame.data.enums.TurnGame.OPPONENT_TURN;

public class BoardActivity extends AppCompatActivity implements View.OnClickListener, Constants,
        OnGetBoardInfo, View.OnLongClickListener {
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private static final int BATTERY_LOW = 15;
    private static final String SHOWCASE_ID = "multi_player_tutorial";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectionService mBluetoothConnectionService;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private BoardView mBoardView;
    private Button mButtonPlay;
    private ImageButton mImageButtonBackHome, mImageButtonSearch, mImageButtonShowVisibility,
        mImageButtonShare;
    private LinearLayout mLinearLayoutPlayerLeft, mLinearLayoutPlayerRight;
    private TextView mTextViewWinLoseLeft, mTextViewWinLoseRight, mTextViewPlayerTurn,
        mTextViewPlayerNameLeft, mTextViewPlayerNameRight;
    private ImageView mImageViewPlayerRight;
    private String mAddress, mOpponentDevice;
    private boolean mIsSurrender, mIsEndGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        initBluetooth();
    }

    private void initViews() {
        mBoardView = new BoardView(this);
        ((HorizontalScrollView) findViewById(R.id.horizontal_scroll_board)).addView(mBoardView);
        mButtonPlay = (Button) findViewById(R.id.button_play);
        mLinearLayoutPlayerLeft = (LinearLayout) findViewById(R.id.layout_profile_player_left);
        mLinearLayoutPlayerRight = (LinearLayout) findViewById(R.id.layout_profile_player_right);
        mImageViewPlayerRight =
            (ImageView) mLinearLayoutPlayerRight.findViewById(R.id.image_player);
        mTextViewWinLoseLeft =
            (TextView) mLinearLayoutPlayerLeft.findViewById(R.id.text_player_win_lose);
        mTextViewWinLoseRight =
            (TextView) mLinearLayoutPlayerRight.findViewById(R.id.text_player_win_lose);
        mImageButtonBackHome = (ImageButton) findViewById(R.id.image_button_back);
        mImageButtonSearch = (ImageButton) findViewById(R.id.image_button_search);
        mImageButtonShowVisibility = (ImageButton) findViewById(R.id.image_button_visibility);
        mImageButtonShare = (ImageButton) findViewById(R.id.image_button_share);
        mTextViewPlayerTurn = (TextView) findViewById(R.id.text_view_player_turn);
        mTextViewPlayerNameLeft =
            (TextView) mLinearLayoutPlayerLeft.findViewById(R.id.text_player_name);
        mTextViewPlayerNameRight =
            (TextView) mLinearLayoutPlayerRight.findViewById(R.id.text_player_name);
        showTutorial();
    }

    private void showTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(SEQUENCE_DELAY_TIME);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
        sequence.setConfig(config);
        sequence.addSequenceItem(mImageButtonBackHome, getString(R.string.tutorial_button_exit),
            getString(R.string.got_it));
        sequence.addSequenceItem(mImageButtonShare, getString(R.string.tutorial_button_share),
            getString(R.string.got_it));
        sequence.addSequenceItem(mImageButtonSearch, getString(R.string.tutorial_button_search),
            getString(R.string.got_it));
        sequence.addSequenceItem(mImageButtonShowVisibility,
            getString(R.string.tutorial_button_visibility), getString(R.string.got_it));
        sequence.addSequenceItem(mLinearLayoutPlayerLeft, getString(R.string.tutorial_player_info),
            getString(R.string.got_it));
        sequence.addSequenceItem(mButtonPlay, getString(R.string.tutorial_button_play), getString
            (R.string.got_it));
        sequence.start();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtils.showToast(this, R.string.bluetooth_not_supported);
            finish();
        } else {
            initViews();
            loadSharedPreferences();
            initOnListener();
        }
    }

    private void initOnListener() {
        mImageButtonBackHome.setOnClickListener(this);
        mImageButtonSearch.setOnClickListener(this);
        mImageButtonShowVisibility.setOnClickListener(this);
        mButtonPlay.setOnClickListener(this);
        mImageButtonShare.setOnClickListener(this);
        mImageButtonBackHome.setOnLongClickListener(this);
        mImageButtonSearch.setOnLongClickListener(this);
        mImageButtonShowVisibility.setOnLongClickListener(this);
        mImageButtonShare.setOnLongClickListener(this);
        mButtonPlay.setOnLongClickListener(this);
    }

    private void showVisibility() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
            TIME_SHOW_VISIBILITY);
        startActivity(discoverableIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_button_back:
                onBackPressed();
                break;
            case R.id.image_button_undo:
                if (mBoardView.getGameState() == GameState.PLAYING) showUndoGame();
                break;
            case R.id.image_button_exit:
                if (mBoardView.getGameState() == GameState.PLAYING) showExitGame();
                break;
            case R.id.image_button_search:
                startActivityForResult(new Intent(this, DevicesListActivity.class),
                    REQUEST_CONNECT_DEVICE);
                break;
            case R.id.image_button_visibility:
                showVisibility();
                break;
            case R.id.button_play:
                handlePlayButton();
                break;
            case R.id.image_button_share:
                ShareUtils.requestShareScreenShot(this);
                break;
        }
    }

    private void handlePlayButton() {
        if (mBoardView == null) return;
        if (getConnectionState() != STATE_CONNECTED) {
            ToastUtils.showToast(this, R.string.not_connected_any_device);
            return;
        }
        if (mBluetoothConnectionService.getState() == STATE_CONNECTED) {
            mBoardView.setGameState(GameState.PLAYING);
            mButtonPlay.setVisibility(View.INVISIBLE);
            mTextViewPlayerTurn.setVisibility(View.VISIBLE);
            String winLose = String.format(Locale.getDefault(), getString(R.string.win_lose_format),
                mSharedPreferences.getInt(WIN, WIN_LOSE_DEFAULT),
                mSharedPreferences.getInt(LOSE, WIN_LOSE_DEFAULT));
            mTextViewWinLoseLeft.setText(winLose);
            mTextViewPlayerNameLeft.setText(R.string.you);
            sendGameData(
                new GameData(null, UPDATE_INFO, OPPONENT_TURN, null, winLose));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BLUETOOTH);
        else if (mBluetoothConnectionService == null)
            setupConnection();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        checkLowBattery();
        if (mBluetoothConnectionService != null &&
            mBluetoothConnectionService.getState() == STATE_NONE)
            mBluetoothConnectionService.start();
    }

    private void setupConnection() {
        mBluetoothConnectionService = new BluetoothConnectionService(this, mHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothConnectionService != null) mBluetoothConnectionService.stop();
    }

    @Override
    public void sendGameData(GameData gameData) {
        if (mBluetoothConnectionService.getState() != STATE_CONNECTED) {
            ToastUtils.showToast(this, R.string.not_connected);
            return;
        }
        mBluetoothConnectionService.write(gameData);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case STATE_CONNECTED:
                            // TODO: 15/08/2016 show state connected
                            break;
                        case STATE_CONNECTING:
                            // TODO: 15/08/2016 show state connecting
                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
                            // TODO: 15/08/2016 show state listen
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    // TODO: 15/08/2016
                    break;
                case MESSAGE_READ:
                    handleMessageRead(message);
                    break;
                case MESSAGE_DEVICE_CONNECTED:
                    mOpponentDevice = message.getData().getString(DEVICE_NAME);
                    mAddress = message.getData().getString(DEVICE_ADDRESS);
                    ToastUtils.showToast(getApplicationContext(), String.format(getString(
                        R.string.connect_to_device), mOpponentDevice));
                    break;
                case MESSAGE_TOAST:
                    ToastUtils
                        .showToast(getApplicationContext(), message.getData().getString(TOAST));
                    break;
                case MESSAGE_DISCONNECT:
                    handleConnectionLost();
                    break;
            }
        }
    };

    private void handleMessageRead(Message message) {
        try {
            ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream((byte[]) message.obj);
            ObjectInputStream objectInputStream =
                new ObjectInputStream(byteArrayInputStream);
            GameData gameData = (GameData) objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
            updateGameDataToBoard(gameData);
            mBoardView.updateGameDataToBoardView(gameData);
        } catch (IOException | ClassNotFoundException e) {
            ToastUtils.showToast(getApplicationContext(), R.string.something_error);
        }
    }

    private void handleConnectionLost() {
        if (!mBluetoothAdapter.isEnabled()) {
            ToastUtils.showToast(this, R.string.turn_on_bluetooth_to_reconnect);
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BLUETOOTH);
        } else if (!mIsSurrender && !mIsEndGame) new AlertDialog.Builder(this)
            .setTitle(R.string.lost_connection)
            .setMessage(R.string.message_lost_connection)
            .setPositiveButton(R.string.retry,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mBluetoothConnectionService != null)
                            mBluetoothConnectionService.stop();
                        mBluetoothConnectionService
                            .connect(mBluetoothAdapter.getRemoteDevice(mAddress));
                    }
                })
            .setNegativeButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show().setCanceledOnTouchOutside(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    mAddress = data.getExtras().getString(INTENT_DEVICE_ADDRESS);
                    mBluetoothConnectionService
                        .connect(mBluetoothAdapter.getRemoteDevice(mAddress));
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    ToastUtils.showToast(this, R.string.bluetooth_turned_on);
                    setupConnection();
                } else {
                    ToastUtils.showToast(this, R.string.turn_on_bluetooth_to_play);
                    finish();
                }
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
                        mIsSurrender = true;
                        sendGameData(new GameData(null, GameState.SURRENDER, TurnGame.NONE, null,
                            null));
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
        mEditor.putInt(LOSE, mSharedPreferences.getInt(LOSE, WIN_LOSE_DEFAULT) + INCREASE_DEFAULT);
        mEditor.apply();
    }

    private void loadSharedPreferences() {
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    private void showExitGame() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.message_exit_game_play)
            .setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        surrender();
                    }
                })
            .setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
            .show().setCanceledOnTouchOutside(false);
    }

    private void showUndoGame() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.message_undo_game_play)
            .setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 15/08/2016
                    }
                })
            .setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
            .show().setCanceledOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        if (mBoardView.getGameState() == GameState.PLAYING) showBackGame();
        else finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    ShareUtils.takeScreenshot(this);
                else ToastUtils.showToast(this, R.string.permission_to_share_image);
                break;
        }
    }

    private void checkLowBattery() {
        Intent batteryStatus =
            registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert batteryStatus != null;
        if (batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, Constants.DEFAULT_INTENT_VALUE) <
            BATTERY_LOW)
            new AlertDialog.Builder(this)
                .setTitle(R.string.low_battery_title)
                .setMessage(R.string.low_battery_message)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    private void updateGameDataToBoard(GameData gameData) {
        switch (gameData.getTurnGame()) {
            case OPPONENT_TURN:
                handleOpponentTurn(gameData);
                break;
            case NONE:
                handleGameStateNone(gameData);
                break;
            case YOUR_TURN:
                if (mBoardView.isPlayerX())
                    setPlayerBackground(R.drawable.surround_item_player_selected,
                        R.drawable.surround_item_player);
                else setPlayerBackground(R.drawable.surround_item_player,
                    R.drawable.surround_item_player_selected);
                break;
        }
    }

    private void handleGameStateNone(GameData gameData) {
        switch (gameData.getGameState()) {
            case NONE:
                ToastUtils.showToast(this, R.string.opponent_end_game);
                mIsEndGame = true;
                finish();
                break;
            case SURRENDER:
                ToastUtils.showToast(this, R.string.opponent_surrender);
                mIsSurrender = true;
                mEditor.putInt(WIN, mSharedPreferences.getInt(WIN, WIN_LOSE_DEFAULT) +
                    INCREASE_DEFAULT);
                mEditor.apply();
                finish();
                break;
            case UPDATE_INFO:
                mImageViewPlayerRight.setImageResource(R.drawable.img_o);
                mTextViewWinLoseRight.setText(gameData.getWinLose());
                mTextViewPlayerNameRight.setText(mOpponentDevice);
                break;
        }
    }

    private void handleOpponentTurn(GameData gameData) {
        mTextViewWinLoseLeft.setText(gameData.getWinLose());
        mTextViewPlayerNameLeft.setText(mOpponentDevice);
        String winLose = String.format(Locale.getDefault(), getString(R.string.win_lose_format),
            mSharedPreferences.getInt(WIN, WIN_LOSE_DEFAULT),
            mSharedPreferences.getInt(LOSE, WIN_LOSE_DEFAULT));
        mTextViewWinLoseRight.setText(winLose);
        mTextViewPlayerNameRight.setText(R.string.you);
        if (gameData.getGameState() == GameState.RESTART_GAME) {
            mBoardView.hideDialogRestartGame();
            updateWinLose(winLose);
            updateOpponentWinLose(gameData.getWinLose());
            ToastUtils.showToast(this, R.string.start_new_game);
            sendGameData(new GameData(null, GameState.RESTART_GAME, TurnGame.NONE, null, winLose));
        } else {
            mButtonPlay.setVisibility(View.INVISIBLE);
            mTextViewPlayerTurn.setVisibility(View.VISIBLE);
            mImageViewPlayerRight.setImageResource(R.drawable.img_o);
            sendGameData(new GameData(null, UPDATE_INFO, TurnGame.NONE, null, winLose));
        }
    }

    @Override
    public int getConnectionState() {
        return mBluetoothConnectionService.getState();
    }

    @Override
    public void setPlayerBackground(@DrawableRes int drawableRes1, @DrawableRes int drawableRes2) {
        mLinearLayoutPlayerLeft.setBackground(ContextCompat.getDrawable(this, drawableRes1));
        mLinearLayoutPlayerRight.setBackground(ContextCompat.getDrawable(this, drawableRes2));
    }

    @Override
    public void onFinishGame() {
        finish();
    }

    @Override
    public void updateWinLose(String winLose) {
        if (mBoardView.isPlayerX()) mTextViewWinLoseLeft.setText(winLose);
        else mTextViewWinLoseRight.setText(winLose);
    }

    @Override
    public void updateOpponentWinLose(String winLose) {
        if (!mBoardView.isPlayerX()) mTextViewWinLoseLeft.setText(winLose);
        else mTextViewWinLoseRight.setText(winLose);
    }

    @Override
    public void setPlayerTurnState(@StringRes int turnState) {
        mTextViewPlayerTurn.setText(getString(turnState));
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.image_button_back:
                ToastUtils.showToast(this, R.string.back_home);
                break;
            case R.id.image_button_share:
                ToastUtils.showToast(this, R.string.share_image);
                break;
            case R.id.image_button_search:
                ToastUtils.showToast(this, R.string.search_device);
                break;
            case R.id.image_button_visibility:
                ToastUtils.showToast(this, R.string.show_visibility);
                break;
            case R.id.button_play:
                ToastUtils.showToast(this, R.string.start_new_game);
                break;
        }
        return true;
    }
}
