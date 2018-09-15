package com.ducdung.carogame.ui.customview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.database.DBHelper;
import com.ducdung.carogame.data.enums.BoardCellState;
import com.ducdung.carogame.data.enums.GameState;
import com.ducdung.carogame.data.enums.TurnGame;
import com.ducdung.carogame.data.model.History;
import com.ducdung.carogame.data.model.ItemCaro;
import com.ducdung.carogame.ui.listener.OnGetSingleBoardInfo;
import com.ducdung.carogame.utility.ConvertUtils;
import com.ducdung.carogame.utility.SoundUtils;
import com.ducdung.carogame.utility.TextUtils;
import com.ducdung.carogame.utility.ToastUtils;


import java.util.Calendar;
import java.util.List;

/**
 * Created by framgia on 19/08/2016.
 */
public class SingleBoardView extends BoardView {
    private OnGetSingleBoardInfo mOnGetSingleBoardInfo;
    private long mTimeGameStart, mTimeGameEnd;
    private DBHelper mDBHelper;
    private boolean mIsNewGame;
    private int mIdDataGame;

    public SingleBoardView(Context context, Intent intent) {
        super(context);
        mTurnGame = TurnGame.HUMAN;
        mTimeGameStart = Calendar.getInstance().getTimeInMillis();
        mDBHelper = new DBHelper(getContext());
        mIsNewGame = intent.getAction().equals(ACTION_NEW_GAME);
        mIdDataGame = intent.getIntExtra(ID_DATA_GAME, ID_DATA_GAME_DEFAULT);
    }

    public void setTimeGameEnd(long timeGameEnd) {
        mTimeGameEnd = timeGameEnd;
    }

    public String getTimeGame() {
        return ConvertUtils.formatTimeGame(getContext(), mTimeGameEnd - mTimeGameStart);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        mOnGetSingleBoardInfo = (OnGetSingleBoardInfo) getContext();
        mOnGetSingleBoardInfo.setPlayerTurnState(R.string.your_turn);
        initBoard();
        if (!mIsNewGame) {
            mTurnGame = TurnGame.NONE;
            loadDataBoard();
        }
    }

