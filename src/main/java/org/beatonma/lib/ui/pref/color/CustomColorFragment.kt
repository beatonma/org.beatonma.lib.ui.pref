package org.beatonma.lib.ui.pref.color

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.transition.*
import androidx.transition.Visibility.MODE_IN
import com.google.android.material.tabs.TabLayout
import org.beatonma.lib.ui.pref.R
import org.beatonma.lib.ui.pref.databinding.FragmentColorCustomBinding
import org.beatonma.lib.ui.activity.BaseFragment
import org.beatonma.lib.ui.style.Interpolate
import org.beatonma.lib.util.Sdk
import org.beatonma.lib.util.kotlin.extensions.autotag
import org.beatonma.lib.util.kotlin.extensions.dp
import org.beatonma.lib.util.toHsv
import java.util.regex.Pattern
import kotlin.math.roundToInt

const val EXTRA_COLOR = "extra_color"
const val EXTRA_TRANSITION_NAME = "extra_transition_name"
const val EXTRA_ALPHA_ENABLED = "extra_alpha_enabled"

// Arbitrary value used to distinguish EditText changes made by the user vs by the app
private const val TAG_NO_UPDATE = "no_update"

class CustomColorFragment : BaseFragment() {
    companion object {
        const val TAG = "CustomColorFragment"
    }

    override val layoutID: Int = R.layout.fragment_color_custom

    lateinit var binding: FragmentColorCustomBinding
    lateinit var viewModel: ColorspaceViewModel

    // Hex color input validation
    private val hexPattern: Pattern = Pattern.compile("[0-9a-fA-F]*")

    // If true, enable editing of color alpha value
    private var alphaEnabled = false

