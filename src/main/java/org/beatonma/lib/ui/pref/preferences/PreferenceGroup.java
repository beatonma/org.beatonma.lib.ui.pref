package org.beatonma.lib.ui.pref.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.beatonma.lib.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class PreferenceGroup extends BasePreference {
    private final static String TAG = "PreferenceGroup";
    public final static String TYPE = "group";

    @SerializedName("keymap")
    private final Map<String, Integer> mKeyMap = new HashMap<>();

    @SerializedName("preferences")
    private final List<BasePreference> mPreferences = new ArrayList<>();

    public PreferenceGroup() {
        super();
    }

    public PreferenceGroup(final Context context,
                           final Resources resources,
                           final JSONObject obj) throws JSONException {
        super(context, resources, obj);
    }

    public boolean isEmpty() {
        return mPreferences.isEmpty();
    }

    @Override
    public void setName(final String name) {
        super.setName(name);
        setPrefs(name);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void load(final SharedPreferences preferences) {
        for (final BasePreference p : mPreferences) {
            p.load(preferences);
        }
    }

    @Override
    public void save(final SharedPreferences.Editor editor) {
        for (final BasePreference p : mPreferences) {
            p.save(editor);
        }
    }

    public void load(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(
                getName(), Context.MODE_PRIVATE);
        Log.d(TAG, "Loading preferences '%s'", getName());
        load(preferences);
    }

    public void save(final Context context) {
        final SharedPreferences.Editor editor = context.getSharedPreferences(
                getName(), Context.MODE_PRIVATE).edit();
        save(editor);
        editor.apply();
    }

    public int notifyUpdate(final String key, final int value) {
        final int position = mKeyMap.get(key);
        mPreferences.get(position).update(value);
        return position;
    }

    public int notifyUpdate(final String key, final String value) {
        final int position = mKeyMap.get(key);
        mPreferences.get(position).update(value);
        return position;
    }

    public int notifyUpdate(final String key, final boolean value) {
        final int position = mKeyMap.get(key);
        mPreferences.get(position).update(value);
        return position;
    }

    public static PreferenceGroup fromJson(@NonNull final Context context, final int resourceId) {
        final PreferenceGroup group = new PreferenceGroup();
        final List<BasePreference> preferences = new ArrayList<>();

        final Resources resources = context.getResources();
        final String resourceText = readTextResource(context, resourceId);

        final JSONObject baseJson;
        final JSONArray jsonItems;

        try {
            baseJson = new JSONObject(resourceText);
            jsonItems = baseJson.getJSONArray("items");
        } catch (final JSONException e) {
            Log.e(TAG, "Unable to read JSON resource id=%d: %s", resourceId, e);
            return group;
        }

        final String prefsName = baseJson.optString("prefs", "prefs");
        for (int i = 0; i < jsonItems.length(); i++) {
            try {
                final JSONObject j = jsonItems.getJSONObject(i);
                final BasePreference p = fromJson(context, resources, j);
                if (p != null) {
                    p.setPrefs(prefsName);
                    preferences.add(p);
                }
                else {
                    Log.d(TAG, "Unable to build preference for JSON: %s", j.toString());
                }
            }
            catch (final JSONException e) {
                Log.e(TAG, "Unable to read JSON object at position %d: %s", i, e);
            }
        }

        group.setName(prefsName);
        group.setPreferences(preferences);

        return group;
    }

    public List<BasePreference> getPreferences() {
        return mPreferences;
    }

    public void setPreferences(final List<BasePreference> prefs) {
        mPreferences.clear();
        mPreferences.addAll(prefs);

        mKeyMap.clear();
        for (int i = 0; i < mPreferences.size(); i++) {
            mKeyMap.put(mPreferences.get(i).getKey(), i);
        }
    }

    /**
     * Construct a BasePreference instance from a given JSONObject
     */
    private static BasePreference fromJson(final Context context,
                                           final Resources resources,
                                           final JSONObject json) throws JSONException {
        final String type = json.optString("type", "");
        switch (type) {
            case BooleanPreference.TYPE:
                return new BooleanPreference(context, resources, json);
            case ListPreference.TYPE:
                return new ListPreference(context, resources, json);
            case ColorPreference.TYPE:
                return new ColorPreference(context, resources, json);
            case SimplePreference.TYPE:
            case "":
                return new SimplePreference(context, resources, json);
            default:
                return null;
        }
    }

    private static String readTextResource(@NonNull final Context context,  final int resourceId) {
        final InputStream stream = context.getResources().openRawResource(resourceId);
        final Writer writer = new StringWriter();

        try {
            final char[] buffer = new char[stream.available()];
            final Reader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

            return writer.toString();
        }
        catch (final IOException e) {
            Log.e(TAG, "Unable to read JSON resource id=%d: %s", resourceId, e);
        }

        return null;
    }

    @Override
    public String toString() {
        return new Gson()
                .newBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(this);
    }
}
