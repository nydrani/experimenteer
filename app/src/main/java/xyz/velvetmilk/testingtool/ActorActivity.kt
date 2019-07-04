package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_actor.*
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import xyz.velvetmilk.testingtool.concurrency.CustomActor
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextUInt

class ActorActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = ActorActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, ActorActivity::class.java)
        }
    }

    // int = value, boolean = forced update
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    private lateinit var actor: CustomActor<Pair<Int, Boolean>>

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    private lateinit var actor2: CustomActor<Pair<Int, Boolean>>

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    @kotlinx.coroutines.ExperimentalCoroutinesApi
    @kotlin.ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actor)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        var count = 0
        var updateTimer = Instant.now()
        var updateTimer2 = Instant.now()
        val startingTime = Instant.now()

        var actorUpdate1 = false
        var actorUpdate2 = false

        // start custom actor
        // actor does actions in order
        actor = CustomActor(this) {
            delay(100)

            actor_view7.text = ChronoUnit.SECONDS.between(startingTime, Instant.now()).toString()
            actor_view6.text = "Refreshing actor1"


            // do task
            if (it.first == 0) {
            }

            // check if task is not fresh
            if (it.second || ChronoUnit.SECONDS.between(updateTimer, Instant.now()) > 20) {
                actorUpdate1 = false
                // task update
                delay(100)
                Timber.d("Updating...")

                if (Random.nextBoolean()) {
                    actorUpdate1 = true
                    updateTimer = Instant.now()
                }
            }

            // task complete
            actor_view2.text = actorUpdate1.toString()
            actor_view6.text = "actor1 wait " + actorUpdate1.toString()
            actorUpdate1
        }

        actor2 = CustomActor(this) {
            delay(100)

            actor_view5.text = "Waiting for actor2"
            actor_view4.text = "Waiting for actor1"
            // making sure first actor is updated first
            val res = actor.waitForResult(it)
            if (!res) {
                actor_view4.text = "actor1 failed"
                actor_view5.text = "actor2 failed"
                actor_view3.text = false.toString()
                return@CustomActor false
            }
            actor_view4.text = "actor1 wait success"


            // do task
            if (it.first == 0) {
            }

            // check if task is not fresh
            if (it.second || ChronoUnit.SECONDS.between(updateTimer2, Instant.now()) > 10) {
                actorUpdate2 = false
                // task update
                delay(100)
                Timber.d("Updating2...")

                if (Random.nextBoolean()) {
                    actorUpdate2 = true
                    updateTimer2 = Instant.now()
                }
            }

            // task complete
            actor_view3.text = actorUpdate2.toString()
            actor_view5.text = "actor2 wait " + actorUpdate2.toString()
            actorUpdate2
        }

        launch {
            while (true) {
                // randomly add stuff
                count++
                actor.send(Pair(count, false))
                val randomDelay = (Random.nextUInt() % 5u + 1u) * 1000u
                delay(randomDelay.toLong())
            }
        }

        launch {
            while (true) {
                // randomly add stuff
                actor2.send(Pair(0, false))
                val randomDelay = (Random.nextUInt() % 5u + 10u) * 1000u
                delay(randomDelay.toLong())
            }
        }

        fab.setOnClickListener {
            launch {
                actor_view.text = actor2.waitForResult(Pair(-1, true)).toString()
            }
        }

        fab2.setOnClickListener {
            launch {
                actor.send(Pair(-1, true))
            }
        }

        fab3.setOnClickListener {
            launch {
                actor2.send(Pair(-1, true))
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
