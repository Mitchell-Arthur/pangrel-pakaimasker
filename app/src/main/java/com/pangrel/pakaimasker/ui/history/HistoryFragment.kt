package com.pangrel.pakaimasker.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pangrel.pakaimasker.R
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.tv_persen
import kotlinx.android.synthetic.main.fragment_history.tv_result
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class HistoryFragment : Fragment() {
    private var mRef: DatabaseReference? = null
    private var mListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onResume() {
        super.onResume()

        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            val instance = FirebaseDatabase.getInstance()
            mRef = instance.getReference("summaries").child(uid)
            mRef?.keepSynced(true)
            mListener = mRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val values = dataSnapshot.value as HashMap<String, Any>?
                    if (values != null) {
                        val today = LocalDate.now()
                        val listHistory = ArrayList<History>()
                        for ((key, value) in values) {
                            val summary = if (value != null) value as HashMap<String, Long> else null
                            val scanned = (summary?.get("totalScanned") ?: 0L).toInt()
                            val masked = (summary?.get("totalMasked") ?: 0L).toInt()
                            val percentage = Math.round((masked.toDouble() / scanned.toDouble() * 100))
                            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
                            val date = LocalDate.parse(key)
                            if (date.isEqual(today) == false)
                                // Tambah multi-bahasa mega
                                listHistory.add(History(date = date.format(formatter).toString(), detail = "$percentage% menggunakan masker dari $scanned scanning"))
                            else {
                                tv_persen.text = percentage.toString() + " %"
                                tv_result.text = masked.toString() + " dari " + scanned.toString() + " scanning terdeteksi menggunakan masker"
                            }
                        }

                        val historyAdapter = HistoryAdapter(listHistory.toList().asReversed())

                        rv_history.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = historyAdapter
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mListener?.let { mRef?.removeEventListener(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}