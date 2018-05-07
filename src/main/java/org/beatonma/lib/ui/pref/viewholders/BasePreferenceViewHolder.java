package org.beatonma.lib.ui.pref.viewholders;

import android.content.SharedPreferences;
import android.support.annotation.CallSuper;
import android.view.View;
import android.widget.TextView;

import org.beatonma.lib.log.Log;
import org.beatonma.lib.prefs.R;
import org.beatonma.lib.ui.pref.preferences.BasePreference;
import org.beatonma.lib.ui.recyclerview.BaseViewHolder;
import org.beatonma.lib.ui.style.Views;

import java.lang.ref.WeakReference;

public abstract class BasePreferenceViewHolder<T extends BasePreference>
        extends BaseViewHolder {
    private final static String TAG ="BasePrefVH";
    private WeakReference<SharedPreferences> mWeakPrefs;

    private final TextView title;
    private final TextView description;

    public BasePreferenceViewHolder(final View v) {
        super(v);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
    }

    @Override
    public void bind(final int position) {

    }

//    @CallSuper
//    public void bind(final WeakReference<SharedPreferences> weakPrefs, final T preference) {
//        mWeakPrefs = weakPrefs;
//    }

    @CallSuper
    public void bind(final WeakReference<SharedPreferences> sharedPrefs, final T preference) {
//        super.bind(sharedPrefs, preference);
        mWeakPrefs = sharedPrefs;
        setTitle(preference.getName());
        setDescription(preference.getDescription());
    }

    public void save(final T preference) {
        final SharedPreferences sharedPrefs = mWeakPrefs.get();
        if (sharedPrefs != null) {
            final SharedPreferences.Editor editor = sharedPrefs.edit();
            preference.save(editor);
            editor.apply();
        }
        else {
            Log.w(TAG, "sharedPrefs (from weakref) is null");
        }
    }

    public void setTitle(final String text) {
        title.setText(text);
    }

    public void setTitle(final int resID) {
        title.setText(resID);
    }

    public void setDescription(final String text) {
        description.setText(text);
        Views.hideIfEmpty(description);
    }

    public void setDescription(final int resID) {
        description.setText(resID);
        Views.hideIfEmpty(description);
    }
}
