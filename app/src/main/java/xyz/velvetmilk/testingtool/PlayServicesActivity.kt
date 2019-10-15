package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_play_services.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class PlayServicesActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = PlayServicesActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, PlayServicesActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_services)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        val googleApiAvailability = GoogleApiAvailability.getInstance()

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE)
            stringBuilder.appendln(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE.toString())
            stringBuilder.appendln(GoogleApiAvailability.GOOGLE_PLAY_STORE_PACKAGE)

            val result = ConnectionResult(googleApiAvailability.isGooglePlayServicesAvailable(this))
            stringBuilder.appendln(result.errorCode)
            stringBuilder.appendln(result.errorMessage)
            stringBuilder.appendln(result.isSuccess)

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            googleApiAvailability.makeGooglePlayServicesAvailable(this@PlayServicesActivity)
                .addOnSuccessListener {
                    base_view.text = "Google play services update success"
                }
                .addOnFailureListener {
                    base_view.text = it.localizedMessage
                }
                .addOnCanceledListener {
                    base_view.text = "Google play services update cancelled"
                }
        }

        fab3.setOnClickListener {
            ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
                override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
//                    recoveryIntent?.let {
//                        startActivityForResult(recoveryIntent, 0)
//                    }
                    googleApiAvailability.showErrorDialogFragment(this@PlayServicesActivity, errorCode, 0)
                }

                override fun onProviderInstalled() {
                    base_view.text = "Provider installed successfully"
                }
            })
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
