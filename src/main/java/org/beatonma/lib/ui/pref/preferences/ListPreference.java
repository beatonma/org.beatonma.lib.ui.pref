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
//    @SerializedName("values")
//    private final int mValuesListResourceId;

    // Resource ID for the selected UI value
    @SerializedName("selected")
    private int mSelectedValue;

    // Text that represents the selected item
    @SerializedName("selected_display")
    private String mSelectedDisplay;


    public ListPreference(final Context context, final Resources resources, final JSONObject obj) throws JSONException {
        super(context, resources, obj);

        // This should point to a string array
        mDisplayListResourceId = Res.getResourceId(context, resources, obj.optString(DISPLAY_LIST_ID, ""));

        // This should point to an integer array
//        mValuesListResourceId = Res.getResourceId(context, resources, obj.optString(VALUES_LIST_ID, ""));

        // An integer, or the resource ID for an integer
        final String selectedRaw = obj.optString(SELECTED_VALUE_ID, "0");

        try {
            mSelectedValue = Integer.valueOf(selectedRaw);
        }
        catch (final NumberFormatException e) {
            mSelectedValue = Res.getInt(context, resources, selectedRaw); // Defaults to zero
        }
//        mSelectedValue = Res.getInt(context, resources, obj.optString(SELECTED_VALUE_ID, ""));

        final String[] display = resources.getStringArray(mDisplayListResourceId);
        mSelectedDisplay = display[mSelectedValue];
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void load(final SharedPreferences preferences) {
        mSelectedValue = preferences.getInt(getKey(), mSelectedValue);
        mSelectedDisplay = preferences.getString(getDisplayKey(), mSelectedDisplay);
    }

    @Override
    public void save(final SharedPreferences.Editor editor) {
        editor.putInt(getKey(), mSelectedValue);
        editor.putString(getDisplayKey(), mSelectedDisplay);
    }

    @Override
    public void update(final int value) {
        super.update(value);
        mSelectedValue = value;
    }

    @Override
    public void update(final String value) {
        super.update(value);
        mSelectedDisplay = value;
    }

    public int getDisplayListResourceId() {
        return mDisplayListResourceId;
    }

//    public int getValuesListResourceId() {
//        return mValuesListResourceId;
//    }

    public int getSelectedValue() {
        return mSelectedValue;
    }

    public String getSelectedDisplay() {
        return mSelectedDisplay;
    }

    public String getDisplayKey() {
        return getKey() + "_display";
    }

    @Override
    public String toString() {
        return "ListPreference{" +
                "mName=" + getName() +
                ", mKey=" + getKey() +
                ", mDisplayListResourceId=" + mDisplayListResourceId +
//                ", mValuesListResourceId=" + mValuesListResourceId +
                ", mSelectedValue=" + mSelectedValue +
                '}';
    }
}
