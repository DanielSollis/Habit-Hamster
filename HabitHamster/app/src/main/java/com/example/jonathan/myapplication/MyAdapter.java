package com.example.jonathan.myapplication;

/**
 * Created by jonathan on 11/14/17.
 */

import android.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private List<ListItem> listItems;
    private Context context;
    Geofence mGeofence;

    public MyAdapter(List<ListItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem listItem = listItems.get(position);

        holder.textViewHead.setText(listItem.getHead());
        holder.textViewDesc.setText(listItem.getDesc());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textViewHead;
        public TextView textViewDesc;
        public ImageButton addLocationButton;

        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(context);

        public ViewHolder(final View itemView) {
            super(itemView);

            textViewHead = (TextView) itemView.findViewById(R.id.textViewHead);
            textViewDesc = (TextView) itemView.findViewById(R.id.textViewDesc);
            addLocationButton = (ImageButton) itemView.findViewById(R.id.addLocation);

            addLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission((MainActivity)context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((MainActivity)context, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION }, 1);
                    }
                    if (ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((MainActivity)context, new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
                    }

                    final FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient((MainActivity)context);
                    final GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient((itemView.getContext()));

                    final Location[] mLocation = new Location[1];
                    LocationManager lm = (LocationManager) itemView.getContext().getSystemService(Context.LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    double lat = location.getLatitude();
                    final double lon = location.getLongitude();

                    mGeofence = new Geofence.Builder()
                            .setRequestId("geo1")
                            .setCircularRegion(lat, lon, 100)
                            .setExpirationDuration(100000000)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                    Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build();

                    mGeofencingClient.addGeofences(createGeofencingRequest(), createGeofencePendingIntent())
                            .addOnSuccessListener((Activity) itemView.getContext(), new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(itemView.getContext(), "Geofence Added", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener((Activity) itemView.getContext(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(itemView.getContext(), "Geofence failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }

    }

    private GeofencingRequest createGeofencingRequest (){
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence(mGeofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent(context, GeofenceTrasitionService.class);
        return PendingIntent.getService(context, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }
}