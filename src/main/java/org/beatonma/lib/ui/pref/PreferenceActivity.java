package org.beatonma.lib.ui.pref;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import androidx.annotation.Nullable;

import org.beatonma.lib.load.AsyncTaskResult;
import org.beatonma.lib.ui.activity.BaseActivity;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;
import org.beatonma.lib.ui.pref.activity.ListPreferenceActivity;
import org.beatonma.lib.ui.pref.preferences.ListPreference;

public abstract class PreferenceActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<AsyncTaskResult<PreferenceGroup>> {
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
        getLoaderManager().initLoader(LOADER_PREFS, null, this);
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
                mAdapter.notifyUpdate(lp.getKey(), lp.getSelectedValue());
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
    public void onLoadFinished(final Loader<AsyncTaskResult<PreferenceGroup>> loader, final AsyncTaskResult<PreferenceGroup> result) {
        mAdapter.setPreferences(this, result.getData());
    }

    @Override
    public void onLoaderReset(final Loader<AsyncTaskResult<PreferenceGroup>> loader) {

    }
}
