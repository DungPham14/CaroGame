package com.ducdung.carogame.utility;

import android.content.Context;

import com.ducdung.carogame.data.Constants;
import com.ducdung.carogame.data.enums.BoardCellState;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by framgia on 05/09/2016.
 */
public class TextUtils {
    private final static String FILE_NAME_DATA_GAME = "dataGame";
    private final static String FORMAT_FILE_TEXT = ".txt";

    public static List<String> readData(Context context, int idDataGame) {
        List<String> dataGame = new ArrayList<>();
        try {
            FileInputStream fileInputStream =
                context.openFileInput(FILE_NAME_DATA_GAME + idDataGame + FORMAT_FILE_TEXT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String data;
            StringBuilder stringBuilder = new StringBuilder();
            while ((data = reader.readLine()) != null) {
                stringBuilder.append(data);
                stringBuilder.append("\n");
                dataGame.add(data);
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataGame;
    }

    public static void writeData(Context context, int idDataGame, int[][] dataGame) {
        List<String> data = new ArrayList<>();
        for (int row = 0; row < Constants.ROW; row++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int col = 0; col < Constants.COL; col++) {
                stringBuilder.append(dataGame[row][col]);
            }
            data.add(stringBuilder.toString());
        }
        try {
            FileOutputStream fileOutputStream =
                context.openFileOutput(FILE_NAME_DATA_GAME + idDataGame + FORMAT_FILE_TEXT,
                    Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            for (int row = 0; row < Constants.ROW; row++) {
                writer.write(data.get(row));
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BoardCellState getBoardCellState(int values) {
        return BoardCellState.values()[values];
    }
}
