package com.ducdung.carogame.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.ducdung.carogame.data.model.History;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by framgia on 02/06/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "caroGame";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_HISTORY = "history";
    private final static String KEY_ID_HISTORY = "id";
    private final static String KEY_HUMAN_STATUS_HISTORY = "humanStatus";
    private final static String KEY_COMPUTER_STATUS_HISTORY = "computerStatus";
    private final static String KEY_LEVEL_HISTORY = "level";
    private final static String KEY_TIME_GAME_HISTORY = "timeGame";
    private final static int CHECK_ADD_TRUE = 0;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onCreateTable(db);
    }

    private void onCreateTable(SQLiteDatabase db) {
        String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_HISTORY + "("
            + KEY_ID_HISTORY + " INTEGER PRIMARY KEY," + KEY_HUMAN_STATUS_HISTORY + " TEXT,"
            + KEY_COMPUTER_STATUS_HISTORY + " TEXT," + KEY_LEVEL_HISTORY + " TEXT,"
            + KEY_TIME_GAME_HISTORY + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void addHistory(History history) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_HUMAN_STATUS_HISTORY, history.getHumanStatus());
        values.put(KEY_COMPUTER_STATUS_HISTORY, history.getComputerStatus());
        values.put(KEY_LEVEL_HISTORY, history.getLevel());
        values.put(KEY_TIME_GAME_HISTORY, history.getTimeGame());
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public boolean updateHistory(History history) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_HUMAN_STATUS_HISTORY, history.getHumanStatus());
        values.put(KEY_COMPUTER_STATUS_HISTORY, history.getComputerStatus());
        values.put(KEY_LEVEL_HISTORY, history.getLevel());
        values.put(KEY_TIME_GAME_HISTORY, history.getTimeGame());
        int checkUpdate = db.update(TABLE_HISTORY, values, KEY_ID_HISTORY + " = ?",
            new String[]{String.valueOf(history.getId())});
        db.close();
        return checkUpdate >= CHECK_ADD_TRUE;
    }

    public History getHistoryById(int id) {
        History history = null;
        String selectQuery =
            "SELECT  * FROM " + TABLE_HISTORY + " WHERE " + KEY_ID_HISTORY + "=?";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{"" + id});
        if (cursor != null && cursor.moveToFirst()) {
            history = new History(cursor.getInt(cursor.getColumnIndex(KEY_ID_HISTORY)),
                cursor.getString(cursor.getColumnIndex(KEY_HUMAN_STATUS_HISTORY)),
                cursor.getString(cursor.getColumnIndex(KEY_COMPUTER_STATUS_HISTORY)),
                cursor.getString(cursor.getColumnIndex(KEY_LEVEL_HISTORY)),
                cursor.getString(cursor.getColumnIndex(KEY_TIME_GAME_HISTORY)));
        }
        db.close();
        return history;
    }

    public int getSizeHistoryList() {
        return (int) DatabaseUtils.queryNumEntries(getWritableDatabase(), TABLE_HISTORY);
    }

    public List<History> getHistoryAll() {
        List<History> historyList = new ArrayList<History>();
        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                History history =
                    new History(cursor.getInt(cursor.getColumnIndex(KEY_ID_HISTORY)),
                        cursor.getString(cursor.getColumnIndex(KEY_HUMAN_STATUS_HISTORY)),
                        cursor.getString(cursor.getColumnIndex(KEY_COMPUTER_STATUS_HISTORY)),
                        cursor.getString(cursor.getColumnIndex(KEY_LEVEL_HISTORY)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIME_GAME_HISTORY))
                    );
                historyList.add(history);
            } while (cursor.moveToNext());
        }
        db.close();
        return historyList;
    }
}