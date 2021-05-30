package com.pangrel.pakaimasker.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pangrel.pakaimasker.R
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter (private val history: List<History>) : RecyclerView.Adapter<HistoryHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): HistoryHolder {
        return HistoryHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.item_history, viewGroup, false))
    }

    override fun getItemCount(): Int = history.size

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.bindHero(history[position])
    }
}

class HistoryHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvDate = view.tv_date
    private val tvDetail = view.tv_detail

    fun bindHero(history: History) {
        tvDate.text = history.date
        tvDetail.text = history.detail
    }
}