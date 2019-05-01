package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_keystore.*
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import android.security.keystore.KeyInfo
import org.spongycastle.asn1.*
import timber.log.Timber
import java.util.*


class KeyStoreActivity : AppCompatActivity() {

    enum class SecurityLevel {
        Software,
        TrustedEnvironment,
        StrongBox;
    }

    enum class VerifiedBootState {
        Verified,
        SelfSigned,
        Unverified,
        Failed;
    }

    companion object {
        private val TAG = KeyStoreActivity::class.java.simpleName
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val RSA_KEY_ALIAS = "RSAbabey"
        private const val GOOGLE_ROOT_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
                "MIIFYDCCA0igAwIBAgIJAOj6GWMU0voYMA0GCSqGSIb3DQEBCwUAMBsxGTAXBgNV\n" +
                "BAUTEGY5MjAwOWU4NTNiNmIwNDUwHhcNMTYwNTI2MTYyODUyWhcNMjYwNTI0MTYy\n" +
                "ODUyWjAbMRkwFwYDVQQFExBmOTIwMDllODUzYjZiMDQ1MIICIjANBgkqhkiG9w0B\n" +
                "AQEFAAOCAg8AMIICCgKCAgEAr7bHgiuxpwHsK7Qui8xUFmOr75gvMsd/dTEDDJdS\n" +
                "Sxtf6An7xyqpRR90PL2abxM1dEqlXnf2tqw1Ne4Xwl5jlRfdnJLmN0pTy/4lj4/7\n" +
                "tv0Sk3iiKkypnEUtR6WfMgH0QZfKHM1+di+y9TFRtv6y//0rb+T+W8a9nsNL/ggj\n" +
                "nar86461qO0rOs2cXjp3kOG1FEJ5MVmFmBGtnrKpa73XpXyTqRxB/M0n1n/W9nGq\n" +
                "C4FSYa04T6N5RIZGBN2z2MT5IKGbFlbC8UrW0DxW7AYImQQcHtGl/m00QLVWutHQ\n" +
                "oVJYnFPlXTcHYvASLu+RhhsbDmxMgJJ0mcDpvsC4PjvB+TxywElgS70vE0XmLD+O\n" +
                "JtvsBslHZvPBKCOdT0MS+tgSOIfga+z1Z1g7+DVagf7quvmag8jfPioyKvxnK/Eg\n" +
                "sTUVi2ghzq8wm27ud/mIM7AY2qEORR8Go3TVB4HzWQgpZrt3i5MIlCaY504LzSRi\n" +
                "igHCzAPlHws+W0rB5N+er5/2pJKnfBSDiCiFAVtCLOZ7gLiMm0jhO2B6tUXHI/+M\n" +
                "RPjy02i59lINMRRev56GKtcd9qO/0kUJWdZTdA2XoS82ixPvZtXQpUpuL12ab+9E\n" +
                "aDK8Z4RHJYYfCT3Q5vNAXaiWQ+8PTWm2QgBR/bkwSWc+NpUFgNPN9PvQi8WEg5Um\n" +
                "AGMCAwEAAaOBpjCBozAdBgNVHQ4EFgQUNmHhAHyIBQlRi0RsR/8aTMnqTxIwHwYD\n" +
                "VR0jBBgwFoAUNmHhAHyIBQlRi0RsR/8aTMnqTxIwDwYDVR0TAQH/BAUwAwEB/zAO\n" +
                "BgNVHQ8BAf8EBAMCAYYwQAYDVR0fBDkwNzA1oDOgMYYvaHR0cHM6Ly9hbmRyb2lk\n" +
                "Lmdvb2dsZWFwaXMuY29tL2F0dGVzdGF0aW9uL2NybC8wDQYJKoZIhvcNAQELBQAD\n" +
                "ggIBACDIw41L3KlXG0aMiS//cqrG+EShHUGo8HNsw30W1kJtjn6UBwRM6jnmiwfB\n" +
                "Pb8VA91chb2vssAtX2zbTvqBJ9+LBPGCdw/E53Rbf86qhxKaiAHOjpvAy5Y3m00m\n" +
                "qC0w/Zwvju1twb4vhLaJ5NkUJYsUS7rmJKHHBnETLi8GFqiEsqTWpG/6ibYCv7rY\n" +
                "DBJDcR9W62BW9jfIoBQcxUCUJouMPH25lLNcDc1ssqvC2v7iUgI9LeoM1sNovqPm\n" +
                "QUiG9rHli1vXxzCyaMTjwftkJLkf6724DFhuKug2jITV0QkXvaJWF4nUaHOTNA4u\n" +
                "JU9WDvZLI1j83A+/xnAJUucIv/zGJ1AMH2boHqF8CY16LpsYgBt6tKxxWH00XcyD\n" +
                "CdW2KlBCeqbQPcsFmWyWugxdcekhYsAWyoSf818NUsZdBWBaR/OukXrNLfkQ79Iy\n" +
                "ZohZbvabO/X+MVT3rriAoKc8oE2Uws6DF+60PV7/WIPjNvXySdqspImSN78mflxD\n" +
                "qwLqRBYkA3I75qppLGG9rp7UCdRjxMl8ZDBld+7yvHVgt1cVzJx9xnyGCC23Uaic\n" +
                "MDSXYrB4I4WHXPGjxhZuCuPBLTdOLU8YRvMYdEvYebWHMpvwGCF6bAx3JBpIeOQ1\n" +
                "wDB5y0USicV3YgYGmi+NZfhA4URSh77Yd6uuJOJENRaNVTzk\n" +
                "-----END CERTIFICATE-----"

        private const val KEY_DESCRIPTION_OID = "1.3.6.1.4.1.11129.2.1.17"
        private const val ATTESTATION_VERSION_INDEX = 0
        private const val ATTESTATION_SECURITY_LEVEL_INDEX = 1
        private const val KEYMASTER_VERSION_INDEX = 2
        private const val KEYMASTER_SECURITY_LEVEL_INDEX = 3
        private const val ATTESTATION_CHALLENGE_INDEX = 4
        private const val UNIQUE_ID_INDEX = 5
        private const val SW_ENFORCED_INDEX = 6
        private const val TEE_ENFORCED_INDEX = 7

        private const val VERIFIED_BOOT_KEY_INDEX = 0
        private const val DEVICE_LOCKED_INDEX = 1
        private const val VERIFIED_BOOT_STATE_INDEX = 2
        private const val VERIFIED_BOOT_STATE_HASH = 3

        fun buildIntent(context: Context): Intent {
            return Intent(context, KeyStoreActivity::class.java)
        }
    }

