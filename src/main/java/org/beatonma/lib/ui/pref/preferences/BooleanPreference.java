package org.beatonma.lib.ui.pref.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.annotations.SerializedName;

import org.beatonma.lib.log.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class BooleanPreference extends SimplePreference {
    private final static String TAG = "BoolPref";

    public final static String TYPE = "boolean";

    private final static String CHECKED = "checked";

    @SerializedName("checked")
    private boolean mChecked;

    @SerializedName("description_selected")
    private String mSelectedDescription;

    @SerializedName("description_unselected")
    private String mUnselectedDescription;


    public BooleanPreference(final Context context,
                             final Resources resources,
                             final JSONObject obj) throws JSONException {
        super(context, resources, obj);
        setChecked(obj.optBoolean(CHECKED, false));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void load(final SharedPreferences preferences) {
        mChecked = preferences.getBoolean(getKey(), mChecked);
        Log.d(TAG, "loading %s=%b", getKey(), mChecked);
    }

    @Override
    public void save(final SharedPreferences.Editor editor) {
        editor.putBoolean(getKey(), mChecked);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(final boolean checked) {
        mChecked = checked;
    }

    public String getSelectedDescription() {
        return mSelectedDescription;
    }

    public void setSelectedDescription(final String selectedDescription) {
        mSelectedDescription = selectedDescription;
    }

    public String getUnselectedDescription() {
        return mUnselectedDescription;
    }

    public void setUnselectedDescription(final String unselectedDescription) {
        mUnselectedDescription = unselectedDescription;
    }

    @Override
    public String toString() {
        return "BooleanPreference{" + '\'' +
                ", mName='" + getName() + '\'' +
                ", mKey='" + getKey() + '\'' +
                ", mChecked=" + mChecked + '\'' +
                ", mSelectedDescription='" + mSelectedDescription + '\'' +
                ", mUnselectedDescription='" + mUnselectedDescription + '\'' +
                '}';
    }
}
