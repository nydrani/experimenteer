package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_constraint_barrier.*

class ConstraintBarrierActivity : AppCompatActivity() {


    companion object {
        fun buildIntent(context: Context): Intent {
            return Intent(context, ConstraintBarrierActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constraint_barrier)

        setSupportActionBar(toolbar)
    }
}
