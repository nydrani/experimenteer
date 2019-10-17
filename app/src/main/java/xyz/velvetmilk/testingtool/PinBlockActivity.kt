package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_pin_block.*
import xyz.velvetmilk.testingtool.jni.TestingJniLib
import xyz.velvetmilk.testingtool.tools.toUByteString
import kotlin.random.Random

class PinBlockActivity : AppCompatActivity() {

    companion object {
        private val TAG = PinBlockActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, PinBlockActivity::class.java)
        }
    }

    private val testingJNILib = TestingJniLib()

    private lateinit var disposer: CompositeDisposable


    @kotlin.ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_block)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        disposer = CompositeDisposable()

        fab.setOnClickListener {
            testingJNILib.prepare()
        }

        fab2.setOnClickListener {
            testingJNILib.addDigit(Random.nextBits(8).toByte())
        }

        fab3.setOnClickListener {
            native_view.text = testingJNILib.complete().toUByteString()
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
