package com.example.simon.represent;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.util.HashMap;

public class detail_view extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        ImageView portrait = (ImageView) findViewById(R.id.imageView3);
        TextView party = (TextView) findViewById(R.id.textView13);
        TextView name = (TextView) findViewById(R.id.textView12);
        TextView dates = (TextView) findViewById(R.id.textView14);
        LinearLayout committees = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout bills = (LinearLayout) findViewById(R.id.linearLayout2);

        Intent intent = getIntent();
        HashMap<String, String> data = (HashMap<String, String>)intent.getSerializableExtra("information");
        HashMap<String, String[]> lists = (HashMap<String, String[]>) intent.getSerializableExtra("lists");

        new DownloadImageTask(portrait).execute(data.get("picture"));
        party.setText(data.get("party"));
        name.setText(data.get("isSenator") + " " + data.get("name"));
        dates.setText(data.get("date"));

        for (int i = 0; i < lists.get("committees").length; i++) {
            TextView currentName = new TextView(this);
            currentName.setText(lists.get("committees")[i]);
            currentName.setGravity(Gravity.CENTER);
            currentName.setTextColor(Color.parseColor("#000000"));
            committees.addView(currentName);
        }

        for (int i = 0; i < lists.get("bills").length; i++) {
            TextView currentBill = new TextView(this);
            currentBill.setText(lists.get("bills")[i]);
            currentBill.setGravity(Gravity.CENTER);
            currentBill.setTextColor(Color.parseColor("#000000"));
            bills.addView(currentBill);

            TextView currentDate = new TextView(this);
            currentDate.setText(lists.get("dates")[i]);
            currentDate.setGravity(Gravity.CENTER);
            currentDate.setTextColor(Color.parseColor("#595959"));
            currentDate.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
            bills.addView(currentDate);
        }

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
