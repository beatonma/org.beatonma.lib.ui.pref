package org.beatonma.lib.ui.pref.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.beatonma.lib.core.util.Sdk;
import org.beatonma.lib.log.Log;
import org.beatonma.lib.prefs.R;
import org.beatonma.lib.ui.activity.ActivityBuilder;
import org.beatonma.lib.ui.pref.color.ColorPatchView;
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity;
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity;
import org.beatonma.lib.ui.pref.preferences.BasePreference;
import org.beatonma.lib.ui.pref.preferences.BooleanPreference;
import org.beatonma.lib.ui.pref.preferences.ColorPreference;
import org.beatonma.lib.ui.pref.preferences.ListPreference;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;
import org.beatonma.lib.ui.pref.preferences.SimplePreference;
import org.beatonma.lib.ui.pref.view.BasePreferenceViewHolder;
import org.beatonma.lib.ui.recyclerview.BaseViewHolder;
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PreferenceAdapter extends EmptyBaseRecyclerViewAdapter {
    private final static String TAG = "PreferenceAdapter";

    private final static int TYPE_SIMPLE = 0;
    private final static int TYPE_BOOLEAN = 1;
    private final static int TYPE_LIST_SINGLE = 2;
    private final static int TYPE_LIST_MULTI = 3;
    private final static int TYPE_COLOR_SINGLE = 11;
    private final static int TYPE_COLOR_GROUP = 12;
    private final static int TYPE_GROUP = 65;

    private final WeakReference<PreferenceFragment> mWeakFragment;
    private WeakReference<SharedPreferences> mWeakPrefs;
    private PreferenceGroup mPreferenceGroup;

    @Override
    public List getItems() {
        return mPreferenceGroup == null ? null : mPreferenceGroup.getPreferences();
    }

    public PreferenceAdapter() {
        this(null);
    }

    public PreferenceAdapter(final PreferenceFragment fragmentContext) {
        this(fragmentContext, null);
    }

    public PreferenceAdapter(final PreferenceFragment fragmentContext, final EmptyViews emptyViews) {
        mWeakFragment = fragmentContext == null ? null : new WeakReference<>(fragmentContext);

        setEmptyViews(emptyViews != null ? emptyViews : new EmptyViewsAdapter() {
            @Override
            public Collection<?> getDataset() {
                return mPreferenceGroup == null ? null : mPreferenceGroup.getPreferences();
            }
        });
    }

    public PreferenceGroup getPreferenceGroup() {
        return mPreferenceGroup;
    }

    public void setPreferences(final Context context, final PreferenceGroup group) {
        mWeakPrefs = new WeakReference<>(
                context.getSharedPreferences(group.getName(), Context.MODE_PRIVATE));
        diff(mPreferenceGroup == null ? null : mPreferenceGroup.getPreferences(), group.getPreferences());
        mPreferenceGroup = group;
    }

    /**
     * Layout must contain TextViews with the following IDs:
     *  - 'title'
     *  - 'description'
     *
     * Layout must contain a {@link CompoundButton} (or subclass) view with ID:
     *  - 'checkable'
     */
    public int getSwitchLayout() {
        return R.layout.vh_pref_switch;
    }

    /**
     * Layout must contain TextViews with the following IDs:
     *  - 'title'
     *  - 'description'
     */
    public int getListSingleLayout() {
        return getSimpleLayout();
    }

    /**
     * Layout must contain TextViews with the following IDs:
     *  - 'title'
     *  - 'description'
     */
    public int getSimpleLayout() {
        return R.layout.vh_pref_simple;
    }

    /**
     * Layout must contain TextViews with the following IDs:
     *  - 'title'
     *  - 'description'
     *
     *  Layout must contain a ColorPatch with ID:
     *  - 'color'
     */
    public int getColorSingleLayout() {
        return R.layout.vh_pref_color_single;
    }

    /**
     * Layout must contain TextViews with the following IDs:
     *  - 'title'
     *  - 'description'
     *
     *  Layout must contain a {@link RecyclerView} with ID:
     *  - 'colors'
     */
    public int getColorGroupLayout() {
        return R.layout.vh_pref_color_group;
    }

    public void notifyUpdate(final String key, final String value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
        }
        else {
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%s", key, value);
        }
    }

    public void notifyUpdate(final String key, final int value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
        }
        else {
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%d", key, value);
        }
    }

    public void notifyUpdate(final String key, final boolean value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
        }
        else {
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%b", key, value);
        }
    }

    public void notifyUpdate(final String key, final Object value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
        }
        else {
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%s", key, value);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        if (mPreferenceGroup == null) {
            return super.getItemViewType(position);
        }

        final BasePreference p = mPreferenceGroup.getPreferences().get(position);
        final String type = p.getType();
        switch (type) {
            case BooleanPreference.TYPE:
                return TYPE_BOOLEAN;
            case ListPreference.TYPE:
                return TYPE_LIST_SINGLE;
            case ColorPreference.TYPE:
                return TYPE_COLOR_SINGLE;
            case BasePreference.TYPE:
            case SimplePreference.TYPE:
                return TYPE_SIMPLE;
            default:
                return super.getItemViewType(position);
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case TYPE_BOOLEAN:
                return new SwitchPreferenceViewHolder(inflate(parent, getSwitchLayout())) {
                    @Override
                    public void bind(final int position) {
                        bind(mWeakPrefs, (BooleanPreference) mPreferenceGroup.getPreferences().get(position));
                    }
                };
            case TYPE_LIST_SINGLE:
                return new ListPreferenceViewHolder(inflate(parent, getListSingleLayout())) {
                    @Override
                    public void bind(final int position) {
                        bind(mWeakPrefs, (ListPreference) mPreferenceGroup.getPreferences().get(position));
                    }
                };
            case TYPE_COLOR_SINGLE:
                return new ColorPreferenceViewHolder(inflate(parent, getColorSingleLayout())) {
                    @Override
                    public void bind(final int position) {
                        bind(mWeakPrefs, (ColorPreference) mPreferenceGroup.getPreferences().get(position));
                    }
                };
            case 0:
                return new BasePreferenceViewHolder(inflate(parent, getSimpleLayout())) {
                    @Override
                    public void bind(final int position) {
                        bind(mWeakPrefs, mPreferenceGroup.getPreferences().get(position));
                    }
                };
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    public class SwitchPreferenceViewHolder extends BasePreferenceViewHolder<BooleanPreference> {
        private final CompoundButton mSwitch;

        SwitchPreferenceViewHolder(final View v) {
            super(v);
            mSwitch = v.findViewById(R.id.checkable);
        }

        @Override
        public void bind(final WeakReference<SharedPreferences> weakPrefs, final BooleanPreference preference) {
            super.bind(weakPrefs, preference);
            mSwitch.setChecked(preference.isChecked());

            mSwitch.setOnCheckedChangeListener(
                    (v, checked) -> {
                        if (Sdk.isKitkat()) {
                            TransitionManager.beginDelayedTransition((ViewGroup) itemView);
                        }
                        preference.setChecked(checked);
                        setDescription(
                                checked
                                        ? preference.getSelectedDescription()
                                        : preference.getUnselectedDescription());
                        save(preference);
                    });

            itemView.setOnClickListener(v -> mSwitch.toggle());
        }
    }

    public class ListPreferenceViewHolder extends BasePreferenceViewHolder<ListPreference> {
        ListPreferenceViewHolder(final View v) {
            super(v);
        }

        @Override
        public void bind(final WeakReference<SharedPreferences> weakPrefs, final ListPreference preference) {
            super.bind(weakPrefs, preference);
            setDescription(preference.getSelectedDisplay());
            itemView.setOnClickListener(
                    v -> ActivityBuilder.from(itemView.getContext())
                            .to(ListPreferenceActivity.class)
                            .putExtra(ListPreferenceActivity.EXTRA_LIST_PREFERENCE, preference)
                            .forResult(mWeakFragment.get(), ListPreferenceActivity.REQUEST_CODE_UPDATE)
                            .animationSource(v)
                            .start());
        }
    }

    public class ColorPreferenceViewHolder extends BasePreferenceViewHolder<ColorPreference> {
        private final ColorPatchView patch;
        private boolean firstDisplay = true;    // We only want to animate on new views
        ColorPreferenceViewHolder(final View v) {
            super(v);
            patch = v.findViewById(R.id.colorpatch);
        }

        @Override
        public void bind(final WeakReference<SharedPreferences> sharedPrefs, final ColorPreference preference) {
            super.bind(sharedPrefs, preference);
            patch.setColor(preference.getColor());
            firstDisplay = false;
            itemView.setOnClickListener((v) ->
                ActivityBuilder.from(v.getContext())
                        .to(SwatchColorPreferenceActivity.class)
                        .putExtra(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE, preference)
                        .forResult(mWeakFragment.get(), SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE)
                        .animationSource(v)
                        .start());
        }
    }

//    public class ColorGroupPreferenceViewHolder extends BasePreferenceViewHolder<ColorGroupPreference> {
//        ColorPreferenceViewHolder(final View v) {
//            super(v);
//        }
//
//        @Override
//        public void bind(final WeakReference<SharedPreferences> sharedPrefs, final ColorGroupPreference preference) {
//            super.bind(sharedPrefs, preference);
//            // TODO
//        }
//    }
}