    private val interpolator = Interpolate.getMotionInterpolator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(ColorspaceViewModel::class.java)
        }
        viewModel.color.value = arguments?.getInt(EXTRA_COLOR, 0) ?: 0

        val hsvTab = binding.tabs.newTab().apply {
            text = getString(R.string.colorspace_hsv)
            tag = Colorspace.HSV.name
        }
        binding.tabs.addTab(hsvTab)

        val rgbTab = binding.tabs.newTab().apply {
            text = getString(R.string.colorspace_rgb)
            tag = Colorspace.RGB.name
        }
        binding.tabs.addTab(rgbTab)

        getColorActivity()?.apply {
            onCustomActionClick(
                    R.string.pref_color_preset,
                    View.OnClickListener { showMaterialColors() })
            onPositiveClick(R.string.dialog_ok, View.OnClickListener { saveAndClose() })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            updateArgs(it.getInt("color"), alphaEnabled = it.getBoolean("alpha"))
        }

        if (Sdk.isLollipop) {
            binding.preview.elevation = context.dp(2F)
        }

        viewModel.color.value = arguments?.getInt(EXTRA_COLOR, 0) ?: 0

        if (viewModel.colorspace.value == Colorspace.RGB) {
            selectTab(Colorspace.RGB)
            showRgbColorspace()
        } else {
            selectTab(Colorspace.HSV)
            showHsvColorspace()
        }
        showAlpha(arguments?.getBoolean(EXTRA_ALPHA_ENABLED) ?: alphaEnabled)

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.colorspace.value = when (tab?.tag) {
                    Colorspace.RGB.name -> Colorspace.RGB
                    else -> Colorspace.HSV
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        val seekbarListener = ColorSeekbarListener()
        with(binding) {
            seekbar1.setOnSeekBarChangeListener(seekbarListener)
            seekbar2.setOnSeekBarChangeListener(seekbarListener)
            seekbar3.setOnSeekBarChangeListener(seekbarListener)
            seekbar4.setOnSeekBarChangeListener(seekbarListener)
        }

        viewModel.color.observe(this, Observer {
            it?.let {
                binding.preview.color = it
                updateSeekbars(it.components())
            }
        })
        viewModel.colorspace.observe(this, Observer {
            it?.let {
                when (it) {
                    Colorspace.HSV -> showHsvColorspace()
                    Colorspace.RGB -> showRgbColorspace()
                }
            }
        })
    }

    private fun getColorActivity(): SwatchColorPreferenceActivity? {
        return activity as SwatchColorPreferenceActivity
    }

    /**
     * Show/hide alpha editing controls
     */
    private fun showAlpha(show: Boolean = true) {
        alphaEnabled = show
        val visibility = if (show) View.VISIBLE else View.GONE
        with(binding) {
            label4.visibility = visibility
            seekbar4.visibility = visibility
            value4.visibility = visibility
        }

        binding.hex.filters = arrayOf(
                // Accept [0-9a-fA-F]*, ignore anything else
                InputFilter { source, start, end, dest, dstart, dend ->
                    val builder = StringBuilder()
                    source.forEach {
                        if (!hexPattern.matcher(it.toString()).matches()) {
                            return@InputFilter ""
                        }
                        else {
                            builder.append(it.toUpperCase())
                        }
                    }

                    if (binding.hex.tag == TAG_NO_UPDATE) return@InputFilter null

                    val result = dest.replaceRange(dstart until dend, source.subSequence(start until end))
                    try {
                        if ((alphaEnabled && result.length == 8)
                                || (!alphaEnabled && result.length == 6)) {
                            viewModel.color.value = Color.parseColor("#$result")
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.d(autotag, "Unable to parse color from string '$result'")
                    } catch (e: StringIndexOutOfBoundsException) {
                        Log.d(autotag, "Unable to parse color from empty string")
                    }

                    builder.toString()
                },
                // Show/hide alpha part of hexadecimal
                InputFilter.LengthFilter(if (show) 8 else 6))
    }

    fun saveAndClose() {
        activity?.let {
            val parentViewModel = ViewModelProviders.of(it).get(ColorPreferenceViewModel::class.java)
            val pref = parentViewModel.colorPreference.value
            val color = viewModel.color.value
            color?.let { pref?.update(color = color) }
            parentViewModel.colorPreference.value = pref
            getColorActivity()?.returnResult(pref)
        }
    }

    fun updateArgs(color: Int,
                   transitionName: String? = null,
                   alphaEnabled: Boolean? = null) {
        arguments = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_TRANSITION_NAME, transitionName)
            putBoolean(EXTRA_ALPHA_ENABLED, alphaEnabled ?: this@CustomColorFragment.alphaEnabled)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putInt("color", viewModel.color.value ?: 0)
            putBoolean("alpha", alphaEnabled)
        }

        super.onSaveInstanceState(outState)
    }

    private fun updateSeekbars(comps: ColorComponents) {
        with(binding) {
            hex.text?.apply {
                hex.tag = TAG_NO_UPDATE  // Temporarily disable input listener
                clear()

                if (alphaEnabled) {
                    append(comps.hex)
                } else {
                    // If alpha editing is disabled, remove the alpha component from hex string
                    if (comps.hex.length == 8) append(comps.hex.substring(2))
                }
//                append(if (alphaEnabled) comps.hex else comps.hex.substring(2))
                hex.tag = null
            }
            if (viewModel.colorspace.value == Colorspace.HSV) {
                seekbar1.progress = comps.hue
                seekbar2.progress = comps.saturation
                seekbar3.progress = comps.value
                seekbar4.progress = comps.alpha

                value1.text = "${comps.hue}"
                value2.text = "${comps.saturation}"
                value3.text = "${comps.value}"
                value4.text = "${comps.alpha}"
            } else if (viewModel.colorspace.value == Colorspace.RGB) {
                seekbar1.progress = comps.red
                seekbar2.progress = comps.green
                seekbar3.progress = comps.blue
                seekbar4.progress = comps.alpha

                value1.text = "${comps.red}"
                value2.text = "${comps.green}"
                value3.text = "${comps.blue}"
                value4.text = "${comps.alpha}"
            }
        }
    }

    private fun showHsvColorspace() {
        // Update colorspace value only if it is not already HSV to avoid triggering observer loops
        if (viewModel.colorspace.value != Colorspace.HSV) viewModel.colorspace.value = Colorspace.HSV
        val components = viewModel.color.value?.components() ?: return
        with(binding) {
            label1.text = getString(R.string.hsv_hue_short)
            value1.text = "${components.hue}"

            label2.text = getString(R.string.hsv_saturation_short)
            value2.text = "${components.saturation}"

            label3.text = getString(R.string.hsv_value_short)
            value3.text = "${components.value}"

            label4.text = getString(R.string.alpha_short)
            value4.text = "${components.alpha}"

            AnimatorSet().apply {
                playTogether(
                        seekbar1.animatorTo(components.hue, 359),
                        seekbar2.animatorTo(components.saturation, 100),
                        seekbar3.animatorTo(components.value, 100),
                        seekbar4.animatorTo(components.alpha, 255)
                )
                interpolator = this@CustomColorFragment.interpolator
                start()
            }
        }
    }

    private fun showRgbColorspace() {
        // Update colorspace value only if it is not already RGB to avoid triggering observer loops
        if (viewModel.colorspace.value != Colorspace.RGB) viewModel.colorspace.value = Colorspace.RGB
        val components = viewModel.color.value?.components() ?: return
        with(binding) {
            label1.text = getString(R.string.rgb_red_short)
            value1.text = "${components.red}"

            label2.text = getString(R.string.rgb_green_short)
            value2.text = "${components.green}"

            label3.text = getString(R.string.rgb_blue_short)
            value3.text = "${components.blue}"

            label4.text = getString(R.string.alpha_short)
            value4.text = "${components.alpha}"

            AnimatorSet().apply {
                playTogether(
                        seekbar1.animatorTo(components.red, 255),
                        seekbar2.animatorTo(components.green, 255),
                        seekbar3.animatorTo(components.blue, 255),
                        seekbar4.animatorTo(components.alpha, 255)
                )
                interpolator = this@CustomColorFragment.interpolator
                start()
            }
        }
    }

    private fun selectTab(colorspace: Colorspace) {
        for (i in 0 until binding.tabs.tabCount) {
            val t = binding.tabs.getTabAt(i)
            if (t?.tag == colorspace.name) {
                t.select()
                return
            }
        }
    }

    inner class ColorSeekbarListener : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekbar: SeekBar?) {}
        override fun onStopTrackingTouch(seekbar: SeekBar?) {}
        override fun onProgressChanged(seekbar: SeekBar?, value: Int, fromUser: Boolean) {
            if (!fromUser) return
            with(binding) {
                if (viewModel.colorspace.value == Colorspace.HSV) {
                    when (seekbar) {
                        seekbar1 -> viewModel.updateHsv(hue = value)
                        seekbar2 -> viewModel.updateHsv(saturation = value)
                        seekbar3 -> viewModel.updateHsv(value = value)
                        seekbar4 -> viewModel.updateHsv(alpha = value)
                    }
                } else if (viewModel.colorspace.value == Colorspace.RGB) {
                    when (seekbar) {
                        seekbar1 -> viewModel.updateRgb(red = value)
                        seekbar2 -> viewModel.updateRgb(green = value)
                        seekbar3 -> viewModel.updateRgb(blue = value)
                        seekbar4 -> viewModel.updateRgb(alpha = value)
                    }
                }
            }
        }
    }
}

