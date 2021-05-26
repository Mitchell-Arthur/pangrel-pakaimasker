package com.pangrel.pakaimasker

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView


class SafeZoneAdapter internal constructor(private val context: Context): BaseAdapter() {

    var locations = ArrayList<String>()
    override fun getCount(): Int = locations.size

    override fun getItem(i: Int): Any = locations[i]

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        var itemView = view
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_safe_zone, viewGroup, false)
        }

        val viewHolder = ViewHolder(itemView as View)

        val locationName = getItem(position) as String
        viewHolder.bind(locationName)
        viewHolder.ivDeleteSafeZone.setOnClickListener {
            locations.removeAt(position)
            notifyDataSetChanged()
        }
        return itemView
    }

    private inner class ViewHolder(view: View) {
        private val tvSafeZone: TextView = view.findViewById(R.id.tv_safe_zone)
        val ivDeleteSafeZone: ImageView = view.findViewById(R.id.iv_delete_safe_zone)

        fun bind(location: String) {
            tvSafeZone.text = location
        }
    }
}