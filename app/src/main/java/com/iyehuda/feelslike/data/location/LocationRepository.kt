package com.iyehuda.feelslike.data.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationDataSource: LocationDataSource
) {
    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun getLocationUpdates() =
        locationDataSource.getLocationUpdates().filterNotNull().distinctUntilChanged()
}
