package com.example.simon.represent;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.util.Log;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Created by Simon on 3/3/16.
 */
public class RepPagerAdapter extends FragmentGridPagerAdapter{

    private final Context mContext;
    private List mRows;
    private String information;
    private Page[][] PAGES;
    private String cityName;

    public RepPagerAdapter(Context ctx, FragmentManager fm, String path) {
        super(fm);
        mContext = ctx;
        information = path;
        createPages();
    }

    static final int[] BG_IMAGES = new int[] {
        R.drawable.dianne, R.drawable.barbara, R.drawable.lee
    };


    // A simple container for static data in each page
    private static class Page {
        // static resources
        String titleRes;
        String textRes;
        int iconRes;

        public Page(String title, String text, int icon) {
            titleRes = title;
            textRes = text;
            iconRes = icon;
        }
    }

    //creating new pages
    private void createPages() {
        if (information != null) {
            String[] allReps = information.split("/");
            PAGES = new Page[allReps.length - 1][2];
            for (int i = 0; i < allReps.length; i++) {
                String[] currentRep = allReps[i].split(" ");

                //handle cityname
                if (currentRep.length == 1) {
                    cityName = currentRep[0];
                    break;
                }
                String name = currentRep[0] + " " + currentRep[1];
                String isSenator = currentRep[3];
                String party = currentRep[2];
                PAGES[i][0] = new Page(name, isSenator + " | " + party, 0);
                PAGES[i][1] = new Page("", "", 0);
            }
        }
        else {
            PAGES = new Page[0][0];
        }
    }

//    // Create a static set of pages in a 2D array
//    private final Page[][] PAGES = {{new Page("Dianne Fienstein", "Senator | Democrat",0)}, {new Page("Barbara Boxer", "Senator | Democrat",0)}, {new Page("Barbara Lee", "Rep | Democrat",0)}};

    // Obtain the UI fragment at the specified position
    @Override
    public Fragment getFragment(int row, int col) {
        Page page = PAGES[row][col];
        String title =
                page.titleRes;
        String text =
                page.textRes;
        Fragment fragment;

        if (col == 0) {
            fragment = RepFragment.create(title, text);
        }
        else {
            fragment = VoteFragment.create(cityName);
        }

        // Advanced settings (card gravity, card expansion/scrolling)
//        fragment.setCardGravity(page.cardGravity);
//        fragment.setExpansionEnabled(page.expansionEnabled);
//        fragment.setExpansionDirection(page.expansionDirection);
//        fragment.setExpansionFactor(page.expansionFactor);
        return fragment;
    }

    // Obtain the background image for the row
    @Override
    public Drawable getBackgroundForRow(int row) {
        return mContext.getResources().getDrawable(
                (BG_IMAGES[row % BG_IMAGES.length]), null);
    }


    // Obtain the background image for the specific page
    @Override
    public Drawable getBackgroundForPage(int row, int column) {
        // Default to background image for row
        return GridPagerAdapter.BACKGROUND_NONE;
    }

    // Obtain the number of pages (vertical)
    @Override
    public int getRowCount() {
        return PAGES.length;
    }

    // Obtain the number of pages (horizontal)
    @Override
    public int getColumnCount(int rowNum) {
        return PAGES[rowNum].length;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
