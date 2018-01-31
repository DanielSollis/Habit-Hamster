package com.example.jonathan.myapplication;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.content.ContextCompat;

import com.example.jonathan.myapplication.db.HabitContract;
import com.example.jonathan.myapplication.db.HabitHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private FusedLocationProviderClient mFusedLocationClient;
    private HabitHelper mHelper;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ListItem> listItems;
    private Paint p = new Paint();
    private ArrayAdapter<String> mAdapter;
    private GeofencingClient mGeofencingClient;
    private List<Geofence> mGeofenceList;
    private static final String TAG = MainActivity.class.getSimpleName();
    HabitHelper myDb;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    private static final int PERMISSION_GEOFENCE_REQUEST = 0;

    static Intent makeNotificationIntent(Context geofenceService, String msg)
    {
        Log.d(TAG,msg);
        return new Intent(geofenceService,MainActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onCreate(Bundle savedInstanceState) {
        showApp();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDb = new HabitHelper(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
            }
        };

        mHelper = new HabitHelper(this);
        recyclerView = (RecyclerView) findViewById(R.id.habitView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.addButton);
        myFab.setImageResource(R.drawable.addbutton);
        float val = 1.3f;
        myFab.setScaleX(val);
        myFab.setScaleY(val);
        myFab.setOnClickListener(this);

        initSwipe();

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        updateUI();
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    deleteHabit(viewHolder.itemView);
                    MediaPlayer trashSound = MediaPlayer.create(MainActivity.this, R.raw.trashsound);
                    trashSound.start();

                } else {
                    MediaPlayer cheerSound = MediaPlayer.create(MainActivity.this, R.raw.swoosh);
                    cheerSound.start();
                    countHabit();
                    updateUI();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recylerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF backround = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(backround, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_check_white_24dp);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white_24dp);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                super.onChildDraw(c, recylerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //action for when floating button is pressed
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.addButton:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_box, null);
                mBuilder.setTitle("Insert Habit");

                final EditText habitText = (EditText) view.findViewById(R.id.habitText);
                final TimePicker habitPicker = (TimePicker) view.findViewById(R.id.habitTime);

                mBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!(Objects.equals(habitText.getText().toString(), ""))) {
                            String habit = String.valueOf(habitText.getText());
                            //String time = hourSpinner.getSelectedItem().toString() + ":" + minuteSpinner.getSelectedItem().toString() + " " + ampmSpinner.getSelectedItem().toString();
                            SQLiteDatabase db = mHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            int hourTime = habitPicker.getHour();
                            int minuteTime = habitPicker.getMinute();
                            String format = "";

                            if (hourTime == 0) {
                                hourTime += 12;
                                format = "AM";
                            } else if (hourTime == 12) {
                                format = "PM";
                            } else if (hourTime > 12) {
                                hourTime -= 12;
                                format = "PM";
                            } else {
                                format = "AM";
                            }
                            String time = String.valueOf(hourTime) + ":" + String.valueOf(minuteTime) + " " + format;

                            //insert into database
                            values.put(HabitContract.HabitEntry.COLUMN_NAME_TITLE, habit);
                            values.put(HabitContract.HabitEntry.COLUMN_NAME_SUBTITLE, time);
                            values.put(HabitContract.HabitEntry.COLUMN_NAME_CHECKED, "false");
                            db.insertWithOnConflict(HabitContract.HabitEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                            //place item into the recyclerview
                            listItems = new ArrayList<>();
                            ListItem listItem = new ListItem(habit, time);
                            listItems.add(listItem);
                            adapter = new MyAdapter(listItems, MainActivity.this);
                            recyclerView.setAdapter(adapter);

                            //  set notifications

                            scheduleNotification(MainActivity.this, hourTime, minuteTime, 100);

                            db.close();
                            updateUI();
                            Toast.makeText(MainActivity.this, habit + " set to " + time, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setView(view);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
                break;
        }
    }

    //update the UI everytime a change is made to the SQL server
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateUI() {

        listItems = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(HabitContract.HabitEntry.TABLE, new String[]{HabitContract.HabitEntry.COLUMN_NAME_TITLE}, null, null, null, null, null);
        int count = cursor.getCount();
        if (count > 0) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String head = cursor.getString(0);
//                String desc = cursor.getString(1);
                int idx = cursor.getColumnIndex(HabitContract.HabitEntry.COLUMN_NAME_TITLE);
                int ddx = cursor.getColumnIndex(HabitContract.HabitEntry.COLUMN_NAME_SUBTITLE);
                ListItem listItem = new ListItem(head, "hi");
                listItems.add(listItem);
            }
        }
        adapter = new MyAdapter(listItems, MainActivity.this);
        recyclerView.setAdapter(adapter);

        cursor.close();
        db.close();

    }

    //delete the habit when completed
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void deleteHabit(View view) {
        View parent = (View) view.getParent();
        TextView habitTextView = (TextView) view.findViewById(R.id.textViewHead);
        String habit = String.valueOf(habitTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(HabitContract.HabitEntry.TABLE, HabitContract.HabitEntry.COLUMN_NAME_TITLE + " = ?", new String[]{habit});

        db.close();
        updateUI();
    }

    //counts the habit for the achievemnts
    private void countHabit() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.query(HabitContract.HabitCount.TABLE, new String[]{HabitContract.HabitCount._ID, HabitContract.HabitCount.COLUMN_NAME_SUBTITLE}, null, null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            ContentValues createCount = new ContentValues();
            createCount.put(HabitContract.HabitCount.COLUMN_NAME_TITLE, "count");
            createCount.put(HabitContract.HabitCount.COLUMN_NAME_SUBTITLE, 1);
            db.insert(HabitContract.HabitCount.TABLE, null, createCount);
        } else if (cursor.getCount() != 0) {
            int cursorIndex = cursor.getColumnIndex(HabitContract.HabitCount.COLUMN_NAME_SUBTITLE);
            int currentCount = cursor.getInt(cursorIndex) + 1;
            ContentValues updateCount = new ContentValues();
            updateCount.put(HabitContract.HabitCount.COLUMN_NAME_TITLE, "count");
            updateCount.put(HabitContract.HabitCount.COLUMN_NAME_SUBTITLE, currentCount);
            db.update(HabitContract.HabitCount.TABLE, updateCount, "_id=" + 1, null);

            db.close();
        }
    }

    //selector for options menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Feedback:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject of email");
                intent.putExtra(Intent.EXTRA_TEXT, "Body of email");
                intent.setData(Uri.parse("mailto:habithamster@gmail.com"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.Documentation:
                FragmentManager helpFrag = getSupportFragmentManager();
                HelpActivity hv = HelpActivity.newInstance("Help Screen");
                hv.show(helpFrag, "HelpActivity");
                return true;
            case R.id.LogOut:
                mAuth.signOut();
                return true;
            case R.id.Progress:
                FragmentManager progFrag = getSupportFragmentManager();
                ProgressActivity pv = ProgressActivity.newInstance("Progress");
                pv.show(progFrag, "ProgressActivity");
                return true;
            case R.id.Map:
                setContentView(R.layout.map_activity);
               return true;
        }
        return true;
    }

    //schedueles the notification for each habit
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void scheduleNotification(Context context, int hour, int minute, int notificationID)
    {
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.habit)
                .setContentTitle("Habit Hamster")
                .setContentText("Don't forget to do your habit")
                .setAutoCancel(true);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);
        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, Notification_reciever.class);
        notificationIntent.putExtra(Notification_reciever.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtra(Notification_reciever.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 00);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        recyclerView = (RecyclerView) findViewById(R.id.habitView);
        if (requestCode == PERMISSION_GEOFENCE_REQUEST) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(recyclerView, "Map permission was granted.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                // Permission request was denied.
                Snackbar.make(recyclerView, "Map permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void showApp(){
        recyclerView = (RecyclerView) findViewById(R.id.habitView);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
        } else {
            // Permission is missing and must be requested.
            requestMapPermission();
        }
    }

    private void requestMapPermission() {
        // Permission has not been granted and must be requested.
        recyclerView = (RecyclerView) findViewById(R.id.habitView);
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(recyclerView, "Location access is required to enable Geofences.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GEOFENCE_REQUEST);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_GEOFENCE_REQUEST);
                }
            }).show();

        } else {
            Toast.makeText(this,
                    "Permission is not available. Requesting location permission.",
                    Toast.LENGTH_LONG).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_GEOFENCE_REQUEST);
        }
    }

}
