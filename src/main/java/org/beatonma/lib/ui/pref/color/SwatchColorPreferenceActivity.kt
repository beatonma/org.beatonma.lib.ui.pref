package org.beatonma.lib.ui.pref.color


import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import org.beatonma.lib.prefs.R
import org.beatonma.lib.prefs.databinding.ActivityColorBinding
import org.beatonma.lib.ui.activity.popup.PopupActivity
import org.beatonma.lib.ui.pref.preferences.ColorPreference


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
