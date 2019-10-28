package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_cipher.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.velvetmilk.testingtool.tools.toBase64
import java.security.KeyStore
import javax.crypto.Cipher
import kotlin.coroutines.CoroutineContext
import javax.crypto.KeyGenerator
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import timber.log.Timber
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.net.SslManager
import java.security.SecureRandom
import java.security.Security
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class CipherActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = CipherActivity::class.simpleName

        private const val TDES_ALGORITHM = "DESede"
        private const val TDES_CIPHER_ALGORITHM = "DESede/CBC/NoPadding"
//        private const val TDES_CIPHER_ALGORITHM = "DESede/CBC/PKCS5Padding"
        private const val TDES_KEY_ALIAS = "TDESbabey"

        private const val KEYSTORE_TYPE = "AndroidKeyStore"

        private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val SPONGYCASTLE_PROVIDER = "SC"


        fun buildIntent(context: Context): Intent {
            return Intent(context, CipherActivity::class.java)
        }
    }

    @Inject
    lateinit var sslManager: SslManager

    private lateinit var store: KeyStore

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cipher)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        job = Job()
        disposer = CompositeDisposable()

        store = KeyStore.getInstance(KEYSTORE_TYPE).apply { this.load(null) }

        // update sslmanager
        launch {
            sslManager.updateProvider(this@CipherActivity)
        }

        fab.setOnClickListener {
            launch(Dispatchers.Default) {
                val secure = SecureRandom()
                val key = ByteArray(192 / 8 )
                secure.nextBytes(key)

                val secretKeyFactory = SecretKeyFactory.getInstance(TDES_ALGORITHM, Security.getProvider(SPONGYCASTLE_PROVIDER))

                val keyProtection = KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()

                val secretKeySpec = SecretKeySpec(key, TDES_ALGORITHM)
                val secretKey = secretKeyFactory.generateSecret(secretKeySpec)

                store.setEntry(TDES_KEY_ALIAS, KeyStore.SecretKeyEntry(secretKey), keyProtection)


                launch(Dispatchers.Main) {
                    cipher_view.text = key.toBase64()
                }
            }
        }

        fab2.setOnClickListener {
            val entry = store.getEntry(TDES_KEY_ALIAS, null) as KeyStore.SecretKeyEntry

            // encrypt data
            val cipherInstance = Cipher.getInstance(TDES_CIPHER_ALGORITHM)
            cipherInstance.init(Cipher.ENCRYPT_MODE, entry.secretKey)
            cipherInstance.update(byteArrayOf(42))

            val encrypted = cipherInstance.doFinal()

            launch(Dispatchers.Main) {
                cipher_view.text = encrypted.toBase64()
            }
        }

        fab3.setOnClickListener {
            launch(Dispatchers.Default) {
                val secure = SecureRandom()
                val randomData = ByteArray(64 / 8)
                secure.nextBytes(randomData)

                val keyGenerator = KeyGenerator.getInstance(TDES_ALGORITHM)
                val desedeKey = keyGenerator.generateKey()

                // encrypt data
                val cipherInstance = Cipher.getInstance(TDES_CIPHER_ALGORITHM)
                cipherInstance.init(Cipher.ENCRYPT_MODE, desedeKey)
                val encrypted = cipherInstance.doFinal(randomData)

                Timber.d(encrypted.toBase64())

                val stringBuilder = StringBuilder()
                stringBuilder.appendln(keyGenerator.provider)
                stringBuilder.appendln(desedeKey.encoded.toBase64())
                stringBuilder.appendln(encrypted.toBase64())

                launch(Dispatchers.Main) {
                    cipher_view.text = stringBuilder.toString()
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
