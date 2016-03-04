package com.example.simon.represent;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Simon on 3/3/16.
 */
public class VoteFragment extends Fragment {


    public static VoteFragment create(String name) {
        VoteFragment fragment = new VoteFragment();
        Bundle args = new Bundle();
        args.putString("city", name);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(
                R.layout.vote_view, container, false);
        TextView city = (TextView) view.findViewById(R.id.textView4);
        TextView obamaVote = (TextView) view.findViewById(R.id.textView2);
        TextView romneyVote = (TextView) view.findViewById(R.id.textView3);
        Random rand = new Random();
        int percentage = rand.nextInt(101) + 0;
        obamaVote.setText(percentage + "%");
        romneyVote.setText((100 - percentage) + "%");
        city.setText(city.getText() + " " + getArguments().getString("city"));
        return view;
    }

}
