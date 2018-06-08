package org.beatonma.lib.ui.pref.core;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import org.beatonma.lib.core.kotlin.extensions.ViewsKt;
import org.beatonma.lib.log.Log;
import org.beatonma.lib.prefs.R;
import org.beatonma.lib.ui.pref.preferences.BasePreference;
import org.beatonma.lib.ui.recyclerview.BaseViewHolder;

import java.lang.ref.WeakReference;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @CallSuper
    public void bind(@Nullable final WeakReference<SharedPreferences> sharedPrefs, @NonNull final T preference) {
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

    public void setDescription(@Nullable final String text) {
        description.setText(text);
        ViewsKt.hideIfEmpty(description);
    }

    public void setDescription(final int resID) {
        description.setText(resID);
        ViewsKt.hideIfEmpty(description);
    }
}
