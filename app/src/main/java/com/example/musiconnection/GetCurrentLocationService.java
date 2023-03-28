package com.example.musiconnection;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

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
    protected LatLng doInBackground(Void... voids)
    {
        double latitude;
        double longitude;

        LocationServices.getFusedLocationProviderClient(weakReference.get())
                .removeLocationUpdates(locationCallback);

        if (locationResult != null && locationResult.getLocations().size() > 0)
        {
            // Get the most recent location from the location result
            int index = locationResult.getLocations().size() - 1;
            latitude = locationResult.getLocations().get(index).getLatitude();
            longitude = locationResult.getLocations().get(index).getLongitude();
            return new LatLng(latitude, longitude);
        }
        return null;
    }
}
