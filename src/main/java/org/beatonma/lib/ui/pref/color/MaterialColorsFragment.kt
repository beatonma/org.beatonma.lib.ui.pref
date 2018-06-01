package org.beatonma.lib.ui.pref.color

import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.FragmentColorMaterialBinding
import org.beatonma.lib.ui.activity.BaseFragment
import org.beatonma.lib.ui.pref.clone
import org.beatonma.lib.ui.recyclerview.BaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.BaseViewHolder


class MaterialColorsFragment : BaseFragment() {
//        LoaderManager.LoaderCallbacks<AsyncResult<MutableList<Swatch>>> {

    companion object {
        const val TAG = "MaterialColorsFrag"
        private const val LOADER_COLORS = 2389
        private const val VIEW_LEVEL_SWATCHES = 0
        private const val VIEW_LEVEL_COLORS = 1
    }
    lateinit var prefViewModel: ColorPreferenceViewModel
    lateinit var swatchViewModel: MaterialColorsViewModel

    private var binding: FragmentColorMaterialBinding? = null

    private val colorsAdapter: PatchAdapter = createAdapter()

//    private val swatches: MutableList<Swatch> = mutableListOf()
    private val visibleColors: MutableList<ColorItem> = mutableListOf()
    private val tempColors: MutableList<ColorItem> = mutableListOf()

    private var level = VIEW_LEVEL_SWATCHES
    private var openSwatch = -1

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with (outState) {
            putInt("level", level)
            putInt("openSwatch", openSwatch)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefViewModel = ViewModelProviders.of(activity!!).get(ColorPreferenceViewModel::class.java)
        swatchViewModel = ViewModelProviders.of(this).get(MaterialColorsViewModel::class.java)

//        swatchViewModel.swatches.observe(this, Observer { colorsAdapter.showSwatches() })

        if (savedInstanceState != null) {
            level = savedInstanceState.getInt("level", VIEW_LEVEL_SWATCHES)
            openSwatch = savedInstanceState.getInt("openSwatch", -1)
        }

//        val transition = TransitionInflater.from(context)
//                .inflateTransition(R.transition.custom_color_unshared)
//        enterTransition = transition
//        exitTransition = transition
    }

    override fun init(binding: ViewDataBinding?) {
        this.binding = binding as FragmentColorMaterialBinding

        binding.recyclerview.apply {
            adapter = colorsAdapter
            layoutManager = GridLayoutManager(context, 4)
            itemAnimator = ColorItemAnimator(gridWidth = 4)
        }

//        swatchViewModel.swatches.observe(this, Observer { colorsAdapter.showSwatches() })

        getColorActivity()?.apply {
            onCustomActionClick(
                    R.string.pref_color_custom,
                    View.OnClickListener {
                        customiseColor(prefViewModel.colorPreference.value?.color ?: 0x000000)
                    }
            )
            onPositiveClick()
        }

//        binding.customButton.setOnClickListener {
//            getColorActivity()?.customiseColor(prefViewModel!!.colorPreference.value?.color ?: 0x000000)
//        }

//        LoaderManager.getInstance(this).initLoader(LOADER_COLORS, null, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
        colorsAdapter.showSwatches()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_color_material
    }

    private fun getColorItemAnimator(): ColorItemAnimator? {
        return binding?.recyclerview?.itemAnimator as ColorItemAnimator
    }

//    override fun onCreateLoader(id: Int, args: Bundle?):
//            Loader<AsyncResult<MutableList<Swatch>>> {
//        return MaterialColorsLoader(context!!)
//    }

    fun onBackPressed(): Boolean {
        if (!isDetached && level == VIEW_LEVEL_COLORS) {
            colorsAdapter.showSwatches()
            return true
        }
        return false
    }

    fun createAdapter(): PatchAdapter {
        return PatchAdapter(object: ColorSelectedCallback {
            override fun onColorSelected(color: Int, swatch: Int, swatchPosition: Int) {
                val pref = prefViewModel.colorPreference.value
                pref?.update(color, swatch, swatchPosition)
                prefViewModel.colorPreference.value = pref
                getColorActivity()?.returnResult(pref)
            }
        })
    }

//    override fun onLoadFinished(loader: Loader<AsyncResult<MutableList<Swatch>>>,
//                                result: AsyncResult<MutableList<Swatch>>) {
//        when (loader.id) {
//            LOADER_COLORS -> {
////                visibleColors.clear()
////                colorsAdapter.notifyDataSetChanged()
//                swatches.clone(result.data)
//                if (!isDetached) {
//                    colorsAdapter.showSwatches()
//                }
//                else Log.d(TAG, "result received while fragment detached")
//            }
//        }
//    }

//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//        Log.d(TAG, "onAttach")
//        if (swatches.isNotEmpty()) {
//            Log.d(TAG, "showing swatches")
//            visibleColors.clear()
//            colorsAdapter.showSwatches()
//        }
//    }

//    override fun onLoaderReset(loader: Loader<AsyncResult<MutableList<Swatch>>>) {}

