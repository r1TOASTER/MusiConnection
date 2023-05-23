package com.example.musiconnection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.musiconnection.databinding.ActivityMainScreenAppBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainScreenApp extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener  {
    GoogleMap mMap = null;
    ListView listViewBands, listViewDialogBand;
    BandAdapter bandAdapter;
    ArrayList<User> members;
    ActivityMainScreenAppBinding binding;
    BandClass lastBandSelected;
    User currentUser;
    Dialog viewDialog, viewBandToJoin;
    Button exitWithoutSavingBand;
    TextView nameOfTheOwner, nameOfTheBand, currentLocationAddressBand;
    MembersAdapter membersAdapter;
    String currentUserMail;
    private LocationManager mLocationManager;
    LocationClass currentUserLocation = null;
    LatLng curreLatLngUser = null;
    private LocationRequest locationRequest;
    FileOutputStream load;
    ArrayList<BandClass> getBandsFromLocation = null;
    int radius;

    float zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please enable location in the app settings", Toast.LENGTH_LONG).show();
        }

        binding = ActivityMainScreenAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sh = getSharedPreferences("currentUser", MODE_PRIVATE);
        currentUserMail = sh.getString("UserMail", "");
        radius = sh.getInt("radius", -1);

        if (radius == -1){
            SharedPreferences.Editor editor = sh.edit();
            editor.putInt("radius", 10);
            editor.apply();
            radius = 10;
        }

        zoom = (float)(16 - Math.log((float) radius * 1.4) / Math.log(2));

        //getting currentUser from mail using mongo
        String ret = dbInteract("searchmail users " + currentUserMail);

        listViewBands = findViewById(R.id.lvBands);

        //if nothing failed -- convert object to user
        if (ret.equals("ServerFailed")) {
            Toast.makeText(this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
            settings.edit().clear().apply();
            Intent intent = new Intent(MainScreenApp.this, MainActivity.class);
            startActivity(intent);
        }
        if (!ret.equals("Failed"))
            currentUser = toUser(ret);
        else {
            Toast.makeText(this, "Error ocuured during a try to connect. Please try again later", Toast.LENGTH_LONG).show();
            SharedPreferences settings = getSharedPreferences("currentUser", Context.MODE_PRIVATE);
            settings.edit().clear().apply();
            Intent intent = new Intent(MainScreenApp.this, MainActivity.class);
            startActivity(intent);
        }

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainScreenApp.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainScreenApp.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()){
                    LocationServices.getFusedLocationProviderClient(MainScreenApp.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult){
                            GetCurrentLocationService getCurrentLocationService = new GetCurrentLocationService(MainScreenApp.this, locationResult, this);
                            try {
                                LatLng newLatLngCurrent = getCurrentLocationService.execute().get();

                                Geocoder geocoder = new Geocoder(MainScreenApp.this);
                                List<Address> addresses;

                                try {
                                    addresses = geocoder.getFromLocation(newLatLngCurrent.latitude, newLatLngCurrent.longitude, 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        Address address = addresses.get(0);
                                        String addressLine = address.getAddressLine(0);
                                        currentUserLocation = new LocationClass(newLatLngCurrent.latitude, newLatLngCurrent.longitude, addressLine);
                                        getBandsFromLocation = getMyBandsFromDBUsingLocation(currentUserLocation);
                                        if (bandAdapter != null){
                                            bandAdapter.clear();
                                            for (BandClass b : getBandsFromLocation){
                                                bandAdapter.add(b);
                                            }
                                            bandAdapter.notifyDataSetChanged();
                                            listViewBands.setAdapter(bandAdapter);
                                        } else {
                                            bandAdapter = new BandAdapter(MainScreenApp.this, 0, 0, getBandsFromLocation, currentUser);
                                            listViewBands.setAdapter(bandAdapter);
                                        }
                                        if (mMap != null) {
                                            for (BandClass b : getBandsFromLocation) {
                                                mMap.addMarker(new MarkerOptions().position(new LatLng(b.getLocation().getLatitude(), b.getLocation().getLongitude())));
                                            }
                                            if (currentUserLocation != null) {
                                                LatLng currentLocation = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
                                                if (currentLocation != null) {
                                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, zoom);
                                                    mMap.animateCamera(cameraUpdate);
                                                }
                                            }
                                        }
                                    }
                                }
                                catch (IOException e){
                                    e.printStackTrace();
                                }

                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }, Looper.getMainLooper());
                } else {
                    turnOnGps();
                }
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }

        if (bandAdapter != null){
            // if no adapter for the bands right now
            if (getBandsFromLocation == null){
                // no bands from the user
                if (currentUserLocation != null) {
                    // there is the current location
                    getBandsFromLocation = getMyBandsFromDBUsingLocation(currentUserLocation);
                    bandAdapter.clear();
                    for (BandClass b : getBandsFromLocation){
                        bandAdapter.add(b);
                    }
                    // get the bands and add it
                }
                else {
                    // use empty bands
                    getBandsFromLocation = new ArrayList<>();
                    bandAdapter.clear();
                    for (BandClass b : getBandsFromLocation){
                        bandAdapter.add(b);
                    }
                }
                bandAdapter.notifyDataSetChanged();
                listViewBands.setAdapter(bandAdapter);

                if (mMap != null) {
                    for (BandClass b : getBandsFromLocation) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(b.getLocation().getLatitude(), b.getLocation().getLongitude())));
                    }
                    if (currentUserLocation != null) {
                        LatLng currentLocation = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, zoom);
                        mMap.animateCamera(cameraUpdate);
                    }
                }
                // set the map with the bands markers
            }
        }
        else {
            // if there is an adapter for the bands
            if (getBandsFromLocation == null){
                // if there are no bands from the user
                if (currentUserLocation != null) {
                    // if there is the current user's location
                    getBandsFromLocation = getMyBandsFromDBUsingLocation(currentUserLocation);
                    bandAdapter = new BandAdapter(MainScreenApp.this, 0, 0, getBandsFromLocation, currentUser);
                    for (BandClass b : getBandsFromLocation){
                        bandAdapter.add(b);
                    }
                    // get the bands
                }
                else {
                    getBandsFromLocation = new ArrayList<>();
                    bandAdapter = new BandAdapter(MainScreenApp.this, 0, 0, getBandsFromLocation, currentUser);
                    for (BandClass b : getBandsFromLocation){
                        bandAdapter.add(b);
                    }
                }
                bandAdapter.notifyDataSetChanged();
                listViewBands.setAdapter(bandAdapter);

                if (mMap != null) {
                    for (BandClass b : getBandsFromLocation) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(b.getLocation().getLatitude(), b.getLocation().getLongitude())));
                    }
                    if (currentUserLocation != null) {
                        LatLng currentLocation = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, zoom);
                        mMap.animateCamera(cameraUpdate);
                    }
                }
                // set the map with the bands
            }
        }


        listViewBands.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lastBandSelected = bandAdapter.getItem(i);
                if (lastBandSelected.getOwner().getMail().equals(currentUser.getMail()) || lastBandSelected.isMember(currentUser)){
                    viewBandDialog(lastBandSelected);
                }
                else {
                    viewBandDialogToJoin(lastBandSelected);
                }
            }
        });

    }

    private void viewBandDialogToJoin(BandClass lastBandSelected) {
        viewBandToJoin = new Dialog(MainScreenApp.this);
        viewBandToJoin.setContentView(R.layout.list_item_band_to_join);
        viewBandToJoin.setCancelable(false);

        TextView nameOfTheBand = viewBandToJoin.findViewById(R.id.nameOfTheBand);
        TextView nameOfTheOwner = viewBandToJoin.findViewById(R.id.nameOfTheOwner);
        TextView currentLocationAddressBand = viewBandToJoin.findViewById(R.id.locationOfTheBandInAddress);

        ListView listViewDialogBand = viewBandToJoin.findViewById(R.id.lvMembersOfBand);

        ArrayList<User> members = new ArrayList<User>();

        for (int i = 0; i < 5; ++i) {
            if (lastBandSelected.getMembers()[i] != null)
                members.add(lastBandSelected.getMembers()[i]);
        }

        MembersAdapter membersAdapter = new MembersAdapter(this, 0, 0, members);
        listViewDialogBand.setAdapter(membersAdapter);

        nameOfTheOwner.setText(lastBandSelected.getOwner().getName());
        nameOfTheBand.setText(lastBandSelected.getName());
        currentLocationAddressBand.setText(lastBandSelected.getLocation().getAddress());

        Button exitWithoutSavingBand = viewBandToJoin.findViewById(R.id.exitViewingBand);
        exitWithoutSavingBand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == exitWithoutSavingBand){
                    viewBandToJoin.dismiss();
                }
            }
        });

        Button requestToJoinBand = viewBandToJoin.findViewById(R.id.requestToJoin);
        requestToJoinBand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == requestToJoinBand) {
                    String theBand = lastBandSelected.toString();
                    if (lastBandSelected.isFull()) {
                        Toast.makeText(MainScreenApp.this, "This band is already full.", Toast.LENGTH_LONG).show();
                    } else {
                        String request_exist = dbInteract("requestexist " + currentUser.toString() + " " + theBand);
                        if (request_exist.equals("ServerFailed")) {
                            Toast.makeText(MainScreenApp.this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
                        }
                        else if (request_exist.equals("True")) {
                            Toast.makeText(MainScreenApp.this, "Already requested to join this band.", Toast.LENGTH_LONG).show();
                        } else {
                            String add_request = dbInteract("addrequest " + currentUser.toString() + " " + theBand);
                            if (add_request.equals("ServerFailed")) {
                                Toast.makeText(MainScreenApp.this, "Server failed to connect. Please try again later", Toast.LENGTH_LONG).show();
                            }
                            else if (add_request.equals("Failed")) {
                                Toast.makeText(MainScreenApp.this, "Failed to request the band owner to join this band. Try again later. ", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainScreenApp.this, "Request sent successfully. ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    viewBandToJoin.dismiss();
                }
            }
        });

        viewBandToJoin.show();
    }

    private void turnOnGps() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainScreenApp.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainScreenApp.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    //converts string from server DB to user class object
    private User toUser(String ret) {
        String[] values = ret.split(","); //spliting by ","
        String name = values[0];
        String mail = values[1];
        String password = values[2];
        User returnUser = new User(name, mail, password);
        for (int i = 0; i < 4; ++i){
            returnUser.setInstrument(i, values[i + 3].equals(" true"));
        }
        return returnUser;
    }


    private void viewBandDialog(BandClass lastBandSelected) {
        viewDialog = new Dialog(MainScreenApp.this);
        viewDialog.setContentView(R.layout.list_item_bands);
        viewDialog.setCancelable(false);

        nameOfTheBand = viewDialog.findViewById(R.id.nameOfTheBand);
        nameOfTheOwner = viewDialog.findViewById(R.id.nameOfTheOwner);
        currentLocationAddressBand = viewDialog.findViewById(R.id.locationOfTheBandInAddress);

        listViewDialogBand = viewDialog.findViewById(R.id.lvMembersOfBand);

        members = new ArrayList<User>();

        for (int i = 0; i < 5; ++i) {
            if (lastBandSelected.getMembers()[i] != null)
                members.add(lastBandSelected.getMembers()[i]);
        }

        membersAdapter = new MembersAdapter(this, 0, 0, members);
        listViewDialogBand.setAdapter(membersAdapter);

        nameOfTheOwner.setText(lastBandSelected.getOwner().getName());
        nameOfTheBand.setText(lastBandSelected.getName());
        currentLocationAddressBand.setText(lastBandSelected.getLocation().getAddress());

        exitWithoutSavingBand = viewDialog.findViewById(R.id.exitViewingBand);
        exitWithoutSavingBand.setOnClickListener(this);

        viewDialog.show();
    }
    // *END OF VIEW BAND DIALOG*


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (currentUserLocation != null) {
            LatLng currentLocation = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        }

        if (getBandsFromLocation != null){
            for (BandClass b : getBandsFromLocation){
                mMap.addMarker(new MarkerOptions().position(new LatLng(b.getLocation().getLatitude(), b.getLocation().getLongitude())));
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == exitWithoutSavingBand) {
            viewDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_screen_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_my_bands:
                Intent intent = new Intent(MainScreenApp.this, MyBandsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                Intent intentS = new Intent(MainScreenApp.this, SettingActivirty.class);
                startActivity(intentS);
                return true;
            case R.id.menu_requests:
                Intent intentR = new Intent(MainScreenApp.this, RequestsPage.class);
                startActivity(intentR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String dbInteract(String message) {
        String response;
        try {
            Sockets dbLinker = new Sockets();
            response = dbLinker.execute(message).get();
        } catch (ExecutionException | InterruptedException e) {
            response = "Failed";
            e.printStackTrace();
        }
        return response;
    }

    private ArrayList<BandClass> getMyBandsFromDBUsingLocation(LocationClass currentUserLocation) {
        ArrayList<BandClass> ret = new ArrayList<>();
        String allofbandsString;
        try {
            Sockets dbLinker = new Sockets();
            allofbandsString = dbLinker.execute("searchlocation " + radius + " " + currentUserLocation.toString()).get();

            String[] bandsArray = allofbandsString.split(":");

            int length;
            try {
                length = Integer.parseInt(bandsArray[1]);
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

            for (int i = 1; i <= length; ++i){
                String currentBand = bandsArray[i + 1];
                BandClass band = toBand(String.join(",", Arrays.copyOfRange(currentBand.split(","), 0, currentBand.length())));

                ret.add(band);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return ret;
    }

    private BandClass toBand(String substring) {
        String[] values = substring.split(",");
        String name = values[0];
        LocationClass location = new LocationClass(Double.parseDouble(values[1]), Double.parseDouble(values[2]), values[3]);
        User owner = toUser(String.join(",", Arrays.copyOfRange(values, 4, 11)));
        int lengthOfMembers = Integer.parseInt(values[11]);
        User[] members = new User[lengthOfMembers];

        for (int i = 0; i < lengthOfMembers; ++i){
            members[i] = toUser(String.join(",", Arrays.copyOfRange(values, i * 7 + 12, i * 7 + 19)));
        }

        BandClass ret = new BandClass(name, location, owner);
        for (int i = 0; i < lengthOfMembers; ++i) {
            ret.addMember(members[i]);
        }
        return ret;
    }


}



