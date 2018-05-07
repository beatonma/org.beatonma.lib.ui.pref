package org.beatonma.lib.ui.pref.activity;

import org.beatonma.lib.ui.activity.PopupActivity;

public abstract class BasePreferencePopupActivity<T> extends PopupActivity {
    public abstract void save(final T obj);
}
