package com.example.jonathan.myapplication;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jonathan.myapplication.db.HabitContract;
import com.example.jonathan.myapplication.db.HabitHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnClickListener{

    private static final String TAG = "MainActivity";
    private HabitHelper mHelper;
    private ListView mHabitView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new HabitHelper(this);
        mHabitView = (ListView) findViewById(R.id.habitList);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.addHabit);
        //CheckBox chkBox = (CheckBox) findViewById(R.id.habitItem);

        myFab.setOnClickListener(this);
       // chkBox.setOnClickListener(this);


        updateUI();

    }

//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    public void onClick (View v){

        final EditText habitText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add a new habit")
                .setMessage("What habit would you like to start working on?")
                .setView(habitText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
            @Override
                    public void onClick(DialogInterface dialog, int which) {
                String habit = String.valueOf(habitText.getText());
                SQLiteDatabase db = mHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(HabitContract.HabitEntry.COLUMN_NAME_TITLE, habit);
                db.insertWithOnConflict(HabitContract.HabitEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                db.close();
                updateUI();

            }
        })
                .setNegativeButton("Cancel", null)
                .create();
                dialog.show();
    }

    public void checkBoxClick(View v){
        ((CheckedTextView)v).toggle();
    }

//    public class CheckBoxClick implements AdapterView.OnItemClickListener{
//
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//            CheckedTextView ctv = (CheckedTextView)arg1.findViewById(R.id.habitItem);
//            if (ctv.isChecked()){
//                Toast.makeText(MainActivity.this, "You have completed your habit for the day", Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(MainActivity.this, "Make sure you complete your task before checking me!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void updateUI(){
        ArrayList<String> habitList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(HabitContract.HabitEntry.TABLE, new String[]{HabitContract.HabitEntry._ID, HabitContract.HabitEntry.COLUMN_NAME_TITLE}, null, null, null, null, null);
        while (cursor.moveToNext()){
            int idx = cursor.getColumnIndex(HabitContract.HabitEntry.COLUMN_NAME_TITLE);
            habitList.add(cursor.getString(idx));
        }
        if (mAdapter == null){
            mAdapter = new ArrayAdapter<>(this, R.layout.item_habit, R.id.habitItem, habitList);
            mHabitView.setAdapter(mAdapter);
        }
        else {
            mAdapter.clear();
            mAdapter.addAll(habitList);
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }

    private void deleteHabit(View view){
        View parent = (View) view.getParent();
        TextView habitTextView = (TextView) parent.findViewById(R.id.habitItem);
        String habit = String.valueOf(habitTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(HabitContract.HabitEntry.TABLE, HabitContract.HabitEntry.COLUMN_NAME_TITLE + " = ?", new String[] {habit});
        db.close();
        updateUI();
    }

}
