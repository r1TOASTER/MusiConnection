package com.example.musiconnection;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

// GetCurrentLocationService is resposible for getting the user's current location as a service.
public class GetCurrentLocationService extends AsyncTask<Void, Void, LatLng>
{
    private WeakReference<MainScreenApp> weakReference;
    private LocationCallback locationCallback;
    private LocationResult locationResult;

    GetCurrentLocationService(MainScreenApp activity, @NonNull LocationResult locationResult, LocationCallback locationCallback)
    {
        weakReference = new WeakReference<>(activity);
        this.locationResult = locationResult;
        this.locationCallback = locationCallback;
    }

    // .execute().get() gets the return value.
    @Override
    // This AsyncTask retrieves the user's current location using the Fused Location Provider API.
    protected LatLng doInBackground(Void... voids)
    {
        // Remove any pending location updates
        LocationServices.getFusedLocationProviderClient(weakReference.get())
                .removeLocationUpdates(locationCallback);

        // If there is at least one location in the location result object
        if (locationResult != null && locationResult.getLocations().size() > 0)
        {
            // Get the most recent location from the location result - the last index means the last location result in the list
            int index = locationResult.getLocations().size() - 1;

            // Store the latitude and the longitude of the last location in the list (most recent)
            double latitude = locationResult.getLocations().get(index).getLatitude();
            double longitude = locationResult.getLocations().get(index).getLongitude();

            // Create a new LatLng object with the latitude and longitude values from the last location
            return new LatLng(latitude, longitude);
        }

        // If no location is available, return null
        return null;
    }
}
