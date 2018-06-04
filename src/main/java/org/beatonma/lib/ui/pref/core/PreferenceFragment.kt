package org.beatonma.lib.ui.pref.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.beatonma.lib.load.AsyncResult
import org.beatonma.lib.ui.activity.BaseFragment
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity
import org.beatonma.lib.ui.pref.preferences.ColorPreference
import org.beatonma.lib.ui.pref.preferences.ListPreference
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup
import java.lang.ref.WeakReference

abstract class PreferenceFragment : BaseFragment(), LoaderManager.LoaderCallbacks<AsyncResult<PreferenceGroup>> {
    companion object {
        private const val LOADER_PREFS = 34659
    }

    val adapter = buildAdapter()
    var weakContext: WeakReference<Context>? = null

    abstract val preferenceDefinitions: Int

    fun buildAdapter(): PreferenceAdapter {
        return PreferenceAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weakContext = WeakReference<Context>(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoaderManager.getInstance(this).initLoader(LOADER_PREFS, null, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            ListPreferenceActivity.REQUEST_CODE_UPDATE -> onListPreferenceUpdated(data)
            SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE -> onColorPreferenceUpdated(data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onListPreferenceUpdated(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null) {
            val pref = extras.getSerializable(ListPreferenceActivity.EXTRA_LIST_PREFERENCE) as ListPreference
            adapter.notifyUpdate(pref.key, pref.selectedValue)
            adapter.notifyUpdate(pref.key, pref.selectedDisplay)
        }
    }

    private fun onColorPreferenceUpdated(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null) {
            val pref = extras.getSerializable(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE) as ColorPreference
            adapter.notifyUpdate(pref.key, pref)
        }
    }

    override fun onCreateLoader(id: Int, @Nullable args: Bundle?): Loader<AsyncResult<PreferenceGroup>> {
        return PreferenceLoader(context!!, preferenceDefinitions)
    }

    override fun onLoadFinished(@NonNull loader: Loader<AsyncResult<PreferenceGroup>>, result: AsyncResult<PreferenceGroup>) {
        weakContext?.get()?.let {
            adapter.setPreferences(it, result.data)
        }
    }

    override fun onLoaderReset(@NonNull loader: Loader<AsyncResult<PreferenceGroup>>) {

    }
}
