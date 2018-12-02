package org.beatonma.lib.ui.pref.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.beatonma.lib.load.Result
import org.beatonma.lib.load.onComplete
import org.beatonma.lib.ui.activity.BaseFragment
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity
import org.beatonma.lib.ui.pref.list.AppListPreferenceActivity
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity
import org.beatonma.lib.ui.pref.preferences.AppListPreference
import org.beatonma.lib.ui.pref.preferences.ColorPreference
import org.beatonma.lib.ui.pref.preferences.ListPreference
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup
import java.lang.ref.WeakReference

private const val LOADER_PREFS = 34659
abstract class BasePreferenceFragment : BaseFragment() {

    lateinit var adapter: PreferenceAdapter
    var weakContext: WeakReference<Context>? = null

    open fun buildAdapter(): PreferenceAdapter {
        return PreferenceAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = buildAdapter()
        weakContext = WeakReference<Context>(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.onRestoreInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            ListPreferenceActivity.REQUEST_CODE_UPDATE -> onListPreferenceUpdated(data)
            SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE -> onColorPreferenceUpdated(data)
            AppListPreferenceActivity.REQUEST_CODE_UPDATE -> onAppListPreferenceUpdated(data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onListPreferenceUpdated(intent: Intent?) {
        intent?.extras?.let {
            val pref = it.getParcelable<ListPreference>(ListPreferenceActivity.EXTRA_LIST_PREFERENCE)
            adapter.notifyUpdate(pref)
        }
    }

    private fun onColorPreferenceUpdated(intent: Intent?) {
        intent?.extras?.let {
            val pref = it.getParcelable<ColorPreference>(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE)
            adapter.notifyUpdate(pref)
        }
    }

    private fun onAppListPreferenceUpdated(intent: Intent?) {
        intent?.extras?.let {
            val pref = it.getParcelable<AppListPreference>(AppListPreferenceActivity.EXTRA_APP_LIST_PREFERENCE)
            adapter.notifyUpdate(pref)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        adapter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}


/**
 * PreferenceFragment implementation that loads preference definitions from a JSON file in
 * the raw resources directory.
 */
abstract class PreferenceFragment : BasePreferenceFragment(),
        LoaderManager.LoaderCallbacks<Result<PreferenceGroup>> {

    abstract val preferenceDefinitions: Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoaderManager.getInstance(this).initLoader(LOADER_PREFS, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<PreferenceGroup>> {
        return context?.let { PreferenceLoader(it, preferenceDefinitions) }
                ?: throw IllegalStateException() // Context should never be null when this is called
    }

    override fun onLoadFinished(loader: Loader<Result<PreferenceGroup>>, result: Result<PreferenceGroup>) {
        weakContext?.get()?.let {ctx ->
            result.apply {
                onComplete { adapter.setPreferences(ctx, it) }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<PreferenceGroup>>) {

    }
}
