package org.beatonma.lib.ui.pref.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

public class SimplePreference extends BasePreference {
    public final static String TYPE = "simple";

    public SimplePreference(final Context context,
                          final Resources resources,
                          final JSONObject obj) throws JSONException {

        super(context, resources, obj);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void load(final SharedPreferences preferences) {

    }

    @Override
    public void save(final SharedPreferences.Editor editor) {

    }
}
