package com.pangrel.pakaimasker.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pangrel.pakaimasker.R
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Dummyyyy nanti hapus yaaaa
        val listHistory = listOf(
            History(date = "17 Mei 2021", detail = "10% Menggunakan masker dari 10 scanning"),
            History(date = "18 Mei 2021", detail = "20% Menggunakan masker dari 100 scanning"),
            History(date = "19 Mei 2021", detail = "30% Menggunakan masker dari 1000 scanning"),
            History(date = "20 Mei 2021", detail = "40% Menggunakan masker dari 10000 scanning"),
            History(date = "21 Mei 2021", detail = "50% Menggunakan masker dari 100000 scanning"),
        )

        val historyAdapter = HistoryAdapter(listHistory)

        rv_history.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = historyAdapter
        }
    }
}