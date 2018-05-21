package org.beatonma.lib.ui.pref.activity

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.core.util.Sdk
import org.beatonma.lib.load.AsyncResult
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.ActivityEditColorBinding
import org.beatonma.lib.ui.activity.popup.PopupActivity
import org.beatonma.lib.ui.pref.activity.data.ColorItem
import org.beatonma.lib.ui.pref.view.ColorPatchView
import org.beatonma.lib.ui.recyclerview.BaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.RVUtil
import org.beatonma.lib.ui.style.Views


class ColorPreferenceActivity : PopupActivity<ActivityEditColorBinding>(),
        LoaderManager.LoaderCallbacks<AsyncResult<MutableList<MutableList<ColorItem>>>> {
    companion object {
        private const val LOADER_COLORS = 2458
    }

    private var patchSizeNormal = 0
    private var patchSizeSelected = 0

    private var binding: ActivityEditColorBinding? = null
    private var groupsAdapter: ColorGroupsAdapter = buildAdapter()


    fun buildAdapter(): ColorGroupsAdapter {
        return ColorGroupsAdapter()
    }

    override fun getBinding(): ActivityEditColorBinding? {
        return binding
    }

    override fun initLayout(binding: ViewDataBinding?) {
        setTitle(R.string.pref_color_choose)

        this.binding = binding as ActivityEditColorBinding
        RVUtil.setup(binding.colors, groupsAdapter)

        patchSizeNormal = resources.getDimensionPixelSize(R.dimen.color_patch_size)
        patchSizeSelected = resources.getDimensionPixelSize(R.dimen.color_patch_selected)

        LoaderManager.getInstance(this)
                .initLoader(LOADER_COLORS, null, this)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_edit_color
    }

    override fun onCreateLoader(id: Int, args: Bundle?):
            Loader<AsyncResult<MutableList<MutableList<ColorItem>>>> {
        return ColorLoader(this)
    }

    override fun onLoadFinished(loader: Loader<AsyncResult<MutableList<MutableList<ColorItem>>>>,
                                result: AsyncResult<MutableList<MutableList<ColorItem>>>?) {
        when (loader.id) {
            LOADER_COLORS -> {
                groupsAdapter.diff(groupsAdapter.colorGroups, result?.data)
                groupsAdapter.colorGroups = result?.data
            }
        }
    }

    override fun onLoaderReset(loader: Loader<AsyncResult<MutableList<MutableList<ColorItem>>>>) {

    }


    /**
     * Shows multiple groups of colors
     */
    inner class ColorGroupsAdapter : EmptyBaseRecyclerViewAdapter() {
        var colorGroups: MutableList<MutableList<ColorItem>>? = null

        override fun getItems(): MutableList<MutableList<ColorItem>>? {
            return colorGroups
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                BaseRecyclerViewAdapter.VIEW_TYPE_DEFAULT ->
                    ColorGroupViewHolder(inflate(parent, R.layout.recyclerview))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        inner class ColorGroupViewHolder(v: View?) : BaseViewHolder(v) {
            private val group: RecyclerView? = v?.findViewById(R.id.recyclerview)
            private val adapter = ColorsAdapter()

            init {
                RVUtil.setup(group!!, adapter, LinearLayoutManager(
                        this@ColorPreferenceActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false))
            }

            override fun bind(position: Int) {
                adapter.colorGroup = colorGroups!![position]
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Displays a group of related colors
     */
    inner class ColorsAdapter : BaseRecyclerViewAdapter() {
        var colorGroup: MutableList<ColorItem>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return ColorViewHolder(inflate(parent, R.layout.vh_pref_color_patch))
        }

        override fun getItemCount(): Int {
            return colorGroup?.size ?: 0
        }

        /**
         * A single color patch
         */
        inner class ColorViewHolder(v: View?) : BaseViewHolder(v) {
            val patch: ColorPatchView? = v?.findViewById(R.id.color)

            override fun bind(position: Int) {
                val color = colorGroup!![position]
                val size = if (color.selected) patchSizeSelected else patchSizeNormal
                Views.setWidthAndHeight(size, size, patch)
                patch?.setColor(color.color)
                patch?.setOnClickListener {
                    color.selected = true
                    if (Sdk.isKitkat()) {
                        TransitionManager.beginDelayedTransition(itemView as ViewGroup?)
                    }
                    Views.setWidthAndHeight(patchSizeSelected, patchSizeSelected, patch)
                }
            }
        }
    }


    class ColorLoader(context: Context) : SupportBaseAsyncTaskLoader<MutableList<MutableList<ColorItem>>>(context) {
        override fun onReleaseResources(data: AsyncResult<MutableList<MutableList<ColorItem>>>?) {

        }

        override fun loadInBackground(): AsyncResult<MutableList<MutableList<ColorItem>>>? {
            val builder: AsyncResult.Builder<MutableList<MutableList<ColorItem>>> = AsyncResult.getBuilder()

            val defs = intArrayOf(
                    R.array.material_red,
                    R.array.material_pink,
                    R.array.material_purple,
                    R.array.material_deep_purple,
                    R.array.material_indigo,
                    R.array.material_blue,
                    R.array.material_light_blue,
                    R.array.material_cyan,
                    R.array.material_teal,
                    R.array.material_green,
                    R.array.material_light_green,
                    R.array.material_lime,
                    R.array.material_yellow,
                    R.array.material_amber,
                    R.array.material_orange,
                    R.array.material_deep_orange,
                    R.array.material_brown,
                    R.array.material_grey,
                    R.array.material_blue_grey
            )
            val groups: MutableList<MutableList<ColorItem>> = MutableList(defs.size) {
                val groupDef = context.resources.getIntArray(defs[it])
                MutableList(groupDef.size) {
                    ColorItem(groupDef[it], selected=false)
                }
            }

            builder.success(groups)

            return builder
        }
    }
}