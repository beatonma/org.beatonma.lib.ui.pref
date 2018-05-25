package org.beatonma.lib.ui.pref.activity.data;

import android.view.View;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.beatonma.lib.core.util.Sdk;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

import androidx.databinding.BindingAdapter;

public class ListItem implements Serializable {
    protected final static String TAG = "ListItem";

    @SerializedName("text")
    private String mText;

    @SerializedName("description")
    private String mDescription;

    @SerializedName("value")
    private int mValue;

    @SerializedName("checked")
    private boolean mChecked;

    public String serialize() {
        return new Gson().toJson(this);
    }

    public static ListItem deserialize(final String serialized) {
        return new Gson().fromJson(serialized, ListItem.class);
    }

    public ListItem() {
        mText = "";
        mValue = 0;
    }

    public String text() {
        if (mText == null) {
            mText = "";
        }
        return mText;
    }

    public ListItem text(final String text) {
        mText = text;
        return this;
    }

    public ListItem text(final int number) {
        mText = String.format(Locale.getDefault(), "%d", number);
        return this;
    }

    public ListItem description(final String text) {
        mDescription = text;
        return this;
    }

    public ListItem description(final int number) {
        mDescription = String.format(Locale.getDefault(), "%d", number);
        return this;
    }

    public String description() {
        return mDescription;
    }

    public int value() {
        return mValue;
    }

    public ListItem value(final int value) {
        mValue = value;
        return this;
    }

    public boolean checked() {
        return mChecked;
    }

    public ListItem checked(final boolean checked) {
        mChecked = checked;
        return this;
    }

    @BindingAdapter("app:visible")
    public static void setVisible(final View view, final boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ListItem) {
            final ListItem other = (ListItem) obj;
            return value() == other.value();
        }
        return false;
    }

    public static Comparator<ListItem> getComparator() {
        return (left, right) -> {
            if (Sdk.isKitkat()) {
                try {
                    return Integer.compare(Integer.valueOf(left.text()), Integer.valueOf(right.text()));
                } catch (final NumberFormatException e) {
                    // pass
                }
            }
            return left.text().compareTo(right.text());
        };
    }

    @Override
    public String toString() {
        return serialize();
    }
}