    private void loadDataBoard() {
        List<String> dataGame = TextUtils.readData(getContext(), mIdDataGame);
        mItemCaros = new ItemCaro[ROW][COL];
        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                mItemCaros[row][col] = new ItemCaro(row, col,
                    TextUtils.getBoardCellState(
                        Integer.parseInt(String.valueOf(dataGame.get(row).charAt(col)))));
            }
        }
        mGameState = GameState.NONE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_SCROLL:
                return true;
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                if (curX <= mRectTable.right && curX >= mRectTable.left
                    && curY <= mRectTable.bottom && curY >= mRectTable.top) {
                    int colIndex = (curX - MARGIN) / mCellSize;
                    int rowIndex = (curY - MARGIN) / mCellSize;
                    if (mItemCaros[rowIndex][colIndex].getBoardCellState() !=
                        BoardCellState.EMPTY) return true;
                    mGameState = GameState.PLAYING;
                    if (mTurnGame == TurnGame.HUMAN) {
                        SoundUtils.playSound(getContext(), R.raw.step, false);
                        mOnGetSingleBoardInfo.setPlayerTurnState(R.string.your_turn);
                        mOnGetSingleBoardInfo.setPlayerBackground(R.drawable
                            .surround_item_player_selected, R.drawable.surround_item_player);
                        mItemCaros[rowIndex][colIndex].setBoardCellState(BoardCellState.HUMAN);
                        mPointLastMove.set(rowIndex, colIndex);
                        if (!isEndGame(mItemCaros[rowIndex][colIndex]))
                            mTurnGame = TurnGame.MACHINE;
                    }
                    if (mTurnGame == TurnGame.MACHINE) {
                        SoundUtils.playSound(getContext(), R.raw.step, false);
                        mOnGetSingleBoardInfo.setPlayerTurnState(R.string.opponent_turn);
                        mOnGetSingleBoardInfo.setPlayerBackground(R.drawable
                            .surround_item_player, R.drawable.surround_item_player_selected);
                        mPointLastMove = computerPlay(mItemCaros);
                        mItemCaros[mPointLastMove.x][mPointLastMove.y]
                            .setBoardCellState(BoardCellState.MACHINE);
                        mTurnGame = TurnGame.HUMAN;
                    }
                    invalidate();
                    if (isEndGame(mItemCaros[mPointLastMove.x][mPointLastMove.y])) {
                        showEndGame();
                        mTurnGame = TurnGame.NONE;
                    }
                    return true;
                } else ToastUtils.showToast(getContext(), R.string.opponent_turn);
        }
        return false;
    }

    public void saveDataGame() {
        int[][] dataGame = new int[ROW][COL];
        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                dataGame[row][col] = mItemCaros[row][col].getBoardCellState().getValues();
            }
        }
        TextUtils.writeData(getContext(), mDBHelper.getSizeHistoryList(), dataGame);
    }

    @Override
    protected void showEndGame() {
        mTimeGameEnd = Calendar.getInstance().getTimeInMillis();
        SoundUtils.playSound(getContext(), R.raw.finish, false);
        History history = null;
        String title = null;
        if (mGameState == GameState.PLAYER_X_WIN) {
            title = getContext().getString(R.string.message_win_game_play);
            mEditor.putInt(WIN_HUMAN, mSharedPreferences.getInt(WIN_HUMAN, WIN_LOSE_DEFAULT)
                + INCREASE_DEFAULT);
            SoundUtils.playSound(getContext(), R.raw.win, false);
            history =
                new History(WIN, LOSE, getContext().getString(R.string.game_easy), getTimeGame());
        } else if (mGameState == GameState.PLAYER_X_LOSE) {
            title = getContext().getString(R.string.message_lose_game_play);
            mEditor.putInt(LOSE_HUMAN, mSharedPreferences.getInt(LOSE_HUMAN, WIN_LOSE_DEFAULT)
                + INCREASE_DEFAULT);
            SoundUtils.playSound(getContext(), R.raw.lose, false);
            history =
                new History(LOSE, WIN, getContext().getString(R.string.game_easy), getTimeGame());
        }
        mDBHelper.addHistory(history);
        mEditor.apply();
        saveDataGame();
        new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setMessage(R.string.request_play_new_game)
            .setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame();
                    }
                }
            )
            .setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mOnGetSingleBoardInfo.onFinishGame();
                    }
                }
            ).show().setCanceledOnTouchOutside(false);
    }

    private void resetGame() {
        resetBoard();
        invalidate();
        mTurnGame = TurnGame.HUMAN;
        mOnGetSingleBoardInfo.updateWinLose();
    }

    public Point computerPlay(ItemCaro[][] itemCaros) {
        int bestScore = 0;
        Point bestCell = new Point();
        int maxRow = mMaxRow + 1 >= ROW ? ROW - 1 : mMaxRow + 1;
        int maxCol = mMaxCol + 1 >= COL ? COL - 1 : mMaxCol + 1;
        int minRow = mMinRow - 1 < 0 ? 0 : mMinRow - 1;
        int minCol = mMinCol - 1 < 0 ? 0 : mMinCol - 1;
        if (maxRow == ROW - 1 || maxCol == COL - 1 || minRow == 0 || minCol == 0) {
            minRow = 0;
            maxRow = maxRow + 1 >= ROW - 1 ? ROW - 1 : maxRow + 1;
            minCol = 0;
            maxCol = maxCol + 1 >= COL - 1 ? COL - 1 : maxCol + 1;
        }
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                if (getBoardCellState(row, col) == BoardCellState.EMPTY.getValues()) {
                    int score = evaluate(itemCaros[row][col], row, col);
                    if (score >= bestScore) {
                        bestScore = score;
                        bestCell.x = row;
                        bestCell.y = col;
                        if (bestScore >= BEST_SCORE) return bestCell;
                    }
                }
            }
        }
        return bestCell;
    }

    private int evaluate(ItemCaro itemCaro, int row, int col) {
        int countBonus, totalPoint = 0;
        itemCaro.setBoardCellState(BoardCellState.MACHINE);
        for (int h = 0; h < LENGTH; h++) {
            Point pointUp, pointDown;
            pointUp = checkLine(row, col, DELTA_ROW[h], DELTA_COL[h]);
            pointDown = checkLine(row, col, -DELTA_ROW[h], -DELTA_COL[h]);
            int distance;
            if (pointUp.x != pointDown.x) distance = Math.abs(pointUp.x - pointDown.x);
            else distance = Math.abs(pointUp.y - pointDown.y);
            if (distance <= WIN_COUNT) {
                if (!(checkBoardCellState(pointUp) && checkBoardCellState(pointDown))) distance--;
                if (!(checkBoardCellState(pointUp) || checkBoardCellState(pointDown)))
                    distance = OUT_OF_DISTANCE;
            }
            switch (distance) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    totalPoint += ATTACK_SCORE[distance - 1];
                    break;
                default:
                    totalPoint += ATTACK_SCORE[5];
                    break;
            }
            if (totalPoint >= BEST_SCORE) {
                itemCaro.setBoardCellState(BoardCellState.EMPTY);
                return totalPoint;
            }
        }
        countBonus = 0;
        for (int h = 0; h < LENGTH; h++) {
            Point pointUp, pointUpMax;
            pointUp = checkLine(row, col, DELTA_ROW[h], DELTA_COL[h]);
            pointUpMax = pointUp;
            if (checkBoardCellState(pointUpMax))
                pointUpMax = checkLine(pointUpMax.x, pointUpMax.y, DELTA_ROW[h], DELTA_COL[h]);
            Point pointDown, pointDownMax;
            pointDown = checkLine(row, col, -DELTA_ROW[h], -DELTA_COL[h]);
            if (checkBoardCellState(pointUpMax) && checkBoardCellState(pointDown)) {
                if (Math.abs(pointUpMax.x - pointDown.x) >= WIN_COUNT + 1 ||
                    Math.abs(pointUpMax.y - pointDown.y) >= WIN_COUNT + 1) {
                    countBonus++;
                    if (countBonus == NUMBER_BONUS) {
                        totalPoint += BONUS[0];
                        break;
                    }
                }
            }
            pointDownMax = pointDown;
            if (checkBoardCellState(pointDownMax)) pointDownMax =
                checkLine(pointDownMax.x, pointDownMax.y, DELTA_ROW[h], DELTA_COL[h]);
            if (checkBoardCellState(pointDownMax) && checkBoardCellState(pointUp)) {
                if (Math.abs(pointDownMax.x - pointUp.x) >= WIN_COUNT + 1 ||
                    Math.abs(pointDownMax.y - pointUp.y) >= WIN_COUNT + 1) {
                    countBonus++;
                    if (countBonus == NUMBER_BONUS) {
                        totalPoint += BONUS[0];
                        break;
                    }
                }
            }
        }
        itemCaro.setBoardCellState(BoardCellState.HUMAN);
        for (int h = 0; h < LENGTH; h++) {
            Point pointUp, pointDown;
            pointUp = checkLine(row, col, DELTA_ROW[h], DELTA_COL[h]);
            pointDown = checkLine(row, col, -DELTA_ROW[h], -DELTA_COL[h]);
            int distance;
            if (pointUp.x != pointDown.x) distance = Math.abs(pointUp.x - pointDown.x);
            else distance = Math.abs(pointUp.y - pointDown.y);
            if (distance <= WIN_COUNT) {
                if (!(checkBoardCellState(pointUp) && checkBoardCellState(pointDown))) distance--;
                if (!isInside(pointUp.x, pointUp.y) || getBoardCellState(pointUp) ==
                    BoardCellState.MACHINE.getValues() ||
                    !isInside(pointDown.x, pointDown.y) ||
                    getBoardCellState(pointDown) == BoardCellState.MACHINE.getValues()) continue;
            }
            switch (distance) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    totalPoint += DEFENSE_SCORE[distance - 1];
                    break;
                default:
                    totalPoint += DEFENSE_SCORE[5];
                    break;
            }
        }
        countBonus = 0;
        for (int h = 0; h < LENGTH; h++) {
            Point pointUp, pointUpMax;
            pointUp = checkLine(row, col, DELTA_ROW[h], DELTA_COL[h]);
            pointUpMax = pointUp;
            if (checkBoardCellState(pointUpMax))
                pointUpMax = checkLine(pointUpMax.x, pointUpMax.y, DELTA_ROW[h], DELTA_COL[h]);
            Point pointDown, pointDownMax;
            pointDown = checkLine(row, col, -DELTA_ROW[h], -DELTA_COL[h]);
            if (checkBoardCellState(pointUpMax) && checkBoardCellState(pointDown))
                if (Math.abs(pointUpMax.x - pointDown.x) >= WIN_COUNT + 1 ||
                    Math.abs(pointUpMax.y - pointDown.y) >= WIN_COUNT + 1) {
                    countBonus++;
                    if (countBonus == NUMBER_BONUS) {
                        totalPoint += BONUS[1];
                        break;
                    }
                }
            pointDownMax = pointDown;
            if (checkBoardCellState(pointDownMax)) pointDownMax =
                checkLine(pointDownMax.x, pointDownMax.y, DELTA_ROW[h], DELTA_COL[h]);
            if (checkBoardCellState(pointDownMax) && checkBoardCellState(pointUp)) {
                if (Math.abs(pointDownMax.x - pointUp.x) >= WIN_COUNT + 1 ||
                    Math.abs(pointDownMax.y - pointUp.y) >= WIN_COUNT + 1) {
                    countBonus++;
                    if (countBonus == NUMBER_BONUS) {
                        totalPoint += BONUS[1];
                        break;
                    }
                }
            }
        }
        itemCaro.setBoardCellState(BoardCellState.EMPTY);
        return totalPoint;
    }

    private boolean checkBoardCellState(Point point) {
        return isInside(point.x, point.y) &&
            getBoardCellState(point) == BoardCellState.EMPTY.getValues();
    }

    private Point checkLine(int row, int col, int dx, int dy) {
        int x = row, y = col;
        do {
            x += dx;
            y += dy;
        } while (isInside(x, y) && getBoardCellState(x, y) == getBoardCellState(row, col));
        return (new Point(x, y));
    }
}
