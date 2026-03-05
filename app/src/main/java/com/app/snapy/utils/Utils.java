package com.app.snapy.utils;

import android.content.Context;

import com.app.snapy.R;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static final String GOOGLE_CLIENT_ID = "653878376900-qrea6e4u7q8eps4s56ku598gsl4i46i1.apps.googleusercontent.com";
    private static final int MAX_LENGTH = 20;

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }//end of method

    public static String convertPublishTime(Date past) {
        Date now = new Date();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
        if (seconds < 60) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    public static String getUserName(String email) {
        String string = email;
        String[] userName = string.split("@");
        String name = userName[0];
        String cap = name.substring(0, 1).toUpperCase() + name.substring(1);
        return cap;

    }

    public static boolean isAdmin(Context context) {
        return Session.CURRENT_USER.getUid().equals(context.getResources().getString(R.string.admin_uid));
    }

}
