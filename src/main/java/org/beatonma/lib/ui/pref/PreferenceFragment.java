package org.beatonma.lib.ui.pref;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.beatonma.lib.load.AsyncTaskResult;
import org.beatonma.lib.ui.activity.BaseFragment;
import org.beatonma.lib.ui.pref.activity.ListPreferenceActivity;
import org.beatonma.lib.ui.pref.preferences.ListPreference;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public abstract class PreferenceFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<AsyncTaskResult<PreferenceGroup>> {
    private final static int LOADER_PREFS = 34659;

    private final PreferenceAdapter mAdapter = buildAdapter();

    public abstract int getPreferenceDefinitions();

    public PreferenceAdapter getAdapter() {
        return mAdapter;
    }

    public PreferenceAdapter buildAdapter() {
        return new PreferenceAdapter(this);
    }

    @Override
    public void postInit() {
        super.postInit();
        getLoaderManager().initLoader(LOADER_PREFS, null, this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case ListPreferenceActivity.REQUEST_CODE_UPDATE:
                if (resultCode == Activity.RESULT_OK) {
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
                mAdapter.notifyUpdate(lp.getKey(), lp.getSelectedDisplay());
            }
        }
    }

    @Override
    public Loader<AsyncTaskResult<PreferenceGroup>> onCreateLoader(final int id, @Nullable final Bundle args) {
        switch (id) {
            case LOADER_PREFS:
                return new PreferenceLoader.SupportPreferenceLoader(mWeakContext.get(), getPreferenceDefinitions());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<AsyncTaskResult<PreferenceGroup>> loader, final AsyncTaskResult<PreferenceGroup> result) {
        final Context context = mWeakContext.get();
        if (context != null) {
            mAdapter.setPreferences(context, result.getData());
        }
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<AsyncTaskResult<PreferenceGroup>> loader) {

    }
}
