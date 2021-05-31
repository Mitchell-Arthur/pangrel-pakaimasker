package com.pangrel.pakaimasker.ui.home

import android.graphics.Color
import android.util.Log
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class Device(val userID: String) {
    val uid = userID
    var position: Int? = null
    var adapter: DeviceAdapter? = null
    var image: String? = null
    var name: String = "Loading..."
    var status: String = "Loading..."
    var lastScan: String = ""
    var color = Color.WHITE
    val mRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
    val mListener = mRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val last =
                if (dataSnapshot.value != null) dataSnapshot.value as HashMap<String, Any> else null
            if (last != null) {
                image = last.get("photo") as String
                name = last.get("name") as String
                if (last.get("last") != null) {
                    // Mega silahkan buat multi-lang nya, sekalian ubah warna status kalau dia menggunakan masker atau nggak
                    val isMasked = (last.get("last") as HashMap<String, Any>).get("masked") as Boolean
                    val lastUpdate = (last.get("last") as HashMap<String, Any>).get("datetime") as String
                    status = (if (isMasked) "Menggunakan Masker" else "Tidak Menggunakan Masker")

                    if (isMasked) {
                        color = Color.WHITE
                    } else {
                        color = Color.YELLOW
                    }

                    lastScan = "Diperbarui pada " + LocalDateTime.parse(lastUpdate).format(DateTimeFormatter.ofPattern("d MMMM yyyy - HH:mm:ss", Locale.getDefault()))
                } else status = "Belum Ditemukan Data"

                position?.let { adapter?.notifyItemChanged(it) }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })

    fun destroy() {
        mRef.removeEventListener(mListener)
    }

}