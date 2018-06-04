package org.beatonma.lib.ui.pref.color

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.transition.*
import androidx.transition.Visibility.MODE_IN
import com.google.android.material.tabs.TabLayout
import org.beatonma.lib.core.kotlin.extensions.dp
import org.beatonma.lib.core.util.Sdk
import org.beatonma.lib.core.util.toHsv
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.FragmentColorCustomBinding
import org.beatonma.lib.ui.activity.BaseFragment

class CustomColorFragment : BaseFragment() {
    companion object {
        const val TAG = "CustomColorFragment"
        const val EXTRA_COLOR = "extra_color"
        const val EXTRA_TRANSITION_NAME = "extra_transition_name"
    }

    override val layoutId: Int = R.layout.fragment_color_custom

    lateinit var binding: FragmentColorCustomBinding
    lateinit var viewModel: ColorspaceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(activity!!).get(ColorspaceViewModel::class.java)
        viewModel.color.value = arguments?.getInt(EXTRA_COLOR, 0) ?: 0

        val fade = Fade().apply {
            mode = MODE_IN
        }
        val slide = Slide().apply {
            slideEdge = Gravity.BOTTOM
            mode = MODE_IN
        }

        enterTransition = TransitionSet().apply {
            duration = 300
            interpolator = OvershootInterpolator(0.6F)
            addTransition(fade)
            addTransition(slide)
        }

        sharedElementEnterTransition = AutoTransition().apply {
            setPathMotion(ArcMotion())
            duration = 260
        }
    }

    override fun init(binding: ViewDataBinding) {
        this.binding = binding as FragmentColorCustomBinding
            ViewCompat.setTransitionName(binding.preview, arguments?.getString(EXTRA_TRANSITION_NAME))

        viewModel.color.observe(this, Observer {
            it?.let {
                binding.preview.setBackgroundColor(it)
                updateSeekbars(it.components())
            }
        })

        val hsvTab = binding.tabs.newTab()
        hsvTab.text = getString(R.string.colorspace_hsv)
        hsvTab.tag = Colorspace.HSV.name
        binding.tabs.addTab(hsvTab)

        val rgbTab = binding.tabs.newTab()
        rgbTab.text = getString(R.string.colorspace_rgb)
        rgbTab.tag = Colorspace.RGB.name
        binding.tabs.addTab(rgbTab)

        binding.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.tag) {
                    Colorspace.RGB.name -> showRgbColorspace()
                    Colorspace.HSV.name -> showHsvColorspace()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        with(binding) {
            val seekbarListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekbar: SeekBar?) {}
                override fun onStopTrackingTouch(seekbar: SeekBar?) {}

                override fun onProgressChanged(seekbar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    if (viewModel.colorspace == Colorspace.HSV) {
                        when (seekbar) {
                            seekbar1 -> viewModel.updateHsv(hue = value)
                            seekbar2 -> viewModel.updateHsv(saturation = value)
                            seekbar3 -> viewModel.updateHsv(value = value)
                        }
                    }
                    else if (viewModel.colorspace == Colorspace.RGB) {
                        when (seekbar) {
                            seekbar1 -> viewModel.updateRgb(red = value)
                            seekbar2 -> viewModel.updateRgb(green = value)
                            seekbar3 -> viewModel.updateRgb(blue = value)
                        }
                    }
                }
            }

            seekbar1.setOnSeekBarChangeListener(seekbarListener)
            seekbar2.setOnSeekBarChangeListener(seekbarListener)
            seekbar3.setOnSeekBarChangeListener(seekbarListener)
        }

        getColorActivity()?.apply {
            onCustomActionClick(
                    R.string.pref_color_preset,
                    View.OnClickListener { showMaterialColors() })
            onPositiveClick(R.string.dialog_ok, View.OnClickListener { saveAndClose() })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }

        if (Sdk.isLollipop) {
            binding.preview.elevation = context.dp(2F)
        }
        viewModel.color.value = arguments?.getInt(EXTRA_COLOR, 0) ?: 0
        showHsvColorspace()
    }

    private fun getColorActivity(): SwatchColorPreferenceActivity? {
        return activity as SwatchColorPreferenceActivity
    }

    fun saveAndClose() {
        val parentViewModel = ViewModelProviders.of(activity!!).get(ColorPreferenceViewModel::class.java)
        val pref = parentViewModel.colorPreference.value
        pref?.update(color = viewModel.color.value!!)
        parentViewModel.colorPreference.value = pref
        getColorActivity()?.returnResult(pref)
    }

    fun updateArgs(color: Int,
                   transitionName: String? = null) {
        arguments = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_TRANSITION_NAME, transitionName)
        }
    }

    private fun updateSeekbars(comps: ColorComponents) {
        with (binding) {
            if (viewModel.colorspace == Colorspace.HSV) {
                seekbar1.progress = comps.hue
                seekbar2.progress = comps.saturation
                seekbar3.progress = comps.value

                value1.text = "${comps.hue}"
                value2.text = "${comps.saturation}"
                value3.text = "${comps.value}"
            }
            else if (viewModel.colorspace == Colorspace.RGB) {
                seekbar1.progress = comps.red
                seekbar2.progress = comps.green
                seekbar3.progress = comps.blue

                value1.text = "${comps.red}"
                value2.text = "${comps.green}"
                value3.text = "${comps.blue}"
            }
        }
    }

    private fun showHsvColorspace() {
        val components = viewModel.color.value!!.components()
        viewModel.colorspace = Colorspace.HSV
        with (binding) {
            label1.text = getString(R.string.hsv_hue_short)
            seekbar1.max = 359

            label2.text = getString(R.string.hsv_saturation_short)
            seekbar2.max = 100

            label3.text = getString(R.string.hsv_value_short)
            seekbar3.max = 100

            val hueAnimator = ValueAnimator.ofInt(seekbar1.progress, components.hue)
            hueAnimator.addUpdateListener { seekbar1.progress = it.animatedValue as Int }

            val satAnimator = ValueAnimator.ofInt(seekbar2.progress, components.saturation)
            satAnimator.addUpdateListener { seekbar2.progress = it.animatedValue as Int }

            val valAnimator = ValueAnimator.ofInt(seekbar3.progress, components.value)
            valAnimator.addUpdateListener { seekbar3.progress = it.animatedValue as Int }

            val set = AnimatorSet()
            set.playTogether(hueAnimator, satAnimator, valAnimator)
            set.start()
        }
    }

    private fun showRgbColorspace() {
        val components = viewModel.color.value!!.components()
        viewModel.colorspace = Colorspace.RGB
        with (binding) {
            label1.text = getString(R.string.rgb_red_short)
            seekbar1.max = 255

            label2.text = getString(R.string.rgb_green_short)
            seekbar2.max = 255

            label3.text = getString(R.string.rgb_blue_short)
            seekbar3.max = 255

            val redAnimator = ValueAnimator.ofInt(seekbar1.progress, components.red)
            redAnimator.addUpdateListener { seekbar1.progress = it.animatedValue as Int }

            val greenAnimator = ValueAnimator.ofInt(seekbar2.progress, components.green)
            greenAnimator.addUpdateListener { seekbar2.progress = it.animatedValue as Int }

            val blueAnimator = ValueAnimator.ofInt(seekbar3.progress, components.blue)
            blueAnimator.addUpdateListener { seekbar3.progress = it.animatedValue as Int }

            val set = AnimatorSet()
            set.playTogether(redAnimator, greenAnimator, blueAnimator)
            set.start()
        }
    }
}

