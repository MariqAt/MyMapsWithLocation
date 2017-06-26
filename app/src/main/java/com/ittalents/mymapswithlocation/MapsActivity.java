package com.ittalents.mymapswithlocation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private AlertDialog alertDialog;
    private Location mLastLocation;
    private LatLng myLocation;
    private String currentLocationName;
    protected boolean fromLocation;
    public static final String INTERNET_CONNECTION = "internetConnection";
    public static final String SERVER_CONNECTION_FAILURE = "serverConnectionFailure";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mGoogleApiClient.disconnect();
//
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        Log.d("test", "onMapready");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        } else { //has permission
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("test", "onConnected");
            if (mLastLocation != null) {
                if(isLocationEnabled(this)) {
                    fromLocation = true;
                    myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("My location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
                } else {
                    showAlertDialog(ACTION_LOCATION_SOURCE_SETTINGS);
                }
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) { // from location services
                    if(isLocationEnabled(this)) {
                        currentLocationName = String.valueOf(mLastLocation.getLatitude()).concat(" ").concat(String.valueOf(mLastLocation.getLongitude()));
                        fromLocation = true;
                    } else {
                        showAlertDialog(ACTION_LOCATION_SOURCE_SETTINGS);
                    }
                } else {
                    showAlertDialog(ACTION_LOCATION_SOURCE_SETTINGS);
                }
            }
        }
    }


    public void showAlertDialog(String event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (event.equals(INTERNET_CONNECTION)) {
            builder.setTitle("No internet Connection");
            builder.setMessage("Please turn on internet connection to continue");
            builder.setCancelable(false);
            builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }

        if (event.equals(SERVER_CONNECTION_FAILURE)) {
            builder.setTitle("Error");
            builder.setMessage("Sorry, something went wrong. A team of highly trained monkeys has been dispatched to deal with this situation");
            builder.setCancelable(false);
            builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
        if(event.equals(ACTION_LOCATION_SOURCE_SETTINGS)){
            builder.setTitle("Location services are off");
            builder.setMessage("Please turn on location services to continue");
            builder.setCancelable(false);
            builder.setNegativeButton("QUIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO
                }
            });
            builder.setPositiveButton("Turn on location", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection to Google Location Services timed out.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Cannot connect to Google Location Services.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("My location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }
}
