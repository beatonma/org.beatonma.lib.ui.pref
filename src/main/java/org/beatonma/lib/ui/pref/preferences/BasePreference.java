package org.beatonma.lib.ui.pref.preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public abstract class BasePreference implements Serializable {
    public final static String TYPE = "base";

    private final static String PREFS = "prefs";
    private final static String KEY = "key";
    private final static String NAME = "name";
    private final static String DESCRIPTION = "description";

    @SerializedName("prefs")
    private String mPrefs;

    @SerializedName("key")
    private String mKey;

    @SerializedName("name")
    private String mName;

    @SerializedName("description")
    private String mDescription;

    public BasePreference() {

    }

    public BasePreference(final Context context,
                            final Resources resources,
                            final JSONObject obj) throws JSONException {

        setName(Res.getString(context, resources, obj.getString(NAME)));
        setKey(obj.getString(KEY));
        setDescription(Res.getString(context, resources, obj.optString(DESCRIPTION, "")));
    }


    public abstract String getType();
    public abstract void load(final SharedPreferences preferences);
    public abstract void save(final SharedPreferences.Editor editor);

    public String getPrefs() {
        return mPrefs;
    }

    public void setPrefs(final String prefsName) {
        mPrefs = prefsName;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(final String key) {
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(final String description) {
        mDescription = description;
    }

    public void update(final int value) {

    }

    public void update(final String value) {

    }

    public void update(final boolean value) {

    }

    @Override
    public String toString() {
        return "BasePreference{" +
                "mKey='" + mKey + '\'' +
                ", mName='" + mName + '\'' +
                '}';
    }
}
