package xyz.velvetmilk.testingtool.jni

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("UnsafeDynamicallyLoadedCode")
class ExternalJniLib @Throws(UnsatisfiedLinkError::class) constructor(context: Context, type: Boolean) {

    init {
        // NOTE: this will only load one, not possible to unload a library with inbuilt classloader
        if (type) {
            System.loadLibrary("external")
        } else {
            System.load(File(context.filesDir, "libexternal.so").absolutePath)
        }
    }

    external fun ping(): Boolean
}
