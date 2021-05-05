package com.example.notesapplication.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {
    public static String getCurrentDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm aa", Locale.getDefault());
        String time = simpledateformat.format(calendar.getTime());
        return time;
    }
}
