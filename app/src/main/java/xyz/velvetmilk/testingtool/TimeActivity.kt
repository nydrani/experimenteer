package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_time.*
import org.threeten.bp.*


class TimeActivity : AppCompatActivity() {

    companion object {
        private val TAG = TimeActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, TimeActivity::class.java)
        }
    }

    private val disposer = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val stringBuilder = StringBuilder()
        stringBuilder.appendln(Instant.now())
        stringBuilder.appendln(LocalDate.now())
        stringBuilder.appendln(LocalDateTime.now())
        stringBuilder.appendln(OffsetDateTime.now())
        stringBuilder.appendln(ZonedDateTime.now())
        stringBuilder.appendln(LocalTime.now())

        time_view.text = String.format("%s", stringBuilder)
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
}