enum class Colorspace {
    RGB,
    HSV
}

class ColorspaceViewModel : ViewModel() {
    val colorspace = MutableLiveData<Colorspace>().apply { value = Colorspace.HSV }

    @ColorInt
    val color = MutableLiveData<Int>().apply { value = 0 }

    fun updateHsv(hue: Int? = null, saturation: Int? = null, value: Int? = null, alpha: Int? = null) {
        val hsv = toHsv(color.value ?: 0)
        if (hue != null) hsv[0] = hue.toFloat()
        if (saturation != null) hsv[1] = saturation.toFloat() / 100F
        if (value != null) hsv[2] = value.toFloat() / 100F
        val a = alpha ?: Color.alpha(color.value ?: 0)

        color.value = Color.HSVToColor(a, hsv)
    }

    fun updateRgb(red: Int? = null, green: Int? = null, blue: Int? = null, alpha: Int? = null) {
        val col = color.value ?: 0
        val argb = intArrayOf(Color.alpha(col), Color.red(col), Color.green(col), Color.blue(col))
        if (alpha != null) argb[0] = alpha
        if (red != null) argb[1] = red
        if (green != null) argb[2] = green
        if (blue != null) argb[3] = blue
        color.value = Color.argb(argb[0], argb[1], argb[2], argb[3])
    }
}

data class ColorComponents(val hue: Int, val saturation: Int, val value: Int,
                           val red: Int, val green: Int, val blue: Int, val alpha: Int,
                           val hex: String)

private fun @receiver:ColorInt Int.components(): ColorComponents {
    val hsv = toHsv(this)
    val hue = hsv[0].roundToInt()
    val saturation = (hsv[1] * 100F).toInt()
    val value = (hsv[2] * 100F).toInt()

    return ColorComponents(
            hue, saturation, value,
            Color.red(this), Color.green(this), Color.blue(this),
            Color.alpha(this),
            Integer.toHexString(this).toUpperCase())
}

private fun SeekBar.animatorTo(targetValue: Int = progress, targetLimit: Int = max): Animator {
    val startValue = progress
    val startLimit = max
    val valueDifference = targetValue - startValue
    val limitDifference = targetLimit - startLimit

    return ValueAnimator.ofFloat(0F, 1F).apply {
        addUpdateListener { anim ->
            val f = anim.animatedFraction
            progress = startValue + (valueDifference * f).roundToInt()
            max = startLimit + (limitDifference * f).roundToInt()
        }
    }
}
