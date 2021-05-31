package com.pangrel.pakaimasker.ui.home

import android.util.Log
import com.google.firebase.database.*

class Device(val userID: String) {
    val uid = userID
    var image: String? = null
    var name: String = "Loading..."
    var status: String = "Loading..."
    var lastScan: String = "Loading..."
    val mRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
    val mListener = mRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val last =
                if (dataSnapshot.value != null) dataSnapshot.value as HashMap<String, Any> else null
            if (last != null) {
                image = last.get("photo") as String
                name = last.get("name") as String
                if (last.get("last") != null) {
                    val isMasked = (last.get("last") as HashMap<String, Any>).get("masked") as Boolean
                    val lastUpdate = (last.get("last") as HashMap<String, Any>).get("datetime") as String
                    status = (if (isMasked) "Menggunakan Masker" else "Tidak Menggunakan Masker") + " pada " + lastUpdate
                }
                status = "Belum Ditemukan Data"

                Log.d("Device", "Name = $name, Status = $status")

                // Update VIEW mega
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    })

    fun destroy() {
        mRef.removeEventListener(mListener)
    }

}