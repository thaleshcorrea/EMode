package com.example.emode.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.emode.R;

public class Preferences {
    public static void setLogin(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        sp.edit().putString(context.getString(R.string.userId), userId).apply();
    }

    public static String getLogin(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.userId), "");
    }

    public static void clearLogin(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
