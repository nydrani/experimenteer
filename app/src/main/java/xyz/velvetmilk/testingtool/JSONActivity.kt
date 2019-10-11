package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.threeten.bp.Instant
import timber.log.Timber
import xyz.velvetmilk.testingtool.models.DeviceInfoCore
import xyz.velvetmilk.testingtool.tools.gzip
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class JSONActivity : AppCompatActivity(), CoroutineScope {

    private class InstantTypeAdapter : TypeAdapter<Instant>() {
        @Throws(IOException::class)
        override fun read(`in`: JsonReader): Instant? {
            return Instant.ofEpochMilli(`in`.nextLong())
        }

        @Throws(IOException::class)
        override fun write(out: JsonWriter, value: Instant?) {
            out.value(value?.toEpochMilli())
        }
    }

    companion object {
        private val TAG = JSONActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, JSONActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_json)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val gson = GsonBuilder()
                .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
                .create()

            val stringBuilder = StringBuilder()

            try {
                gson.toJson(
                    DeviceInfoCore.generateDeviceInfo(
                        contentResolver,
                        packageManager,
                        packageName
                    ), stringBuilder
                )
            } catch (e: PackageManager.NameNotFoundException) {
                stringBuilder.appendln(e.localizedMessage)
            }

            base_view.text = stringBuilder.length.toString()
            Timber.d(stringBuilder.toString())
        }

        fab2.setOnClickListener {
            // random test
            val gson = GsonBuilder()
                .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
                .create()

            val jsonBuilder = StringBuilder()
            val stringBuilder = StringBuilder()

            try {
                gson.toJson(
                    DeviceInfoCore.generateDeviceInfo(
                        contentResolver,
                        packageManager,
                        packageName
                    ), jsonBuilder
                )
                val compressed = gzip(jsonBuilder.toString().toByteArray(Charsets.UTF_8))

                stringBuilder.appendln(jsonBuilder.toString().toByteArray(Charsets.UTF_8).size)
                stringBuilder.appendln(compressed.size)
            } catch (e: PackageManager.NameNotFoundException) {
                stringBuilder.appendln(e.localizedMessage)
            }

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
