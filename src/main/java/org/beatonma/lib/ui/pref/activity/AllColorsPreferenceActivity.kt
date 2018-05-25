package org.beatonma.lib.ui.pref.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import org.beatonma.lib.load.AsyncResult
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.ActivityEditColorBinding
import org.beatonma.lib.ui.activity.popup.PopupActivity
import org.beatonma.lib.ui.pref.activity.data.ColorItem
import org.beatonma.lib.ui.pref.view.ColorPatchView
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.RVUtil
import org.beatonma.lib.ui.recyclerview.SlideInItemAnimator


class AllColorsPreferenceActivity : PopupActivity<ActivityEditColorBinding>(),
        LoaderManager.LoaderCallbacks<AsyncResult<MutableList<ColorItem>>> {
    companion object {
        private const val LOADER_COLORS = 2458
        private const val COLOR_GROUP_WIDTH = 13
    }

    private var patchSizeNormal = 0
    private var patchSizeSelected = 0
    private var spacerWidth = 0

    private var binding: ActivityEditColorBinding? = null
    private var colorAdapter: ColorAdapter = buildAdapter()

    private var colors: MutableList<ColorItem>? = null


    fun buildAdapter(): ColorAdapter {
        return ColorAdapter()
    }

    override fun getBinding(): ActivityEditColorBinding? {
        return binding
    }

    override fun initLayout(binding: ViewDataBinding?) {
        setTitle(R.string.pref_color_choose)

        this.binding = binding as ActivityEditColorBinding
        RVUtil.setup(binding.colors, colorAdapter,
                GridLayoutManager(this, COLOR_GROUP_WIDTH))
        binding.colors.itemAnimator = SlideInItemAnimator(3)

        patchSizeNormal = resources.getDimensionPixelSize(R.dimen.color_patch_size)
        patchSizeSelected = resources.getDimensionPixelSize(R.dimen.color_patch_selected)
        spacerWidth = resources.getDimensionPixelSize(R.dimen.item_padding_default)

        LoaderManager.getInstance(this)
                .initLoader(LOADER_COLORS, null, this)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_edit_color
    }

    override fun onCreateLoader(id: Int, args: Bundle?):
            Loader<AsyncResult<MutableList<ColorItem>>> {
        return ColorLoader(this)
    }

    override fun onLoadFinished(loader: Loader<AsyncResult<MutableList<ColorItem>>>,
                                result: AsyncResult<MutableList<ColorItem>>?) {
        when (loader.id) {
            LOADER_COLORS -> {
                colorAdapter.diff(colors, result?.data)
                colors = result?.data
            }
        }
    }

    override fun onLoaderReset(loader: Loader<AsyncResult<MutableList<ColorItem>>>) {

    }

    inner class ColorAdapter : EmptyBaseRecyclerViewAdapter() {
        override fun getItems(): MutableList<ColorItem>? {
            return colors
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                VIEW_TYPE_DEFAULT ->
                    ColorViewHolder(inflate(parent, R.layout.vh_pref_color_patch))
                else -> return super.onCreateViewHolder(parent, viewType)
            }
        }
    }

    inner class ColorViewHolder(val view: View) : BaseViewHolder(view) {
        val patch: ColorPatchView = view.findViewById(R.id.colorpatch)

        override fun bind(position: Int) {
            val color = colors!![position]
            patch.setColor(color.color)
        }
    }


    class ColorLoader(context: Context) : SupportBaseAsyncTaskLoader<MutableList<ColorItem>>(context) {
        override fun onReleaseResources(data: AsyncResult<MutableList<ColorItem>>?) {

        }

        override fun loadInBackground(): AsyncResult<MutableList<ColorItem>>? {
            val builder: AsyncResult.Builder<MutableList<ColorItem>> = AsyncResult.getBuilder()

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
                    R.array.material_blue_grey,
                    R.array.material_grey
            )
            val colors = mutableListOf<ColorItem>()
            defs.forEach {
                val shades = context.resources.getIntArray(it)
                (0 until COLOR_GROUP_WIDTH).forEach { index ->
                    val isColor = index < shades.size
                    colors.add(
                            ColorItem(
                                    if (isColor) shades[index] else 0x00000000,
                                    selectable = isColor))
                }
            }

            builder.success(colors)

            return builder
        }
    }
}