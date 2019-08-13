package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_native.*
import org.apache.commons.codec.binary.Hex
import timber.log.Timber
import xyz.velvetmilk.testingtool.jni.TestingJniLib
import xyz.velvetmilk.testingtool.tools.fromHexStringUTF8
import xyz.velvetmilk.testingtool.tools.toHexStringUTF8
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

class NativeActivity : AppCompatActivity() {

    companion object {
        private val TAG = NativeActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, NativeActivity::class.java)
        }
    }

    private val testingJNILib = TestingJniLib()
    private val disposer = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener {
            native_view.text = testingJNILib.nativeString()
        }

        fab2.setOnClickListener {
            native_view.text = testingJNILib.nativeByteArray("cool string".fromHexStringUTF8())
        }

        fab3.setOnClickListener {
            native_view.text = testingJNILib.nativeToByteArray("hello there").toHexStringUTF8()
        }

        fab4.setOnClickListener {
            val stringBuilder = StringBuilder()
            // grab the sha256 and show it off here
            val file = File(applicationInfo.nativeLibraryDir, "libtesting.so")
            val path = file.absolutePath

            stringBuilder.appendln(testingJNILib.nativeGrabSha256(path))

            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(file.readBytes())

            stringBuilder.appendln(Hex.encodeHexString(hash))
            native_view.text = stringBuilder.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
