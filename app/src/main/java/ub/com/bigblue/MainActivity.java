package ub.com.bigblue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener{
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected static final String TAG = "basic-location-sample";
    private EditText ownNumberET, emergencyNumberET;
    private TextView statusTV;
    private Button submitB;
    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        ownNumberET = (EditText)findViewById(R.id.ownNumber);
        emergencyNumberET = (EditText)findViewById(R.id.emergencyNumber);
        statusTV = (TextView)findViewById(R.id.statusTextView);
        submitB = (Button)findViewById(R.id.submitButton);
    }

    public void submit(View view) {
        final String self = ownNumberET.getText().toString();
        final String emergency = emergencyNumberET.getText().toString();
        final double []coordinates = getOwnLocation();
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Shishir");
        query.whereEqualTo("myNumber", self);
        Log.i("objectId","here : "+self);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject myNumber, ParseException e) {
                if (e == null) {
                    query.getInBackground(myNumber.getObjectId(), new GetCallback<ParseObject>() {
                        public void done(ParseObject person, ParseException e) {
                            if (e == null) {
                                double[] coordinates = getOwnLocation();
                                person.put("latitude", Double.toString(coordinates[0]));
                                person.put("longitude", Double.toString(coordinates[1]));
                                person.put("isSafe", "y");
                                person.saveInBackground();
                                statusTV.setText("Your profile has been updated");
                                submitB.setEnabled(false);
                            }
                        }
                    });
                } else {
                    Log.d("MainActivity","e is not null : "+e.toString());
                    ParseObject userP = new ParseObject("Shishir");
                    userP.put("myNumber", self);
                    userP.put("emergencyNumber", emergency);
                    userP.put("latitude", Double.toString(coordinates[0]));
                    userP.put("longitude",Double.toString(coordinates[1]));
                    userP.put("isSafe","y");
                    userP.saveInBackground();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("ownNumber", self);
                    editor.putString("emergencyNumber", emergency);

                    // Commit the edits!
                    editor.commit();

                    statusTV.setText("Your profile has been created");
                    submitB.setEnabled(false);
                }
            }
        });


    }

    public double[] getOwnLocation(){
        double []coordinates = new double[2];
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            coordinates[0] = mLastLocation.getLatitude();
            coordinates[1] = mLastLocation.getLongitude();
            //Toast.makeText(this, "Latitude : "+String.valueOf(mLastLocation.getLatitude())+", Longitude : "+String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No location found", Toast.LENGTH_LONG).show();
        }
        return coordinates;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
