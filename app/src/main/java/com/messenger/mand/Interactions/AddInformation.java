package com.messenger.mand.Interactions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddInformation {
    public String getTimeNow(String format) {
        Date currentDate = new Date();
        DateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        return  timeFormat.format(currentDate);
    }
}
