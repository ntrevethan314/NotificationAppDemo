package smartchairinc.notificationappdemo;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {


        Bundle getLOC = getIntent().getExtras();
        double latitude = getLOC.getDouble("Latitude");
        double longitude = getLOC.getDouble("Longitude");
        double mylatitude = getLOC.getDouble("myLatitude");
        double mylongitude = getLOC.getDouble("myLongitude");
        Log.v("DevDebug","IN MAP LONG:"+latitude);
        Log.v("DevDebug","IN MAP LAT:"+longitude);
        mMap = googleMap;

        double fFactor = 0.00001;
        LatLng AlrtLoc = new LatLng(latitude,longitude);
        //LatLng MyLoc = new LatLng(mylatitude,mylongitude);
        LatLng MyLoc = new LatLng(mylatitude+fFactor,mylongitude+fFactor); // Offset current notification phone location for demon purposes (sensor and notification app in same room)

        mMap.addMarker(new MarkerOptions().position(AlrtLoc).title("Alert Location")); // Add marker for alert location
        mMap.addMarker(new MarkerOptions().position(MyLoc).title("My Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))); // add a blue marker for notification location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(AlrtLoc)); // Move map view to location of alert
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15)); // Default zoom
    }
}
