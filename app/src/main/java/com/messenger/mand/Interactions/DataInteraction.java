package com.messenger.mand.Interactions;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class DataInteraction {

    public static String getTimeNow() {
        String pattern = "yyyy',' d MMM',' HH:mm";
        java.util.Date currentDate = new java.util.Date();
        DateFormat timeFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return timeFormat.format(currentDate);
    }

    public static String getFilePath(Uri uri) {
        File file = new File(uri.getPath());
        return file.getPath();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception ignored) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static byte[] convertDrawableToByteArr(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }
}
