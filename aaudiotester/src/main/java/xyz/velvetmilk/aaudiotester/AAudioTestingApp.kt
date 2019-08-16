package xyz.velvetmilk.aaudiotester

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import timber.log.Timber

class AAudioTestingApp : Application() {

    companion object {
        private val TAG = AAudioTestingApp::class.simpleName
    }

    override fun onCreate() {
        super.onCreate()

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
    }
}
