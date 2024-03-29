package xyz.velvetmilk.testingtool.tools

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.Base64
import android.util.TypedValue
import android.view.View
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.experimental.and

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
        builder.setLength(builder.length - 2)
    }
    builder.append(']')

    return builder.toString()
}

@kotlin.ExperimentalUnsignedTypes
fun ByteArray?.toUByteString(): String {
    if (this == null) {
        return "null"
    }

    val builder = StringBuilder()
    builder.append('[')
    for (item in this) {
        builder.append(item.toUByte())
        builder.append(", ")
    }
    if (this.isNotEmpty()) {
        builder.setLength(builder.length - 2)
    }
    builder.append(']')

    return builder.toString()
}


@kotlin.ExperimentalUnsignedTypes
fun ByteArray?.toUNibbleString(): String {
    if (this == null) {
        return "null"
    }

    val builder = StringBuilder()
    builder.append('[')
    for (item in this) {
        builder.append((item.toInt() and 0xF0) ushr 4)
        builder.append(", ")
        builder.append(item and 0x0F)
        builder.append(", ")
    }
    if (this.isNotEmpty()) {
        builder.setLength(builder.length - 2)
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

fun encodeHexString(data: ByteArray): String {
    val digitsLower = charArrayOf(
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'a',
        'b',
        'c',
        'd',
        'e',
        'f'
    )
    val l = data.size
    val out = CharArray(l shl 1)
    // two characters form the hex value.
    var i = 0
    var j = 0
    while (i < l) {
        out[j++] = digitsLower[(0xF0 and data[i].toInt()).ushr(4)]
        out[j++] = digitsLower[0x0F and data[i].toInt()]
        i++
    }
    return String(out)
}

fun gzip(bytes: ByteArray): ByteArray {
    ByteArrayOutputStream().use { bos ->
        GZIPOutputStream(bos).use {
            it.write(bytes)
        }
        return bos.toByteArray()
    }
}

fun ungzip(bytes: ByteArray): ByteArray {
    GZIPInputStream(bytes.inputStream()).use {
        return it.readBytes()
    }
}
