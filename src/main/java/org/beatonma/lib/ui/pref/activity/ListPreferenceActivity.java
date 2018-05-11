package org.beatonma.lib.ui.pref.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.beatonma.lib.core.util.CollectionUtil;
import org.beatonma.lib.load.AsyncTaskResult;
import org.beatonma.lib.load.BaseAsyncTaskLoader;
import org.beatonma.lib.log.Log;
import org.beatonma.lib.prefs.R;
import org.beatonma.lib.prefs.databinding.ActivityListBinding;
import org.beatonma.lib.prefs.databinding.VhListItemSingleBinding;
import org.beatonma.lib.ui.pref.ListItem;
import org.beatonma.lib.ui.pref.preferences.ListPreference;
import org.beatonma.lib.ui.recyclerview.BaseViewHolder;
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter;
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter.EmptyViewsAdapter;
import org.beatonma.lib.ui.recyclerview.RVUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

public class ListPreferenceActivity extends BasePreferencePopupActivity<Integer>
        implements LoaderManager.LoaderCallbacks<AsyncTaskResult<List<ListItem>>> {
    public final static String EXTRA_LIST_PREFERENCE = "extra_list_preference";
    public final static int REQUEST_CODE_UPDATE = 936;

    private final static int LIST_LOADER = 91;

    private ListPreference mListPreference;

    private ActivityListBinding mBinding;
    private final ListAdapter mAdapter = new ListAdapter();
    private List<ListItem> mItems;

    @Override
    protected void initExtras(final Bundle extras) {
        super.initExtras(extras);
        mListPreference = (ListPreference) extras.getSerializable(EXTRA_LIST_PREFERENCE);
    }

    @Override
    public void save(final Integer obj) {
//        final SharedPreferences.Editor editor = getSharedPreferences(mListPreference.getPrefs(), MODE_PRIVATE).edit();
//        editor.putInt(mListPreference.getKey(), obj);
//        editor.apply();
    }

    public void saveAndClose(final int value, final String name) {
        final SharedPreferences.Editor editor = getSharedPreferences(mListPreference.getPrefs(), MODE_PRIVATE).edit();
        editor.putInt(mListPreference.getKey(), value);
        editor.putString(mListPreference.getDisplayKey(), name);
        editor.apply();

        mListPreference.update(value);
        mListPreference.update(name);
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_LIST_PREFERENCE, mListPreference);
        setResult(RESULT_OK, intent);
        close();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_list;
    }

    @Override
    protected void initLayout(final ViewDataBinding binding) {
        mBinding = (ActivityListBinding) binding;
        RVUtil.setup(mBinding.recyclerview, mAdapter);
        mAdapter.setEmptyViews(new EmptyViewsAdapter() {
            @Override
            public Collection getDataset() {
                return mItems;
            }
        });

        setTitle(mListPreference.getName());

        getLoaderManager().initLoader(LIST_LOADER, null, this);
    }

    @Override
    protected ActivityListBinding getBinding() {
        return mBinding;
    }

    @Override
    public Loader<AsyncTaskResult<List<ListItem>>> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LIST_LOADER:
                return new ListLoader(this, mListPreference);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<AsyncTaskResult<List<ListItem>>> loader,
                               final AsyncTaskResult<List<ListItem>> result) {
        switch (loader.getId()) {
            case LIST_LOADER:
                if (result.isFailure()) {
                    Log.w(TAG, "List loading failed: %s",
                            CollectionUtil.toString(result.getErrors()));
                }
                mAdapter.diff(mItems, result.getData());
                mItems = result.getData();
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<AsyncTaskResult<List<ListItem>>> loader) {

    }



    private static class ListLoader extends BaseAsyncTaskLoader<List<ListItem>> {
        private final ListPreference preference;

        ListLoader(final Context context, final ListPreference preference) {
            super(context);
            this.preference = preference;
        }

        @Override
        public AsyncTaskResult<List<ListItem>> loadInBackground() {
            final AsyncTaskResult.Builder<List<ListItem>> result = AsyncTaskResult.getBuilder();
            final List<ListItem> items = new ArrayList<>();

            final Context context = getContext();
            final Resources resources = context.getResources();

            final String[] display =
                    preference.getDisplayListResourceId() == 0
                            ? null
                            : resources.getStringArray(preference.getDisplayListResourceId());
//            final int[] values =
//                    preference.getValuesListResourceId() == 0
//                            ? null
//                            :resources.getIntArray(preference.getValuesListResourceId());
            if (display == null) {
                result.failure("Unable to read display list");
                return result;
            }

            for (int i = 0; i < display.length; i++) {
//                final int val = values == null ? i : values[i];
                items.add(
                        new ListItem()
                                .text(display[i])
                                .value(i)
                                .checked(i == preference.getSelectedValue()));
            }

            result.success(items);

            return result;
        }

        @Override
        protected void onReset() {
            super.onReset();
        }

        @Override
        protected void onReleaseResources(final AsyncTaskResult<List<ListItem>> data) {

        }
    }

    private class ListAdapter extends EmptyBaseRecyclerViewAdapter {
        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            switch (viewType) {
                case 0:
                    return new ItemViewHolder(inflate(parent, R.layout.vh_list_item_single));
                default:
                    return super.onCreateViewHolder(parent, viewType);
            }
        }

        private class ItemViewHolder extends BaseViewHolder {
            final VhListItemSingleBinding binding;
            ItemViewHolder(final View v) {
                super(v);
                binding = VhListItemSingleBinding.bind(v);
            }

            @Override
            public void bind(final int position) {
                final ListItem item = mItems.get(position);
                binding.setItem(item);

                final View.OnClickListener clickListener = v -> {
                    final int adapterPosition = getAdapterPosition();
                    for (int i = 0; i < mAdapter.getItemCount(); i++) {
                        final ItemViewHolder holder = (ItemViewHolder) mBinding.recyclerview.findViewHolderForAdapterPosition(i);

                        if (holder != null) {
                            holder.binding.radioButton.setChecked(i == adapterPosition);
                        }
                    }

                    saveAndClose(item.value(), item.text());
                };

                itemView.setOnClickListener(clickListener);
                binding.radioButton.setOnClickListener(clickListener);
            }
        }
    }
}
