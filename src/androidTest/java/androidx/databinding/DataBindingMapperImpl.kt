@file:JvmName("DataBindingMapperImpl")

package androidx.databinding

/**
 * Needed to avoid
 *  java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/databinding/DataBinderMapperImpl
 * when running Espresso tests.
 *
 * Only seems to affect test build. Caused by error in databinding library? Or build environment?
 *
 * This solution was found at
 *   https://github.com/robolectric/robolectric/issues/3789#issuecomment-400562494
 */
class DataBinderMapperImpl: MergedDataBinderMapper() {
    init {
        addMapper(org.beatonma.lib.ui.pref.DataBinderMapperImpl())
        addMapper(org.beatonma.lib.ui.activity.DataBinderMapperImpl())
    }
}
