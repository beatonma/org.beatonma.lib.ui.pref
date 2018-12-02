package org.beatonma.lib.ui.pref.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.core.content.edit
import androidx.databinding.ViewDataBinding
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.load.Result
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.ui.activity.popup.RecyclerViewPopupActivity
import org.beatonma.lib.ui.pref.R
import org.beatonma.lib.ui.pref.preferences.ListPreference
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.kotlin.extensions.setup
import org.beatonma.lib.ui.style.Views
import org.beatonma.lib.util.kotlin.extensions.autotag
import org.beatonma.lib.util.kotlin.extensions.toPrettyString
import java.util.*

private const val TAG = "ListPreferenceActivity"
private const val LIST_LOADER = 91

open class ListPreferenceActivity : RecyclerViewPopupActivity(),
        LoaderManager.LoaderCallbacks<Result<List<ListItem>>> {

    companion object {
        const val EXTRA_LIST_PREFERENCE = "extra_list_preference"
        const val REQUEST_CODE_UPDATE = 936
    }

    private lateinit var listPreference: ListPreference

    private lateinit var adapter: ListAdapter
    private var listItems: List<ListItem>? = null

    open fun buildAdapter(): ListAdapter {
        return ListAdapter()
    }

    override fun initExtras(extras: Bundle?) {
        super.initExtras(extras)
        extras?.getParcelable<ListPreference>(EXTRA_LIST_PREFERENCE)?.let {
            listPreference = it
        }
    }

    fun saveAndClose(item: ListItem) {
        listPreference.selectedValue = item.value
        listPreference.selectedDisplay = item.text


        getSharedPreferences(listPreference.prefs, Context.MODE_PRIVATE).edit(commit = true) {
            listPreference.save(this)
        }

        val intent = Intent()
        intent.putExtra(EXTRA_LIST_PREFERENCE, listPreference)
        setResult(Activity.RESULT_OK, intent)
        close()
    }

    override fun initLayout(binding: ViewDataBinding) {
        super.initLayout(binding)
        setTitle(listPreference.name)
        LoaderManager.getInstance(this).initLoader(LIST_LOADER, null, this)
    }

    override fun setup(recyclerView: RecyclerView) {
        adapter = buildAdapter()
        recyclerView.setup(adapter)
    }

    @NonNull
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<List<ListItem>>> {
        return PrefListLoader(this, listPreference)
    }

    override fun onLoadFinished(loader: Loader<Result<List<ListItem>>>,
                                result: Result<List<ListItem>>) {
        when (loader.id) {
            LIST_LOADER -> {
                if (result.isFailure) {
                    Log.w(TAG, "List loading failed: ${result.errors.toPrettyString()}")
                }
                adapter.diff(listItems, result.data)
                listItems = result.data
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<List<ListItem>>>) {

    }


    class PrefListLoader
    internal constructor(
            context: Context,
            private val preference: ListPreference
    ) : SupportBaseAsyncTaskLoader<List<ListItem>>(context) {

        override fun loadInBackground(): Result<List<ListItem>> {
            val result = Result.getBuilder<List<ListItem>>(null)
            val items = ArrayList<ListItem>()

            val display = if (preference.displayListResourceId == 0)
                null
            else
                context.resources.getStringArray(preference.displayListResourceId)
            //            final int[] values =
            //                    preference.getValuesListResourceId() == 0
            //                            ? null
            //                            :resources.getIntArray(preference.getValuesListResourceId());
            if (display == null) {
                result.failure("Unable to read display list")
                return result
            }

            for (i in display.indices) {
                items.add(
                        ListItem().apply {
                            text = display[i]
                            value = i
                            checked = i == preference.selectedValue
                        })
            }

            result.success(items)

            return result
        }

        override fun onReleaseResources(data: Result<List<ListItem>>?) {

        }
    }

    open inner class ListAdapter : EmptyBaseRecyclerViewAdapter {
        constructor() : super()
        constructor(nullLayoutID: Int) : super(nullLayoutID = nullLayoutID)

        override val items: List<ListItem>?
            get() = listItems

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                0 -> ItemViewHolder(inflate(parent, R.layout.vh_list_item_single))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        private inner class ItemViewHolder(v: View) : BaseViewHolder(v) {
            internal val itemBinding: VhListItemSingleBinding = VhListItemSingleBinding.bind(v)

            override fun bind(position: Int) {
                val item = items?.get(position) ?: run {
                    Log.w(autotag, "Item is null!")
                    return@bind
                }

                itemBinding.radioButton.isChecked = item.checked
                itemBinding.text.text = item.text
                itemBinding.description.text = item.description
                Views.hideIfEmpty(itemBinding.text, itemBinding.description)

                val clickListener = View.OnClickListener {
                    Log.d(autotag, "item click at $adapterPosition")
                    val adapterPosition = adapterPosition
                    for (i in 0 until adapter.itemCount) {

                        val holder = recyclerView
                                .findViewHolderForAdapterPosition(i) as? ItemViewHolder
                        holder?.let { it.itemBinding.radioButton.isChecked = i == adapterPosition }
                    }

                    saveAndClose(item)
                }

                itemView.setOnClickListener(clickListener)
                itemBinding.radioButton.setOnClickListener(clickListener)
            }
        }
    }
}
