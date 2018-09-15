package com.ducdung.carogame.ui.customview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import com.ducdung.carogame.R;
import com.ducdung.carogame.data.Constants;
import com.ducdung.carogame.data.enums.BoardCellState;
import com.ducdung.carogame.data.enums.GameState;
import com.ducdung.carogame.data.enums.Navigation;
import com.ducdung.carogame.data.enums.TurnGame;
import com.ducdung.carogame.data.model.GameData;
import com.ducdung.carogame.data.model.ItemCaro;
import com.ducdung.carogame.ui.listener.OnGetBoardInfo;
import com.ducdung.carogame.utility.ConvertUtils;
import com.ducdung.carogame.utility.SoundUtils;
import com.ducdung.carogame.utility.ToastUtils;

import java.util.Locale;

import static com.ducdung.carogame.data.enums.BoardCellState.PLAYER_O;
import static com.ducdung.carogame.data.enums.BoardCellState.PLAYER_X;

public class BoardView extends View implements Constants {
    private Paint mLinePaint, mBmpPaint;
    private Bitmap mBitmapBackground, mBitmapPlayerX, mBitmapPlayerO, mBitmapPlayerXYellow,
        mBitmapPlayerOYellow;
    protected Rect mRectTable = new Rect();
    protected Rect mRectCell = new Rect();
    private boolean mIsPlayerX = true;
    protected ItemCaro[][] mItemCaros;
    protected TurnGame mTurnGame;
    protected GameData mGameData;
    protected int mMinCol, mMinRow, mMaxCol, mMaxRow;
    private OnGetBoardInfo mOnGetBoardInfo;
    protected SharedPreferences mSharedPreferences;
    protected SharedPreferences.Editor mEditor;
    private AlertDialog mDialogRestartGame;
    protected GameState mGameState;
    protected Point mPointLastMove;
    protected int mCellSize;
    protected boolean mIsBlockTwoHeadWin;

    public BoardView(Context context) {
        super(context);
        mBitmapBackground = getResBitmap(R.drawable.img_white);
        mBitmapPlayerX = getResBitmap(R.drawable.img_x);
        mBitmapPlayerO = getResBitmap(R.drawable.img_o);
        mBitmapPlayerXYellow = getResBitmap(R.drawable.img_x_background_yellow);
        mBitmapPlayerOYellow = getResBitmap(R.drawable.img_o_background_yellow);
        mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(STROKE_WIDTH);
        mLinePaint.setStyle(Style.STROKE);
        mCellSize = ConvertUtils.convertDpToPx(Constants.CELL_SIZE_DP);
        mRectCell.set(MARGIN, MARGIN, mCellSize, mCellSize);
        mGameData = new GameData();
        mTurnGame = TurnGame.YOUR_TURN;
        mSharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mPointLastMove = new Point(DEFAULT_LAST_MOVE, DEFAULT_LAST_MOVE);
        mIsBlockTwoHeadWin = mSharedPreferences.getBoolean(TWO_HEAD_WIN_BLOCK, false);
    }

    protected void initBoard() {
        mRectTable.set(MARGIN, MARGIN, (mCellSize * COL) + MARGIN, (mCellSize * ROW) + MARGIN);
        mItemCaros = new ItemCaro[ROW][COL];
        resetBoard();
    }

    protected void resetBoard() {
        for (int i = 0; i < ROW; i++)
            for (int j = 0; j < COL; j++)
                if (mItemCaros[i][j] != null)
                    mItemCaros[i][j].setBoardCellState(BoardCellState.EMPTY);
                else mItemCaros[i][j] = new ItemCaro(i, j, BoardCellState.EMPTY);
        mGameState = GameState.NONE;
    }

