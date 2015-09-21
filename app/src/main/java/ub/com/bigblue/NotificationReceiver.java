package ub.com.bigblue;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class NotificationReceiver extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    protected static final String TAG = "NotificationReceiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_receiver);
        NotificationManager notifManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancel(SilentReceiver.NOTIFICATION_ID);
        //notifManager.cancelAll();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radioButtonYes:
                if (checked) {
                    Log.d(TAG, "User is safe");
                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    String number = preferences.getString("ownNumber",null);
                    final ParseQuery<ParseObject> query = ParseQuery.getQuery("Shishir");
                    query.whereEqualTo("myNumber", number);
                    Log.i(TAG,"here");
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        public void done(ParseObject myNumber, ParseException e) {
                            if (e == null) {
                                query.getInBackground(myNumber.getObjectId(), new GetCallback<ParseObject>() {
                                    public void done(ParseObject person, ParseException e) {
                                        if (e == null) {
                                            person.put("isSafe", "y");
                                            person.saveInBackground();
                                            Toast.makeText(getBaseContext(), "You are safe and your emergency contact has been notified.", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    }
                                });
                            } else {
                                Log.i(TAG, "Error: " + e.getMessage());
                            }
                        }
                    });
                }
                    break;
            case R.id.radioButtonNo:
                if (checked){
                    Log.d(TAG,"User is unsafe");
                    Toast.makeText(this, "Help is on its way. Hang in there.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification_receiver, menu);
        return true;
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
