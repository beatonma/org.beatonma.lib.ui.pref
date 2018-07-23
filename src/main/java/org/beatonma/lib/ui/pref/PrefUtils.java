package org.beatonma.lib.ui.pref;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

@Deprecated
public class PrefUtils {
    private final static String DEFAULT_PREFS = "prefs";

    public static SharedPreferences get(@NonNull final Context context) {
        return context.getApplicationContext().getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor edit(@NonNull final Context context) {
        return context.getApplicationContext().getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit();
    }

    public static SharedPreferences get(@NonNull final Context context, final String name) {
        if (name == null) {
            return get(context);
        }
        return context.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor edit(@NonNull final Context context, final String name) {
        if (name == null) {
            return edit(context);
        }
        return context.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
    }
}
