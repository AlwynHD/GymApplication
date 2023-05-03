package com.example.gymconsultationapp

import android.location.Location
//import com.example.gymconsultationapp.clusters.ZoneClusterItem

data class MapState(
    val lastKnownLocation: Location?,
    val clusterItems: List<ZoneClusterItem>,
)