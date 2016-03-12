package com.example.simon.represent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class showRepresentatives extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_representatives);
        Intent intent = getIntent();
        TextView topText = (TextView) findViewById(R.id.textView3);
        topText.setText(topText.getText() + " " + intent.getStringExtra("postalCode") + " (" + intent.getStringExtra("cityName") + ", " + intent.getStringExtra("state") +  ")");

        //list View with adaptors

        //final ListView senatorList = (ListView) findViewById(R.id.listView);
        final LinearLayout senatorList = (LinearLayout) findViewById(R.id.listView);
        String[] pictureLinks = intent.getStringArrayExtra("pictureLinks");
        String[] names = intent.getStringArrayExtra("names");
        String[] parties = intent.getStringArrayExtra("parties");
        String[] emails = intent.getStringArrayExtra("emails");
        String[] websites = intent.getStringArrayExtra("websites");
        String[] dates = intent.getStringArrayExtra("dates");
        boolean[] isSenator = intent.getBooleanArrayExtra("isSenator");
        String[] ids = intent.getStringArrayExtra("ids");
        String sunlightApiKey = intent.getStringExtra("sunlightKey");
        String[] twitterHandles = intent.getStringArrayExtra("twitterHandles");


        final RepArrayAdapter adapter = new RepArrayAdapter(this, pictureLinks, names, parties, emails, websites, dates, isSenator, ids, sunlightApiKey, twitterHandles);
        for (int i = 0; i < 2; i++) {
            View view = adapter.getView(i, null, senatorList);
            senatorList.addView(view);
            if (i < 1) {
                View v = new View(this);
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        5
                ));
                v.setBackgroundColor(Color.parseColor("#B3B3B3"));
                senatorList.addView(v);
            }
        }


        //senatorList.setAdapter(adapter);

        //final ListView repList = (ListView) findViewById(R.id.listView2);
        final LinearLayout repList = (LinearLayout) findViewById(R.id.listView2);
//        String[] repPicLink = {"http://pbs.twimg.com/profile_images/430378206353317888/3QKYak-Z_400x400.jpeg"};
//        String[] repNames = {"Barbara Lee"};
//        String[] repParties = {"Democrat"};
//        String[] repEmails = {"Rep.Lee@opencongress.org"};
//        String[] repWebsites = {"http://lee.house.gov"};
//        String[] repTweets = {"Great story by @katiecouric highlighting the history & vibrancy of Oakland! #CA13 #OaklandPride #CitiesRising http://yhoo.it/1O5EGOJ"};
//        String[] repDates = {"January 3, 2013 - January 3, 2019"};

        for (int i = 2; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, repList);
            repList.addView(view);
            if (i < adapter.getCount() - 1) {
                View v = new View(this);
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        5
                ));
                v.setBackgroundColor(Color.parseColor("#B3B3B3"));
                repList.addView(v);
            }
        }

        //repList.setAdapter(repAdapter);

    }

}
