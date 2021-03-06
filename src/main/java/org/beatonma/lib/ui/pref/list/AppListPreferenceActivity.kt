package org.beatonma.lib.ui.pref.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.load.Result
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.ui.activity.popup.RecyclerViewPopupActivity
import org.beatonma.lib.ui.pref.R
import org.beatonma.lib.ui.pref.databinding.VhAppListItemSingleBinding
import org.beatonma.lib.ui.pref.preferences.AppListPreference
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.LoadingRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.kotlin.extensions.setup
import org.beatonma.lib.ui.recyclerview.simpleDiffCallback
import org.beatonma.lib.ui.style.Views
import org.beatonma.lib.util.kotlin.extensions.autotag
import org.beatonma.lib.util.kotlin.extensions.toPrettyString
import org.beatonma.lib.util.kotlin.extensions.toast

class App(manager: PackageManager, activityInfo: ActivityInfo, var selected: Boolean = false) {
    val packageName: String = activityInfo.packageName
    val activityName: String = activityInfo.name
    val niceName: String = activityInfo.loadLabel(manager).toString()

    fun sameAs(pref: AppListPreference): Boolean {
        return packageName == pref.selectedAppPackage && activityName == pref.selectedAppActivity
    }

    override fun toString(): String {
        return "Activity: '$activityName'"
    }
}

open class AppListPreferenceActivity : RecyclerViewPopupActivity(),
        LoaderManager.LoaderCallbacks<Result<List<App>>> {
    companion object {
        const val EXTRA_APP_LIST_PREFERENCE = "extra_app_list_preference"
        const val REQUEST_CODE_UPDATE = 762

        private const val APP_LIST_LOADER = 237
    }

    private lateinit var preference: AppListPreference
    private lateinit var adapter: AppListAdapter

    private var apps: List<App>? = null

    override fun initExtras(extras: Bundle?) {
        super.initExtras(extras)
        extras?.getParcelable<AppListPreference>(EXTRA_APP_LIST_PREFERENCE)?.let {
            preference = it
        }
    }

//    override fun initContentLayout(binding: ViewDataBinding) {
//        this.binding = binding as ActivityListBinding
//        setTitle(R.string.pref_app_choose)
//        adapter = buildAdapter()
//        binding.recyclerview.setup(adapter)
//
//        LoaderManager.getInstance(this).initLoader(APP_LIST_LOADER, null, this)
//    }


    override fun setup(recyclerView: RecyclerView) {
        setTitle(R.string.pref_app_choose)
        adapter = buildAdapter()
        recyclerView.setup(adapter)

        LoaderManager.getInstance(this).initLoader(APP_LIST_LOADER, null, this)
    }

    fun saveAndClose(item: App) {
        preference.selectedAppName = item.niceName
        preference.selectedAppPackage = item.packageName
        preference.selectedAppActivity = item.activityName

        val editor = getSharedPreferences(preference.prefs, Context.MODE_PRIVATE).edit()
        preference.save(editor)
        editor.apply()

        val intent = Intent()
        intent.putExtra(EXTRA_APP_LIST_PREFERENCE, preference)
        setResult(Activity.RESULT_OK, intent)
        close()
    }

    open fun buildAdapter(): AppListAdapter {
        return AppListAdapter()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<List<App>>> {
        return AppListLoader(this, preference)
    }

    override fun onLoadFinished(loader: Loader<Result<List<App>>>, result: Result<List<App>>) {
        when (loader.id) {
            APP_LIST_LOADER -> {
                if (result.isFailure) {
                    Log.w(autotag, "App list loading failed: ${result.errors.toPrettyString()}")
                }
                adapter.diff(
                        simpleDiffCallback(apps, result.data,
                                sameItem = { old, new ->
                                    (old?.packageName?.equals(new?.packageName) ?: false)
                                            && (old?.activityName?.equals(new?.activityName)
                                            ?: false)
                                },
                                sameContent = { old, new -> old?.selected == new?.selected }))
                apps = result.data
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<List<App>>>) {

    }

    open inner class AppListAdapter : LoadingRecyclerViewAdapter {
        constructor() : super()
        constructor(nullLayoutID: Int) : super(nullLayoutID = nullLayoutID)

        override val items: List<App>?
            get() = apps

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                0 -> AppViewHolder(inflate(parent, R.layout.vh_app_list_item_single))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        private inner class AppViewHolder(v: View) : BaseViewHolder(v) {
            val appBinding: VhAppListItemSingleBinding = VhAppListItemSingleBinding.bind(v)

            override fun bind(position: Int) {
                val app = items?.get(position) ?: return

                appBinding.radioButton.isChecked = app.selected
                appBinding.text.text = app.niceName
                appBinding.description.text = app.packageName

                Views.hideIfEmpty(appBinding.text, appBinding.description)

                val clickListener = View.OnClickListener {
                    for (i in 0 until adapter.itemCount) {
                        val holder = binding.recyclerview
                                .findViewHolderForAdapterPosition(i) as? AppViewHolder
                                ?: continue
                        holder.appBinding.radioButton.isChecked = i == adapterPosition
                    }

                    saveAndClose(app)
                }

                itemView.setOnClickListener(clickListener)
                appBinding.radioButton.setOnClickListener(clickListener)

                itemView.setOnLongClickListener {
                    toast(app.toString())
                    true
                }
            }
        }
    }

    private class AppListLoader(
            context: Context, private val preference: AppListPreference
    ) : SupportBaseAsyncTaskLoader<List<App>>(context) {
        override fun loadInBackground(): Result<List<App>>? {
            val result = Result.Builder<List<App>>()

            val manager = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            val launchables = manager.queryIntentActivities(mainIntent, 0)
            val apps = launchables.map {
                App(manager, it.activityInfo).apply {
                    selected = sameAs(preference)
                }
            }.sortedBy { it.niceName.toLowerCase() }

            result.success(apps)

            return result
        }

        override fun onReleaseResources(data: Result<List<App>>?) {

        }
    }
}
