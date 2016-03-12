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
import java.util.Objects;

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
        if (Objects.equals(data.get("party"), "Democrat")) {
            party.setTextColor(Color.parseColor("#311ECC"));
        }
        else if (Objects.equals(data.get("party"), "Independent")) {
            party.setTextColor(Color.parseColor("#000000"));
        }
        else {
            party.setTextColor(Color.parseColor("#cc0000"));
        }
        name.setText(data.get("isSenator") + " " + data.get("name"));
        String[] startEndDates = data.get("date").split("to");
        String startDate = "";
        String endDate = "";

        HashMap<String, String> dateHash = new HashMap<String, String>();
        dateHash.put("01", "Jan");
        dateHash.put("02", "Feb");
        dateHash.put("03", "Mar");
        dateHash.put("04", "Apr");
        dateHash.put("05", "May");
        dateHash.put("06", "June");
        dateHash.put("07", "July");
        dateHash.put("08", "Aug");
        dateHash.put("09", "Sept");
        dateHash.put("10", "Oct");
        dateHash.put("11", "Nov");
        dateHash.put("12", "Dec");

        String[] startDateArray = startEndDates[0].split("-");
        startDate = dateHash.get(startDateArray[1].trim()) + " " + startDateArray[2].trim() + ", " + startDateArray[0].trim();
        String[] endDateArray = startEndDates[1].split("-");
        endDate = dateHash.get(endDateArray[1].trim()) + " " + endDateArray[2].trim() + ", " + endDateArray[0].trim();

        dates.setText(startDate + " - " + endDate);

        for (int i = 0; i < lists.get("committees").length; i++) {
            TextView currentName = new TextView(this);
            currentName.setText(lists.get("committees")[i]);
            currentName.setGravity(Gravity.CENTER);
            currentName.setTextColor(Color.parseColor("#000000"));
            currentName.setPadding(0,0,0,10);
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
            currentDate.setPadding(0,0,0,10);
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
