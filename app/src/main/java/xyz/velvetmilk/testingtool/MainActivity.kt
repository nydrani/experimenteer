package xyz.velvetmilk.testingtool

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.slf4j.LoggerFactory
import timber.log.Timber
import xyz.velvetmilk.testingtool.systemservices.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val logger = LoggerFactory.getLogger(MainActivity::class.java)

    private lateinit var disposer: CompositeDisposable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        disposer = CompositeDisposable()

        Log.d(TAG, "hello there from android log")
        Timber.d("hello there from timber")
        logger.debug("hello there from slf4j")

        fab.setOnClickListener {
            Snackbar.make(it, R.string.test_message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.test_action) {
                        Toast.makeText(this, R.string.test_message, Toast.LENGTH_SHORT).show()
                    }
                    .show()
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_activity_manager -> {
                    startActivity(ActivityManagerActivity.buildIntent(this))
                    true
                }
                R.id.nav_actor -> {
                    startActivity(ActorActivity.buildIntent(this))
                    true
                }
                R.id.nav_animation -> {
                    startActivity(AnimationActivity.buildIntent(this))
                    true
                }
                R.id.nav_antidebugging -> {
                    startActivity(AntiDebuggingActivity.buildIntent(this))
                    true
                }
                R.id.nav_appbar_layout -> {
                    startActivity(AppBarLayoutTestActivity.buildIntent(this))
                    true
                }
                R.id.nav_attestation -> {
                    startActivity(AttestationActivity.buildIntent(this))
                    true
                }
                R.id.nav_barcode_scanner -> {
                    startActivity(BarcodeScannerActivity.buildIntent(this))
                    true
                }
                R.id.nav_base -> {
                    startActivity(BaseActivity.buildIntent(this))
                    true
                }
                R.id.nav_cipher -> {
                    startActivity(CipherActivity.buildIntent(this))
                    true
                }
                R.id.nav_collapsing_toolbar -> {
                    startActivity(CollapsingToolbarLayoutTestActivity.buildIntent(this))
                    true
                }
                R.id.nav_connectivity -> {
                    startActivity(ConnectivityActivity.buildIntent(this))
                    true
                }
                R.id.nav_constraint_barrier -> {
                    startActivity(ConstraintBarrierActivity.buildIntent(this))
                    true
                }
                R.id.nav_coroutine -> {
                    startActivity(CoroutineActivity.buildIntent(this))
                    true
                }
                R.id.nav_crypto -> {
                    startActivity(CryptoActivity.buildIntent(this))
                    true
                }
                R.id.nav_dagger -> {
                    startActivity(DaggerActivity.buildIntent(this))
                    true
                }
                R.id.nav_flow -> {
                    startActivity(FlowActivity.buildIntent(this))
                    true
                }
                R.id.nav_focusable -> {
                    startActivity(FocusableActivity.buildIntent(this))
                    true
                }
                R.id.nav_gzip -> {
                    startActivity(GzipActivity.buildIntent(this))
                    true
                }
                R.id.nav_hardware_properties -> {
                    startActivity(HardwarePropertiesActivity.buildIntent(this))
                    true
                }
                R.id.nav_info -> {
                    startActivity(InfoActivity.buildIntent(this))
                    true
                }
                R.id.nav_job_scheduler -> {
                    startActivity(JobSchedulerActivity.buildIntent(this))
                    true
                }
                R.id.nav_json -> {
                    startActivity(JSONActivity.buildIntent(this))
                    true
                }
                R.id.nav_keystore -> {
                    startActivity(KeyStoreActivity.buildIntent(this))
                    true
                }
                R.id.nav_kotlin -> {
                    startActivity(KotlinActivity.buildIntent(this))
                    true
                }
                R.id.nav_location -> {
                    startActivity(LocationActivity.buildIntent(this))
                    true
                }
                R.id.nav_material -> {
                    startActivity(MaterialActivity.buildIntent(this))
                    true
                }
                R.id.nav_media_projection -> {
                    startActivity(MediaProjectionActivity.buildIntent(this))
                    true
                }
                R.id.nav_native -> {
                    startActivity(NativeActivity.buildIntent(this))
                    true
                }
                R.id.nav_network -> {
                    startActivity(NetworkActivity.buildIntent(this))
                    true
                }
                R.id.nav_nfc -> {
                    startActivity(NfcActivity.buildIntent(this))
                    true
                }
                R.id.nav_notification -> {
                    startActivity(NotificationActivity.buildIntent(this))
                    true
                }
                R.id.nav_opensles -> {
                    startActivity(OpenSlesActivity.buildIntent(this))
                    true
                }
                R.id.nav_package -> {
                    startActivity(PackageActivity.buildIntent(this))
                    true
                }
                R.id.nav_pin_block -> {
                    startActivity(PinBlockActivity.buildIntent(this))
                    true
                }
                R.id.nav_play_services -> {
                    startActivity(PlayServicesActivity.buildIntent(this))
                    true
                }
                R.id.nav_ripple -> {
                    startActivity(RippleActivity.buildIntent(this))
                    true
                }
                R.id.nav_rng -> {
                    startActivity(RNGActivity.buildIntent(this))
                    true
                }
                R.id.nav_safety_net -> {
                    startActivity(SafetyNetActivity.buildIntent(this))
                    true
                }
                R.id.nav_secure_socket -> {
                    startActivity(SecureSocketActivity.buildIntent(this))
                    true
                }
                R.id.nav_sensor -> {
                    startActivity(SensorActivity.buildIntent(this))
                    true
                }
                R.id.nav_signal -> {
                    startActivity(SignalActivity.buildIntent(this))
                    true
                }
                R.id.nav_socket -> {
                    startActivity(SocketActivity.buildIntent(this))
                    true
                }
                R.id.nav_subscription -> {
                    startActivity(SubscriptionActivity.buildIntent(this))
                    true
                }
                R.id.nav_system -> {
                    startActivity(SystemActivity.buildIntent(this))
                    true
                }
                R.id.nav_system_health -> {
                    startActivity(SystemHealthActivity.buildIntent(this))
                    true
                }
                R.id.nav_telecom -> {
                    startActivity(TelecomActivity.buildIntent(this))
                    true
                }
                R.id.nav_telephony -> {
                    startActivity(TelephonyActivity.buildIntent(this))
                    true
                }
                R.id.nav_time -> {
                    startActivity(TimeActivity.buildIntent(this))
                    true
                }
                R.id.nav_transition -> {
                    startActivity(TransitionActivity.buildIntent(this))
                    true
                }
                R.id.nav_trusted_screen -> {
                    startActivity(TrustedScreenActivity.buildIntent(this))
                    true
                }
                R.id.nav_usb -> {
                    startActivity(USBActivity.buildIntent(this))
                    true
                }
                R.id.nav_vibrator -> {
                    startActivity(VibratorActivity.buildIntent(this))
                    true
                }
                R.id.nav_window -> {
                    startActivity(WindowActivity.buildIntent(this))
                    true
                }
                R.id.nav_work -> {
                    startActivity(WorkActivity.buildIntent(this))
                    true
                }
                else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(nav_view)) {
            drawer_layout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }
}
