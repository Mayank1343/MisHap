package com.example.mishappawarenessapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val firestore = FirebaseFirestore.getInstance()
    private val LOCATION_PERMISSION_REQUEST = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Safe Argument Retrieval
        val latStr = arguments?.getString("targetLat") ?: "0.0"
        val lngStr = arguments?.getString("targetLng") ?: "0.0"

        val targetLat = latStr.toDoubleOrNull() ?: 0.0
        val targetLng = lngStr.toDoubleOrNull() ?: 0.0

        if (targetLat != 0.0 && targetLng != 0.0) {
            val postLocation = LatLng(targetLat, targetLng)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 16f))
            Toast.makeText(context, "Showing report location", Toast.LENGTH_SHORT).show()
        } else {
            val defaultLocation = LatLng(28.6139, 77.2090)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        }

        enableUserLocation()
        fetchIncidentsNearby()
        fetchPostsOnMap()
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    private fun fetchPostsOnMap() {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val geoPoint = doc.getGeoPoint("location") ?: continue
                    val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                    val content = doc.getString("content") ?: "Mishap Report"
                    val username = doc.getString("username") ?: "Anonymous"

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Reported by: $username")
                            .snippet(content)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    )?.tag = doc.id
                }
            }
    }

    private fun fetchIncidentsNearby() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) return@addOnSuccessListener
            val userLat = location.latitude
            val userLng = location.longitude

            firestore.collection("incidents").get().addOnSuccessListener { documents ->
                for (doc in documents) {
                    val geoPoint = doc.getGeoPoint("location") ?: continue
                    val distance = distanceInKm(userLat, userLng, geoPoint.latitude, geoPoint.longitude)

                    if (distance <= 30) {
                        val type = doc.getString("type") ?: "accident"
                        val iconHue = when (type) {
                            "fire" -> BitmapDescriptorFactory.HUE_RED
                            "flood" -> BitmapDescriptorFactory.HUE_BLUE
                            else -> BitmapDescriptorFactory.HUE_ORANGE
                        }
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                                .title(doc.getString("title") ?: "Incident")
                                .icon(BitmapDescriptorFactory.defaultMarker(iconHue))
                        )?.tag = doc.id
                    }
                }
            }
        }
    }

    private fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        return r * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
            fetchIncidentsNearby()
            fetchPostsOnMap()
        }
    }
}