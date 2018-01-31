package com.example.jonathan.myapplication;

/**
 * Created by Daniel on 10/28/2017.
 */

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View;

public class HelpActivity extends DialogFragment {

    private int imageIndex = 0;

    public HelpActivity() {
    }

    public static HelpActivity newInstance(String title){
        //create new dialog
        HelpActivity hView = new HelpActivity();
        Bundle args = new Bundle();
        args.putString("title", title);
        hView.setArguments(args);
        return hView;
    }

    @Override
    //inflate view into dialog
    public View onCreateView(LayoutInflater helpInflator, ViewGroup container,
                             Bundle savedInstanceState) {
        return helpInflator.inflate(R.layout.help_layout, container);
    }

    @Override
    //setup dialog
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        final ImageView helpImage = v.findViewById(R.id.help_view);
        final int[] imagesArray = new int[]{R.drawable.help1, R.drawable.help2, R.drawable.help3,
                R.drawable.help4, R.drawable.help5, R.drawable.help6, R.drawable.help7, };

        helpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (imageIndex < imagesArray.length - 1) {
                    helpImage.setImageResource(imagesArray[imageIndex + 1]);
                    imageIndex++;
                }
                else {
                    dismiss();
                }
            }
        });

        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
    }
}