    private fun getColorActivity(): SwatchColorPreferenceActivity? {
        return activity as SwatchColorPreferenceActivity
    }

    inner class PatchAdapter(val callback: ColorSelectedCallback) : BaseRecyclerViewAdapter() {
        override fun getItemCount(): Int {
            return visibleColors.size
        }

//        override fun getItems(): MutableList<*> {
//            return visibleColors
//        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return PatchViewHolder(inflate(parent, R.layout.vh_pref_color_patch))
//                else -> super.onCreateViewHolder(parent, viewType)
//            }
        }

        inner class PatchViewHolder(view: View) : BasePatchViewHolder(view) {
            override fun bind(position: Int) {
                index = position
                val color = visibleColors[position]

                patch.color = color.color
                ViewCompat.setTransitionName(
                        patch,
                        "${getString(R.string.transition_color_preview)}_$position")

                patch.setOnLongClickListener {
                    (activity as SwatchColorPreferenceActivity)
                            .customiseColor(patch.color, sharedView = patch)
                    true
                }

                val colorPref = prefViewModel.colorPreference.value
                when (level) {
                    VIEW_LEVEL_SWATCHES -> {
                        patch.choose(position == colorPref?.swatch, true)
                        patch.setOnClickListener { showSwatch(position) }
                    }
                    VIEW_LEVEL_COLORS -> {
                        patch.chosen = false
                            patch.choose(
                                    openSwatch == colorPref?.swatch
                                            && position == colorPref.swatchPosition,
                                    true)

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
            openSwatch = -1
            level = VIEW_LEVEL_SWATCHES

            tempColors.clear()
            swatchViewModel.swatches.forEach { tempColors.add(it.canonical) }
            applyDiff(visibleColors, tempColors)
            visibleColors.clone(tempColors)
        }

        fun showSwatch(position: Int) {
            openSwatch = position
            level = VIEW_LEVEL_COLORS

            getColorItemAnimator()?.setEpicenter(position)

            swatchViewModel.swatches.let {
                tempColors.clone(it[position].colors)
            }
//            tempColors.clone(swatches[position].colors)
            applyDiff(visibleColors, tempColors)
            visibleColors.clone(tempColors)
        }


        /**
         * We don't want views to move around - they should only be added, removed or
         * change in-place - so the normal DiffUtils diff method is not ideal.
         */
        fun applyDiff(old: MutableList<ColorItem>?, new: MutableList<ColorItem>?) {
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

class MaterialColorsViewModel(context: Application): AndroidViewModel(context) {
    val swatches: List<Swatch>

    init {
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
        swatches = MutableList(defs.size) {
            val swatchDef: IntArray = context.resources.getIntArray(defs[it])
            val colors: MutableList<ColorItem> = MutableList(swatchDef.size) { id ->
                ColorItem(swatchDef[id])
            }
            val s = Swatch(colors, colors[5])
            s
        }
    }
}


//class MaterialColorsLoader(context: Context) : SupportBaseAsyncTaskLoader<MutableList<Swatch>>(context) {
//    override fun onReleaseResources(data: AsyncResult<MutableList<Swatch>>) {}
//
//    override fun loadInBackground(): AsyncResult<MutableList<Swatch>>? {
//        val builder: AsyncResult.Builder<MutableList<Swatch>> = AsyncResult.getBuilder()
//
//        val defs = intArrayOf(
//                R.array.material_red,
//                R.array.material_pink,
//                R.array.material_purple,
//                R.array.material_deep_purple,
//                R.array.material_indigo,
//                R.array.material_blue,
//                R.array.material_light_blue,
//                R.array.material_cyan,
//                R.array.material_teal,
//                R.array.material_green,
//                R.array.material_light_green,
//                R.array.material_lime,
//                R.array.material_yellow,
//                R.array.material_amber,
//                R.array.material_orange,
//                R.array.material_deep_orange,
//                R.array.material_blue_grey,
//                R.array.material_brown,
//                R.array.material_grey
//        )
//        val swatches: MutableList<Swatch> = MutableList(defs.size) {
//            val swatchDef: IntArray = context.resources.getIntArray(defs[it])
//            val colors: MutableList<ColorItem> = MutableList(swatchDef.size) { id ->
//                ColorItem(swatchDef[id])
//            }
//            val s = Swatch(colors, colors[5])
//            s
//        }
//        builder.success(swatches)
//
//        return builder
//    }
//}

data class Swatch(val colors: MutableList<ColorItem>, val canonical: ColorItem)
