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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String cityName;
    private String postalCode;

    String[] pictureLinks = {"http://d229l5sflpl9cp.cloudfront.net/canphoto/53273_lg.jpg", "http://d229l5sflpl9cp.cloudfront.net/canphoto/53274_lg.jpg", "http://pbs.twimg.com/profile_images/430378206353317888/3QKYak-Z_400x400.jpeg"};
    String[] names = {"Dianne Feinstein", "Barbara Boxer", "Barbara Lee"};
    String[] parties = {"Democrat", "Democrat", "Democrat"};
    String[] emails = {"senator@feinstein.senate.gov", "Sen.Boxer@opencongress.org", "Rep.Lee@opencongress.org"};
    String[] websites = {"http://feinstein.senate.gov/", "http://www.boxer.senate.gov", "http://lee.house.gov"};
    String[] tweets = {"Whether it’s the wildflowers or tortoises, the desert has so much unique wildlife and vegetation. #ProtectCADesert", "News of Powell & Rice emails exposes the phony, fraudulent, taxpayer-funded #GOP attacks on Secretary Clinton. http://cnn.it/1T0YMBy", "Great story by @katiecouric highlighting the history & vibrancy of Oakland! #CA13 #OaklandPride #CitiesRising http://yhoo.it/1O5EGOJ"};
    String[] dates = {"January 3, 2013 - January 3, 2019", "January 3, 2013 - January 3, 2019", "January 3, 2013 - January 3, 2019"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        final MyLocationListener locationListener = new MyLocationListener();
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

        final EditText edittext = (EditText) findViewById(R.id.editText);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String city = locationListener.getLocationFromZip(edittext.getText().toString());
                    Intent intent = new Intent(v.getContext(), showRepresentatives.class);
                    intent.putExtra("cityName", city);
                    intent.putExtra("postalCode", edittext.getText().toString());
                    intent.putExtra("pictureLinks", pictureLinks);
                    intent.putExtra("names", names);
                    intent.putExtra("parties", parties);
                    intent.putExtra("emails", emails);
                    intent.putExtra("websites", websites);
                    intent.putExtra("tweets", tweets);
                    intent.putExtra("dates", dates);
                    startActivity(intent);

                    Intent sendIntent = new Intent(v.getContext(), PhoneToWatchService.class);
                    sendIntent.putExtra("names", names);
                    sendIntent.putExtra("parties", parties);
                    String[] isSenator = {"Senator", "Senator", "Rep"};
                    sendIntent.putExtra("isSenator", isSenator);
                    sendIntent.putExtra("cityName", edittext.getText().toString());
                    startService(sendIntent);
                    return true;
                }
                return false;
            }
        });
    }

    public void useCurrentLocation(View view) {
        final MyLocationListener locationListener = new MyLocationListener();
        Intent intent = new Intent(this, showRepresentatives.class);
        intent.putExtra("cityName", cityName);
        intent.putExtra("postalCode", postalCode);
        intent.putExtra("pictureLinks", pictureLinks);
        intent.putExtra("names", names);
        intent.putExtra("parties", parties);
        intent.putExtra("emails", emails);
        intent.putExtra("websites", websites);
        intent.putExtra("tweets", tweets);
        intent.putExtra("dates", dates);
        startActivity(intent);

        Intent sendIntent = new Intent(this, PhoneToWatchService.class);
        sendIntent.putExtra("names", names);
        sendIntent.putExtra("parties", parties);
        String[] isSenator = {"Senator", "Senator", "Rep"};
        sendIntent.putExtra("isSenator", isSenator);
        sendIntent.putExtra("cityName", postalCode);
        startService(sendIntent);
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
                    cityName = addresses.get(0).getLocality();
                    postalCode = addresses.get(0).getPostalCode();
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
}
