package com.example.simon.represent;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Simon on 3/3/16.
 */
public class PhoneListenerService extends WearableListenerService {


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());

        final MyLocationListener locationListener = new MyLocationListener();

        int index = 0;

        String[] pictureLinks = {"http://d229l5sflpl9cp.cloudfront.net/canphoto/53273_lg.jpg", "http://d229l5sflpl9cp.cloudfront.net/canphoto/53274_lg.jpg", "http://pbs.twimg.com/profile_images/430378206353317888/3QKYak-Z_400x400.jpeg"};
        String[] names = {"Dianne Feinstein", "Barbara Boxer", "Barbara Lee"};
        String[] parties = {"Democrat", "Democrat", "Democrat"};
        String[] emails = {"senator@feinstein.senate.gov", "Sen.Boxer@opencongress.org", "Rep.Lee@opencongress.org"};
        String[] websites = {"http://feinstein.senate.gov/", "http://www.boxer.senate.gov", "http://lee.house.gov"};
        String[] tweets = {"Whether it’s the wildflowers or tortoises, the desert has so much unique wildlife and vegetation. #ProtectCADesert", "News of Powell & Rice emails exposes the phony, fraudulent, taxpayer-funded #GOP attacks on Secretary Clinton. http://cnn.it/1T0YMBy", "Great story by @katiecouric highlighting the history & vibrancy of Oakland! #CA13 #OaklandPride #CitiesRising http://yhoo.it/1O5EGOJ"};
        String[] dates = {"January 3, 2013 - January 3, 2019", "January 3, 2013 - January 3, 2019", "January 3, 2013 - January 3, 2019"};
        String[] isSenator = {"Senator", "Senator", "Representative"};

        if (messageEvent.getPath().length() <= 2) {
            index = Integer.parseInt(messageEvent.getPath());

            System.out.println(index);

            if (index > 2 && index % 2 == 1) {
                index = 0;
            }
            if (index > 2 && index % 2 == 0) {
                index = 2;
            }


            HashMap<String, String> data = new HashMap<String, String>();
            data.put("picture", pictureLinks[index]);
            data.put("name", names[index]);
            data.put("party", parties[index]);
            data.put("date", dates[index]);
            data.put("isSenator", isSenator[index]);

            //hashmap for committees and bills
            HashMap<String, String[]> lists = new HashMap<String, String[]>();
            lists.put("committees", new String[]{"Committee on Appropriations", "Committee on the Judiciary", "Committee on Rules and Administration", "Select Committee on Intelligence"});
            lists.put("bills", new String[]{"Medicare Advantage Act", "State Secrets Protection Act", "Northern Cheyenne Lands Act"});
            lists.put("dates", new String[]{"Feb. 11, 2016", "Feb. 10, 2016", "Jan. 12, 2016"});

            Intent intent = new Intent(this, detail_view.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("information", data);
            intent.putExtra("lists", lists);

            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, showRepresentatives.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("cityName", locationListener.getLocationFromZip(messageEvent.getPath()));
            intent.putExtra("postalCode", messageEvent.getPath());
            intent.putExtra("pictureLinks", pictureLinks);
            intent.putExtra("names", names);
            intent.putExtra("parties", parties);
            intent.putExtra("emails", emails);
            intent.putExtra("websites", websites);
            intent.putExtra("tweets", tweets);
            intent.putExtra("dates", dates);

            startActivity(intent);
        }

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

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
