package com.example.finalproject2.locations

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import com.example.finalproject2.R
import com.example.finalproject2.databinding.ActivityLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.GeoApiContext

class locationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val apiKey = "AIzaSyCjKahrzuFEugbwt6iIXuJxRq9BIKyHD-U"
        val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true

        map.uiSettings.isZoomControlsEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                addMarker(currentLatLng, "My Location", Color.RED)

                map.addPolyline(
                    PolylineOptions().add(currentLatLng).add(LatLng(31.513816, 34.441997))
                        .color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.551822027618943, 34.46192456962971)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.558199412720914, 34.486216078003196)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.538938843149445, 34.51498357522477)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.461931836598325, 34.42375876513152)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.41879348305172, 34.350978496263814)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.522063708405366, 34.485469779485584)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.52637556011188, 34.436248114978035)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.532797857648593, 34.5234672207831)).color(Color.DKGRAY)

                )

                map.addPolyline(
                    PolylineOptions().add(currentLatLng)
                        .add(LatLng(31.361065536513845, 34.329789630476625)).color(Color.DKGRAY)

                )


            }
        }
    }

    private fun addMarker(latLng: LatLng, title: String, color: Int) {
        val markerOptions = MarkerOptions().position(latLng).title(title)

        map.addMarker(markerOptions)

        val lib_programming = LatLng(31.513816, 34.441997)
        map.addMarker(
            MarkerOptions().position(lib_programming).title("Marker in Programming Library")
        )


        val lib_arabic = LatLng(31.551822027618943, 34.46192456962971)
        map.addMarker(MarkerOptions().position(lib_arabic).title("Marker in Arabic Library"))


        val lib_english = LatLng(31.558199412720914, 34.486216078003196)
        map.addMarker(MarkerOptions().position(lib_english).title("Marker in English Library"))


        val lib_database = LatLng(31.538938843149445, 34.51498357522477)
        map.addMarker(MarkerOptions().position(lib_database).title("Marker in Database Library"))


        val lib_dataScience = LatLng(31.461931836598325, 34.42375876513152)
        map.addMarker(
            MarkerOptions().position(lib_dataScience).title("Marker in Data Science Library")
        )


        val lib_marketing = LatLng(31.41879348305172, 34.350978496263814)
        map.addMarker(MarkerOptions().position(lib_marketing).title("Marker in Marketing Library"))


        val lib_android = LatLng(31.522063708405366, 34.485469779485584)
        map.addMarker(
            MarkerOptions().position(lib_android).title("Marker in Android Development Library")
        )


        val lib_security = LatLng(31.52637556011188, 34.436248114978035)
        map.addMarker(MarkerOptions().position(lib_security).title("Marker in Security Library"))


        val lib_AI = LatLng(31.532797857648593, 34.5234672207831)
        map.addMarker(
            MarkerOptions().position(lib_AI).title("Marker in Artificial Intelligence Library")
        )


        val lib_softwareEng = LatLng(31.361065536513845, 34.329789630476625)
        map.addMarker(
            MarkerOptions().position(lib_softwareEng)
                .title("Marker in Software Engineering Library")
        )


    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}


