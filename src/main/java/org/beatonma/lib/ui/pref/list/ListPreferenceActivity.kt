package org.beatonma.lib.ui.pref.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.ViewDataBinding
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.beatonma.lib.core.kotlin.extensions.toPrettyString
import org.beatonma.lib.load.Result
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.log.Log
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.ActivityListBinding
import org.beatonma.lib.prefs.databinding.VhListItemSingleBinding
import org.beatonma.lib.ui.activity.popup.PopupActivity
import org.beatonma.lib.ui.pref.preferences.ListPreference
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter.EmptyViewsAdapter
import org.beatonma.lib.ui.recyclerview.kotlin.extensions.setup
import java.util.*

class ListPreferenceActivity : PopupActivity(), LoaderManager.LoaderCallbacks<Result<List<ListItem>>> {

    companion object {
        const val EXTRA_LIST_PREFERENCE = "extra_list_preference"
        const val REQUEST_CODE_UPDATE = 936

        private const val LIST_LOADER = 91
    }

    private lateinit var mListPreference: ListPreference

    private lateinit var mBinding: ActivityListBinding
    private val mAdapter = buildAdapter()
    private var listItems: List<ListItem>? = null

    override val layoutId: Int
        get() = R.layout.activity_list

    fun buildAdapter(): ListAdapter {
        val adapter = ListAdapter()
        adapter.setEmptyViews(object : EmptyViewsAdapter() {
            override val dataset: Collection<*>?
                get() = listItems
        })
        return adapter
    }

    override fun initExtras(extras: Bundle?) {
        super.initExtras(extras)
        mListPreference = extras?.getSerializable(EXTRA_LIST_PREFERENCE) as ListPreference
    }

    fun saveAndClose(value: Int, name: String?) {
        mListPreference.selectedValue = value
        mListPreference.selectedDisplay = name

        val editor = getSharedPreferences(mListPreference.prefs, Context.MODE_PRIVATE).edit()
        mListPreference.save(editor)
        editor.apply()

        val intent = Intent()
        intent.putExtra(EXTRA_LIST_PREFERENCE, mListPreference)
        setResult(Activity.RESULT_OK, intent)
        close()
    }

    override fun initLayout(binding: ViewDataBinding) {
        mBinding = binding as ActivityListBinding
        binding.recyclerview.setup(mAdapter)

        setTitle(mListPreference.name)

        LoaderManager.getInstance(this).initLoader(LIST_LOADER, null, this)
    }

    @NonNull
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<List<ListItem>>> {
        return PrefListLoader(this, mListPreference)
    }

    override fun onLoadFinished(loader: Loader<Result<List<ListItem>>>,
                                result: Result<List<ListItem>>) {
        when (loader.id) {
            LIST_LOADER -> {
                if (result.isFailure) {
                    Log.w(PopupActivity.TAG, "List loading failed: %s",
                            result.errors.toPrettyString())
                }
                mAdapter.diff(listItems, result.data)
                listItems = result.data
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<List<ListItem>>>) {

    }

    class PrefListLoader internal constructor(context: Context, private val preference: ListPreference) : SupportBaseAsyncTaskLoader<List<ListItem>>(context) {

        override fun loadInBackground(): Result<List<ListItem>> {
            val result = Result.getBuilder<List<ListItem>>(null)
            val items = ArrayList<ListItem>()

            val context = context
            val resources = context.resources

            val display = if (preference.displayListResourceId == 0)
                null
            else
                resources.getStringArray(preference.displayListResourceId)
            //            final int[] values =
            //                    preference.getValuesListResourceId() == 0
            //                            ? null
            //                            :resources.getIntArray(preference.getValuesListResourceId());
            if (display == null) {
                result.failure("Unable to read display list")
                return result
            }

            for (i in display.indices) {
                //                final int val = values == null ? i : values[i];
                items.add(
                        ListItem()
                                .text(display[i])
                                .value(i)
                                .checked(i == preference.selectedValue))
            }

            result.success(items)

            return result
        }

        override fun onReleaseResources(data: Result<List<ListItem>>?) {

        }
    }

    inner class ListAdapter : EmptyBaseRecyclerViewAdapter() {
        override val items: List<ListItem>?
            get() = listItems

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                0 -> ItemViewHolder(inflate(parent, R.layout.vh_list_item_single))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        private inner class ItemViewHolder(v: View) : BaseViewHolder(v) {
            internal val binding: VhListItemSingleBinding = VhListItemSingleBinding.bind(v)

            override fun bind(position: Int) {
                val item = items?.get(position)
                binding.item = item
                item ?: return

                val clickListener = View.OnClickListener {
                    val adapterPosition = adapterPosition
                    for (i in 0 until mAdapter.itemCount) {
                        val holder = mBinding.recyclerview.findViewHolderForAdapterPosition(i) as ItemViewHolder?
                        holder?.binding?.radioButton?.isChecked = i == adapterPosition
                    }

                    saveAndClose(item.value(), item.text())
                }

                itemView.setOnClickListener(clickListener)
                binding.radioButton.setOnClickListener(clickListener)
            }
        }
    }
}
