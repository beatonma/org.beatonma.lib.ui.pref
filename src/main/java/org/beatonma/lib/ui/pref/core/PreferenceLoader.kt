package org.beatonma.lib.ui.pref.core

import android.content.Context
import org.beatonma.lib.load.Result
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup

/**
 * Asynchronously parse a preferences JSON file and return a PreferenceGroup instance
 */
class PreferenceLoader(context: Context, private val mResourceID: Int) : SupportBaseAsyncTaskLoader<PreferenceGroup>(context) {

    override fun loadInBackground(): Result<PreferenceGroup> {
        val result = Result.getBuilder<PreferenceGroup>()
        val prefs: PreferenceGroup
        try {
             prefs = PreferenceGroup.fromJson(context, mResourceID)
        }
        catch (e: Exception) {
            result.failure(e)
            return result
        }

        if (prefs.isEmpty) {
            result.failure("Loaded preferences are empty - please check your definitions!")
            return result
        }

        prefs.load(context)
        result.success(prefs)
        return result
    }

    override fun onReleaseResources(data: Result<PreferenceGroup>?) {

    }
}
