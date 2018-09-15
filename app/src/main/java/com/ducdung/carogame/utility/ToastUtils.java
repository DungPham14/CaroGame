package com.ducdung.carogame.utility;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Created by framgia on 11/08/2016.
 */
public class ToastUtils {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, @StringRes int stringRes) {
        Toast.makeText(context, context.getString(stringRes), Toast.LENGTH_SHORT).show();
    }
}
