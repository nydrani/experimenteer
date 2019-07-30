package xyz.velvetmilk.testingtool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_gps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.tools.PermissionsHelper
import kotlin.coroutines.CoroutineContext
import timber.log.Timber
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Tasks
import org.threeten.bp.Instant

class GpsActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = GpsActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, GpsActivity::class.java)
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequest = LocationRequest.create()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            Timber.d(locationAvailability.isLocationAvailable.toString())
        }

        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(String.format("Accuracy: %f", location.accuracy))
            stringBuilder.appendln(String.format("Altitude: %f", location.altitude))
            stringBuilder.appendln(String.format("Bearing: %f", location.bearing))
            stringBuilder.appendln(String.format("Latitude: %f", location.latitude))
            stringBuilder.appendln(String.format("Longitude: %f", location.longitude))
            stringBuilder.appendln(String.format("Speed: %f", location.speed))
            stringBuilder.appendln(String.format("Time: %s", Instant.ofEpochMilli(location.time)))

            base_view.text = stringBuilder.toString()
        }
    }

    init {
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // grab permission for serial read
        PermissionsHelper.requestPermissions(this, PermissionsHelper.gpsPermissions)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val settingsClient = LocationServices.getSettingsClient(this)

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(false)
                .setNeedBle(false)

            try {
                settingsClient.checkLocationSettings(builder.build())
                    .addOnSuccessListener {
                        stringBuilder.appendln(String.format("isGpsUsable: %b", it.locationSettingsStates.isGpsUsable))
                        stringBuilder.appendln(String.format("isNetworkLocationUsable: %b", it.locationSettingsStates.isNetworkLocationUsable))
                        stringBuilder.appendln(String.format("isLocationUsable: %b", it.locationSettingsStates.isLocationUsable))

                    }
                    .addOnFailureListener {
                        val exception = it as ApiException
                        stringBuilder.appendln(exception.statusCode)
                        stringBuilder.appendln(LocationSettingsStatusCodes.getStatusCodeString(exception.statusCode))

                        if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            // what to do
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(this, 0)
                        }
                    }
                    .addOnCanceledListener {
                        Timber.d("Cancelled")
                    }
            } catch (e: SecurityException) {
                // missing permissions
                // whatever
                stringBuilder.appendln("Missing location permissions")
            }

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            val stringBuilder = StringBuilder()

            try {
                fusedLocationClient.locationAvailability
                    .addOnSuccessListener {
                        if (it == null) {
                            // rip
                            stringBuilder.appendln("Unknown location availability")
                            return@addOnSuccessListener
                        }

                        Timber.d(it.isLocationAvailable.toString())
                    }
                    .addOnFailureListener {
                        stringBuilder.appendln("Location availability failed")
                        stringBuilder.appendln(it.localizedMessage)
                    }
                    .addOnCanceledListener {
                        Timber.d("Cancelled")
                    }
                    .continueWithTask {
                        if (it.isSuccessful && it.result!!.isLocationAvailable) {
                            Timber.d("Location available")
                            return@continueWithTask fusedLocationClient.lastLocation
                        }
                        Tasks.forResult<Location>(null)
                    }
                    .addOnSuccessListener {
                        if (it == null) {
                            // rip
                            stringBuilder.appendln("No last location")
                            return@addOnSuccessListener
                        }

                        stringBuilder.appendln(String.format("Accuracy: %f", it.accuracy))
                        stringBuilder.appendln(String.format("Altitude: %f", it.altitude))
                        stringBuilder.appendln(String.format("Bearing: %f", it.bearing))
                        stringBuilder.appendln(String.format("Latitude: %f", it.latitude))
                        stringBuilder.appendln(String.format("Longitude: %f", it.longitude))
                        stringBuilder.appendln(String.format("Speed: %f", it.speed))
                        stringBuilder.appendln(String.format("Time: %s", Instant.ofEpochMilli(it.time)))

                    }
                    .addOnFailureListener {
                        stringBuilder.appendln("Last location failed")
                        stringBuilder.appendln(it.localizedMessage)
                    }
                    .addOnCanceledListener {
                        Timber.d("Cancelled")
                    }
            } catch (e: SecurityException) {
                // missing permissions
                // whatever
                stringBuilder.appendln("Missing location permissions")
            }

            base_view.text = stringBuilder.toString()
        }

        fab3.setOnClickListener {
            requestSingleLocationUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient.removeLocationUpdates(locationCallback)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                //something
                if (resultCode == Activity.RESULT_OK) {
                    // win
                    Timber.d("Resolved")
                    requestSingleLocationUpdate()
                } else {
                    Timber.d("Unresolved")
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


    private fun requestSingleLocationUpdate() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                .addOnSuccessListener {
                    Timber.d("Location update callback added")
                }
                .addOnFailureListener {
                    Timber.d("Location request failed")
                    Timber.e(it)
                }
                .addOnCanceledListener {
                    Timber.d("Cancelled")
                }
        } catch (e: SecurityException) {
            // missing permissions
            // whatever
            base_view.text = "Missing location permissions"
        }
    }
}
