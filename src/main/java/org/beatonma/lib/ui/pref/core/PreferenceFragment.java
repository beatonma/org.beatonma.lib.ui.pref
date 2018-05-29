package org.beatonma.lib.ui.pref.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.beatonma.lib.load.AsyncResult;
import org.beatonma.lib.ui.activity.BaseFragment;
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity;
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity;
import org.beatonma.lib.ui.pref.preferences.ColorPreference;
import org.beatonma.lib.ui.pref.preferences.ListPreference;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public abstract class PreferenceFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<AsyncResult<PreferenceGroup>> {
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
        LoaderManager.getInstance(this).initLoader(LOADER_PREFS, null, this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ListPreferenceActivity.REQUEST_CODE_UPDATE:
                onListPreferenceUpdated(data);
                break;
            case SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE:
                onColorPreferenceUpdated(data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onListPreferenceUpdated(final Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final ListPreference pref = (ListPreference) extras.getSerializable(ListPreferenceActivity.EXTRA_LIST_PREFERENCE);
            if (pref != null) {
                mAdapter.notifyUpdate(pref.getKey(), pref.getSelectedValue());
                mAdapter.notifyUpdate(pref.getKey(), pref.getSelectedDisplay());
            }
        }
    }

    private void onColorPreferenceUpdated(final Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            final ColorPreference pref = (ColorPreference) extras.getSerializable(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE);
            if (pref != null) {
                mAdapter.notifyUpdate(pref.getKey(), pref);
            }
        }
    }


    @Override
    public Loader<AsyncResult<PreferenceGroup>> onCreateLoader(final int id, @Nullable final Bundle args) {
        switch (id) {
            case LOADER_PREFS:
                return new PreferenceLoader(mWeakContext.get(), getPreferenceDefinitions());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<AsyncResult<PreferenceGroup>> loader, final AsyncResult<PreferenceGroup> result) {
        final Context context = mWeakContext.get();
        if (context != null) {
            mAdapter.setPreferences(context, result.getData());
        }
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<AsyncResult<PreferenceGroup>> loader) {

    }
}
