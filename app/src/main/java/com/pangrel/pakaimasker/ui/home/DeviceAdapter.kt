package com.pangrel.pakaimasker.ui.home

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pangrel.pakaimasker.R
import com.pangrel.pakaimasker.ui.history.History
import com.pangrel.pakaimasker.ui.history.HistoryHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_device.view.*
import kotlinx.android.synthetic.main.item_history.view.*

class DeviceAdapter (private val device: List<Device>) : RecyclerView.Adapter<DeviceHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): DeviceHolder {
        return DeviceHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.item_device, viewGroup, false))
    }

    override fun getItemCount(): Int = device.size

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        device[position].position = position
        device[position].adapter = this
        holder.bindDevice(device[position])
    }
}

class DeviceHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvNama= view.tv_namadevice
    private val tvStatus = view.tv_statusdevice
    private val tvLastScan = view.tv_lastscandevice
    private val imgDevice = view.img_profiledevice

    fun bindDevice(device: Device) {
        tvNama.text = device.name
        tvStatus.text = device.status
        tvLastScan.text = device.lastScan
        tvStatus.setTextColor(device.color)
        if (device.image != null)
            Glide.with(itemView.context)
                .load(Base64.decode(device.image, Base64.DEFAULT))
                .apply(RequestOptions().override(250, 250))
                .into(imgDevice)
    }
}