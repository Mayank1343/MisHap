package com.example.mishappawarenessapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Default location (Delhi)
        val defaultLocation = LatLng(28.6139, 77.2090)
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f)
        )

        googleMap.setOnMarkerClickListener { marker ->

            val incidentId = marker.tag as? String ?: return@setOnMarkerClickListener false

            val action =
                MapFragmentDirections.actionMapFragmentToIncidentDetailFragment(incidentId)

            requireView().findNavController().navigate(action)

            true
        }

        enableUserLocation()
        fetchIncidentsNearby()
    }
    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )

        }
    }

    private fun distanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371 // km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun fetchIncidentsNearby() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location == null) return@addOnSuccessListener

                val userLat = location.latitude
                val userLng = location.longitude

                FirebaseFirestore.getInstance()
                    .collection("incidents")
                    .get()
                    .addOnSuccessListener { documents ->

                        googleMap.clear()

                        for (doc in documents) {

                            val geoPoint = doc.getGeoPoint("location") ?: continue
                            val lat = geoPoint.latitude
                            val lng = geoPoint.longitude

                            val title = doc.getString("title") ?: "Incident"
                            val type = doc.getString("type") ?: "accident"

                            val distance = distanceInKm(
                                userLat, userLng,
                                lat, lng
                            )

                            if (distance <= 30) {

                                val markerPosition = LatLng(lat, lng)

                                val iconHue = when (type) {
                                    "fire" -> BitmapDescriptorFactory.HUE_RED
                                    "flood" -> BitmapDescriptorFactory.HUE_BLUE
                                    else -> BitmapDescriptorFactory.HUE_ORANGE
                                }

                                val marker = googleMap.addMarker(
                                    MarkerOptions()
                                        .position(markerPosition)
                                        .title(title)
                                        .snippet("${distance.toInt()} km away")
                                        .icon(BitmapDescriptorFactory.defaultMarker(iconHue))
                                )

                                marker?.tag = doc.id

                                googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(markerPosition, 14f)
                                )
                            }
                        }
                    }
            }
    }



    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
            fetchIncidentsNearby()
        }
    }

}
