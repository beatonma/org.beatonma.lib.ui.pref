package org.beatonma.lib.ui.pref.preferences;

import android.content.Context;
import android.content.res.Resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for reading values from {@link Resources} using plaintext resource IDs
 * e.g. "@string/name"
 */
class Res {
    private final static String TAG = "PrefUtil";

    /**
     * If the text represents a @resource, return the value of that resource
     * else return the given text
     */
    public static String getString(final Context context,
                                   final Resources resources,
                                   final String text) {
        final int resId = Res.getResourceId(context, resources, text);
        return resId == 0 ? text : resources.getString(resId);
    }

    public static int getInt(final Context context,
                                    final Resources resources,
                                    final String text) {
        final int resId = getResourceId(context, resources, text);
        return resId == 0 ? 0 : resources.getInteger(resId);
    }

    public static int[] getIntArray(final Context context,
                                          final Resources resources,
                                          final String text) {
        final int resId = getResourceId(context, resources, text);
        return resId == 0 ? null : resources.getIntArray(resId);
    }

    public static String[] getStringArray(final Context context,
                                          final Resources resources,
                                          final String text) {
        final int resId = getResourceId(context, resources, text);
        return resId == 0 ? null : resources.getStringArray(resId);
    }

    public static int getResourceId(final Context context,
                                    final Resources resources,
                                    final String key) {
        final Pattern pattern = Pattern.compile("@(\\w+)/(.*)");
        final Matcher m = pattern.matcher(key);
        if (m.matches()) {
            final String resType = m.group(1);
            final String name = m.group(2);
            return resources.getIdentifier(
                    name,
                    resType,
                    context.getPackageName());
        }
        return 0;
    }
}
