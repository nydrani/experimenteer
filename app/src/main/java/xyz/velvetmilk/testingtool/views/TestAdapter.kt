package xyz.velvetmilk.testingtool.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.item_test.view.*
import xyz.velvetmilk.testingtool.R

class TestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val viewClickSubject: Subject<Pair<View, Int>> = PublishSubject.create<Pair<View, Int>>()
    private val items: MutableList<String> = mutableListOf()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TestViewHolder) {
            holder.bind(items[position], viewClickSubject)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TestViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_test, parent, false)
        )
    }

    fun updateItems(newItems: List<String>) {
        val diffResult = DiffUtil.calculateDiff(
            TestDiffUtilCallback(
                items,
                newItems
            )
        )

        items.clear()
        items.addAll(newItems)

        diffResult.dispatchUpdatesTo(this)
    }

    fun addItem(string: String) {
        val newItems = items.toList() + string
        val diffResult = DiffUtil.calculateDiff(
            TestDiffUtilCallback(
                items,
                newItems
            )
        )

        items.add(string)

        diffResult.dispatchUpdatesTo(this)
    }

    class TestDiffUtilCallback(private val oldItems: List<String>, private val newItems: List<String>) : DiffUtil.Callback() {
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun getOldListSize(): Int {
            return oldItems.size
        }
    }

    class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(string: String, clickSubject: Subject<Pair<View, Int>>) {
            itemView.setOnClickListener {
                clickSubject.onNext(Pair(it, layoutPosition))
            }
            itemView.test_text.text = string
        }
    }
}
