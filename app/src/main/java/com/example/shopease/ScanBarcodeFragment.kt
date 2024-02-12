package com.example.shopease

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class BarcodeScannerFragment : Fragment() {

    private lateinit var barcodeView: CompoundBarcodeView
    private lateinit var scanButton: Button

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
        scanButton = view.findViewById(R.id.scanButton)

        scanButton.setOnClickListener {
            checkCameraPermission()
        }

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

    private fun showProductDialog(context: Context, productName: String, imageUrl: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Product Details")
            .setMessage("Name: $productName")

        // Load and set the image using Glide asynchronously
        val imageView = ImageView(context)
        if (!imageUrl.isNullOrBlank()) {
            Thread {
                try {
                    val url = URL(imageUrl)
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                    // Load the bitmap on the main thread using runOnUiThread
                    requireActivity().runOnUiThread {
                        imageView.setImageBitmap(bmp)
                    }
                } catch (e: IOException) {
                    Log.e("Image Loading", "Error loading image", e)
                }
            }.start()
        }

        builder.setView(imageView)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun startScanning() {
        // Start barcode scanning
        barcodeView.resume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scanning
                startScanning()
            } else {
                // Permission denied, show a message or handle accordingly
                Log.d("Permission", "Camera permission denied")
            }
        }
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
                        val product = jsonResponse.optJSONObject("product")
                        val productNameEn = product?.optString("product_name_en", "Product Name not available")
                        val images = product?.optJSONObject("selected_images")
                        val ingredientsImage = images?.optJSONObject("ingredients")?.optJSONObject("small")?.optString("en")

                        Log.d("Product Info", "Product Name (English): $productNameEn")

                        // Load image on the main thread
                        requireActivity().runOnUiThread {
                            showProductDialog(requireContext(), productNameEn!!, ingredientsImage)
                        }

                    } catch (e: Exception) {
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

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}
