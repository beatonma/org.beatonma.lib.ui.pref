package org.beatonma.lib.ui.pref;

import android.content.Context;
import android.support.annotation.Nullable;

import org.beatonma.lib.load.AsyncTaskResult;
import org.beatonma.lib.load.BaseAsyncTaskLoader;
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;

public class PreferenceLoader extends BaseAsyncTaskLoader<PreferenceGroup> {
    private final int mResourceID;

    public PreferenceLoader(final Context context, final int resourceID) {
        super(context);
        mResourceID = resourceID;
    }

    @Override
    public AsyncTaskResult<PreferenceGroup> loadInBackground() {
        final AsyncTaskResult.Builder<PreferenceGroup> result = AsyncTaskResult.getBuilder();
        final PreferenceGroup prefs = PreferenceGroup.fromJson(getContext(), mResourceID);
        prefs.load(getContext());
        result.success(prefs);
        return result;
    }

    @Override
    protected void onReleaseResources(final AsyncTaskResult data) {

    }

    public static class SupportPreferenceLoader extends SupportBaseAsyncTaskLoader<PreferenceGroup> {
        private final int mResourceID;

        public SupportPreferenceLoader(final Context context, final int resourceID) {
            super(context);
            mResourceID = resourceID;
        }

        @Nullable
        @Override
        public AsyncTaskResult<PreferenceGroup> loadInBackground() {
            final AsyncTaskResult.Builder<PreferenceGroup> result = AsyncTaskResult.getBuilder();
            final PreferenceGroup prefs = PreferenceGroup.fromJson(getContext(), mResourceID);

            if (prefs.isEmpty()) {
                result.failure("Loaded preferences are empty - please check your definitions!");
                return result;
            }

            prefs.load(getContext());
            result.success(prefs);
            return result;
        }

        @Override
        protected void onReleaseResources(final AsyncTaskResult data) {

        }
    }
}
