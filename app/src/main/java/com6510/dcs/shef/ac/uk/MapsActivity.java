package com6510.dcs.shef.ac.uk;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsInterface {

    private GoogleMap mMap;
    private GalleryViewModel galleryViewModel;
    private static final int ACCESS_FINE_LOCATION = 123;
    private RecyclerView mRecyclerView;
    private MapsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Photo> mDataset=new ArrayList<Photo>();
    private boolean mLocationPermissionGranted;
    private Button mSearch;
    private PopupWindow searchPopup;
    private View mPopupView;
    private String title;
    private String date;

    public class MarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {

        private final View markerInfoView;

        public MarkerInfoAdapter(){
            markerInfoView=getLayoutInflater().inflate(R.layout.marker_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker){
            return null;
        }

        @Override
        public View getInfoContents(Marker marker){
            ImageView markerInfoThumbnail= ((ImageView)markerInfoView.findViewById(R.id.marker_info_thumbnail));
            Drawable drawable=Drawable.createFromPath(marker.getSnippet());
            markerInfoThumbnail.setImageBitmap(((BitmapDrawable)drawable).getBitmap());
            TextView markerInfoTitle= ((TextView)markerInfoView.findViewById(R.id.marker_info_title));
            markerInfoTitle.setText(marker.getTitle());
            markerInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, markerInfoTitle.getTextSize()*1.5f);
            markerInfoTitle.setTypeface(null, Typeface.BOLD);
            LatLng location=marker.getPosition();
            TextView markerInfoLat= ((TextView)markerInfoView.findViewById(R.id.marker_info_lat));
            markerInfoLat.setText("Latitude: "+Double.toString(location.latitude));
            TextView markerInfoLng= ((TextView)markerInfoView.findViewById(R.id.marker_info_lng));
            markerInfoLng.setText("Longitude: "+Double.toString(location.longitude));
            return markerInfoView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        title="%%";
        date="%%";
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getLocationPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        Bundle extras=getIntent().getExtras();
//        if(extras!=null){
//            mDataset=extras.getParcelableArrayList("Photos");
//        }
        mPopupView = getLayoutInflater().inflate(R.layout.search_popup, null);
        searchPopup = new PopupWindow(mPopupView);
        mRecyclerView = (RecyclerView) findViewById(R.id.imgStrip);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, 0, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MapsAdapter(mDataset, this);
        mRecyclerView.setAdapter(mAdapter);
        queueData();
        mSearch = (Button) findViewById(R.id.imgSearch);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title="%y%";
                queueData();
//                searchPopup.showAtLocation(findViewById(R.id.main_map_layout), Gravity.CENTER, 32, 32);
            }
        });
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
    public void onMapReady(GoogleMap googleMap) throws SecurityException{
        mMap = googleMap;
        if(mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 5.0f ) );
        mMap.setInfoWindowAdapter(new MarkerInfoAdapter());
        populateMap(mMap);
    }

    @Override
    public void thumbnailClick(Photo photo){
        LatLng movLoc = new LatLng(photo.getImLat(), photo.getImLng());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(movLoc,14));
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void reprocessData(ArrayList<Photo> dataset){
        mAdapter.resetDataset(dataset);
        mAdapter.notifyDataSetChanged();
    }

    private void populateMap(GoogleMap map) {
        map.clear();
        for(Photo location: mDataset) {
            LatLng coords = new LatLng(location.getImLat(), location.getImLng());
            Marker marker = mMap.addMarker(new MarkerOptions().position(coords).title(location.getImTitle()).icon(BitmapDescriptorFactory.fromPath(location.getImThumbPath())).snippet(location.getImThumbPath()));
        }
    }

    private void queueData() {
        galleryViewModel=ViewModelProviders.of(this).get(GalleryViewModel.class);
        galleryViewModel.refreshDatabase(getApplicationContext());
        galleryViewModel.getFilteredPhotos(title, date).observe(this, new Observer<List<Photo>>(){
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                mDataset=(ArrayList<Photo>) photos;
                System.out.println("onChanged: size " + photos.size());
                reprocessData(mDataset);
                if(mMap!=null){
                    populateMap(mMap);
                }
            }});
    }
}
