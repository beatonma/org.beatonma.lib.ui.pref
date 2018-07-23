package org.beatonma.lib.ui.pref.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.beatonma.lib.load.Result
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
abstract class PreferenceFragment : BaseFragment(),
        LoaderManager.LoaderCallbacks<Result<PreferenceGroup>> {

    lateinit var adapter: PreferenceAdapter
    var weakContext: WeakReference<Context>? = null

    abstract val preferenceDefinitions: Int

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
        LoaderManager.getInstance(this).initLoader(LOADER_PREFS, null, this)
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
            val pref = it.getSerializable(ListPreferenceActivity.EXTRA_LIST_PREFERENCE) as ListPreference
            adapter.notifyUpdate(pref)
        }
    }

    private fun onColorPreferenceUpdated(intent: Intent?) {
        intent?.extras?.let {
            val pref = it.getSerializable(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE) as ColorPreference
            adapter.notifyUpdate(pref)
        }
    }

    private fun onAppListPreferenceUpdated(intent: Intent?) {
        intent?.extras?.let {
            val pref = it.getSerializable(AppListPreferenceActivity.EXTRA_APP_LIST_PREFERENCE) as AppListPreference
            adapter.notifyUpdate(pref)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<PreferenceGroup>> {
        return context?.let { PreferenceLoader(it, preferenceDefinitions) }
                ?: throw IllegalStateException() // Context should never be null when this is called
    }

    override fun onLoadFinished(loader: Loader<Result<PreferenceGroup>>, result: Result<PreferenceGroup>) {
        weakContext?.get()?.let {
            if (result.isComplete) {
                val data = result.data
                if (data != null) {
                    adapter.setPreferences(it, data)
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<PreferenceGroup>>) {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        adapter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}
