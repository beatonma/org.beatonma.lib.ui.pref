package org.beatonma.lib.ui.pref.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

public class ListPreference extends BasePreference {
    public final static String TYPE = "list_single";

    private final static String DISPLAY_LIST_ID = "items";
    private final static String VALUES_LIST_ID = "values";
    private final static String SELECTED_VALUE_ID = "default";

    // Resource ID for the list that will represent the list in UI
    @SerializedName("items")
    private final int mDisplayListResourceId;

    // Resource ID for the list that will represent the list in back end
    @SerializedName("values")
    private final int mValuesListResourceId;

    // Resource ID for the selected UI value
    @SerializedName("selected")
    private int mSelectedValue;


    public ListPreference(final Context context, final Resources resources, final JSONObject obj) throws JSONException {
        super(context, resources, obj);

        // This should point to a string array
        mDisplayListResourceId = Res.getResourceId(context, resources, obj.optString(DISPLAY_LIST_ID, ""));

        // This should point to an integer array
        mValuesListResourceId = Res.getResourceId(context, resources, obj.optString(VALUES_LIST_ID, ""));

        // An integer, or the resource ID for an integer
        mSelectedValue = Res.getInt(context, resources, obj.optString(SELECTED_VALUE_ID, ""));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void load(final SharedPreferences preferences) {
        mSelectedValue = preferences.getInt(getKey(), 0);
    }

    @Override
    public void save(final SharedPreferences.Editor editor) {
        editor.putInt(getKey(), mSelectedValue);
    }

    @Override
    public void update(final int value) {
        super.update(value);
        mSelectedValue = value;
    }

    public int getDisplayListResourceId() {
        return mDisplayListResourceId;
    }

    public int getValuesListResourceId() {
        return mValuesListResourceId;
    }

    public int getSelectedValue() {
        return mSelectedValue;
    }

    @Override
    public String toString() {
        return "ListPreference{" +
                "mName=" + getName() +
                ", mKey=" + getKey() +
                ", mDisplayListResourceId=" + mDisplayListResourceId +
                ", mValuesListResourceId=" + mValuesListResourceId +
                ", mSelectedValue=" + mSelectedValue +
                '}';
    }
}
