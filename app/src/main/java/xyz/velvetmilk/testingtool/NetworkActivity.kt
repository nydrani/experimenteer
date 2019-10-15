package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import okhttp3.CertificatePinner
import retrofit2.HttpException
import retrofit2.converter.scalars.ScalarsConverterFactory
import xyz.velvetmilk.testingtool.net.GzipInterceptor

class NetworkActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = NetworkActivity::class.simpleName
        private const val SERVER_URL = "http://192.168.105.14:3000/"

        fun buildIntent(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }
    }

    interface NetworkService {
        data class TestResponse(val message: String)

        @POST("/test")
        suspend fun testPost(): TestResponse

        @GET("/test")
        suspend fun testGet(): TestResponse

        @GET("/")
        suspend fun homeGet(): String
    }

    private lateinit var service: NetworkService
    private lateinit var gzipService: NetworkService

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        val certificatePinner = CertificatePinner.Builder()
            .add("test.shield.airpayapp.com.au", "sha256/LK+SR338fYlYypXGIS3BQLBvgKKdC7gdn8PFf4vm6ps=")
            .add("test.shield.airpayapp.com.au", "sha256/RkhWTcfJAQN/YxOR12VkPo+PhmIoSfWd/JVkg44einY=")
            .add("test.shield.airpayapp.com.au", "sha256/x4QzPSC810K5/cMjb05Qm4k3Bw5zBn4lTdO/nEW/Td4=")
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(GzipInterceptor())
            .certificatePinner(certificatePinner)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val gzipRetrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

        service = retrofit.create(NetworkService::class.java)
        gzipService = gzipRetrofit.create(NetworkService::class.java)

        fab.setOnClickListener {
            launch {
                try {
                    network_view.text = service.homeGet()
                } catch (e: IOException) {
                    // io exception
                    e.printStackTrace()
                } catch (e: HttpException) {
                    // http exception
                    e.printStackTrace()
                }
            }
        }

        fab2.setOnClickListener {
            launch {
                try {
                    network_view.text = service.testPost().message
                } catch (e: IOException) {
                    // io exception
                    e.printStackTrace()
                } catch (e: HttpException) {
                    // http exception
                    e.printStackTrace()
                }
            }
        }

        fab3.setOnClickListener {
            launch {
                try {
                    network_view.text = service.testGet().message
                } catch (e: IOException) {
                    // io exception
                    e.printStackTrace()
                } catch (e: HttpException) {
                    // http exception
                    e.printStackTrace()
                }
            }
        }

        fab4.setOnClickListener {
            launch {
                try {
                    network_view.text = gzipService.testPost().message
                } catch (e: IOException) {
                    // io exception
                    e.printStackTrace()
                } catch (e: HttpException) {
                    // http exception
                    e.printStackTrace()
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
