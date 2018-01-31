package com.example.jonathan.myapplication;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by jonathan on 11/29/17.
 */

public class Location_Activity extends DialogFragment {
    public Location_Activity() {
    }

    public static Location_Activity newInstance(String title) {
        Location_Activity pView = new Location_Activity();
        Bundle args = new Bundle();
        args.putString("title", title);
        pView.setArguments(args);
        return pView;
    }

    @Override
    public View onCreateView(LayoutInflater helpInflator, ViewGroup container,
                             Bundle savedInstanceState) {
        return helpInflator.inflate(R.layout.location_layout, container);
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

    }
}

