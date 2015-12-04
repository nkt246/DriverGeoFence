package com.geofence.drivergeofence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.geofence.drivergeofence.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;

import static com.google.android.gms.maps.UiSettings.*;

/**
 * Created by pc on 12/3/2015.
 */
public class DriverMapsActivity extends FragmentActivity implements  OnMapReadyCallback,LocationListener,GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    MapView mMapView;
    Marker startPerc;
    GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    PolylineOptions lineOpt;
    PolygonOptions polyOpt;
    String src, dest;
    LocationRequest mLocationRequest;

    ArrayList<LatLng> pathList = new ArrayList<LatLng>();
    ArrayList<LatLng> leftFenceList = new ArrayList<LatLng>();
    ArrayList<LatLng> rightFenceList = new ArrayList<LatLng>();

    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent i = getIntent();

        Bundle b = getIntent().getBundleExtra("pathBundle");
        Bundle bPt = getIntent().getExtras();
        if (b != null) {
          //  list1 = (ArrayList<String>) b.getStringArrayList("list");
            pathList = b.getParcelableArrayList("pathLatLng");
            leftFenceList = b.getParcelableArrayList("leftFenceLatLng");
            rightFenceList = b.getParcelableArrayList("rightFenceLatLng");

            src = bPt.getString("Source");
            dest = bPt.getString("Destination");
            Log.d("pathlist", pathList.toString());
        }

        initializemap();
            buildGoogleApiClient();
        createLocationRequest();
    }

    private void initializemap() {
        setUpMapIfNeeded();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        } else {
            TextView txt = (TextView) findViewById(R.id.err);
            txt.setText("Cannot do this");
        }

//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        // Create a criteria object to retrieve provider
//        Criteria criteria = new Criteria();
//
//        // Get the name of the best provider
//        String provider = locationManager.getBestProvider(criteria, true);
//
//        // Get Current Location
//        Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//
//        //set map type
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//
//        // Get latitude of the current location
//        double latitude = myLocation.getLatitude();
//
//        // Get longitude of the current location
//        double longitude = myLocation.getLongitude();
//
//        // Create a LatLng object for the current location
//        LatLng latLng = new LatLng(latitude, longitude);
//
//        // Show the current location in Google Map
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//
//        // Zoom in the Google Map
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(pathList.get(0)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(pathList.get(pathList.size() - 1)).title("End"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pathList.get(0)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        lineOpt = new PolylineOptions();
        polyOpt = new PolygonOptions();

        lineOpt.addAll(pathList);
        lineOpt.width(10);
        lineOpt.color(Color.RED);

        mMap.addPolyline(lineOpt);

        Collections.reverse(rightFenceList);
        polyOpt.addAll(leftFenceList).strokeColor(Color.BLUE).strokeWidth(3);
        polyOpt.addAll(rightFenceList).strokeColor(Color.BLUE).strokeWidth(3);

        mMap.addPolygon(polyOpt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // if (requestCode == MY_LOCATION_REQUEST_CODE) {
        if (permissions.length == 1 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            onLocationChanged(mLastLocation);
        } else {
            // Permission was denied. Display an error message.
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//        startPerc.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
//        LatLng origin = new LatLng(3.214732, 101.747047);
//        LatLng dest = new LatLng(3.214507, 101.749697);
        final ParseGeoPoint driverLoc = new ParseGeoPoint(location.getLatitude(),location.getLongitude());

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("PathFence");
        query2.whereEqualTo("Source", src).whereEqualTo("Destination", dest);
        query2.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject objects, com.parse.ParseException e) {
                        if(objects != null){
                            objects.put("DriverLocation", driverLoc);
                            objects.saveInBackground();
                            Toast.makeText(getApplication(),"sucessfully updated",Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        //locationManager.removeUpdates((android.location.LocationListener) this);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
//            if (mMap != null) {
//                setUpMap();
//            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
              Log.d("DriverLoc", String.valueOf(mLastLocation.getLatitude()));
              Log.d("DriverLoc", String.valueOf(mLastLocation.getLongitude()));
//            Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();

        }
        startLocationUpdates();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    protected void onStart(){
        super.onStart();
        if(mGoogleApiClient!=null)
        {
            mGoogleApiClient.connect();
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

//    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
//    }
}