enum class Colorspace {
    RGB,
    HSV
}

class ColorspaceViewModel: ViewModel() {
    var colorspace: Colorspace = Colorspace.HSV

    @ColorInt
    var color: MutableLiveData<Int> = MutableLiveData()

    fun updateHsv(hue: Int? = null, saturation: Int? = null, value: Int? = null) {
        val hsv = toHsv(color.value ?: 0)
        if (hue != null)        hsv[0] = hue.toFloat()
        if (saturation != null) hsv[1] = saturation.toFloat() / 100F
        if (value != null)      hsv[2] = value.toFloat() / 100F

        color.value = Color.HSVToColor(hsv)
    }

    fun updateRgb(red: Int? = null, green: Int? = null, blue: Int? = null) {
        val col = color.value ?: 0
        val rgb = intArrayOf(Color.red(col), Color.green(col), Color.blue(col))
        if (red != null)    rgb[0] = red
        if (green != null)  rgb[1] = green
        if (blue != null)   rgb[2] = blue
        color.value = Color.rgb(rgb[0], rgb[1], rgb[2])
    }
}

data class ColorComponents(val hue: Int, val saturation: Int, val value: Int,
                           val red: Int, val green: Int, val blue: Int)

private fun @receiver:ColorInt Int.components(): ColorComponents {
    val hsv = toHsv(this)
    val hue = hsv[0].toInt()
    val saturation = (hsv[1] * 100F).toInt()
    val value = (hsv[2] * 100F).toInt()

    return ColorComponents(
            hue, saturation, value,
            Color.red(this), Color.green(this), Color.blue(this))
}