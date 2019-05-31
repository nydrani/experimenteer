package xyz.velvetmilk.testingtool

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import java.security.Security


class TestingApp : Application() {

    private lateinit var backgroundRunner: BackgroundCoroutineRunner

    override fun onCreate() {
        super.onCreate()

        System.setProperty("javax.net.debug", "all")

        AndroidThreeTen.init(this)
        Timber.plant(Timber.DebugTree())
        backgroundRunner = BackgroundCoroutineRunner()
        backgroundRunner.init()


        listProviders()
    }

    override fun onTerminate() {
        super.onTerminate()

        backgroundRunner.deinit()
    }


    private fun listProviders() {
        for (prov in Security.getProviders()) {
            Timber.d(prov.name)
            Timber.d(prov.info)
        }
    }
}
