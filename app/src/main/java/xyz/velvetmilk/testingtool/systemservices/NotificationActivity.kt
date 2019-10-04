package xyz.velvetmilk.testingtool.systemservices

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import android.app.NotificationManager
import android.app.NotificationManager.Policy.*
import android.content.*
import xyz.velvetmilk.testingtool.R
import java.lang.StringBuilder

class NotificationActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = NotificationActivity::class.simpleName
        private const val PREFERENCE_DND_KEY = "PREFERENCE_DND_KEY"

        fun buildIntent(context: Context): Intent {
            return Intent(context, NotificationActivity::class.java)
        }
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    private val notificationPolicyListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val stringBuilder = StringBuilder()
            stringBuilder.appendln("Action: ${intent.action}")
            stringBuilder.appendln("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}")

            base_view2.text = stringBuilder.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // register broadcast receiver here
        registerReceiver(notificationPolicyListener, IntentFilter(NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED))

        // Check if the notification policy access has been granted for the app.
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }

        fab.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                notificationManager.notificationPolicy = NotificationManager.Policy(
                    PRIORITY_CATEGORY_SYSTEM or PRIORITY_CATEGORY_ALARMS or PRIORITY_CATEGORY_MEDIA or
                            PRIORITY_CATEGORY_CALLS or PRIORITY_CATEGORY_EVENTS or
                            PRIORITY_CATEGORY_MESSAGES or PRIORITY_CATEGORY_REMINDERS or
                            PRIORITY_CATEGORY_REPEAT_CALLERS,
                    PRIORITY_SENDERS_ANY,
                    SUPPRESSED_EFFECT_AMBIENT or SUPPRESSED_EFFECT_BADGE or
                            SUPPRESSED_EFFECT_FULL_SCREEN_INTENT or SUPPRESSED_EFFECT_LIGHTS or
                            SUPPRESSED_EFFECT_NOTIFICATION_LIST or SUPPRESSED_EFFECT_PEEK or
                            SUPPRESSED_EFFECT_STATUS_BAR
                )
            } else {
                notificationManager.notificationPolicy = NotificationManager.Policy(
                    PRIORITY_CATEGORY_CALLS or PRIORITY_CATEGORY_EVENTS or
                            PRIORITY_CATEGORY_MESSAGES or PRIORITY_CATEGORY_REMINDERS or
                            PRIORITY_CATEGORY_REPEAT_CALLERS,
                    PRIORITY_SENDERS_ANY,
                    SUPPRESSED_EFFECT_SCREEN_OFF or SUPPRESSED_EFFECT_SCREEN_ON
                )
            }
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            sharedPreferences.edit()
                .putBoolean(PREFERENCE_DND_KEY, true)
                .apply()

            base_view.text = "INTERRUPTION_FILTER_PRIORITY"
        }

        fab2.setOnClickListener {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            sharedPreferences.edit()
                .putBoolean(PREFERENCE_DND_KEY, false)
                .apply()

            base_view.text = "INTERRUPTION_FILTER_ALL"
        }

        fab3.setOnClickListener {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)

            base_view.text = "INTERRUPTION_FILTER_NONE"
        }

        fab4.setOnClickListener {
            val stringBuilder = StringBuilder()
            stringBuilder.appendln(getSharedPreferences(TAG, Context.MODE_PRIVATE).getBoolean(
                PREFERENCE_DND_KEY, false))
            stringBuilder.appendln(notificationManager.currentInterruptionFilter >= NotificationManager.INTERRUPTION_FILTER_PRIORITY)

            base_view.text = stringBuilder.toString()
        }

        fab5.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        disposer.clear()

        unregisterReceiver(notificationPolicyListener)
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
