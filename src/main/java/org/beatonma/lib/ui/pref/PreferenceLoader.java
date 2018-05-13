package org.beatonma.lib.ui.pref;

import android.content.Context;

import org.beatonma.lib.load.AsyncResult;
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;

/**
 * Asynchronously parse a preferences JSON file and return a PreferenceGroup instance
 */
public class PreferenceLoader extends SupportBaseAsyncTaskLoader<PreferenceGroup> {
    private final int mResourceID;

    public PreferenceLoader(final Context context, final int resourceID) {
        super(context);
        mResourceID = resourceID;
    }

    @Override
    public AsyncResult<PreferenceGroup> loadInBackground() {
        final AsyncResult.Builder<PreferenceGroup> result = AsyncResult.getBuilder();
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
    protected void onReleaseResources(final AsyncResult data) {

    }
}
