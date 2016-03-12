package com.example.simon.represent;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Integer.parseInt;

/**
 * Created by Simon on 3/3/16.
 */
public class PhoneListenerService extends WearableListenerService {

    private String sunlightApiKey = "41f8c6edf2514258bbdeebeb25bc7659";
    private String sunlightURL = "http://congress.api.sunlightfoundation.com/legislators/locate?";
    private String googleApiKey = "AIzaSyDiZygyg-TsxH-wnFYzsla-1Y5sx_D7S3Y";
    private String[] committees;
    private String[] bills;
    private String[] billDates;
    private HashMap<String, String[]> lists;
    private HashMap<String, String> data;
    private String latitude;
    private String longitude;
    private String zipCode;
    private String county;
    private String state;
    private double obama;
    private double romney;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());

        final MyLocationListener locationListener = new MyLocationListener();

        String[] message = messageEvent.getPath().split(" ");

        if (Objects.equals(message[0], "Rep")) {

            // rep id
            String repId = message[1];

            System.out.println(repId);

            String repURL = "http://congress.api.sunlightfoundation.com/legislators?bioguide_id=" + repId + "&apikey=" + sunlightApiKey;
            new CallAPI().execute(repURL);
            String url = "http://congress.api.sunlightfoundation.com/committees?member_ids=" + repId + "&apikey=" + sunlightApiKey;
            new CallAPI().execute(url);
            String billUrl = "http://congress.api.sunlightfoundation.com/bills?sponsor_id=" + repId + "&apikey=" + sunlightApiKey;
            new CallAPI().execute(billUrl);
        }
        else {

            int index = parseInt(message[1]);

            String votingData = loadJSONFromAsset("election-county-2012.json");

            try {
                JSONArray jsonVotingData = new JSONArray(votingData);
                JSONObject object = jsonVotingData.getJSONObject(index);
                county = object.getString("county-name");
                state = object.getString("state-postal");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            locationListener.getLocationFromAddress(county + ", " + state);
            try {
                locationListener.getLocationFromLongLat();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String dataURL = sunlightURL + "latitude=" + latitude + "&longitude=" + longitude + "&apikey=" + sunlightApiKey;

            String votingDataModified = loadJSONFromAsset("newelectioncounty2012.json");

            JSONObject jsonVotingData = null;
            try {
                jsonVotingData = new JSONObject(votingDataModified);


                JSONObject countyData = jsonVotingData.getJSONObject(county + " County" + ", " + state);

                romney = countyData.getDouble("romney");

                obama = countyData.getDouble("obama");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            new CallAPI().execute(dataURL);
        }

    }

    public String loadJSONFromAsset(String file) {
        String json = null;
        try {

            InputStream is = getAssets().open(file);

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



    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();

        /*------- To get city name from coordinates -------- */
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    String city = addresses.get(0).getLocality();
                    String code = addresses.get(0).getPostalCode();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getLocationFromZip(String zip) {
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocationName(zip, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    // Use the address as needed
                    String message = String.format("Latitude: %f, Longitude: %f",
                            address.getLatitude(), address.getLongitude());
                    return address.getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return zip;
        }

        public void getLocationFromAddress(String strAddress){

            Geocoder coder = new Geocoder(getBaseContext());
            List<Address> address;

            try {
                address = coder.getFromLocationName(strAddress,5);
                if (address==null) {
                    return;
                }
                Address location=address.get(0);
                latitude = "" + location.getLatitude();
                longitude = "" + location.getLongitude();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void getLocationFromLongLat() throws IOException {
            Geocoder coder = new Geocoder(getBaseContext());
            List<Address> addresses = coder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
            zipCode = addresses.get(0).getPostalCode();
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
            try {
                JSONObject testObj = resultArray.getJSONObject(0);

                int count = reader.getInt("count");

                if (count > 1 && testObj.has("nickname")) {
                    System.out.println("wrong");
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
                                } else if (Objects.equals(party, "R")) {
                                    parties[indexInArray] = "Republican";
                                } else {
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
                                } else if (Objects.equals(party, "R")) {
                                    parties[indexInArray] = "Republican";
                                } else {
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
                    intent.putExtra("cityName", county + " County");
                    intent.putExtra("postalCode", zipCode);
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(intent);

                    Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                    sendIntent.putExtra("names", names);
                    sendIntent.putExtra("parties", parties);
                    sendIntent.putExtra("isSenator", isSenator);
                    sendIntent.putExtra("cityName", county + " County");
                    sendIntent.putExtra("state", state);
                    sendIntent.putExtra("ids", ids);
                    sendIntent.putExtra("obama", obama + "");
                    sendIntent.putExtra("romney", romney + "");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startService(sendIntent);
                }

                else {
                    System.out.println("here");

                    if (testObj.has("committee_id")) {
                        System.out.println("adding committee");
                        String[] committeesTemp = new String[resultArray.length()];
                        for (int i = 0; i < resultArray.length(); i++) {
                            try {
                                JSONObject currentObj = resultArray.getJSONObject(i);
                                committeesTemp[i] = currentObj.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        committees = committeesTemp;
                        System.out.println("done");
                    }
                    else if (testObj.has("twitter_id")) {
                        data = new HashMap<String, String>();
                        JSONObject currentObj = resultArray.getJSONObject(0);
                        data.put("picture", "https://twitter.com/" + currentObj.getString("twitter_id") + "/profile_image?size=original");
                        data.put("name", currentObj.getString("first_name") + " " + currentObj.getString("last_name"));
                        if (Objects.equals(currentObj.getString("party"), "D")) {
                            data.put("party", "Democrat");
                        }
                        else {
                            data.put("party", "Republican");
                        }
                        data.put("date", currentObj.getString("term_start") + " to " + currentObj.getString("term_end"));
                        if (Objects.equals(currentObj.getString("chamber"), "senate"))
                            data.put("isSenator", "Senator");
                        else
                            data.put("isSenator", "Representative");

                    }
                    else {
                        String[] billsTemp = new String[4];
                        String[] billDatesTemp = new String[4];
                        int currentIndex = 0;
                        for (int i = 0; i < resultArray.length(); i++) {
                            System.out.println("adding stuff");
                            try {
                                JSONObject currentObj = resultArray.getJSONObject(i);
                                if (Objects.equals(currentObj.getString("short_title"), "null")) {
                                    continue;
                                } else {
                                    if (currentIndex < 4) {
                                        billsTemp[currentIndex] = currentObj.getString("short_title");
                                        billDatesTemp[currentIndex] = currentObj.getString("introduced_on");
                                        currentIndex++;
                                    } else {
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        bills = billsTemp;
                        billDates = billDatesTemp;
                        System.out.println("done");

                        //hashmap for committees and bills
                        lists = new HashMap<String, String[]>();
                        lists.put("committees", committees);
                        lists.put("bills", bills);
                        lists.put("dates", billDates);

                        Intent intent = new Intent(getBaseContext(), detail_view.class);
                        intent.putExtra("information", data);
                        intent.putExtra("lists", lists);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getBaseContext().startActivity(intent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
