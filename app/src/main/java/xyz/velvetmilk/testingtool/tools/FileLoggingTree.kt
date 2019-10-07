package xyz.velvetmilk.testingtool.tools

import android.content.Context
import android.util.Log
import org.threeten.bp.LocalDateTime
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.temporal.ChronoField
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException

class FileLoggingTree : Timber.DebugTree() {

    companion object {
        private val TAG = FileLoggingTree::class.simpleName
        private const val FOLDER_NAME = "logs"

        private val BASIC_ISO_DATE_TIME = DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter()
            .withChronology(IsoChronology.INSTANCE)

        fun clearLogs(context: Context) {
            val folder = File(context.getExternalFilesDir(null), FOLDER_NAME)
            folder.delete()
        }
    }

    private lateinit var logFile: File


    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!::logFile.isInitialized) {
            // didnt make the file (dont log anything)
            return
        }

        val stringBuilder = StringBuilder()

        when (priority) {
            Log.ASSERT -> stringBuilder.append('A')
            Log.DEBUG -> stringBuilder.append('D')
            Log.ERROR -> stringBuilder.append('E')
            Log.INFO -> stringBuilder.append('I')
            Log.WARN -> stringBuilder.append('W')
            Log.VERBOSE -> stringBuilder.append('V')
            else -> stringBuilder.append('?')
        }
        stringBuilder.append('/')
        stringBuilder.append(tag)
        stringBuilder.append(": ")
        stringBuilder.appendln(message)

        FileWriter(logFile, true).use {
            it.write(stringBuilder.toString())
        }
    }

    fun init(context: Context): Boolean {
        // Get the directory for the app's private pictures directory.
        val folder = File(context.getExternalFilesDir(null), FOLDER_NAME)
        folder.mkdirs()

        // clear old logs
        if ((folder.list()?.size ?: 0) > 25) {
            clearLogs(context)
        }

        val fileName = String.format("log-%s.log", BASIC_ISO_DATE_TIME.format(LocalDateTime.now()))

        try {
            logFile = File(folder, fileName).also { it.createNewFile() }
        } catch (e: IOException) {
            // rip log file not created
            return false
        }

        return true
    }

    fun reset() {
        if (!::logFile.isInitialized) {
            // didnt make the file (dont clear anything)
            return
        }

        FileWriter(logFile).use {}
    }
}
