package com6510.oak.shef.ac.uk;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com6510.oak.shef.ac.uk.gallery.R;

/**
 * Class to show the location of a given photo on a map. This is meant to supplement the single image view. Shows just the location of the image.
 */
public class ShowPhotoLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Photo photo;

    /**
     * On Create
     * @param savedInstanceState State
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo_location);

        Bundle data = getIntent().getExtras();
        photo = (Photo) data.getParcelable("Photo");

        /* set up google map fragment */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.photo_location_map);
        mapFragment.getMapAsync(this);
    }

    /**
     * On Map ready. Place a marker at the required location and zoom to it.
     * @param googleMap Map in question
     * @throws SecurityException
     */
    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException{
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //mMap.setInfoWindowAdapter(new MarkerInfoAdapter());

        /* show marker */
        mMap.clear();
        if (photo.getImHasCoordinates() == false) {
            Toast.makeText(getApplicationContext(), "This photo does not have location data!", Toast.LENGTH_LONG).show();
            return;
        }
        LatLng coords = new LatLng(photo.getImLat(), photo.getImLng());
        mMap.addMarker(new MarkerOptions()
                .position(coords)
                .title(photo.getImTitle()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords,18.0f));
    }
}
