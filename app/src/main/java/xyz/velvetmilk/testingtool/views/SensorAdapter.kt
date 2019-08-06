package xyz.velvetmilk.testingtool.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.item_sensor.view.*
import xyz.velvetmilk.testingtool.R

class SensorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val viewClickSubject: Subject<Pair<View, Int>> = PublishSubject.create()
    private val items: MutableList<String> = mutableListOf()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SensorViewHolder) {
            holder.bind(items[position], viewClickSubject)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SensorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        )
    }

    fun updateItems(newMap: Map<String, FloatArray>) {
        // generate a list from the map
        val newItems = newMap.map {
            val stringBuilder = StringBuilder()
            stringBuilder.append(it.key)
            stringBuilder.append(" ")
            for (value in it.value) {
                stringBuilder.append(value)
                stringBuilder.append(" ")
            }
            stringBuilder.toString()
        }.sorted()

//        val diffResult = DiffUtil.calculateDiff(
//            SensorDiffUtilCallback(
//                items,
//                newItems
//            )
//        )

        items.clear()
        items.addAll(newItems)


        notifyDataSetChanged()
        //diffResult.dispatchUpdatesTo(this)
    }

//    class SensorDiffUtilCallback(private val oldItems: List<String>, private val newItems: List<String>) : DiffUtil.Callback() {
//        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldItems[oldItemPosition] == newItems[newItemPosition]
//        }
//
//        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldItems[oldItemPosition] == newItems[newItemPosition]
//        }
//
//        override fun getNewListSize(): Int {
//            return newItems.size
//        }
//
//        override fun getOldListSize(): Int {
//            return oldItems.size
//        }
//    }

    class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(string: String, clickSubject: Subject<Pair<View, Int>>) {
            itemView.setOnClickListener {
                clickSubject.onNext(Pair(it, layoutPosition))
            }
            itemView.sensor_text.text = string
        }
    }
}
