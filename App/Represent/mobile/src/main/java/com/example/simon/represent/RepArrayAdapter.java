package com.example.simon.represent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.HashMap;
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
    private final String[] tweets;
    private final String[] dates;
    private final boolean senator;

    public static TextView[] showMore;

    public RepArrayAdapter(Context context, String[] pictureLinks, String[] names, String[] parties, String[] emails, String[] websites, String[] tweets, String[] dates, boolean senator) {
        this.context = context;
        this.pictureLinks = pictureLinks;
        this.names = names;
        this.parties = parties;
        this.emails = emails;
        this.websites = websites;
        this.tweets = tweets;
        this.dates = dates;
        this.senator = senator;
        this.showMore = new TextView[4];
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
        TextView website = (TextView) repRow.findViewById(R.id.textView8);
        TextView twitter = (TextView) repRow.findViewById(R.id.textView10);

        new DownloadImageTask(repImage).execute(pictureLinks[position]);
        name.setText(names[position]);
        party.setText(parties[position]);
        if (Objects.equals(parties[position], "Democrat")) {
            party.setTextColor(Color.parseColor("#311ECC"));
        }
        email.setText(emails[position]);
        website.setText(websites[position]);
        twitter.setText(tweets[position]);

        TextView more = (TextView) repRow.findViewById(R.id.textView11);
        showMore[position] = more;
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put("picture", pictureLinks[currentPosition]);
                data.put("name", names[currentPosition]);
                data.put("party", parties[currentPosition]);
                data.put("date", dates[currentPosition]);

                //hashmap for committees and bills
                HashMap<String, String[]> lists = new HashMap<String, String[]>();
                lists.put("committees", new String[] {"Committee on Appropriations", "Committee on the Judiciary", "Committee on Rules and Administration", "Select Committee on Intelligence"});
                lists.put("bills", new String[] {"Medicare Advantage Act", "State Secrets Protection Act", "Northern Cheyenne Lands Act"});
                lists.put("dates", new String[] {"Feb. 11, 2016", "Feb. 10, 2016", "Jan. 12, 2016"});

                if (senator) {
                    data.put("isSenator", "Senator");
                }
                else {
                    data.put("isSenator", "Representative");
                }
                Intent intent = new Intent(context, detail_view.class);
                intent.putExtra("information", data);
                intent.putExtra("lists", lists);
                context.startActivity(intent);
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
        return pictureLinks.length;
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
