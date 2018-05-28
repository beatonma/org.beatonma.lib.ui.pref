package org.beatonma.lib.ui.pref.color


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import org.beatonma.lib.load.AsyncResult
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.ActivityColorBinding
import org.beatonma.lib.prefs.databinding.PrefColorRecyclerviewBinding
import org.beatonma.lib.ui.activity.BaseFragment
import org.beatonma.lib.ui.activity.popup.PopupActivity
import org.beatonma.lib.ui.pref.clone
import org.beatonma.lib.ui.pref.preferences.ColorPreference
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter


class SwatchColorPreferenceActivity : PopupActivity<ActivityColorBinding>() {
    private var binding: ActivityColorBinding? = null
    private lateinit var viewModel: ColorViewModel

    companion object {
        const val EXTRA_COLOR_PREFERENCE = "extra_color_preference"
        const val REQUEST_CODE_UPDATE = 378
    }

    override fun onPreCreate() {
        super.onPreCreate()
        viewModel = ViewModelProviders.of(this).get(ColorViewModel::class.java)
        viewModel.colorPreference.observe(this, Observer { save(it) })
    }

    override fun getBinding(): ActivityColorBinding? {
        return binding
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_color
    }

    override fun initLayout(binding: ViewDataBinding?) {
        this.binding = binding as ActivityColorBinding
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MaterialColorsFragment(), MaterialColorsFragment.TAG)
                .commit()
    }

    override fun initExtras(extras: Bundle?) {
        super.initExtras(extras)
        viewModel.colorPreference.value =
                extras?.getSerializable(EXTRA_COLOR_PREFERENCE) as ColorPreference
    }

    fun save(pref: ColorPreference?) {
        if (pref == null) return
        val editor = getSharedPreferences(pref.prefs, MODE_PRIVATE).edit()
        pref.save(editor)
        editor.apply()
    }

    fun returnResult(pref: ColorPreference?) {
        viewModel.colorPreference.removeObservers(this)

        val intent = Intent()
        intent.putExtra(EXTRA_COLOR_PREFERENCE, pref)
        setResult(RESULT_OK, intent)
        close()
    }
}

class ColorViewModel: ViewModel() {
    val colorPreference: MutableLiveData<ColorPreference> = MutableLiveData()
}

interface ColorSelectedCallback {
    fun onColorSelected(color: Int, swatch: Int = -1, swatchPosition: Int = -1)
}


