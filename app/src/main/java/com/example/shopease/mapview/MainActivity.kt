//package com.example.shopease.mapview
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.shopease.databinding.ActivityMainBinding
//import com.example.shopease.models.Place
//import com.example.shopease.models.UserMap
//
//const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var mapsAdapter: MapsAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//        binding.rvMap.layoutManager = LinearLayoutManager(this@MainActivity)
//        mapsAdapter = MapsAdapter()
//        binding.rvMap.adapter = mapsAdapter
//        mapsAdapter.map = sampleData()
//    }
//
//    private fun sampleData(): List<UserMap> {
//        return listOf(
//            UserMap(
//                "Sport",
//                listOf(
//                    Place("Itai Gafni", "Best Faculty Building", 6.466241, 3.199929),
//                    Place("Science Room", "The place to relax in Science", 6.467073, 3.199710)
//                )
//            ),
//            UserMap(
//                "Art",
//                listOf(
//                    Place("CR7", "For BTA Tutorials", 6.464109, 3.201280),
//                    Place("Random Place", "Random Stuff", 6.465179, 3.201280)
//                )
//            )
//     )
//    }
//}