package xyz.velvetmilk.testingtool

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.security.ConfirmationAlreadyPresentingException
import android.security.ConfirmationCallback
import android.security.ConfirmationNotAvailableException
import android.security.ConfirmationPrompt
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_trusted_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import xyz.velvetmilk.testingtool.tools.toBase64
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@TargetApi(28)
class TrustedScreenActivity : AppCompatActivity(), CoroutineScope {

    class TrustedConfirmationCallback : ConfirmationCallback() {
        override fun onConfirmed(dataThatWasConfirmed: ByteArray) {
            // Sign dataThatWasConfirmed using your generated signing key.
            // By completing this process, you generate a "signed statement".
            Timber.d("onConfirmed")
            Timber.d(dataThatWasConfirmed.toBase64())
        }

        override fun onDismissed() {
            Timber.d("onDismissed")
            // Handle case where user declined the prompt in the
            // confirmation dialog.
        }

        override fun onCanceled() {
            Timber.d("onCancelled")
            // Handle case where your app closed the dialog before the user
            // could respond to the prompt.
        }

        override fun onError(e: Throwable) {
            Timber.e("onError")
            e.printStackTrace()
            // Handle the exception that the callback captured.
        }
    }

    companion object {
        private val TAG = TrustedScreenActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, TrustedScreenActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trusted_screen)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // die on devices lower than P
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
            finish()
            return
        }

        fab.setOnClickListener {
            base_view.text = Random.nextInt().toString()

            val extraData: ByteArray = byteArrayOf()
            val threadReceivingCallback = Executor { runnable -> runnable.run() }
            val callback = TrustedConfirmationCallback()

            val dialog = ConfirmationPrompt.Builder(this@TrustedScreenActivity)
                .setPromptText("Hello world")
                .setExtraData(extraData)
                .build()

            try {
                dialog.presentPrompt(threadReceivingCallback, callback)
            } catch (e: ConfirmationNotAvailableException) {
                e.printStackTrace()
                base_view.text = "ConfirmationNotAvailableException"
            } catch (e: ConfirmationAlreadyPresentingException) {
                e.printStackTrace()
                base_view.text = "ConfirmationAlreadyPresentingException"
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
