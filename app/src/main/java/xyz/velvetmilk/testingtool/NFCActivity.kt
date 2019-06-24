package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_nfc.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.velvetmilk.testingtool.tools.toByteString

class NFCActivity : AppCompatActivity() {

    companion object {
        private val TAG = NFCActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, NFCActivity::class.java)
        }
    }

    private var count = 0
    private var nfcAdapter: NfcAdapter? = null
    private val disposer = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // check for nfc availability
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            nfc_view.text = "No NFC available on this device"
        } else {
            nfc_view.text = "NFC found on this device"
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Turning on NFC read mode", Snackbar.LENGTH_SHORT).show()
            count++

            // enable nfc
            nfcAdapter?.let {
                if (!it.isEnabled) {
                    return@let
                }

                it.enableReaderMode(this,
                    { tag ->
                        GlobalScope.launch(Dispatchers.Main) {
                            Snackbar.make(view, "NFC read something", Snackbar.LENGTH_LONG).show()
                            Snackbar.make(view, String.format("Count: %d", count), Snackbar.LENGTH_LONG).show()
                        }

                        val stringBuilder = StringBuilder()
                        for (tech in tag.techList) {
                            when (tech) {
                                NfcA::class.java.name -> {
                                    val nfcA = NfcA.get(tag)
                                    nfcA.connect()

                                    stringBuilder.appendln("NFC-A")
                                    stringBuilder.appendln(nfcA.isConnected)
                                    stringBuilder.appendln(nfcA.atqa.toByteString())
                                    stringBuilder.appendln(nfcA.maxTransceiveLength.toString())
                                    stringBuilder.appendln(nfcA.sak.toString())

                                    nfcA.close()
                                }
                                NfcB::class.java.name -> {
                                    val nfcB = NfcB.get(tag)
                                    nfcB.connect()

                                    stringBuilder.appendln("NFC-B")
                                    stringBuilder.appendln(nfcB.isConnected)
                                    stringBuilder.appendln(nfcB.applicationData.toByteString())
                                    stringBuilder.appendln(nfcB.maxTransceiveLength)
                                    stringBuilder.appendln(nfcB.protocolInfo.toByteString())

                                    nfcB.close()
                                }
                                IsoDep::class.java.name -> {
                                    val nfcIso = IsoDep.get(tag)
                                    nfcIso.connect()

                                    stringBuilder.appendln()
                                    stringBuilder.appendln("NFC-ISODEP")
                                    stringBuilder.appendln(nfcIso.isConnected)
                                    stringBuilder.appendln(nfcIso.historicalBytes.toByteString())
                                    stringBuilder.appendln(nfcIso.hiLayerResponse.toByteString())
                                    stringBuilder.appendln(nfcIso.isExtendedLengthApduSupported)
                                    stringBuilder.appendln(nfcIso.maxTransceiveLength)
                                    stringBuilder.appendln(nfcIso.timeout)

                                    nfcIso.close()
                                }
                            }
                        }

                        Timber.d(stringBuilder.toString())

                        GlobalScope.launch(Dispatchers.Main) {
                            nfc_view.text = stringBuilder.toString()
                        }
                    },
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_B,
                    null)
            }
        }

        fab2.setOnClickListener { view ->
            Snackbar.make(view, "Turning off NFC read mode", Snackbar.LENGTH_SHORT).show()

            // disable nfc
            nfcAdapter?.let {
                if (it.isEnabled) {
                    it.disableReaderMode(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()

        nfcAdapter?.disableReaderMode(this)
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
