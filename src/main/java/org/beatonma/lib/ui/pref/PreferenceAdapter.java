package org.beatonma.lib.ui.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.beatonma.lib.prefs.R;
import org.beatonma.lib.ui.activity.ActivityBuilder;
import org.beatonma.lib.ui.pref.activity.ListPreferenceActivity;
import org.beatonma.lib.ui.pref.preferences.BasePreference;
import org.beatonma.lib.ui.pref.preferences.BooleanPreference;
import org.beatonma.lib.ui.pref.preferences.ListPreference;
import org.beatonma.lib.ui.pref.preferences.PreferenceGroup;
import org.beatonma.lib.ui.pref.preferences.SimplePreference;
import org.beatonma.lib.ui.pref.viewholders.BasePreferenceViewHolder;
import org.beatonma.lib.ui.recyclerview.BaseViewHolder;
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.Collection;

import androidx.annotation.NonNull;

public class PreferenceAdapter extends EmptyBaseRecyclerViewAdapter {
    private final static String TAG = "PreferenceAdapter";

    private final static int TYPE_SIMPLE = 0;
    private final static int TYPE_BOOLEAN = 1;
    private final static int TYPE_LIST_SINGLE = 2;
    private final static int TYPE_LIST_MULTI = 3;
    private final static int TYPE_GROUP = 65;

    private final WeakReference<PreferenceFragment> mWeakFragment;
    private WeakReference<SharedPreferences> mWeakPrefs;
    private PreferenceGroup mPreferenceGroup;

    public PreferenceAdapter() {
        this(null);
    }

    public PreferenceAdapter(final PreferenceFragment fragmentContext) {
        mWeakFragment = fragmentContext == null ? null : new WeakReference<>(fragmentContext);
        setEmptyViews(new EmptyViewsAdapter() {
            @Override
            public Collection<?> getDataset() {
                return mPreferenceGroup == null ? null : mPreferenceGroup.getPreferences();
            }
        });
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

    public void notifyUpdate(final String key, final String value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    public void notifyUpdate(final String key, final int value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    public void notifyUpdate(final String key, final boolean value) {
        final int position = mPreferenceGroup.notifyUpdate(key, value);
        if (position >= 0) {
            notifyItemChanged(position);
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
                    v -> ActivityBuilder.forActivity(itemView.getContext(), ListPreferenceActivity.class)
                            .putExtra(ListPreferenceActivity.EXTRA_LIST_PREFERENCE, preference)
                            .forResult(mWeakFragment.get(), ListPreferenceActivity.REQUEST_CODE_UPDATE)
                            .animationSource(v)
                            .start());
        }
    }

}