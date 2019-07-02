package xyz.velvetmilk.testingtool

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import xyz.velvetmilk.testingtool.di.ApplicationComponent
import xyz.velvetmilk.testingtool.di.ApplicationModule
import xyz.velvetmilk.testingtool.di.DaggerApplicationComponent
import xyz.velvetmilk.testingtool.di.NetworkModule
import xyz.velvetmilk.testingtool.jni.SignalJNILib
import xyz.velvetmilk.testingtool.services.BackgroundCoroutineRunner
import java.security.Security

class TestingApp : Application() {

    companion object {
        private val TAG = TestingApp::class.simpleName
    }

    internal lateinit var appComponent: ApplicationComponent
        private set

    private lateinit var backgroundRunner: BackgroundCoroutineRunner

    internal val signalJNILib = SignalJNILib()

    init {
        signalJNILib.setupSignalHandler()
    }


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

        val lifecycleObserver = object : DefaultLifecycleObserver {
            /*
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
            }
            */

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)

                Timber.d("Entered foreground")
                Toast.makeText(applicationContext, "Entered foreground", Toast.LENGTH_SHORT).show()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)

                Timber.d("Entered background")
                Toast.makeText(applicationContext, "Entered background", Toast.LENGTH_SHORT).show()
            }
        }

        // Process lifecycle observer (single processed app only)
        // NOTE: it doesnt add multiple of the same observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

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
