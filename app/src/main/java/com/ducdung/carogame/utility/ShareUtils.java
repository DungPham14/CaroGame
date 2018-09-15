package com.ducdung.carogame.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.View;

import com.ducdung.carogame.data.Constants;
import com.ducdung.carogame.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by framgia on 26/08/2016.
 */
public class ShareUtils implements Constants {
    public static void requestShareScreenShot(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_EXTERNAL_STORAGE);
        else ShareUtils.takeScreenshot(activity);
    }

    public static void takeScreenshot(Activity activity) {
        Date now = new Date();
        DateFormat.format(DATE_FORMAT, now);
        try {
            String path = String.format(IMAGE_PATH_FORMAT, Environment.getExternalStorageDirectory()
                .toString(), now);
            View rootView = activity.getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
            rootView.setDrawingCacheEnabled(false);
            File imageFile = new File(path);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream);
            outputStream.flush();
            outputStream.close();
            ShareUtils.shareImage(activity.getApplicationContext(), imageFile);
        } catch (IOException e) {
            ToastUtils.showToast(activity.getApplicationContext(), R.string.something_error);
        }
    }

    public static void shareImage(Context context, File imageFile) {
        if (imageFile == null) {
            ToastUtils.showToast(context, R.string.something_error);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(SHARE_IMAGE_TYPE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        if (intent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(intent);
        else ToastUtils.showToast(context, R.string.no_app_can_share_image);
    }
}
