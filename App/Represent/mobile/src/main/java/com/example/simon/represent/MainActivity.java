package com.example.simon.represent;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;


import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetui.UserTimeline;


public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "CcQMj9JNzCzSzvSDo2javRr5K";
    private static final String TWITTER_SECRET = "cAwsKuEhNU6E7U4lt5FpPZlhqG2svRBh1xy2Ivpj0KaZ099TUq";


    private String cityName;
    private String state;
    private String postalCode;
    private String latitude;
    private String longitude;
    private String sunlightApiKey;
    private String sunlightURL;
    private Location currentLocation;
    private MyLocationListener locationListener;
    private TwitterLoginButton loginButton;
    private String googleApiKey;
    private double romney;
    private double obama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

        sunlightURL = "http://congress.api.sunlightfoundation.com/legislators/locate?";
        sunlightApiKey = "41f8c6edf2514258bbdeebeb25bc7659";

        googleApiKey = "AIzaSyDiZygyg-TsxH-wnFYzsla-1Y5sx_D7S3Y";

        final EditText edittext = (EditText) findViewById(R.id.editText);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press

                    locationListener.getLocationFromZip(edittext.getText().toString());
                    postalCode = edittext.getText().toString();
                    String dataURL = sunlightURL + "latitude=" + latitude + "&longitude=" + longitude + "&zip=" + postalCode + "&apikey=" + sunlightApiKey;

                    String googleURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + googleApiKey;

                    new CallAPI().execute(googleURL);

                    new CallAPI().execute(dataURL);
                    return true;
                }
                return false;
            }
        });

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("newelectioncounty2012.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void useCurrentLocation(View view) throws JSONException {
        locationListener.getCurrentLocation(currentLocation);

        String dataURL = sunlightURL + "latitude=" + latitude + "&longitude=" + longitude + "&zip=" + postalCode + "&apikey=" + sunlightApiKey;

        String googleURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + googleApiKey;

        new CallAPI().execute(googleURL);

        new CallAPI().execute(dataURL);
    }

    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();

            currentLocation = loc;

        /*------- To get city name from coordinates -------- */
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void getCurrentLocation(Location loc) {
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),loc.getLongitude(), 1);
                if (addresses.size() > 0) {

                    postalCode = addresses.get(0).getPostalCode();
                    latitude = loc.getLatitude() + "";
                    longitude = loc.getLongitude() + "";
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void getLocationFromZip(String zip) {
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocationName(zip, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    // Use the address as needed
                    String message = String.format("Latitude: %f, Longitude: %f",
                            address.getLatitude(), address.getLongitude());
                    latitude = address.getLatitude() + "";
                    longitude = address.getLongitude() + "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getLocationAndStateFromZip(String zip) {
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocationName(zip, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    // Use the address as needed
                    String message = String.format("Latitude: %f, Longitude: %f",
                            address.getLatitude(), address.getLongitude());
                    return address.getLocality() + "," + address.getAdminArea();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return zip;
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private class CallAPI extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlString=params[0]; // URL to call


            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }

            } catch (Exception e ) {
                System.out.println(e.getMessage());

                return e.getMessage();

            }
        }

        protected void onPostExecute(String result) {

            JSONObject reader;

            try {
                reader = new JSONObject(result);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }

            JSONArray resultArray = reader.optJSONArray("results");

            JSONObject firstObj = null;

            try {
                firstObj = resultArray.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (firstObj.has("chamber")) {
                String[] pictureLinks = new String[resultArray.length()];
                String[] names = new String[resultArray.length()];
                String[] parties = new String[resultArray.length()];
                String[] emails = new String[resultArray.length()];
                String[] websites = new String[resultArray.length()];
                String[] dates = new String[resultArray.length()];
                boolean[] isSenator = new boolean[resultArray.length()];
                String[] ids = new String[resultArray.length()];
                String[] twitterHandles = new String[resultArray.length()];

                int indexInArray = 0;


                // loop for senators
                for (int i = 0; i < resultArray.length(); i++) {
                    try {
                        JSONObject currentObj = resultArray.getJSONObject(i);
                        String office = currentObj.getString("chamber");
                        if (Objects.equals(office, "senate")) {
                            names[indexInArray] = currentObj.getString("first_name") + " " + currentObj.getString("last_name");
                            String party = currentObj.getString("party");
                            if (Objects.equals(party, "D")) {
                                parties[indexInArray] = "Democrat";
                            } else if (Objects.equals(party, "R")){
                                parties[indexInArray] = "Republican";
                            }
                            else {
                                parties[indexInArray] = "Independent";
                            }
                            emails[indexInArray] = currentObj.getString("oc_email");
                            websites[indexInArray] = currentObj.getString("website");
                            dates[indexInArray] = currentObj.getString("term_start") + " to " + currentObj.getString("term_end");
                            isSenator[indexInArray] = true;
                            ids[indexInArray] = currentObj.getString("bioguide_id");
                            twitterHandles[indexInArray] = currentObj.getString("twitter_id");
                            pictureLinks[indexInArray] = "https://twitter.com/" + currentObj.getString("twitter_id") + "/profile_image?size=original";
                            indexInArray++;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // loop for representatives
                for (int i = 0; i < resultArray.length(); i++) {
                    try {
                        JSONObject currentObj = resultArray.getJSONObject(i);
                        String office = currentObj.getString("chamber");
                        if (Objects.equals(office, "house")) {
                            names[indexInArray] = currentObj.getString("first_name") + " " + currentObj.getString("last_name");
                            String party = currentObj.getString("party");
                            if (Objects.equals(party, "D")) {
                                parties[indexInArray] = "Democrat";
                            } else if (Objects.equals(party, "R")){
                                parties[indexInArray] = "Republican";
                            }
                            else {
                                parties[indexInArray] = "Independent";
                            }
                            emails[indexInArray] = currentObj.getString("oc_email");
                            websites[indexInArray] = currentObj.getString("website");
                            dates[indexInArray] = currentObj.getString("term_start") + " to " + currentObj.getString("term_end");
                            isSenator[indexInArray] = false;
                            ids[indexInArray] = currentObj.getString("bioguide_id");
                            twitterHandles[indexInArray] = currentObj.getString("twitter_id");
                            pictureLinks[indexInArray] = "https://twitter.com/" + currentObj.getString("twitter_id") + "/profile_image?size=original";
                            indexInArray++;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(getBaseContext(), showRepresentatives.class);
                intent.putExtra("cityName", cityName);
                intent.putExtra("postalCode", postalCode);
                intent.putExtra("pictureLinks", pictureLinks);
                intent.putExtra("names", names);
                intent.putExtra("parties", parties);
                intent.putExtra("emails", emails);
                intent.putExtra("websites", websites);
                intent.putExtra("dates", dates);
                intent.putExtra("isSenator", isSenator);
                intent.putExtra("ids", ids);
                intent.putExtra("twitterHandles", twitterHandles);
                intent.putExtra("sunlightKey", sunlightApiKey);
                intent.putExtra("state", state);
                startActivity(intent);

                Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                sendIntent.putExtra("names", names);
                sendIntent.putExtra("parties", parties);
                sendIntent.putExtra("isSenator", isSenator);
                sendIntent.putExtra("cityName", cityName);
                sendIntent.putExtra("state", state);
                sendIntent.putExtra("ids", ids);
                sendIntent.putExtra("obama", obama + "");
                sendIntent.putExtra("romney", romney + "");
                startService(sendIntent);
            }
            else {

                try {
                    JSONObject components = resultArray.getJSONObject(0);
                    JSONArray address_components = components.getJSONArray("address_components");
                    for (int i = 0; i < address_components.length(); i++) {
                        JSONObject address = address_components.getJSONObject(i);
                        JSONArray types = address.getJSONArray("types");
                        if (Objects.equals(types.getString(0), "administrative_area_level_2")) {
                            cityName = address.getString("short_name");
                        }
                        if (Objects.equals(types.getString(0), "administrative_area_level_1")) {
                            state = address.getString("short_name");
                        }
                    }

                    String votingData = loadJSONFromAsset();

                    JSONObject jsonVotingData = new JSONObject(votingData);

                    System.out.println(cityName + ", " + state);

                    JSONObject countyData = jsonVotingData.getJSONObject(cityName + ", " + state);

                    romney = countyData.getDouble("romney");

                    obama = countyData.getDouble("obama");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    } // end CallAPI
}
