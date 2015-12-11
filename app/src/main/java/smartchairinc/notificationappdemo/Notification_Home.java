/** Main Activity
 * Hosts:
 *      Server Connection Methods
 *      Location Methods
 */

package smartchairinc.notificationappdemo;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.*;
import com.google.android.gms.location.LocationServices;
import com.microsoft.windowsazure.messaging.*;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.math.BigDecimal;


public class Notification_Home extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String SENDER_ID = "270356465391"; // Google Project Number
    private GoogleCloudMessaging gcm;
    private NotificationHub hub;
    private String HubName = "androidpushnotificationtest";  // Name of Azure Notification Hub
    private String HubListenConnectionString = "Endpoint=sb://androidpushnotificationtest-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=/C5g3BdR61uD772gUoQrlEnoUcYAOpy1pcBxpzqrDT0=";
    private static Boolean isVisible = false;
    private static final String TAG = Notification_Home.class.getSimpleName(); // LogCat tag
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private android.location.Location mLastLocation; // Last location
    private GoogleApiClient mGoogleApiClient; // Google client to interact with Google API
    private double[] LOC = {0, 0}; // Latitude, Longitude

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification__home);

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        MyHandler.mainActivity = this;  // Declare handler in this instance
        NotificationsManager.handleNotifications(this, SENDER_ID, MyHandler.class);
        gcm = GoogleCloudMessaging.getInstance(this);
        hub = new NotificationHub(HubName, HubListenConnectionString, this);
        registerWithNotificationHubs();
    }

    public void getLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) { //Checks to see if last location was available, else throws an error
            LOC[0] = mLastLocation.getLatitude();
            LOC[1] = mLastLocation.getLongitude();
        } else {
            Log.v("DEBUGGING","Couldn't get the location. Make sure location is enabled on the device");
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

    @SuppressWarnings("unchecked")
    private void registerWithNotificationHubs() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String regid = gcm.register(SENDER_ID);
                    DialogNotify("Registered Successfully","RegId : " +
                            hub.register(regid).getRegistrationId());  // MAKE THIS A LOG ENTRY to remove toast?
                } catch (Exception e) {
                    DialogNotify("Exception",e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        checkPlayServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

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

    /**
     * A modal AlertDialog for displaying a message on the UI thread
     * when there's an exception or message to report.
     *
     * @param title   Title for the AlertDialog box.
     * @param message The message displayed for the AlertDialog box.
     */
    public void DialogNotify(final String title,final String message)
    {
        if (isVisible == false)
            return;

        final AlertDialog.Builder dlg;
        dlg = new AlertDialog.Builder(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dlgAlert = dlg.create();
                dlgAlert.setTitle(title);
                dlgAlert.setButton(DialogInterface.BUTTON_POSITIVE,
                        (CharSequence) "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dlgAlert.setMessage(message);
                dlgAlert.setCancelable(false);
                dlgAlert.show();

                if (message.contains(",")) { // Parse message to check for LAT,LONG format
                    String[] parts = message.split(",");
                    double part1 = Double.parseDouble(parts[0]); // Latitude from alert
                    double part2 = Double.parseDouble(parts[1]); // Longitude from alert
                    getLocation();
                    Intent myIntent = new Intent(Notification_Home.this, Alert.class);
                    myIntent.putExtra("Latitude", part1);
                    myIntent.putExtra("Longitude", part2);
                    myIntent.putExtra("myLatitude", LOC[0]);
                    myIntent.putExtra("myLongitude", LOC[1]);
                    Notification_Home.this.startActivity(myIntent);
                }
            }
        });
    }

    // Demo Alert Button Method
    public void DemoAlert(View view) {
        getLocation();
        Intent myIntent = new Intent(Notification_Home.this, Alert.class);
        myIntent.putExtra("Latitude", 45);
        myIntent.putExtra("Longitude", 45);
        myIntent.putExtra("myLatitude", LOC[0]);
        myIntent.putExtra("myLongitude", LOC[1]);
        Notification_Home.this.startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification__home, menu);
        return true;
    } // No current options in menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
