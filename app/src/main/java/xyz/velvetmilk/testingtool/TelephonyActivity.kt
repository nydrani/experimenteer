package xyz.velvetmilk.testingtool

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_telephony.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import android.telephony.TelephonyManager
import xyz.velvetmilk.testingtool.tools.PermissionsHelper

@SuppressLint("HardwareIds")
class TelephonyActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = TelephonyActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, TelephonyActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telephony)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // Request permissions
        PermissionsHelper.requestPermissions(this, PermissionsHelper.telephonyPermissions)


        fab.setOnClickListener {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(telephonyManager.callState)
            stringBuilder.appendln(telephonyManager.dataActivity)
            stringBuilder.appendln(telephonyManager.dataState)
            stringBuilder.appendln(telephonyManager.isHearingAidCompatibilitySupported)
            stringBuilder.appendln(telephonyManager.isNetworkRoaming)
            stringBuilder.appendln(telephonyManager.isSmsCapable)
            stringBuilder.appendln(telephonyManager.isVoiceCapable)
            stringBuilder.appendln(telephonyManager.isWorldPhone)
            stringBuilder.appendln(telephonyManager.mmsUAProfUrl)
            stringBuilder.appendln(telephonyManager.mmsUserAgent)
            stringBuilder.appendln(telephonyManager.networkCountryIso)
            stringBuilder.appendln(telephonyManager.networkOperator)
            stringBuilder.appendln(telephonyManager.networkOperatorName)
            stringBuilder.appendln(telephonyManager.networkType)
            stringBuilder.appendln(telephonyManager.phoneCount)
            stringBuilder.appendln(telephonyManager.phoneType)
            stringBuilder.appendln(telephonyManager.simCountryIso)
            stringBuilder.appendln(telephonyManager.simOperator)
            stringBuilder.appendln(telephonyManager.simOperatorName)
            stringBuilder.appendln(telephonyManager.simState)

            // SecurityException
            try {
                stringBuilder.appendln("===== All Cell Info =====")
                telephonyManager.allCellInfo?.let {
                    for (cellInfo in it) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            stringBuilder.appendln(cellInfo.cellConnectionStatus)
                        }
                        stringBuilder.appendln(cellInfo.isRegistered)
                        stringBuilder.appendln(cellInfo.timeStamp)
                    }
                }
                stringBuilder.appendln("===== End All Cell Info =====")
            } catch (e: SecurityException) {
                stringBuilder.appendln("TelephonyManager: No location permissions")
            }

            try {
                stringBuilder.appendln(telephonyManager.dataNetworkType)
                stringBuilder.appendln(telephonyManager.deviceSoftwareVersion)
                stringBuilder.appendln(telephonyManager.groupIdLevel1)
                stringBuilder.appendln(telephonyManager.line1Number)
                stringBuilder.appendln(telephonyManager.simSerialNumber)
                stringBuilder.appendln(telephonyManager.subscriberId)
                stringBuilder.appendln(telephonyManager.voiceMailAlphaTag)
                stringBuilder.appendln(telephonyManager.voiceMailNumber)
                stringBuilder.appendln(telephonyManager.voiceNetworkType)
            } catch (e: SecurityException) {
                stringBuilder.appendln("TelephonyManager: No read phone permissions")
            }

            stringBuilder.appendln(telephonyManager.canChangeDtmfToneLength())
            stringBuilder.appendln(telephonyManager.hasCarrierPrivileges())
            stringBuilder.appendln(telephonyManager.hasIccCard())

            // android O
            stringBuilder.appendln("===== Android O =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                stringBuilder.appendln(telephonyManager.isConcurrentVoiceAndDataSupported)
                stringBuilder.appendln(telephonyManager.isDataEnabled)
                stringBuilder.appendln(telephonyManager.networkSpecifier)

                try {
                    stringBuilder.appendln(telephonyManager.carrierConfig)
                    stringBuilder.appendln(telephonyManager.forbiddenPlmns.toString())
                    stringBuilder.appendln(telephonyManager.imei)
                    stringBuilder.appendln(telephonyManager.meid)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        stringBuilder.appendln(telephonyManager.serviceState.cdmaNetworkId)
                        stringBuilder.appendln(telephonyManager.serviceState.cdmaSystemId)
                        stringBuilder.appendln(telephonyManager.serviceState.cellBandwidths.toString())
                        stringBuilder.appendln(telephonyManager.serviceState.channelNumber)
                        stringBuilder.appendln(telephonyManager.serviceState.duplexMode)
                    }
                    stringBuilder.appendln(telephonyManager.serviceState.isManualSelection)
                    stringBuilder.appendln(telephonyManager.serviceState.operatorAlphaLong)
                    stringBuilder.appendln(telephonyManager.serviceState.operatorAlphaShort)
                    stringBuilder.appendln(telephonyManager.serviceState.operatorNumeric)
                    stringBuilder.appendln(telephonyManager.serviceState.roaming)
                    stringBuilder.appendln(telephonyManager.serviceState.state)
                    stringBuilder.appendln(telephonyManager.visualVoicemailPackageName)
                } catch (e: SecurityException) {
                    stringBuilder.appendln("Android O: No read phone permissions")
                }
            } else {
                // imei
                try {
                    stringBuilder.appendln(telephonyManager.deviceId)
                } catch (e: SecurityException) {
                    stringBuilder.appendln("Android O: No read phone permissions")
                }
            }

            // android P
            stringBuilder.appendln("===== Android P =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                stringBuilder.appendln(telephonyManager.signalStrength)
                stringBuilder.appendln(telephonyManager.simCarrierId)
                stringBuilder.appendln(telephonyManager.simCarrierIdName)

                try {
                    stringBuilder.appendln(telephonyManager.nai)
                } catch (e: SecurityException) {
                    stringBuilder.appendln("Android P: No read phone permissions")
                }
            } else {
                stringBuilder.appendln(telephonyManager.isTtyModeSupported)
            }

            // android Q
            stringBuilder.appendln("===== Android Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stringBuilder.appendln(telephonyManager.cardIdForDefaultEuicc)
                stringBuilder.appendln(telephonyManager.carrierIdFromSimMccMnc)
                for (map in telephonyManager.emergencyNumberList) {
                    for (number in map.value) {
                        stringBuilder.appendln(number.countryIso)
                        stringBuilder.appendln(number.mnc)
                        stringBuilder.appendln(number.number)
                    }
                }
                stringBuilder.appendln(telephonyManager.isDataRoamingEnabled)
                stringBuilder.appendln(telephonyManager.isMultiSimSupported)
                stringBuilder.appendln(telephonyManager.isRttSupported)
                stringBuilder.appendln(telephonyManager.manufacturerCode)
                stringBuilder.appendln(telephonyManager.preferredOpportunisticDataSubscription)
                stringBuilder.appendln(telephonyManager.simSpecificCarrierId)
                stringBuilder.appendln(telephonyManager.simSpecificCarrierIdName)
                stringBuilder.appendln(telephonyManager.typeAllocationCode)
                stringBuilder.appendln(telephonyManager.uiccCardsInfo)

                stringBuilder.appendln(telephonyManager.doesSwitchMultiSimConfigTriggerReboot())
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
