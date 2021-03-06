package org.beatonma.lib.ui.pref.color

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.transaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import org.beatonma.lib.ui.activity.popup.PopupActivity
import org.beatonma.lib.ui.pref.R
import org.beatonma.lib.ui.pref.preferences.ColorPreference

class SwatchColorPreferenceActivity : PopupActivity() {

    override val contentLayoutID: Int = R.layout.activity_color

    internal lateinit var viewModel: ColorPreferenceViewModel

    companion object {
        const val EXTRA_COLOR_PREFERENCE = "extra_color_preference"
        const val REQUEST_CODE_UPDATE = 378
    }

    override fun onPreLayout() {
        super.onPreLayout()
        viewModel = ViewModelProviders.of(this).get(ColorPreferenceViewModel::class.java)
        viewModel.colorPreference.observe(this, Observer { save(it) })
    }

    override fun initContentLayout(binding: ViewDataBinding) {
        setTitle(R.string.pref_color_choose)

        if (viewModel.colorPreference.value?.color?.swatch ?: 0 < 0) {
            customiseColor(viewModel.colorPreference.value?.color?.color)
        } else {
            showMaterialColors()
        }
    }

//    override fun loadState(savedState: Bundle?) {
//        super.loadState(savedState)
//    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentByTag(MaterialColorsFragment.TAG) as? MaterialColorsFragment)?.let {
            if (it.onBackPressed()) {
                // If fragment consumes the event then stop here
                return
            }
        }
        super.onBackPressed()
    }

    override fun initExtras(extras: Bundle?) {
        super.initExtras(extras)
        viewModel.colorPreference.value = extras?.getParcelable(EXTRA_COLOR_PREFERENCE)
    }

    fun showMaterialColors() {
        val fragment = supportFragmentManager.findFragmentByTag(MaterialColorsFragment.TAG)
                ?: MaterialColorsFragment()
        prepareResize()
        supportFragmentManager.transaction {
            replace(R.id.fragment_container, fragment, MaterialColorsFragment.TAG)
            setReorderingAllowed(true)
        }
    }

    fun customiseColor(color: Int?, sharedView: View? = null) {
        val transitionName = if (sharedView != null) ViewCompat.getTransitionName(sharedView) else null
        val fragment: CustomColorFragment =
                supportFragmentManager.findFragmentByTag(CustomColorFragment.TAG) as? CustomColorFragment
                        ?: CustomColorFragment()
        color?.let {
            fragment.updateArgs(color, transitionName, viewModel.colorPreference.value?.alphaEnabled)
        }

        prepareResize()
        supportFragmentManager.transaction {
            replace(R.id.fragment_container, fragment, CustomColorFragment.TAG)
            setReorderingAllowed(true)
            if (transitionName != null) {
                sharedView?.let {
                    addSharedElement(it, transitionName)
                }

                // If we are customising a template color, allow the user to return to it easily
                addToBackStack(CustomColorFragment.TAG)
            }
        }
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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {

        }
    }
}

class ColorPreferenceViewModel : ViewModel() {
    val colorPreference: MutableLiveData<ColorPreference> = MutableLiveData()
}

interface ColorSelectedCallback {
    fun onColorSelected(color: Int, swatch: Int = -1, swatchPosition: Int = -1)
}
