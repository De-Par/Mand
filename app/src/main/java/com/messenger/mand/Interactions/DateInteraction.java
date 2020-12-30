package com.messenger.mand.Interactions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateInteraction {

    public static String getTimeNow() {
        java.util.Date currentDate = new java.util.Date();
        DateFormat timeFormat = new SimpleDateFormat("yyyy_d_MMM_HH:mm:ss", Locale.getDefault());
        return  timeFormat.format(currentDate);
    }
}
