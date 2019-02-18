package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_native.*
import timber.log.Timber
import java.nio.charset.Charset

class NativeActivity : AppCompatActivity() {

    companion object {
        private val TAG = NativeActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, NativeActivity::class.java)
        }
    }

    private val testingJNILib = TestingJNILib()
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
            native_view.text = testingJNILib.nativeByteArray("cool string".toByteArrayUTF8())
        }

        fab3.setOnClickListener {
            native_view.text = testingJNILib.nativeToByteArray("hello there").toHexStringUTF8()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun ByteArray.toHexStringUTF8() : String {
        return String(this, Charset.forName("UTF-8"))
    }

    private fun String.toByteArrayUTF8() : ByteArray {
        return toByteArray(Charsets.UTF_8)
    }
}
