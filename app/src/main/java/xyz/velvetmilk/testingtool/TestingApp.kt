package xyz.velvetmilk.testingtool

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber


class TestingApp : Application() {

    private lateinit var backgroundRunner: BackgroundCoroutineRunner

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
        Timber.plant(Timber.DebugTree())
        backgroundRunner = BackgroundCoroutineRunner()
        backgroundRunner.init()
    }

    override fun onTerminate() {
        super.onTerminate()

        backgroundRunner.deinit()
    }
}
