package xyz.velvetmilk.testingtool.tools

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.Base64
import android.util.TypedValue
import android.view.View
import io.reactivex.Observable
import java.lang.StringBuilder


fun dpToPx(context: Context, valueInDp: Float): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
}

fun ByteArray?.toHexStringUTF8(): String {
    if (this == null) {
        return "null"
    }

    return String(this, Charsets.UTF_8)
}

fun String?.fromHexStringUTF8(): ByteArray {
    if (this == null) {
        return byteArrayOf()
    }

    return toByteArray(Charsets.UTF_8)
}

fun ByteArray?.toBase64(): String {
    if (this == null) {
        return "null"
    }

    return Base64.encodeToString(this, Base64.DEFAULT)
}

fun String?.fromBase64(): ByteArray {
    if (this == null) {
        return byteArrayOf()
    }

    return Base64.decode(this, Base64.DEFAULT)
}

fun ByteArray?.toByteString(): String {
    if (this == null) {
        return "null"
    }

    val builder = StringBuilder()
    builder.append('[')
    for (item in this) {
        builder.append(item)
        builder.append(", ")
    }
    if (this.isNotEmpty()) {
        builder.deleteCharAt(builder.length - 1)
        builder.deleteCharAt(builder.length - 1)
    }
    builder.append(']')

    return builder.toString()
}

fun ByteArray?.toRawString(size: Int, offset: Int): String {
    if (this == null) {
        return "null"
    }

    val stringBuilder = StringBuilder()
    for (i in offset until size + offset) {
        stringBuilder.append(this[i])
    }

    return stringBuilder.toString()
}

fun keyboardCheckObservable(activity: Activity): Observable<Boolean> {
    val contentView = activity.findViewById<View>(android.R.id.content)

    return Observable.create<Boolean> {
        // NOTE: May need to remove global layout listener?
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect().apply {
                contentView.getWindowVisibleDisplayFrame(this)
            }

            val heightDiff = contentView.rootView.height - (r.bottom - r.top)
            if (heightDiff > dpToPx(activity, 200.0f)) { // if more than 200 dp, it's probably a keyboard...
                it.onNext(true)
            } else {
                it.onNext(false)
            }
        }
    }.distinctUntilChanged()
}

fun getRandomString(length: Int) : String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz"
    return (1..length)
        .map {
            allowedChars.random()
        }
        .joinToString("")
}
