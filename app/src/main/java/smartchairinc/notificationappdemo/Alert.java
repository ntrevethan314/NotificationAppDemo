package smartchairinc.notificationappdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class Alert extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Notification_Home.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private android.location.Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private double[] LOC = {0, 0}; // Latitude, Longitude


    double latitude = -91.0;
    double longitude = -181.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Log.v("DevDebug", "ON CREATE");
        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }


    /**
     * Method to display the location on UI
     */
    public void getLocation(View view) {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            LOC[0] = mLastLocation.getLatitude();
            LOC[1] = mLastLocation.getLongitude();

        } else {
            Log.v("DevDebug", "Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void BeginMap(View view) {

        // Get locations from transmitted alert (Bundle from Notification_Home Activity
        Bundle getLOC = getIntent().getExtras();
        double latitude = getLOC.getDouble("Latitude");
        double longitude = getLOC.getDouble("Longitude");
        getLocation(view); // Get notification phone's current location

        if (latitude == 0.0 && longitude == 0.0) { // Error
            StartMap(45,45,view);

        } else {
            StartMap(latitude,longitude,view);
        }
    }

    /** Method to put locations of alert and notification apps into the map activity
     *
     * @param Lttd
     * @param Lngt
     * @param view
     */
    void StartMap(double Lttd, double Lngt, View view){
        Bundle locbndl = getIntent().getExtras(); // Lattitude and Longitude from broadcast alert
        double myLttd = locbndl.getDouble("myLatitude");
        double myLngt = locbndl.getDouble("myLongitude");

        Intent myIntent = new Intent(Alert.this, Map.class); // Put locations into map activity
        myIntent.putExtra("Latitude", Lttd);
        myIntent.putExtra("Longitude", Lngt);
        myIntent.putExtra("myLatitude", myLttd);
        myIntent.putExtra("myLongitude", myLngt);
        Alert.this.startActivity(myIntent); // Start map
    }
}
