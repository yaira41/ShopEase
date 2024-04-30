package com.example.shopease.wishLists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.shopease.R

class ShopItemOptionsBottomSheetDialogFragment : DialogFragment() {

    interface BottomSheetListener {
        fun onDeleteClicked(position: Int)
        fun onConfirmClicked(position: Int, title: String, count: Int, unit: String)
    }

    var listener: BottomSheetListener? = null
    var position: Int = -1

    companion object {
        const val ARG_TITLE = "arg_title"
        const val ARG_COUNT = "arg_count"
        const val ARG_UNIT = "arg_unit"

        fun newInstance(
            title: String,
            count: Int,
            unit: String
        ): ShopItemOptionsBottomSheetDialogFragment {
            val fragment = ShopItemOptionsBottomSheetDialogFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putInt(ARG_COUNT, count)
                putString(ARG_UNIT, unit)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shop_item_options_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnDeleteItem: Button = view.findViewById(R.id.btnDeleteItem)
        val btnConfirmUpdate: Button = view.findViewById(R.id.btnConfirmUpdate)
        val etItemTitleUpdate: EditText = view.findViewById(R.id.etItemTitleUpdate)
        val etQuantityUpdate: EditText = view.findViewById(R.id.etQuantityUpdate)
        val unitSpinnerUpdate = view.findViewById<Spinner>(R.id.unitSpinnerUpdate)

        val title = arguments?.getString(ARG_TITLE, "") ?: ""
        val count = arguments?.getInt(ARG_COUNT, 1) ?: 1
        val unit = arguments?.getString(ARG_UNIT, "") ?: "יחידות"

        // Set initial values
        etItemTitleUpdate.setText(title)
        etQuantityUpdate.setText(count.toString())

        val unitList = listOf("יחידות", "קג", "ג", "מל", "ליטר") // Replace with your list of units
        val unitAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitList)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinnerUpdate.adapter = unitAdapter
        val unitPosition = unitAdapter.getPosition(unit)
        unitSpinnerUpdate.setSelection(unitPosition)

        btnDeleteItem.setOnClickListener {
            listener?.onDeleteClicked(position)
            dismiss()
        }

        btnConfirmUpdate.setOnClickListener {
            val updatedTitle = etItemTitleUpdate.text.toString()
            val updatedCount = etQuantityUpdate.text.toString().toInt()
            val updatedUnit = unitSpinnerUpdate.selectedItem.toString()

            listener?.onConfirmClicked(position, updatedTitle, updatedCount, updatedUnit)
            dismiss()
        }
    }
}
