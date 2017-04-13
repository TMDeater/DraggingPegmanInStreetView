package com.example.msi.dragstreetview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button streetV;
    private Marker marker;
    private ImageView pegman;
    private ImageView streetPreview;
    private Boolean streetViewIsAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        streetViewIsAvailable=false;

        streetPreview= (ImageView) findViewById(R.id.streetview_preview);

        pegman = (ImageView) findViewById(R.id.user_icon);
        pegman.setOnTouchListener(new View.OnTouchListener(){
            private float originalX;
            private float originalY;
            private float initialTouchX;
            private float initialTouchY;
            private Projection projection;

            int[] location = new int[2];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        originalX = pegman.getX();
                        originalY = pegman.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        pegman.getLocationOnScreen(location);
                        projection = mMap.getProjection();
                        LatLng geoPosition = projection.fromScreenLocation(
                                new Point(location[0],location[1]));

                        try {
                            droppegmanstreetview(geoPosition);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            loadPreviewImage(geoPosition);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pegman.setX(originalX);
                        pegman.setY(originalY);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        pegman.setX(originalX+ (int) event.getRawX() - initialTouchX);
                        pegman.setY(originalY+ (int) event.getRawY() - initialTouchY);
                        return true;
                }
                return false;
            }
        });


    }

    public void streetview(View view){
        Intent intent = new Intent();

        LatLng latlng = marker.getPosition();
        double latitude=latlng.latitude;
        double longtitude =latlng.longitude;
        intent.putExtra("Lat", latitude);
        intent.putExtra("Long", longtitude);
        intent.setClass(this , SVActivity.class);
        startActivity(intent);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void droppegmanstreetview(LatLng latlng) throws ExecutionException, InterruptedException {
        streetViewIsAvailable = new CheckStreetViewAvailability().execute(latlng).get();
        if (!streetViewIsAvailable){
            streetPreview.setVisibility(View.INVISIBLE);

            return;
        };

        Intent intent = new Intent();

        double latitude=latlng.latitude;
        double longtitude =latlng.longitude;
        intent.putExtra("Lat", latitude);
        intent.putExtra("Long", longtitude);
        intent.setClass(this , SVActivity.class);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void loadPreviewImage(LatLng latLng) throws ExecutionException, InterruptedException {
        streetViewIsAvailable = new CheckStreetViewAvailability().execute(latLng).get();
        if (!streetViewIsAvailable){
            streetPreview.setVisibility(View.INVISIBLE);
            return;
        };
        String sLat = String.valueOf(latLng.latitude);
        String sLng = String.valueOf(latLng.longitude);
        String urlTemplate=new String("https://maps.googleapis.com/maps/api/streetview?size=200x100&location=#,%&key=AIzaSyAN0mv-2HUaBcRD0V8yOsV-11pW8-WD1J4");
        String url = urlTemplate.replace("#",sLat).replace("%",sLng);

        Picasso.with(getApplicationContext())
                .load(url)
                .into(streetPreview);
        streetPreview.setVisibility(View.VISIBLE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng hongkong = new LatLng(22.296464, 114.171931);

        //resize icon
        Drawable dr = getResources().getDrawable(R.drawable.user_yellow);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 60, 80, true));
        Bitmap resizedIcon =((BitmapDrawable) d).getBitmap();

        marker=mMap.addMarker(new MarkerOptions().position(hongkong).title("Marker now")
                .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hongkong));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),16));

    }

}
