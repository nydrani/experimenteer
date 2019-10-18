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
import xyz.velvetmilk.testingtool.tools.toUNibbleString
import kotlin.experimental.and
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

        val currentPin = StringBuilder()

        fab.setOnClickListener {
            testingJNILib.prepare()
            currentPin.clear()
        }

        fab2.setOnClickListener {
            val number = Random.nextBits(8).toByte() and 0x0F

            if (testingJNILib.addDigit(number)) {
                currentPin.append(Integer.toHexString(number.toInt()))
            }

            native_view.text = currentPin.toString()
        }

        fab3.setOnClickListener {
            if (testingJNILib.removeDigit()) {
                currentPin.setLength(currentPin.length - 1)
            }

            native_view.text = currentPin.toString()
        }

        fab4.setOnClickListener {
            val stringBuilder = StringBuilder()
            val res = testingJNILib.complete()
            currentPin.clear()

            stringBuilder.appendln(res.toUByteString())
            stringBuilder.appendln(res.toUNibbleString())

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
