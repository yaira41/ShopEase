package com.example.shopease.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.dbHelpers.ShopListsDatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class BarcodeScannerFragment : Fragment() {

    private lateinit var barcodeView: CompoundBarcodeView
    private val shopListsHelper by lazy { ShopListsDatabaseHelper() }
    private val CAMERA_PERMISSION_REQUEST_CODE = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner_barcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeView = view.findViewById(R.id.barcode_scanner)
        barcodeView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    val barcodeNumber = it.text
                    Log.d("Barcode", "Scanned barcode: $barcodeNumber")

                    // Fetch product information using the Open Food Facts API
                    fetchProductInfo(barcodeNumber)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Handle possible result points
            }
        })
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, start scanning
            startScanning()
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProductDialog(context: Context, productName: String, imageUrl: String?) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_product_info, null)
        builder.setView(dialogView)

        // Load and set the image using Glide asynchronously
        val productImageView = dialogView.findViewById<ImageView>(R.id.productImageView)
        val productNameTextView = dialogView.findViewById<TextView>(R.id.productNameTextView)
        productNameTextView.text = " שם מוצר:  $productName"
        if (!imageUrl.isNullOrBlank()) {
            Thread {
                try {
                    val url = URL(imageUrl)
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                    // Load the bitmap on the main thread using runOnUiThread
                    requireActivity().runOnUiThread {
                        productImageView.setImageBitmap(bmp)
                    }
                } catch (e: IOException) {
                    Log.e("Image Loading", "Error loading image", e)
                }
            }.start()
        }
        // Add button to add product to lists
        builder.setPositiveButton("הוסף מוצר לרשימה") { _, _ ->
            showChooseListDialog(productName)
        }

        builder.setNegativeButton("בטל") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showChooseListDialog(productName: String) {
        val username = (activity as BaseActivity?)?.user?.username
        shopListsHelper.getAllUserLists(username!!) { shopLists ->
            val listNames = shopLists.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("בחר רשימה:")
                .setItems(listNames) { dialog, which ->
                    val selectedList = shopLists[which]
                    showAddToShopListDialog(selectedList.id!!, productName)
                    dialog.dismiss()
                }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun showAddToShopListDialog(listId: String, productName: String) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_add_to_shop_list, null)
        builder.setView(dialogView)

        val countItemEditText = dialogView.findViewById<TextInputEditText>(R.id.countItemEditText)
        val addToShopListButton = dialogView.findViewById<MaterialButton>(R.id.addToShopListButton)

        addToShopListButton.setOnClickListener {
            val countItem = countItemEditText.text.toString().toIntOrNull() ?: 1

            // Call the method to add the product to the selected shop list
            addToShopList(listId, productName, countItem, "יחידות")

            // Dismiss the dialog
            builder.create().dismiss()
            showToast("נוסף בהצלחה. $productName המוצר")
        }

        builder.setPositiveButton("בטל") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun addToShopList(listId: String, title: String, countItem: Int, unit: String) {
        val shopListsHelper = ShopListsDatabaseHelper()
        shopListsHelper.addProductToList(listId, title, countItem, unit)
    }

    private fun startScanning() {
        // Start barcode scanning
        barcodeView.resume()
    }

    private fun fetchProductInfo(barcodeNumber: String) {
        val apiUrl = "https://world.openfoodfacts.org/api/v2/product/$barcodeNumber.json"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(apiUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Request", "Failed to fetch product information", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val status = jsonResponse.optInt("status")
                        if (status == 0) {
                            // Product not found, handle accordingly (e.g., show a message)
                            Log.d("Product Info", "Product not found")
                            requireActivity().runOnUiThread {
                                // Show a message or perform any action for a not found product
                                // You can also modify the showProductDialog method to handle this case
                                showProductDialog(
                                    requireContext(),
                                    "מצטערים, המוצר אינו נמצא.",
                                    null
                                )
                            }
                        } else {
                            // Product found, continue parsing and displaying details
                            val product = jsonResponse.optJSONObject("product")
                            val productNameEn =
                                product?.optString("product_name", "שם המוצר אינו קיים.")
                            val frontImage = product?.optString("image_front_url")
                                ?: product?.optString("image_front_small_url")

                            Log.d("Product Info", "Product Name (English): $productNameEn")

                            // Load image on the main thread
                            requireActivity().runOnUiThread {
                                showProductDialog(
                                    requireContext(),
                                    productNameEn!!,
                                    frontImage
                                )
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e("JSON Parsing", "Error parsing JSON response", e)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}
