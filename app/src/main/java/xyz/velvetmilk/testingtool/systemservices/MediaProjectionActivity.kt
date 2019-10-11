package xyz.velvetmilk.testingtool.systemservices

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_media_projection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import xyz.velvetmilk.testingtool.R
import kotlin.coroutines.CoroutineContext
import android.util.DisplayMetrics
import java.io.File

class MediaProjectionActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = MediaProjectionActivity::class.simpleName
        private const val SCREEN_CAPTURE_REQUEST_CODE = 1234

        fun buildIntent(context: Context): Intent {
            return Intent(context, MediaProjectionActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var virtualDisplay: VirtualDisplay

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()

            Timber.d("onStop")
            if (::virtualDisplay.isInitialized) {
                virtualDisplay.release()
            }

            mediaRecorder.stop()
            mediaRecorder.reset()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_projection)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaRecorder = MediaRecorder()


        fab.setOnClickListener {
            // if one already exists quit
            val intent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(intent, SCREEN_CAPTURE_REQUEST_CODE)
        }

        fab2.setOnClickListener {
            val stringBuilder = StringBuilder()
            base_view.text = stringBuilder.toString()
        }

        fab3.setOnClickListener {
            if (!::mediaProjection.isInitialized) {
                return@setOnClickListener
            }

            mediaProjection.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::mediaProjection.isInitialized) {
            mediaProjection.stop()
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SCREEN_CAPTURE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        prepareMediaRecorder()
                        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)
                        mediaProjection.registerCallback(mediaProjectionCallback, null)

                        // create new display
                        // old display should automatically stop
                        val metrics = DisplayMetrics()
                        windowManager.defaultDisplay.getMetrics(metrics)
                        val screenDensity = metrics.densityDpi

                        virtualDisplay = mediaProjection.createVirtualDisplay(
                            "MediaProjectionActivity",
                            1080, 2160, screenDensity,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                            mediaRecorder.surface, null, null
                        )
                        mediaRecorder.start()
                    }
                }
            }
            else ->  {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun prepareMediaRecorder() {
        // prepare here
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setVideoEncodingBitRate(12288 * 1000)
        mediaRecorder.setVideoFrameRate(60)
        mediaRecorder.setVideoSize(1080, 2160)
        mediaRecorder.setOutputFile(File.createTempFile("ScreenRecord", ".mp4", getExternalFilesDir(null)).absolutePath)
        mediaRecorder.prepare()
    }
}