    public void setGameState(GameState gameState) {
        mGameState = gameState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = mRectTable.left;
        int y = mRectTable.top;
        int xr = mRectTable.right;
        int yb = mRectTable.bottom;
        canvas.drawBitmap(mBitmapBackground,
            new Rect(MARGIN, MARGIN, mBitmapBackground.getWidth(),
                mBitmapBackground.getHeight()), mRectTable, mBmpPaint);
        canvas.drawRect(mRectTable, mLinePaint);
        for (int i = 1; i < ROW; i++)
            canvas.drawLine(x, y + i * mCellSize, xr, x + i * mCellSize, mLinePaint);
        for (int i = 1; i < COL; i++)
            canvas.drawLine(x + i * mCellSize, y, x + i * mCellSize, yb, mLinePaint);
        for (int row = 0; row < ROW; row++) {
            for (int col = 0; col < COL; col++) {
                switch (mItemCaros[row][col].getBoardCellState()) {
                    case PLAYER_X:
                    case HUMAN:
                        mRectCell
                            .offsetTo(col * mCellSize + PADDING, row * mCellSize + PADDING);
                        canvas.drawBitmap(mPointLastMove.x == row && mPointLastMove.y == col
                                ? mBitmapPlayerXYellow : mBitmapPlayerX, null, mRectCell,
                            mBmpPaint);
                        break;
                    case PLAYER_O:
                    case MACHINE:
                        mRectCell
                            .offsetTo(col * mCellSize + PADDING, row * mCellSize + PADDING);
                        canvas.drawBitmap(mPointLastMove.x == row && mPointLastMove.y == col
                                ? mBitmapPlayerOYellow : mBitmapPlayerO, null, mRectCell,
                            mBmpPaint);
                        break;
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mOnGetBoardInfo = (OnGetBoardInfo) getContext();
        initBoard();
    }

    private Bitmap getResBitmap(int bmpResId) {
        return BitmapFactory.decodeResource(getResources(), bmpResId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(COL * mCellSize, ROW * mCellSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_SCROLL:
                return true;
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                if (mOnGetBoardInfo.getConnectionState() != STATE_CONNECTED) {
                    ToastUtils.showToast(getContext(), R.string.not_connected_any_device);
                    return true;
                }
                if (mGameState != GameState.PLAYING) {
                    ToastUtils.showToast(getContext(), R.string.press_play_button);
                    return true;
                }
                if (mTurnGame == TurnGame.YOUR_TURN) {
                    int curX = (int) event.getX();
                    int curY = (int) event.getY();
                    if (curX <= mRectTable.right && curX >= mRectTable.left
                        && curY <= mRectTable.bottom && curY >= mRectTable.top) {
                        int colIndex = (curX - MARGIN) / mCellSize;
                        int rowIndex = (curY - MARGIN) / mCellSize;
                        if (mItemCaros[rowIndex][colIndex].getBoardCellState() !=
                            BoardCellState.EMPTY) return true;
                        if (mIsPlayerX) mItemCaros[rowIndex][colIndex]
                            .setBoardCellState(PLAYER_X);
                        else mItemCaros[rowIndex][colIndex]
                            .setBoardCellState(PLAYER_O);
                        invalidate();
                        if (isEndGame(mItemCaros[rowIndex][colIndex])) {
                            mGameData.updateGameData(mItemCaros[rowIndex][colIndex], mGameState,
                                mTurnGame, Navigation.NONE);
                            showEndGame();
                        } else mGameData.updateGameData(mItemCaros[rowIndex][colIndex],
                            GameState.NONE, mTurnGame, Navigation.NONE);
                        mOnGetBoardInfo.sendGameData(mGameData);
                        mTurnGame = TurnGame.OPPONENT_TURN;
                        mOnGetBoardInfo.setPlayerTurnState(R.string.opponent_turn);
                        if (isPlayerX()) mOnGetBoardInfo.setPlayerBackground(
                            R.drawable.surround_item_player,
                            R.drawable.surround_item_player_selected);
                        else mOnGetBoardInfo.setPlayerBackground(
                            R.drawable.surround_item_player_selected,
                            R.drawable.surround_item_player);
                    }
                    return true;
                } else ToastUtils.showToast(getContext(), R.string.opponent_turn);
        }
        return false;
    }

    protected void showEndGame() {
        SoundUtils.playSound(getContext(), R.raw.finish, false);
        String title = null;
        if (mIsPlayerX && mGameState == GameState.PLAYER_X_WIN) {
            title = getContext().getString(R.string.message_win_game_play);
            mEditor.putInt(WIN, mSharedPreferences.getInt(WIN, WIN_LOSE_DEFAULT) +
                INCREASE_DEFAULT);
            SoundUtils.playSound(getContext(), R.raw.win, false);
        } else if (mIsPlayerX && mGameState == GameState.PLAYER_X_LOSE) {
            title = getContext().getString(R.string.message_lose_game_play);
            mEditor.putInt(LOSE, mSharedPreferences.getInt(LOSE, WIN_LOSE_DEFAULT) +
                INCREASE_DEFAULT);
            SoundUtils.playSound(getContext(), R.raw.lose, false);
        } else if (!mIsPlayerX && mGameState == GameState.PLAYER_X_WIN) {
            title = getContext().getString(R.string.message_lose_game_play);
            mEditor.putInt(LOSE, mSharedPreferences.getInt(LOSE, WIN_LOSE_DEFAULT) +
                INCREASE_DEFAULT);
            SoundUtils.playSound(getContext(), R.raw.lose, false);
        } else if (!mIsPlayerX && mGameState == GameState.PLAYER_X_LOSE) {
            title = getContext().getString(R.string.message_win_game_play);
            mEditor.putInt(WIN, mSharedPreferences.getInt(WIN, WIN_LOSE_DEFAULT) +
                INCREASE_DEFAULT);
            SoundUtils.playSound(getContext(), R.raw.win, false);
        }
        mEditor.apply();
        mDialogRestartGame = new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setMessage(R.string.request_play_new_game)
            .setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtils.showToast(getContext(), R.string.start_new_game);
                        resetBoard();
                        invalidate();
                        handlePlayNewGame();
                    }
                })
            .setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mOnGetBoardInfo.sendGameData(new GameData(null, GameState.NONE,
                            TurnGame.NONE, null, null));
                        mOnGetBoardInfo.onFinishGame();
                    }
                }).show();
        mDialogRestartGame.setCanceledOnTouchOutside(false);
    }

    public void hideDialogRestartGame() {
        if (mDialogRestartGame != null && mDialogRestartGame.isShowing())
            mDialogRestartGame.dismiss();
    }

    private void handlePlayNewGame() {
        if (mOnGetBoardInfo.getConnectionState() == STATE_CONNECTED) {
            mGameState = GameState.PLAYING;
            String winLose = String.format(Locale.getDefault(), getContext().getString(R.string
                    .win_lose_format), mSharedPreferences.getInt(WIN, WIN_LOSE_DEFAULT),
                mSharedPreferences.getInt(LOSE, WIN_LOSE_DEFAULT));
            mOnGetBoardInfo.updateWinLose(winLose);
            mOnGetBoardInfo.sendGameData(new GameData(null, GameState.RESTART_GAME,
                TurnGame.OPPONENT_TURN, null, winLose));
        } else ToastUtils.showToast(getContext(), R.string.not_connected_any_device);
    }

    private boolean checkWin(ItemCaro itemCaro) {
        updateMinMaxRowCol(itemCaro.getPosX(), itemCaro.getPosY());
        return checkWinVertical(itemCaro) || checkWinHorizontal(itemCaro) ||
            checkWinRightDiagonalUp(itemCaro) || checkWinRightDiagonalDown(itemCaro) ||
            checkWinLeftDiagonalUp(itemCaro) || checkWinLeftDiagonalDown(itemCaro);
    }

    protected boolean isEndGame(ItemCaro itemCaro) {
        if (!checkWin(itemCaro)) return false;
        switch (itemCaro.getBoardCellState()) {
            case PLAYER_X:
            case HUMAN:
                mGameState = GameState.PLAYER_X_WIN;
                break;
            case PLAYER_O:
            case MACHINE:
                mGameState = GameState.PLAYER_X_LOSE;
                break;
        }
        return true;
    }

    public GameState getGameState() {
        return mGameState;
    }

    public void updateGameDataToBoardView(GameData gameData) {
        switch (gameData.getTurnGame()) {
            case OPPONENT_TURN:
                if (gameData.getGameState() != GameState.RESTART_GAME)
                    mIsPlayerX = !mIsPlayerX;
                else {
                    resetBoard();
                    invalidate();
                }
                mGameState = GameState.PLAYING;
                break;
            case YOUR_TURN:
                handleYourTurn(gameData);
                break;
            case NONE:
                if (gameData.getGameState() == GameState.RESTART_GAME)
                    mOnGetBoardInfo.updateOpponentWinLose(gameData.getWinLose());
                break;
        }
    }

    public boolean isPlayerX() {
        return mIsPlayerX;
    }

    private void updateMinMaxRowCol(int row, int col) {
        mMinRow = row - WIN_COUNT < 0 ? 0 : row - WIN_COUNT;
        mMaxRow = row + WIN_COUNT > ROW - 1 ? ROW - 1 : row + WIN_COUNT;
        mMinCol = col - WIN_COUNT < 0 ? 0 : col - WIN_COUNT;
        mMaxCol = col + WIN_COUNT > COL - 1 ? COL - 1 : col + WIN_COUNT;
    }

    protected boolean checkWinVertical(ItemCaro itemCaro) {
        BoardCellState boardCellState = itemCaro.getBoardCellState();
        int count, row;
        for (int col = mMinCol; col <= mMaxCol; col++) {
            row = mMinRow;
            count = DEFAULT_COUNT;
            while (count < WIN_COUNT && row <= mMaxRow) {
                if (mItemCaros[row][col].getBoardCellState() ==
                    itemCaro.getBoardCellState())
                    count++;
                else count = DEFAULT_COUNT;
                row++;
            }
            if (count == WIN_COUNT) {
                if (!mIsBlockTwoHeadWin) {
                    int borderRowMin = row - WIN_COUNT - 1;
                    return !isInside(borderRowMin, col) || !isInside(row, col) ||
                        !isBlock(borderRowMin, col, boardCellState) ||
                        !isBlock(row, col, boardCellState) ||
                        !(isBlock(borderRowMin, col, boardCellState) &&
                            isBlock(row, col, boardCellState));
                } else return true;
            }
        }
        return false;
    }

    protected boolean checkWinHorizontal(ItemCaro itemCaro) {
        BoardCellState boardCellState = itemCaro.getBoardCellState();
        int count, col;
        for (int row = mMinRow; row <= mMaxRow; row++) {
            col = mMinCol;
            count = DEFAULT_COUNT;
            while (count < WIN_COUNT && col <= mMaxCol) {
                if (mItemCaros[row][col].getBoardCellState() == boardCellState) count++;
                else count = DEFAULT_COUNT;
                col++;
            }
            if (count == WIN_COUNT) {
                if (!mIsBlockTwoHeadWin) {
                    int borderColMin = col - WIN_COUNT - 1;
                    return !isInside(row, borderColMin) || !isInside(row, col) ||
                        !isBlock(row, borderColMin, boardCellState) ||
                        !isBlock(row, col, boardCellState) ||
                        !(isBlock(row, borderColMin, boardCellState) &&
                            isBlock(row, col, boardCellState));
                } else return true;
            }
        }
        return false;
    }

    protected boolean checkWinRightDiagonalUp(ItemCaro itemCaro) {
        BoardCellState boardCellState = itemCaro.getBoardCellState();
        int count, col, row, colIndex;
        for (col = mMinCol; col <= mMaxCol; col++) {
            row = mMinRow;
            count = DEFAULT_COUNT;
            colIndex = col;
            while (count < WIN_COUNT && row <= mMaxRow && colIndex <= mMaxCol) {
                if (mItemCaros[row][colIndex].getBoardCellState() == boardCellState) count++;
                else count = DEFAULT_COUNT;
                row++;
                colIndex++;
            }
            if (count == WIN_COUNT) {
                if (!mIsBlockTwoHeadWin) {
                    int borderRowMin = row - WIN_COUNT - 1;
                    int borderColMin = colIndex - WIN_COUNT - 1;
                    return !isInside(borderRowMin, borderColMin) || !isInside(row, colIndex) ||
                        !isBlock(borderRowMin, borderColMin, boardCellState) ||
                        !isBlock(row, colIndex, boardCellState) ||
                        !(isBlock(borderRowMin, borderColMin, boardCellState) &&
                            isBlock(row, colIndex, boardCellState));
                } else return true;
            }
        }
        return false;
    }

    protected boolean checkWinRightDiagonalDown(ItemCaro itemCaro) {
        BoardCellState boardCellState = itemCaro.getBoardCellState();
        int count, col, row, rowIndex;
        for (row = mMinRow; row <= mMaxRow; row++) {
            rowIndex = row;
            count = DEFAULT_COUNT;
            col = mMinCol;
            while (count < WIN_COUNT && rowIndex <= mMaxRow && col <= mMaxCol) {
                if (mItemCaros[rowIndex][col].getBoardCellState() == boardCellState) count++;
                else count = DEFAULT_COUNT;
                rowIndex++;
                col++;
            }
            if (count == WIN_COUNT) {
                if (!mIsBlockTwoHeadWin) {
                    int borderRowMin = rowIndex - WIN_COUNT - 1;
                    int borderColMin = col - WIN_COUNT - 1;
                    return !isInside(borderRowMin, borderColMin) || !isInside(rowIndex, col) ||
                        !isBlock(borderRowMin, borderColMin, boardCellState) ||
                        !isBlock(rowIndex, col, boardCellState) ||
                        !(isBlock(borderRowMin, borderColMin, boardCellState) &&
                            isBlock(rowIndex, col, boardCellState));
                } else return true;
            }
        }
        return false;
    }

    protected boolean checkWinLeftDiagonalUp(ItemCaro itemCaro) {
        BoardCellState boardCellState = itemCaro.getBoardCellState();
        int count, col, row, colIndex;
        for (col = mMinCol; col <= mMaxCol; col++) {
            row = mMaxRow;
            count = DEFAULT_COUNT;
            colIndex = col;
            while (count < WIN_COUNT && row >= mMinRow && colIndex <= mMaxCol) {
                if (mItemCaros[row][colIndex].getBoardCellState() == boardCellState) count++;
                else count = DEFAULT_COUNT;
                row--;
                colIndex++;
            }
            if (count == WIN_COUNT) {
                if (!mIsBlockTwoHeadWin) {
                    int borderRowMax = row + WIN_COUNT + 1;
                    int borderColMin = colIndex - WIN_COUNT - 1;
                    return !isInside(borderRowMax, borderColMin) || !isInside(row, colIndex) ||
                        !isBlock(borderRowMax, borderColMin, boardCellState) ||
                        !isBlock(row, colIndex, boardCellState) ||
                        !(isBlock(borderRowMax, borderColMin, boardCellState) &&
                            isBlock(row, colIndex, boardCellState));
                } else return true;
            }
        }
        return false;
    }

    protected boolean checkWinLeftDiagonalDown(ItemCaro itemCaro) {
        BoardCellState boardCellState = itemCaro.getBoardCellState();
        int count, col, row, rowIndex;
        for (row = mMinRow; row <= mMaxRow; row++) {
            rowIndex = row;
            count = DEFAULT_COUNT;
            col = mMinCol;
            while (count < WIN_COUNT && rowIndex >= mMinRow && col <= mMaxCol) {
                if (mItemCaros[rowIndex][col].getBoardCellState() == boardCellState) count++;
                else count = DEFAULT_COUNT;
                rowIndex--;
                col++;
            }
            if (count == WIN_COUNT) {
                if (!mIsBlockTwoHeadWin) {
                    int borderRowMax = rowIndex + WIN_COUNT + 1;
                    int borderColMin = col - WIN_COUNT - 1;
                    return !isInside(borderRowMax, borderColMin) || !isInside(rowIndex, col) ||
                        !isBlock(borderRowMax, borderColMin, boardCellState) ||
                        !isBlock(rowIndex, col, boardCellState) ||
                        !(isBlock(borderRowMax, borderColMin, boardCellState) &&
                            isBlock(rowIndex, col, boardCellState));
                } else return true;
            }
        }
        return false;
    }

    protected boolean isInside(int row, int col) {
        return row >= 0 && row < ROW && col >= 0 && col < COL;
    }

    protected int getBoardCellState(int row, int col) {
        return mItemCaros[row][col].getBoardCellState().getValues();
    }

    private void handleYourTurn(GameData gameData) {
        ItemCaro itemCaro = gameData.getItemCaro();
        mPointLastMove.x = itemCaro.getPosX();
        mPointLastMove.y = itemCaro.getPosY();
        SoundUtils.playSound(getContext(), R.raw.step, false);
        ToastUtils.showToast(getContext(), R.string.your_turn);
        mOnGetBoardInfo.setPlayerTurnState(R.string.your_turn);
        GameState gameState = gameData.getGameState();
        if (gameState != GameState.NONE) mGameState = gameState;
        switch (itemCaro.getBoardCellState()) {
            case PLAYER_X:
                mItemCaros[mPointLastMove.x][mPointLastMove.y]
                    .setBoardCellState(PLAYER_X);
                break;
            case PLAYER_O:
                mItemCaros[mPointLastMove.x][mPointLastMove.y]
                    .setBoardCellState(PLAYER_O);
                break;
        }
        if (mGameState == GameState.PLAYER_X_WIN || mGameState == GameState.PLAYER_X_LOSE)
            showEndGame();
        invalidate();
        mTurnGame = TurnGame.YOUR_TURN;
    }

    protected int getBoardCellState(Point point) {
        return mItemCaros[point.x][point.y].getBoardCellState().getValues();
    }

    protected boolean isBlock(int row, int col, BoardCellState boardCellState) {
        return !(getBoardCellState(row, col) == BoardCellState.EMPTY.getValues() ||
            getBoardCellState(row, col) == boardCellState.getValues());
    }
}

