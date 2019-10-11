package xyz.velvetmilk.testingtool.systemservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_telecom.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import xyz.velvetmilk.testingtool.R
import xyz.velvetmilk.testingtool.tools.PermissionsHelper

class TelecomActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = TelecomActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, TelecomActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telecom)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // Request permissions
        PermissionsHelper.requestPermissions(this, PermissionsHelper.telecomPermissions)


        fab.setOnClickListener {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(telecomManager.defaultDialerPackage)
            stringBuilder.appendln(telecomManager.simCallManager)

            try {
                stringBuilder.appendln(telecomManager.isInCall)
                val accounts = telecomManager.callCapablePhoneAccounts
                stringBuilder.appendln(accounts)

                // other calls
                stringBuilder.appendln(telecomManager.getDefaultOutgoingPhoneAccount("tel"))

                for (account in accounts) {
                    stringBuilder.appendln(account)
                    stringBuilder.appendln(telecomManager.getLine1Number(account))
                    stringBuilder.appendln(telecomManager.getVoiceMailNumber(account))
                    stringBuilder.appendln(telecomManager.getPhoneAccount(account))
                }
            } catch (e: SecurityException) {
                stringBuilder.appendln("TelecomManager: No READ_PHONE_STATE permission")
            }

            stringBuilder.appendln("===== Android O =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    stringBuilder.appendln(telecomManager.isInManagedCall)
                    stringBuilder.appendln(telecomManager.selfManagedPhoneAccounts)
                } catch (e: SecurityException) {
                    stringBuilder.appendln("TelecomManager: No READ_PHONE_STATE permission")
                }
            }

            stringBuilder.appendln("===== Android P =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                try {
                    stringBuilder.appendln(telecomManager.isTtySupported)
                } catch (e: SecurityException) {
                    stringBuilder.appendln("TelecomManager: No READ_PHONE_STATE permission")
                }
            }

            stringBuilder.appendln("===== Android Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stringBuilder.appendln(telecomManager.systemDialerPackage)
                stringBuilder.appendln(telecomManager.userSelectedOutgoingPhoneAccount)
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
