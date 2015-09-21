package ub.com.bigblue;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by sahajbedi on 19-Sep-15.
 */
public class SilentReceiver extends ParsePushBroadcastReceiver {
    private LocationManager locationManager;
    ParseGeoPoint ownLocation ;
    public static final String PREFS_NAME = "MyPrefsFile";
    protected static final String TAG = "SilentReceiver";
    static final int NOTIFICATION_ID = 001;


    @Override
    public void onReceive(Context context, Intent intent) throws SecurityException {
        final Context tempContext = context;
        Location location = getLocation(context);
        if (location != null) {
            Log.d(TAG,"Location found : "+location.getLatitude()+", "+location.getLongitude());
            ownLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
        } else {
            Log.d(TAG, "Location not found");
            return;
        }
        String action = intent.getAction();
        int i = 0;
        if (action.equals(ParsePushBroadcastReceiver.ACTION_PUSH_RECEIVE)) {
            JSONObject extras;
            String notification;
            try {
                extras = new JSONObject(intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA));
                notification = extras.getString("alert");       //incoming request is in json format and the actual message is under the alert field
                notification = notification.trim();
                Log.i(TAG, "Notification is" + notification);
                String []information = notification.split(" ");
                int instances = information.length/3;           //individual data consists of 3 fields.
                double magnitude[] = new double[instances];
                double latitude[] = new double[instances];
                double longitude[] = new double[instances];
                int count = 0;
                for(i=0;i<information.length;i=i+3){
                    magnitude[count] = Double.parseDouble(information[i]);
                    latitude[count] = Double.parseDouble(information[i+1]);
                    longitude[count] = Double.parseDouble(information[i+2]);
                    count++;
                }
                for(i=0;i<instances;i++){
                    ParseGeoPoint temp = new ParseGeoPoint(latitude[i],longitude[i]);
                    Number distance = ownLocation.distanceInMilesTo(temp);
                    Log.d(TAG, "Location ("+latitude[i]+","+longitude[i]+") ---> Distance "+distance.toString());
                    if(distance.doubleValue()<=100){
                        Log.d(TAG, "User in danger");
                        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
                        String number = preferences.getString("ownNumber",null);
                        Log.d(TAG,"User number is "+number);
                        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Shishir");
                        query.whereEqualTo("myNumber", number);
                        Log.i("objectId", "here");
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            public void done(ParseObject myNumber, ParseException e) {
                                if (e == null) {
                                    query.getInBackground(myNumber.getObjectId(), new GetCallback<ParseObject>() {
                                        public void done(ParseObject person, ParseException e) {
                                            if (e == null) {
                                                double[] coordinates = getCurrentCoordinates(tempContext);
                                                person.put("latitude", Double.toString(coordinates[0]));
                                                person.put("longitude", Double.toString(coordinates[1]));
                                                person.put("isSafe", "n");
                                                person.saveInBackground();
                                            }
                                        }
                                    });
                                } else {
                                    Log.i(TAG, "Error: " + e.getMessage());
                                }
                            }
                        });
                        pushNotification(context);
                        break;
                    }
                }
                if(i == instances) {                //User did not fall in the vicinity of any of the calamities
                    Log.d(TAG, "User is safe");
                    Toast.makeText(context, "User is safe", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void pushNotification(Context context){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.small_question)
                        .setContentTitle("Terra Nova")
                        .setContentText("Are you safe?");
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Intent resultIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        Intent yesReceive = new Intent();
        Bundle yesBundle = new Bundle();
        yesBundle.putInt("userAnswer", 1);//This is the value I want to pass
        yesReceive.putExtras(yesBundle);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.small_tick, "Yes", pendingIntentYes);

        Intent noReceive = new Intent();
        Bundle noBundle = new Bundle();
        noBundle.putInt("userAnswer", 2);//This is the value I want to pass
        noReceive.putExtras(noBundle);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context, 12345, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.small_cross, "No", pendingIntentNo);

        int mNotificationId = NOTIFICATION_ID;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mBuilder.setLights(Color.BLUE, 500, 500);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //need to add vibrate alert as well
        mBuilder.setSound(uri);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public double[] getCurrentCoordinates(Context context) throws SecurityException{
        Location location = getLocation(context);
        double []coordinates = new double[2];
        if (location != null) {
            Log.d(TAG,"Location found : "+location.getLatitude()+", "+location.getLongitude());
            coordinates[0] = location.getLatitude();
            coordinates[1] = location.getLongitude();
            return coordinates;
        } else {
            Log.d(TAG, "Location not found");
            return coordinates;
        }
    }

    public Location getLocation(Context context) throws SecurityException{
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return location;
    }
}


