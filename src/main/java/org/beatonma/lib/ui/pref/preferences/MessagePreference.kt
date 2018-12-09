package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import org.json.JSONObject

class MessagePreference: DismissiblePreference {
    @VisibleForTesting constructor() : super()

    constructor(source: MessagePreference): super(source)

    constructor(context: Context, obj: JSONObject) : super(context, obj)

    constructor(bundle: Bundle?) : super(bundle)

    override val type: String
        get() = TYPE


    override fun copyOf() = MessagePreference(this)

    companion object {
        const val TYPE = "message"

        @JvmField
        val CREATOR = object : Parcelable.Creator<MessagePreference> {
            override fun createFromParcel(parcel: Parcel?): MessagePreference {
                return MessagePreference(parcel?.readBundle(MessagePreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<MessagePreference?> = arrayOfNulls(size)
        }
    }
}
