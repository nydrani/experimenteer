package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.safetynet.SafetyNet
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_safety_net.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SafetyNetActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SafetyNetActivity::class.simpleName

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