    private lateinit var store: KeyStore
    private lateinit var keyPairGenerator: KeyPairGenerator
    private lateinit var keyFactory: KeyFactory

    private val secureRoot = CertificateFactory.getInstance("X.509")
        .generateCertificate(ByteArrayInputStream(GOOGLE_ROOT_CERTIFICATE.toByteArrayUTF8())) as X509Certificate


    private val disposer = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keystore)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        store = KeyStore.getInstance(KEYSTORE_TYPE).apply { this.load(null) }
        keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val rsaSpec = KeyGenParameterSpec.Builder(RSA_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
            .setDigests(KeyProperties.DIGEST_SHA1)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setAttestationChallenge("yeet".toByteArrayUTF8())
            .setKeySize(2048)
            .build()
        keyPairGenerator.initialize(rsaSpec)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Generating RSA Asymmetric key", Snackbar.LENGTH_LONG).show()

            disposer.clear()
            Observable.fromCallable { keyPairGenerator.genKeyPair() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    key_view.text = it.public.encoded.toHexStringUTF8()
                }
                .addTo(disposer)
        }

        fab2.setOnClickListener { view ->
            Snackbar.make(view, "Checking certificates", Snackbar.LENGTH_LONG).show()
            val builder = StringBuilder()

            val chain = store.getCertificateChain(RSA_KEY_ALIAS)
            // verify certificate chain
            for (i in chain.indices) {
                builder.appendln(chain[i].toString())
                if (i == chain.size - 1) {
                    chain[i].verify(chain[i].publicKey)
                } else {
                    chain[i].verify(chain[i + 1].publicKey)
                }
            }

            if (Arrays.equals((chain.last() as X509Certificate).tbsCertificate, secureRoot.tbsCertificate)) {
                builder.appendln("Final cert is from Google")
            }

            // check that key is inside secure hardware
            val privEntry = store.getEntry(RSA_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
            val keyInfo = keyFactory.getKeySpec(privEntry.privateKey, KeyInfo::class.java)
            builder.appendln("isInsideSecureHardware: " + keyInfo.isInsideSecureHardware.toString())
            builder.appendln("isInvalidatedByBiometricEnrollment: " + keyInfo.isInvalidatedByBiometricEnrollment.toString())
            builder.appendln("isUserAuthenticationRequired: " + keyInfo.isUserAuthenticationRequired.toString())
            builder.appendln("isUserAuthenticationRequirementEnforcedBySecureHardware: " + keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware.toString())
            builder.appendln("isUserAuthenticationValidWhileOnBody: " + keyInfo.isUserAuthenticationValidWhileOnBody.toString())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                builder.appendln("isTrustedUserPresenceRequired: " + keyInfo.isTrustedUserPresenceRequired.toString())
                builder.appendln("isUserConfirmationRequired: " + keyInfo.isUserConfirmationRequired.toString())
            }

            key_view.text = builder.toString()
        }

        fab3.setOnClickListener { view ->
            Snackbar.make(view, "Decoding attestation certificate", Snackbar.LENGTH_LONG).show()
            val builder = StringBuilder()

            val attestationCert = store.getCertificateChain(RSA_KEY_ALIAS)[0] as X509Certificate
            val attestationExtensionBytes = attestationCert.getExtensionValue(KEY_DESCRIPTION_OID)
            if (attestationExtensionBytes == null || attestationExtensionBytes.isEmpty()) {
                Snackbar.make(view, "Couldn't find the keystore attestation extension data.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Timber.d(attestationExtensionBytes.toHexStringUTF8())

            ASN1InputStream(attestationExtensionBytes).use { asn1InputStream ->
                // The extension contains one object, a sequence, in the
                // Distinguished Encoding Rules (DER)-encoded form. Get the DER
                // bytes.
                val derSequenceBytes = (asn1InputStream.readObject() as ASN1OctetString).octets

                Timber.d(derSequenceBytes.toHexStringUTF8())
                // Decode the bytes as an ASN1 sequence object.
                ASN1InputStream(derSequenceBytes).use {
                    Timber.d(it.toString())
                    val sequence = it.readObject() as ASN1Sequence
                    builder.appendln(sequence)

                    val attVersion = sequence.getObjectAt(ATTESTATION_VERSION_INDEX) as ASN1Integer
                    val attSecLevel = sequence.getObjectAt(ATTESTATION_SECURITY_LEVEL_INDEX) as ASN1Enumerated
                    val kmVersion = sequence.getObjectAt(KEYMASTER_VERSION_INDEX) as ASN1Integer
                    val kmSecLevel = sequence.getObjectAt(KEYMASTER_SECURITY_LEVEL_INDEX) as ASN1Enumerated
                    val attChallenge = sequence.getObjectAt(ATTESTATION_CHALLENGE_INDEX) as ASN1OctetString
                    val uniqueId = sequence.getObjectAt(UNIQUE_ID_INDEX) as ASN1OctetString
                    val swEnforced = sequence.getObjectAt(SW_ENFORCED_INDEX) as ASN1Sequence
                    val teeEnforced = sequence.getObjectAt(TEE_ENFORCED_INDEX) as ASN1Sequence

                    builder.appendln()
                    builder.appendln("ATTESTATION_VERSION_INDEX " + attVersion.value.toString())
                    builder.appendln("ATTESTATION_SECURITY_LEVEL_INDEX " + SecurityLevel.values()[attSecLevel.value.toInt()])
                    builder.appendln("KEYMASTER_VERSION_INDEX " + kmVersion.value.toString())
                    builder.appendln("KEYMASTER_SECURITY_LEVEL_INDEX " + SecurityLevel.values()[kmSecLevel.value.toInt()])
                    builder.appendln("ATTESTATION_CHALLENGE_INDEX " + attChallenge.octets.toHexStringUTF8())
                    builder.appendln("UNIQUE_ID_INDEX " + uniqueId.octets.toHexStringUTF8())
                    builder.appendln("SW_ENFORCED_INDEX " + parseAuthorisationList(swEnforced))
                    builder.appendln("TEE_ENFORCED_INDEX " + parseAuthorisationList(teeEnforced))
                }
            }

            key_view.text = builder.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun parseAuthorisationList(list: ASN1Sequence): String {
        val builder = StringBuilder()
        builder.append("[ ")

        val usefulTags = listOf(702, 704, 705, 706, 718, 719)

        val intSetList = listOf(1, 5, 6)
        val intList = listOf(2, 3, 10, 200, 400, 401, 402, 504, 505, 701, 702, 705, 706, 718, 719)
        val nullList = listOf(303, 503, 506, 507, 508, 509, 600)
        val octetStringList = listOf(601, 709, 710, 711, 712, 713, 714, 715, 716, 717)
        val rootOfTrustTag = 704
        for (i in usefulTags) {
            for (j in list) {
                if ((j as ASN1TaggedObject).tagNo != i) {
                    continue
                }
                builder.append(String.format("%d ", i))
                when (i) {
                    in intSetList -> {
                        val set = j.`object` as ASN1Set
                        for (k in set.toArray()) {
                            builder.append((k as ASN1Integer).value.toInt())
                            builder.append(" ")
                        }
                        builder.appendln()
                    }
                    in intList -> {
                        builder.appendln((j.`object` as ASN1Integer).value.toInt())
                    }
                    in nullList -> {
                        builder.appendln((j.`object` as ASN1Null).encoded.toHexStringUTF8())
                    }
                    in octetStringList -> {
                        builder.appendln((j.`object` as ASN1OctetString).octets.toHexStringUTF8())
                    }
                    rootOfTrustTag -> {
                        val sequence = j.`object` as ASN1Sequence
                        val verBootKey = sequence.getObjectAt(VERIFIED_BOOT_KEY_INDEX) as ASN1OctetString
                        val deviceLocked = sequence.getObjectAt(DEVICE_LOCKED_INDEX) as ASN1Boolean
                        val verBootState = sequence.getObjectAt(VERIFIED_BOOT_STATE_INDEX) as ASN1Enumerated
                        val verBootStateHash = sequence.getObjectAt(VERIFIED_BOOT_STATE_HASH) as ASN1OctetString

                        builder.appendln("VERIFIED_BOOT_KEY_INDEX: " + verBootKey.octets.toHexStringUTF8())
                        builder.appendln("DEVICE_LOCKED_INDEX: " + deviceLocked.isTrue)
                        builder.appendln("VERIFIED_BOOT_STATE_INDEX: " + VerifiedBootState.values()[verBootState.value.toInt()])
                        builder.appendln("VERIFIED_BOOT_STATE_HASH: " + verBootStateHash.octets.toHexStringUTF8())
                    }
                }
                break
            }
        }

        builder.append(" ]")
        return builder.toString()
    }
}
