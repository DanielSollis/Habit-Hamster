package com.example.jonathan.myapplication;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.Nullable;

import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.example.jonathan.myapplication.db.HabitContract;
import com.example.jonathan.myapplication.db.HabitHelper;


/**
 * Created by Daniel on 10/29/2017.
 */

public class ProgressActivity extends DialogFragment {
    public ProgressActivity() {
    }

    public static ProgressActivity newInstance(String title) {
        ProgressActivity pView = new ProgressActivity();
        Bundle args = new Bundle();
        args.putString("title", title);
        pView.setArguments(args);
        return pView;
    }

    @Override
    public View onCreateView(LayoutInflater helpInflator, ViewGroup container,
                             Bundle savedInstanceState) {
        return helpInflator.inflate(R.layout.progress_layout, container);
    }

    @Override
    //setup dialog
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        int count = 0;
        try {
            HabitHelper mHelper = new HabitHelper(getContext());
            SQLiteDatabase db = mHelper.getReadableDatabase();
            Cursor cursor = db.query(HabitContract.HabitCount.TABLE, new String[]{HabitContract.HabitCount._ID, HabitContract.HabitCount.COLUMN_NAME_SUBTITLE}, null, null, null, null, null);
            cursor.moveToFirst();
            count = cursor.getInt(1);
        }catch(CursorIndexOutOfBoundsException E){}

        final ImageView achievment_1 = v.findViewById(R.id.achievment_1);
        final ImageView achievment_2 = v.findViewById(R.id.achievment_2);
        final ImageView achievment_3 = v.findViewById(R.id.achievment_3);
        final ImageView achievment_4 = v.findViewById(R.id.achievment_4);
        final ImageView achievment_5 = v.findViewById(R.id.achievment_5);
        final ImageView achievment_6 = v.findViewById(R.id.achievment_6);
        final ImageView achievment_7 = v.findViewById(R.id.achievment_7);
        final ImageView achievment_8 = v.findViewById(R.id.achievment_8);
        final ImageView achievment_9 = v.findViewById(R.id.achievment_9);

        if (count < 3) {
            achievment_1.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 6) {
            achievment_2.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 9) {
            achievment_3.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 12) {
            achievment_4.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 15) {
            achievment_5.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 18) {
            achievment_6.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 21) {
            achievment_7.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 24) {
            achievment_8.setColorFilter(getResources().getColor(R.color.Gray));
        }
        if(count < 30) {
            achievment_9.setColorFilter(getResources().getColor(R.color.Gray));
        }

        if (count % 3 == 0 && count < 31) {
            MediaPlayer cheerSound = MediaPlayer.create(getContext(), R.raw.cheer);
            cheerSound.start();
        }

        final TextView level = v.findViewById(R.id.progress_level);
        if (count < 30) {
            level.setText("LEVEL: " + count);
        }
        else {
            level.setText("LEVEL: MAX");
        }
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
    }
}
