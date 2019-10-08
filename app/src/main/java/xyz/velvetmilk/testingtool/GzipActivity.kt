package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_gzip.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.tools.getRandomString
import xyz.velvetmilk.testingtool.tools.gzip
import xyz.velvetmilk.testingtool.tools.ungzip
import kotlin.coroutines.CoroutineContext

class GzipActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = GzipActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, GzipActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gzip)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()

            val coolString = getRandomString(1000)
            stringBuilder.appendln(coolString)
            stringBuilder.appendln(coolString.toByteArray(Charsets.UTF_8).size)

            val compressed = gzip(coolString.toByteArray(Charsets.UTF_8))
            stringBuilder.appendln(compressed.size)

            val decompressed = ungzip(compressed)
            stringBuilder.appendln(decompressed.size)
            stringBuilder.appendln(decompressed.toString(Charsets.UTF_8))

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
