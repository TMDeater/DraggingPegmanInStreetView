package com.example.msi.dragstreetview;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class SVActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    private double lat;
    private double lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sv);
        StreetViewPanoramaFragment mF = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.sview);
        mF.getStreetViewPanoramaAsync(this);
    }
    @Override
    public void onStreetViewPanoramaReady (StreetViewPanorama panorama) {
        Intent intent = getIntent();
        lat = intent.getDoubleExtra("Lat", 0);
        lng = intent.getDoubleExtra("Long", 0);
        panorama.setPosition(new LatLng(lat, lng), 100);

    }

}
