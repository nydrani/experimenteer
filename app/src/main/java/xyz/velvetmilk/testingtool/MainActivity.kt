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

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val disposer = CompositeDisposable()
    private val logger = LoggerFactory.getLogger(MainActivity::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        Log.d(TAG, "hello there from android log")
        Timber.d("hello there from timber")
        logger.warn("hello there from slf4j")

        fab.setOnClickListener {
            Snackbar.make(it, R.string.test_message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.test_action) {
                        Toast.makeText(this, R.string.test_message, Toast.LENGTH_SHORT).show()
                    }
                    .show()
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
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
                R.id.nav_base -> {
                    startActivity(BaseActivity.buildIntent(this))
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
                R.id.nav_focusable -> {
                    startActivity(FocusableActivity.buildIntent(this))
                    true
                }
                R.id.nav_info -> {
                    startActivity(InfoActivity.buildIntent(this))
                    true
                }
                R.id.nav_keystore -> {
                    startActivity(KeyStoreActivity.buildIntent(this))
                    true
                }
                R.id.nav_material -> {
                    startActivity(MaterialActivity.buildIntent(this))
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
                    startActivity(NFCActivity.buildIntent(this))
                    true
                }
                R.id.nav_opensles -> {
                    startActivity(OpenSLESActivity.buildIntent(this))
                    true
                }
                R.id.nav_package -> {
                    startActivity(PackageActivity.buildIntent(this))
                    true
                }
                R.id.nav_ripple -> {
                    startActivity(RippleActivity.buildIntent(this))
                    true
                }
                R.id.nav_secure_socket -> {
                    startActivity(SecureSocketActivity.buildIntent(this))
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
