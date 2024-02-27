package com.example.shopease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("בית")
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        var btnGoToScan = view.findViewById<MaterialButton>(R.id.btnGoToScan)
        btnGoToScan.setOnClickListener {
            (activity as BaseActivity?)?.loadFragment(BarcodeScannerFragment())
        }
        return view // Return the view you've inflated
    }
}

