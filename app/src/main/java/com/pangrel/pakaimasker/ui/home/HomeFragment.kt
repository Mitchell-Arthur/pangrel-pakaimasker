package com.pangrel.pakaimasker.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pangrel.pakaimasker.CamService
import com.pangrel.pakaimasker.ImageClassification
import com.pangrel.pakaimasker.R
import com.pangrel.pakaimasker.isServiceRunning
import kotlinx.android.synthetic.main.dialog_connect.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment() {
    private lateinit var mRef: DatabaseReference
    private lateinit var mListener: ValueEventListener
    private lateinit var mAuth: FirebaseAuth

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d("HomeFragment", "receive " + p1?.action)
            when (p1?.action) {
                ACTION_MONITOR_ON -> startMonitoring()
                ACTION_MONITOR_OFF -> stopMonitoring()
                ACTION_UPDATE_RESULT -> updateResult()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(ACTION_MONITOR_ON)
        filter.addAction(ACTION_MONITOR_OFF)
        filter.addAction(ACTION_UPDATE_RESULT)
        activity?.registerReceiver(receiver, filter)

        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            val instance = FirebaseDatabase.getInstance()
            val date = LocalDate.now().toString()
            mRef = instance.getReference("summaries").child(uid).child(date)
            mRef.keepSynced(true)
            mListener = mRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val summary =
                        if (dataSnapshot.value != null) dataSnapshot.value as HashMap<String, Long> else null
                    val scanned = (summary?.get("totalScanned") ?: 0L).toInt()
                    val masked = (summary?.get("totalMasked") ?: 0L).toInt()

                    if (scanned > 0) {
                        val percentage = Math.round((masked.toDouble() / scanned.toDouble() * 100))
                        tv_persen.text = percentage.toString() + " %"
                        // Tambah multi-bahasa mega
                        tv_result.text =
                            masked.toString() + " dari " + scanned.toString() + " scanning terdeteksi menggunakan masker"
                    } else {
                        tv_persen.text = "~ %"
                        // Tambah multi-bahasa mega
                        tv_result.text = "Belum menemukan data scanning"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    tv_persen.text = "~ %"
                    // Tambah multi-bahasa mega
                    tv_result.text = "Terjadi kesalahan saat memperoleh data"
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()

        activity?.unregisterReceiver(receiver)
        mRef.removeEventListener(mListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = getActivity()

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        val fullName = currentUser?.displayName
        val firstName = fullName?.split(" ")?.first()
        tv_name.text = firstName

        btnConnectDevice.setOnClickListener {
            val dialog = AlertDialog.Builder(context).create()
            val dialogView = layoutInflater.inflate(R.layout.dialog_connect, null)
            dialog.setView(dialogView)
            dialog.setCancelable(true)
            val btnAdd = dialogView.findViewById<Button>(R.id.btn_add)
            val etConnect = dialogView.findViewById<EditText>(R.id.et_connect)
            dialog.show()
            btnAdd.setOnClickListener {
                val deviceCode = etConnect.text.toString().toUpperCase() // dummy, isi sama yang user input
                val uid = FirebaseAuth.getInstance().uid
                FirebaseDatabase.getInstance().getReference("/codes/" + deviceCode).get()
                    .addOnSuccessListener {
                        if (it.exists() == false) {
                            // Mega isi ketika gk ketemu, kasih toast berisi pesan device code tida kditemukan
                            Toast.makeText(
                                context,
                                "Device Code Tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (it.value == uid) {
                                // Mega kasih toast error
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            } else {
                                FirebaseDatabase.getInstance()
                                    .getReference("/pairs/" + uid + "/" + deviceCode).setValue(true)
                                Toast.makeText(
                                    context,
                                    "Device Code Berhasil Ditambahkan",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Tutup dialognya meg
                                dialog.dismiss()
                            }
                        }
                    }.addOnFailureListener {
                        // Mega kasih toast error
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        if (activity != null) {
            updateButtonText(isServiceRunning(activity.applicationContext, CamService::class.java))
        }

        btnMonitoring.setOnClickListener {
            btnMonitoring.isEnabled = false

            if (activity !== null) {
                if (!isServiceRunning(activity.applicationContext, CamService::class.java)) {
                    val intent = Intent(activity.applicationContext, CamService::class.java)
                    activity.startService(intent)
                } else {
                    activity.stopService(
                        Intent(
                            activity.applicationContext,
                            CamService::class.java
                        )
                    )
                }
            }
        }
        if (activity != null) {
            val isRunning = isServiceRunning(activity.applicationContext, CamService::class.java)
            updateButtonText(isRunning)

            if (isRunning) {
                updateResult()
            }
        }
        //DUMMY NANTI HAPUS
        val listDevice = listOf(
            Device(
                imgDevice = R.drawable.safezone_icon,
                name = "Thor",
                status = "diisi apa ini wkwk",
                lastScan = "Last Status : Masked at 23:59:59"
            ),
            Device(
                imgDevice = R.drawable.unmasked_icon,
                name = "Captain America",
                status = "diisi apa ini wkwk",
                lastScan = "Last Status : Masked at 23:59:59"
            ),
            Device(
                imgDevice = R.drawable.bingung_icon,
                name = "Iron Man",
                status = "diisi apa ini wkwk",
                lastScan = "Last Status : Masked at 23:59:59"
            )
        )

        val Deviceadapter = DeviceAdapter(listDevice)

        rv_device.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = Deviceadapter
        }
    }

    fun stopMonitoring() {
        updateButtonText(false)
        img_status.setImageResource(R.drawable.bingung_icon)
        lastStatusLabel.text = getString(R.string.monitoroff)
        tv_laststatus.text = ""

        // Mega disini perlu di reset view UInya (teks) ubah kembali ke saat belum start monitor //DONE
        tv_status.setText(getString(R.string.caption))
    }

    fun startMonitoring() {
        updateButtonText(true)

        lastStatusLabel.text = getString(R.string.starting)
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private fun updateButtonText(running: Boolean) {
        if (running) {
            btnMonitoring.text = getString(R.string.OFF)
            btnMonitoring.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.off)))
        } else {
            btnMonitoring.text = getString(R.string.ON)
            btnMonitoring.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.primary)))
        }
        btnMonitoring.isEnabled = true
    }

    private fun updateResult() {
        val lastStatus =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getString("lastStatus", "")
        val lastUpdate =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getString("lastUpdate", "")

        var dateTime = LocalDateTime.now()

        if (lastUpdate !== "") {
            dateTime = LocalDateTime.parse(lastUpdate)
        }

        if (lastStatus == "in_safezone") {
            val zoneName = activity?.getPreferences(Context.MODE_PRIVATE)?.getString("zoneName", "")
            val zoneDistance =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("zoneDistance", 0)

            img_status.setImageResource(R.drawable.safezone_icon)
            tv_status.setText(getString(R.string.safe) + " " + zoneDistance + " " + getString(R.string.safezo) + zoneName + ")")

            lastStatusLabel.setText(getString(R.string.insafezone))
            tv_laststatus.setText("")
        }

        if (lastStatus == "out_of_schedule") {
            img_status.setImageResource(R.drawable.safezone_icon)
            // UBAH INI MEGA //UDA GINI AJA BAGUS ELAH UBAH APALAGI
            tv_status.setText(getString(R.string.statusOut))

            lastStatusLabel.setText(getString(R.string.OutSchedule))
            tv_laststatus.setText("")
        }

        if (lastStatus == "classification") {
            val classificationResult =
                activity?.getPreferences(Context.MODE_PRIVATE)
                    ?.getInt("classificationResult", ImageClassification.UNDEFINED)

            var status = ""
            if (classificationResult === ImageClassification.UNSURE) {
                status = getString(R.string.Unsure)
            }
            if (classificationResult === ImageClassification.NOT_FOUND) {
                status = getString(R.string.No_Face)
            }
            if (classificationResult === ImageClassification.WITH_MASK) {
                status = getString(R.string.Masked)
                img_status.setImageResource(R.drawable.masked_icon)
                // UBAH INI MEGA //DONE
                tv_status.setText(getString(R.string.statusYes))
            }
            if (classificationResult === ImageClassification.WITHOUT_MASK) {
                status = getString(R.string.Unmasked)
                img_status.setImageResource(R.drawable.unmasked_icon)
                // UBAH INI MEGA //DONE
                tv_status.setText(getString(R.string.statusNo))
            }

            lastStatusLabel.setText(getString(R.string.Last_Status))
            tv_laststatus.setText(
                status + " " + getString(R.string.at) + " " + dateTime.truncatedTo(
                    ChronoUnit.SECONDS
                ).format(DateTimeFormatter.ISO_LOCAL_TIME)
            )
        }
    }

    companion object {
        val ACTION_MONITOR_ON = "homefragment.action.MONITOR_ON"
        val ACTION_MONITOR_OFF = "homefragment.action.MONITOR_OFF"
        val ACTION_UPDATE_RESULT = "homefragment.action.UPDATE_RESULT"
        val ACTION_UPDATE_SAFEZONE = "homefragment.action.UPDATE_SAFEZONE"
    }
}