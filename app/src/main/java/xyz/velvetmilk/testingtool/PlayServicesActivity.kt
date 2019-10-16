package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import timber.log.Timber
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.net.SslManager
import java.security.Security
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PlayServicesActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = PlayServicesActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, PlayServicesActivity::class.java)
        }
    }

    @Inject
    lateinit var sslManager: SslManager

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_services)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        job = Job()
        disposer = CompositeDisposable()

        val googleApiAvailability = GoogleApiAvailability.getInstance()

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE)
            stringBuilder.appendln(GoogleApiAvailability.GOOGLE_PLAY_STORE_PACKAGE)
            stringBuilder.appendln(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE.toString())

            try {
                val pack = packageManager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0)
                val version: Long = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    pack.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pack.versionCode.toLong()
                }
                stringBuilder.appendln(version)
            } catch (e: PackageManager.NameNotFoundException) {
                stringBuilder.appendln(e.localizedMessage)
            }

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
            sslManager.updateProviderAsync(this, object : ProviderInstaller.ProviderInstallListener {
                override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
//                    recoveryIntent?.let {
//                        startActivityForResult(recoveryIntent, 0)
//                    }
                    googleApiAvailability.showErrorDialogFragment(this@PlayServicesActivity, errorCode, 0)
                }

                override fun onProviderInstalled() {
                    base_view.text = "Provider installed successfully"

                    for (prov in Security.getProviders()) {
                        Timber.d(prov.name)
                        Timber.d(prov.info)
                    }
                }
            })
        }

        fab4.setOnClickListener {
            val stringBuilder = StringBuilder()

            try {
                val provider = sslManager.getGoogleSecurityProvider()

                stringBuilder.appendln(provider.name)
                stringBuilder.appendln(provider.info)
                stringBuilder.appendln(provider.version)
            } catch (e: IllegalStateException) {
                stringBuilder.appendln(e.localizedMessage)
            }

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
