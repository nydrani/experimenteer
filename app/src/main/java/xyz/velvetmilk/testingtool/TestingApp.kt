package xyz.velvetmilk.testingtool

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import xyz.velvetmilk.testingtool.di.ApplicationComponent
import xyz.velvetmilk.testingtool.di.ApplicationModule
import xyz.velvetmilk.testingtool.di.DaggerApplicationComponent
import xyz.velvetmilk.testingtool.di.NetworkModule
import xyz.velvetmilk.testingtool.services.BackgroundCoroutineRunner
import java.security.Security


class TestingApp : Application() {

    companion object {
        private val TAG = TestingApp::class.simpleName
    }

    internal lateinit var appComponent: ApplicationComponent
        private set

    private lateinit var backgroundRunner: BackgroundCoroutineRunner

    override fun onCreate() {
        super.onCreate()

        System.setProperty("javax.net.debug", "all")

        AndroidThreeTen.init(this)
        Timber.plant(Timber.DebugTree())
        backgroundRunner = BackgroundCoroutineRunner()
        backgroundRunner.init()

        // dependency injection
        appComponent = DaggerApplicationComponent
            .factory()
            .create(ApplicationModule(this), NetworkModule())

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
