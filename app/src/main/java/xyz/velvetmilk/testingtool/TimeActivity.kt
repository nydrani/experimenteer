package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_time.*
import org.threeten.bp.*
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit

class TimeActivity : AppCompatActivity() {

    companion object {
        private val TAG = TimeActivity::class.simpleName

        private val BASIC_ISO_DATE_TIME = DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter()
            .withChronology(IsoChronology.INSTANCE)

        fun buildIntent(context: Context): Intent {
            return Intent(context, TimeActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        disposer = CompositeDisposable()

        val stringBuilder = StringBuilder()
        stringBuilder.appendln(Instant.now())
        stringBuilder.appendln(LocalDate.now())
        stringBuilder.appendln(LocalDateTime.now())
        stringBuilder.appendln(OffsetDateTime.now())
        stringBuilder.appendln(ZonedDateTime.now())
        stringBuilder.appendln(LocalTime.now())
        stringBuilder.appendln(LocalDate.of(2019, 6, 1))
        stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDate.of(2019, 7, 1), LocalDate.now()))
        stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDate.of(2019, 7, 2), LocalDate.now()))
        stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDateTime.of(2019, 7, 1, 0, 0, 0), LocalDateTime.now()))
        stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDateTime.of(2019, 7, 2, 0, 0, 0), LocalDateTime.now()))
        stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDateTime.of(2019, 7, 3, 0, 0, 0), LocalDateTime.now()))
        stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDateTime.of(2019, 7, 3, 0, 0, 0).toLocalDate(), LocalDate.now()))
        stringBuilder.appendln(DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()))
        stringBuilder.appendln(BASIC_ISO_DATE_TIME.format(LocalDateTime.now()))
        stringBuilder.appendln(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))

        // NOTE: crashes
        // stringBuilder.appendln(ChronoUnit.DAYS.between(LocalDateTime.of(2019, 7, 3, 0, 0, 0), LocalDate.now()))

        time_view.text = String.format("%s", stringBuilder)
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