class MaterialColorsFragment : BaseFragment(),
        LoaderManager.LoaderCallbacks<AsyncResult<MutableList<Swatch>>> {

    companion object {
        const val TAG = "MaterialColorsFrag"
        private const val LOADER_COLORS = 2389
        private const val VIEW_LEVEL_SWATCHES = 0
        private const val VIEW_LEVEL_COLORS = 1
    }
    var viewModel: ColorViewModel? = null

    private var binding: PrefColorRecyclerviewBinding? = null

    private val colorsAdapter: PatchAdapter = createAdapter()

    private var swatches: MutableList<Swatch> = mutableListOf()
    private var visibleColors: MutableList<ColorItem> = mutableListOf()
    private var tempColors: MutableList<ColorItem> = mutableListOf()

    private var level = VIEW_LEVEL_SWATCHES
    private var openSwatch = -1

    override fun init(binding: ViewDataBinding?) {
        viewModel = ViewModelProviders.of(activity!!).get(ColorViewModel::class.java)
        this.binding = binding as PrefColorRecyclerviewBinding

        with(binding.recyclerview) {
            adapter = colorsAdapter
            layoutManager = GridLayoutManager(context, 4)
            itemAnimator = ColorItemAnimator(gridWidth = 4)
        }

        LoaderManager.getInstance(this).initLoader(LOADER_COLORS, null, this)
    }

    override fun getLayoutId(): Int {
        return R.layout.pref_color_recyclerview
    }

    private fun getColorItemAnimator(): ColorItemAnimator? {
        return binding?.recyclerview?.itemAnimator as ColorItemAnimator
    }

    override fun onCreateLoader(id: Int, args: Bundle?):
            Loader<AsyncResult<MutableList<Swatch>>> {
        return MaterialColorsLoader(context!!)
    }

    fun createAdapter(): PatchAdapter {
        return PatchAdapter(object: ColorSelectedCallback {
            override fun onColorSelected(color: Int, swatch: Int, swatchPosition: Int) {
                val pref = viewModel?.colorPreference?.value
                pref?.update(color, swatch, swatchPosition)
                viewModel?.colorPreference?.value = pref
                (activity as SwatchColorPreferenceActivity).returnResult(pref)
            }
        })
    }

    override fun onLoadFinished(loader: Loader<AsyncResult<MutableList<Swatch>>>,
                                result: AsyncResult<MutableList<Swatch>>) {
        when (loader.id) {
            LOADER_COLORS -> {
                colorsAdapter.diff(swatches, result.data)
                swatches = result.data
                colorsAdapter.showSwatches()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<AsyncResult<MutableList<Swatch>>>) {}

    inner class PatchAdapter(val callback: ColorSelectedCallback) : EmptyBaseRecyclerViewAdapter() {

        override fun getItems(): MutableList<*> {
            return visibleColors
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                VIEW_TYPE_DEFAULT -> PatchViewHolder(inflate(parent, R.layout.vh_pref_color_patch))
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        inner class PatchViewHolder(view: View) : BasePatchViewHolder(view) {
            override fun bind(position: Int) {
                index = position
                val color = visibleColors[position]

                patch.color = color.color

                val colorPref = viewModel?.colorPreference?.value
                when (level) {
                    VIEW_LEVEL_SWATCHES -> {
//                        patch.chosen = false
                        patch.postDelayed({
                            patch.choose(position == colorPref?.swatch, false)
                        }, getColorItemAnimator()!!.duration)
                        patch.setOnClickListener { showSwatch(position) }
                    }
                    VIEW_LEVEL_COLORS -> {
                        patch.chosen = false
                        patch.postDelayed({
                            patch.choose(
                                    openSwatch == colorPref?.swatch
                                    && position == colorPref.swatchPosition,
                                    true)
                        }, getColorItemAnimator()!!.duration)

                        patch.setOnClickListener { selectColor(patch, position) }
                    }
                }
            }
        }

        private fun selectColor(patch: ColorPatchView, position: Int) {
            patch.choose(patch.chosen, true)
            callback.onColorSelected(patch.color, openSwatch, position)
        }

        fun showSwatches() {
            (activity as SwatchColorPreferenceActivity).setTitle("Swatches")
            openSwatch = -1
            level = VIEW_LEVEL_SWATCHES

            tempColors.clear()
            swatches.forEach { tempColors.add(it.canonical) }
            applyDiff(visibleColors, tempColors)
            visibleColors.clone(tempColors)
        }

        fun showSwatch(position: Int) {
            (activity as SwatchColorPreferenceActivity).setTitle("Swatch colors")
            openSwatch = position
            level = VIEW_LEVEL_COLORS

            getColorItemAnimator()?.setEpicenter(position)

            tempColors.clone(swatches[position].colors)
            applyDiff(visibleColors, tempColors)
            visibleColors.clone(tempColors)
        }


        /**
         * We don't want views to move around - they should only be added, removed or
         * change in-place - so the normal DiffUtils diff method is not ideal.
         */
        private fun applyDiff(old: MutableList<ColorItem>?, new: MutableList<ColorItem>?) {
            if (old == null && new == null) {
                return
            }

            val oldSize = old?.size ?: 0
            val newSize = new?.size ?: 0

            if (oldSize > newSize) notifyItemRangeRemoved(newSize, oldSize - newSize)
            else if (oldSize < newSize) notifyItemRangeInserted(oldSize, newSize - oldSize)

            // Force all remaining views to rebind so we can update listeners
            val minSize = Math.min(oldSize, newSize)
            (0 until minSize).forEach { notifyItemChanged(it) }
        }
    }
}


class MaterialColorsLoader(context: Context) : SupportBaseAsyncTaskLoader<MutableList<Swatch>>(context) {
    override fun onReleaseResources(data: AsyncResult<MutableList<Swatch>>) {

    }

    override fun loadInBackground(): AsyncResult<MutableList<Swatch>>? {
        val builder: AsyncResult.Builder<MutableList<Swatch>> = AsyncResult.getBuilder()

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
                R.array.material_blue_grey,
                R.array.material_brown,
                R.array.material_grey
        )
        val swatches: MutableList<Swatch> = MutableList(defs.size) {
            val swatchDef: IntArray = context.resources.getIntArray(defs[it])
            val colors: MutableList<ColorItem> = MutableList(swatchDef.size) { id ->
                ColorItem(swatchDef[id])
            }
            val s = Swatch(colors, colors[5])
            s
        }
        builder.success(swatches)

        return builder
    }
}

data class Swatch(val colors: MutableList<ColorItem>, val canonical: ColorItem)
