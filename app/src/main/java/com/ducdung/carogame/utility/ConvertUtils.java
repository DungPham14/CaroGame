package com.ducdung.carogame.utility;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.ducdung.carogame.R;

/**
 * Created by framgia on 24/08/2016.
 */
public class ConvertUtils {
    private final static int FORMAT_SECOND = 1000;
    private final static int FORMAT_MINUTE = 60;
    private final static int FORMAT_HOUR = 60;
    private final static int TIME_NULL = 0;

    public static int convertDpToPx(int dp) {
        return Math.round(
            dp * Resources.getSystem().getDisplayMetrics().densityDpi /
                DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String formatTimeGame(Context context, long timeGame) {
        int second = (int) (timeGame / FORMAT_SECOND);
        int minute = second / FORMAT_MINUTE;
        int hour = minute / FORMAT_HOUR;
        second = second - minute * FORMAT_MINUTE;
        minute = minute - hour * FORMAT_HOUR;
        if (hour > TIME_NULL) {
            return String.format(context.getResources().getString(R.string.time_game_hour_default),
                hour, minute);
        } else if (minute == TIME_NULL && hour == TIME_NULL) {
            return String.format(context.getResources()
                .getString(R.string.time_game_not_hour_not_minute_default), second);
        } else {
            return String.format(context.getResources()
                .getString(R.string.time_game_not_hour_default), minute, second);
        }
    }
}
