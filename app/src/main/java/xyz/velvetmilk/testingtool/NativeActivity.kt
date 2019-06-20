package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_native.*


class NativeActivity : AppCompatActivity() {

    companion object {
        private val TAG = NativeActivity::class.simpleName

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
            native_view.text = testingJNILib.nativeByteArray("cool string".fromHexStringUTF8())
        }

        fab3.setOnClickListener {
            native_view.text = testingJNILib.nativeToByteArray("hello there").toHexStringUTF8()
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
