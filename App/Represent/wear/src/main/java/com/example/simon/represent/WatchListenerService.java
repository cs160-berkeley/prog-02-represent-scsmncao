package com.example.simon.represent;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchListenerService extends WearableListenerService {
    // In PhoneToWatchService, we passed in a path, either "/FRED" or "/LEXY"
    // These paths serve to differentiate different phone-to-watch messages
    private static final String FRED_FEED = "/Fred";
    private static final String LEXY_FEED = "/Lexy";


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());

        Intent intent = new Intent(this, MainActivity.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //you need to add this flag since you're starting a new activity from a service
        intent.putExtra("information", messageEvent.getPath());
        Log.d("T", "about to start watch MainActivity");
        startActivity(intent);

    }
}