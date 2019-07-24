package xyz.velvetmilk.testingtool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_subscription.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class SubscriptionActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SubscriptionActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SubscriptionActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // Request permissions
        requestPermissions()


        fab.setOnClickListener {
            val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val stringBuilder = StringBuilder()

            val numSubscriptions: Int = try {
                subscriptionManager.activeSubscriptionInfoCount
            } catch (e: SecurityException) {
                0
            }
            val maxSubscriptions = subscriptionManager.activeSubscriptionInfoCountMax

            // static methods first
            stringBuilder.appendln(SubscriptionManager.getDefaultDataSubscriptionId())
            stringBuilder.appendln(SubscriptionManager.getDefaultSmsSubscriptionId())
            stringBuilder.appendln(SubscriptionManager.getDefaultSubscriptionId())
            stringBuilder.appendln(SubscriptionManager.getDefaultVoiceSubscriptionId())

            stringBuilder.appendln("===== START STATIC ANDROID Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                for (i in 0..maxSubscriptions) {
                    stringBuilder.appendln(SubscriptionManager.getSlotIndex(i))
                    stringBuilder.appendln(SubscriptionManager.isUsableSubscriptionId(i))
                    stringBuilder.appendln(SubscriptionManager.isValidSubscriptionId(i))
                }
            }
            stringBuilder.appendln("===== END STATIC ANDROID Q =====")


            stringBuilder.appendln(subscriptionManager.activeSubscriptionInfoCountMax)
            for (i in 0..maxSubscriptions) {
                stringBuilder.appendln(subscriptionManager.isNetworkRoaming(i))
            }
            try {
                for (i in 0..maxSubscriptions) {
                    val info = subscriptionManager.getActiveSubscriptionInfo(i)
                    info?.let {
                        stringBuilder.appendln(it.subscriptionId)
                        stringBuilder.appendln(it.simSlotIndex)
                        stringBuilder.appendln(it.number)
                    }
                }
                stringBuilder.appendln(subscriptionManager.activeSubscriptionInfoCount)
//                stringBuilder.appendln(subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex())
            } catch (e: SecurityException) {
                stringBuilder.appendln("SubscriptionManager: No read phone permissions")
            }

            stringBuilder.appendln("===== START ANDROID P =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                subscriptionManager.accessibleSubscriptionInfoList?.let {
                    for (subs in it) {
                        stringBuilder.append(subs.displayName)
                        stringBuilder.append(" | ")
                        stringBuilder.appendln(subs.subscriptionId)
                    }
                }
//                stringBuilder.appendln(subscriptionManager.canManageSubscription())
//                stringBuilder.appendln(subscriptionManager.getSubscriptionPlans())
            }
            stringBuilder.appendln("===== END ANDROID P =====")

            stringBuilder.appendln("===== START ANDROID Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                stringBuilder.appendln(subscriptionManager.getSubscriptionIds())
//                stringBuilder.appendln(subscriptionManager.getSubscriptionsInGroup())
//                stringBuilder.appendln(subscriptionManager.isActiveSubscriptionId())
                for (subs in subscriptionManager.opportunisticSubscriptions) {
                    stringBuilder.append(subs.displayName)
                    stringBuilder.append(" | ")
                    stringBuilder.appendln(subs.subscriptionId)
                }
            }
            stringBuilder.appendln("===== END ANDROID Q =====")

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

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0)
                break
            }
        }
    }
}
