package org.beatonma.lib.ui.pref.core;

import android.content.Intent;
import android.os.Bundle;

import org.beatonma.lib.load.Result;
import org.beatonma.lib.ui.activity.BaseActivity;
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity;
import org.beatonma.lib.ui.pref.preferences.ListPreference;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

@Deprecated
/*
 * TODO convert to simple container for {@link PreferenceFragment}
 */
public abstract class _PreferenceActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Result<PreferenceGroup>> {
    private final static int LOADER_PREFS = 34657;

    private final PreferenceAdapter mAdapter = new PreferenceAdapter();

    /**
     * @return The resource ID for the JSON preference definitions you want to show.
     */
    public abstract int getPreferenceDefinitions();

    public PreferenceAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LoaderManager.getInstance(this).initLoader(LOADER_PREFS, null, this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case ListPreferenceActivity.REQUEST_CODE_UPDATE:
                if (resultCode == RESULT_OK) {
                    onListPreferenceUpdated(data);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onListPreferenceUpdated(final Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final ListPreference lp = (ListPreference) extras.getSerializable(ListPreferenceActivity.EXTRA_LIST_PREFERENCE);
            if (lp != null) {
//                mAdapter.notifyUpdate(lp.getKey(), lp.getSelectedValue());
            }
        }
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_PREFS:
                new PreferenceLoader(this, getPreferenceDefinitions());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Result<PreferenceGroup>> loader, final Result<PreferenceGroup> result) {
        mAdapter.setPreferences(this, result.getData());
    }

    @Override
    public void onLoaderReset(final Loader<Result<PreferenceGroup>> loader) {

    }
}
