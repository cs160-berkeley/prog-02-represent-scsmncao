package com.example.simon.represent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by Simon on 3/1/16.
 */
public class RepArrayAdapter extends BaseAdapter {

    private final Context context;
    private final String[] pictureLinks;
    private final String[] names;
    private final String[] parties;
    private final String[] emails;
    private final String[] websites;
    private final String[] dates;
    private final boolean[] senator;
    private final String[] ids;
    private final String sunlightKey;
    private HashMap<String, String[]> lists;
    private HashMap<String, String> data;


    public static TextView[] showMore;

    private String[] committees;
    private String[] bills;
    private String[] billDates;
    private String[] twitterHandles;



    public RepArrayAdapter(Context context, String[] pictureLinks, String[] names, String[] parties, String[] emails, String[] websites, String[] dates, boolean[] senator, String[] ids, String sunlightKey, String[] twitterHandles) {
        this.context = context;
        this.pictureLinks = pictureLinks;
        this.names = names;
        this.parties = parties;
        this.emails = emails;
        this.websites = websites;
        this.dates = dates;
        this.senator = senator;
        this.showMore = new TextView[4];
        this.ids = ids;
        this.sunlightKey = sunlightKey;
        this.twitterHandles = twitterHandles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View repRow = inflater.inflate(R.layout.representative_layout, parent, false);

        final int currentPosition = position;

        ImageView repImage = (ImageView) repRow.findViewById(R.id.imageView);
        TextView name = (TextView) repRow.findViewById(R.id.textView5);
        TextView party = (TextView) repRow.findViewById(R.id.textView6);
        TextView email = (TextView) repRow.findViewById(R.id.textView7);

        email.setPaintFlags(email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent it = new Intent(android.content.Intent.ACTION_SEND);
                it.putExtra(Intent.EXTRA_EMAIL, new String[] {emails[currentPosition]});
                it.setType("message/rfc822");
                context.startActivity(Intent.createChooser(it, "Send mail..."));
            }
        });

        TextView website = (TextView) repRow.findViewById(R.id.textView8);
        website.setPaintFlags(website.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = websites[currentPosition];
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            }
        });
        final TextView twitter = (TextView) repRow.findViewById(R.id.textView10);

        if (!Objects.equals(twitterHandles[currentPosition], "null")) {
            new DownloadImageTask(repImage).execute(pictureLinks[position]);
        }
        else {
            repImage.setImageResource(R.drawable.flag);
        }
        name.setText(names[position]);
        party.setText(parties[position]);
        if (Objects.equals(parties[position], "Democrat")) {
            party.setTextColor(Color.parseColor("#311ECC"));
        }
        else if (Objects.equals(parties[position], "Independent")) {
            party.setTextColor(Color.parseColor("#000000"));
        }
        else {
            party.setTextColor(Color.parseColor("#cc0000"));
        }
        email.setText(emails[position]);
        website.setText(websites[position]);

        if (!Objects.equals(twitterHandles[currentPosition], "null")) {
            TwitterCore.getInstance().getApiClient(Twitter.getSessionManager().getActiveSession()).getStatusesService()
                    .userTimeline(null,
                            twitterHandles[currentPosition],
                            1, //the number of tweets we want to fetch,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            new Callback<List<Tweet>>() {
                                @Override
                                public void success(Result<List<Tweet>> result) {
                                    for (Tweet t : result.data) {
                                        twitter.setText(t.text);
                                    }
                                }

                                @Override
                                public void failure(TwitterException exception) {
                                    android.util.Log.d("twittercommunity", "exception " + exception);
                                }
                            });
        }
        else {
            twitter.setText("This representative doesn't have a Twitter.");
        }

        TextView more = (TextView) repRow.findViewById(R.id.textView11);
        showMore[position] = more;
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data = new HashMap<String, String>();
                data.put("picture", pictureLinks[currentPosition]);
                data.put("name", names[currentPosition]);
                data.put("party", parties[currentPosition]);
                data.put("date", dates[currentPosition]);


                if (senator[currentPosition]) {
                    data.put("isSenator", "Senator");
                }
                else {
                    data.put("isSenator", "Representative");
                }
                String url = "http://congress.api.sunlightfoundation.com/committees?member_ids=" + ids[currentPosition] + "&apikey=" + sunlightKey;
                new CallAPI().execute(url);
                String billUrl = "http://congress.api.sunlightfoundation.com/bills?sponsor_id=" + ids[currentPosition] + "&apikey=" + sunlightKey;
                new CallAPI().execute(billUrl);
            }
        });

        return repRow;
    }

    @Override
    public long getItemId(int position) {
        long replace = 0;
        return replace;
    }

    public TextView getTextView(int position) {
        return showMore[position];
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    public int getCount() {
        return names.length;
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
                            }
                            else {
                                if (currentIndex < 4) {
                                    billsTemp[currentIndex] = currentObj.getString("short_title");
                                    billDatesTemp[currentIndex] = currentObj.getString("introduced_on");
                                    currentIndex++;
                                }
                                else {
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

                    Intent intent = new Intent(context, detail_view.class);
                    intent.putExtra("information", data);
                    intent.putExtra("lists", lists);
                    context.startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
