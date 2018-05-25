package org.beatonma.lib.ui.pref.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.beatonma.lib.core.util.CollectionUtil;
import org.beatonma.lib.load.AsyncResult;
import org.beatonma.lib.load.SupportBaseAsyncTaskLoader;
import org.beatonma.lib.log.Log;
import org.beatonma.lib.prefs.R;
import org.beatonma.lib.prefs.databinding.ActivityListBinding;
import org.beatonma.lib.prefs.databinding.VhListItemSingleBinding;
import org.beatonma.lib.ui.activity.popup.PopupActivity;
import org.beatonma.lib.ui.pref.activity.data.ListItem;
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
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public class ListPreferenceActivity extends PopupActivity<ActivityListBinding>
        implements LoaderManager.LoaderCallbacks<AsyncResult<List<ListItem>>> {
    public final static String EXTRA_LIST_PREFERENCE = "extra_list_preference";
    public final static int REQUEST_CODE_UPDATE = 936;

    private final static int LIST_LOADER = 91;

    private ListPreference mListPreference;

    private ActivityListBinding mBinding;
    private final ListAdapter mAdapter = buildAdapter();
    private List<ListItem> mItems;

    @Override
    protected ActivityListBinding getBinding() {
        return mBinding;
    }

    public ListAdapter buildAdapter() {
        final ListAdapter adapter = new ListAdapter();
        adapter.setEmptyViews(new EmptyViewsAdapter() {
            @Override
            public Collection getDataset() {
                return getItems();
            }
        });
        return adapter;
    }

    public List<ListItem> getItems() {
        return mItems;
    }

    @Override
    protected void initExtras(final Bundle extras) {
        super.initExtras(extras);
        mListPreference = (ListPreference) extras.getSerializable(EXTRA_LIST_PREFERENCE);
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

        setTitle(mListPreference.getName());

        LoaderManager.getInstance(this).initLoader(LIST_LOADER, null, this);
    }

//    @Override
//    protected ActivityListBinding getBinding() {
//        return mBinding;
//    }

    @Override
    public Loader<AsyncResult<List<ListItem>>> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LIST_LOADER:
                return new PrefListLoader(this, mListPreference);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<AsyncResult<List<ListItem>>> loader,
                               final AsyncResult<List<ListItem>> result) {
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
    public void onLoaderReset(final Loader<AsyncResult<List<ListItem>>> loader) {

    }


    public static class PrefListLoader extends SupportBaseAsyncTaskLoader<List<ListItem>> {
        private final ListPreference preference;

        PrefListLoader(final Context context, final ListPreference preference) {
            super(context);
            this.preference = preference;
        }

        @Override
        public AsyncResult<List<ListItem>> loadInBackground() {
            final AsyncResult.Builder<List<ListItem>> result = AsyncResult.getBuilder();
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
        protected void onReleaseResources(final AsyncResult<List<ListItem>> data) {

        }
    }

    private class ListAdapter extends EmptyBaseRecyclerViewAdapter {
        @Override
        public List getItems() {
            return mItems;
        }

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
