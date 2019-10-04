package xyz.velvetmilk.testingtool.systemservices

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_window.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.R
import kotlin.coroutines.CoroutineContext

class WindowActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = WindowActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, WindowActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_window)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(display.appVsyncOffsetNanos)
            stringBuilder.appendln(display.displayId)
            stringBuilder.appendln(display.flags)
            stringBuilder.appendln(display.hdrCapabilities)
            stringBuilder.appendln(display.isValid)
            stringBuilder.appendln(display.mode)
            stringBuilder.appendln(display.name)
            stringBuilder.appendln(display.presentationDeadlineNanos)
            stringBuilder.appendln(display.refreshRate)
            stringBuilder.appendln(display.rotation)
            stringBuilder.appendln(display.state)
            stringBuilder.appendln(display.supportedModes)

            stringBuilder.appendln("===== ANDROID O =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                stringBuilder.appendln(display.isHdr)
                stringBuilder.appendln(display.isWideColorGamut)
            }

            stringBuilder.appendln("===== ANDROID Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stringBuilder.appendln(display.cutout)
                stringBuilder.appendln(display.preferredWideGamutColorSpace)
            }

            // other calls
            val outSmallestPoint = Point()
            val outLargestPoint = Point()
            display.getCurrentSizeRange(outSmallestPoint, outLargestPoint)
            stringBuilder.appendln(outSmallestPoint)
            stringBuilder.appendln(outLargestPoint)

            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            stringBuilder.appendln(metrics)

            val realMetrics = DisplayMetrics()
            display.getRealMetrics(realMetrics)
            stringBuilder.appendln(realMetrics)

            val realSize = Point()
            display.getRealSize(realSize)
            stringBuilder.appendln(realSize)

            val rectSize = Rect()
            display.getRectSize(rectSize)
            stringBuilder.appendln(rectSize)

            val size = Point()
            display.getSize(size)
            stringBuilder.appendln(size)


            base_view.text = stringBuilder.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
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
