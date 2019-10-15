package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetStatusCodes
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_safety_net.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class SafetyNetActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SafetyNetActivity::class.simpleName
        private const val apiKey = "***REMOVED***"

        fun buildIntent(context: Context): Intent {
            return Intent(context, SafetyNetActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_net)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            SafetyNet.getClient(this)
                .enableVerifyApps()
                .addOnSuccessListener {
                    launch(Dispatchers.Main) {
                        base_view.text = it.isVerifyAppsEnabled.toString()
                    }
                }
                .addOnFailureListener {
                    launch(Dispatchers.Main) {
                        base_view.text = it.localizedMessage
                    }
                }
        }

        fab2.setOnClickListener {
            SafetyNet.getClient(this)
                .isVerifyAppsEnabled
                .addOnSuccessListener {
                    launch(Dispatchers.Main) {
                        base_view.text = it.isVerifyAppsEnabled.toString()
                    }
                }
                .addOnFailureListener {
                    launch(Dispatchers.Main) {
                        base_view.text = it.localizedMessage
                    }
                }
        }

        fab3.setOnClickListener {
            launch {
                val stringBuilder = StringBuilder()

                try {
                    val res = SafetyNet.getClient(this@SafetyNetActivity)
                        .listHarmfulApps()
                        .await()

                    stringBuilder.appendln(res.harmfulAppsList)
                    stringBuilder.appendln(res.hoursSinceLastScanWithHarmfulApp)
                    stringBuilder.appendln(res.lastScanTimeMs)
                } catch (e: ApiException) {
                    stringBuilder.appendln(e.statusCode)
                    stringBuilder.appendln(SafetyNetStatusCodes.getStatusCodeString(e.statusCode))
                }

                base_view.text = stringBuilder.toString()
            }
        }

        fab4.setOnClickListener {
            launch(Dispatchers.IO) {
                val stringBuilder = StringBuilder()

                try {
                    val res = SafetyNet.getClient(this@SafetyNetActivity)
                        .attest("yeet".toByteArray(Charsets.UTF_8), apiKey)
                        .await()

                    val jwtParts = res.jwsResult.split(".")
                    val decodedResult = Base64.decode(jwtParts[1], Base64.DEFAULT).toString(Charsets.UTF_8)
                    val map = Gson().fromJson(decodedResult, Map::class.java)

                    stringBuilder.appendln(map)
                } catch (e: ApiException) {
                    stringBuilder.appendln(e.statusCode)
                    stringBuilder.appendln(SafetyNetStatusCodes.getStatusCodeString(e.statusCode))
                }

                launch(Dispatchers.Main) {
                    base_view.text = stringBuilder.toString()
                }
            }
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
