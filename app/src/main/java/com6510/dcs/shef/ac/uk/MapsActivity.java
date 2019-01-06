package com6510.dcs.shef.ac.uk;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
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

import java.util.HashMap;
import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsInterface {

    /* maps */
    private GoogleMap mMap;

    /* MVVM */
    private GalleryViewModel galleryViewModel;

    /* UI */
    private RecyclerView mRecyclerView;
    private MapsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mSearch;
    private PopupWindow searchPopup;
    private View mPopupView;

    private String title;
    private String description;
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
            HashMap<String, String> extraValues=(HashMap<String, String>) marker.getTag();
            ImageView markerInfoThumbnail= ((ImageView)markerInfoView.findViewById(R.id.marker_info_thumbnail));
            Drawable drawable=Drawable.createFromPath(extraValues.get("ThumbnailPath"));
            markerInfoThumbnail.setImageBitmap(((BitmapDrawable)drawable).getBitmap());
            TextView markerInfoTitle= ((TextView)markerInfoView.findViewById(R.id.marker_info_title));
            markerInfoTitle.setText(marker.getTitle());
            markerInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, markerInfoTitle.getTextSize()*1.5f);
            markerInfoTitle.setTypeface(null, Typeface.BOLD);
            TextView markerInfoDesc= ((TextView)markerInfoView.findViewById(R.id.marker_info_description));
            markerInfoTitle.setText(extraValues.get("Description"));
            LatLng location=marker.getPosition();
            TextView markerInfoLat= ((TextView)markerInfoView.findViewById(R.id.marker_info_lat));
            markerInfoLat.setText(String.format("Latitude: %.6f", location.latitude));
            TextView markerInfoLng= ((TextView)markerInfoView.findViewById(R.id.marker_info_lng));
            markerInfoLng.setText(String.format("Longitude: %.6f", location.longitude));
            if(extraValues.get("Date")!=null && !extraValues.get("Date").isEmpty()) {
                TextView markerInfoDate = ((TextView) markerInfoView.findViewById(R.id.marker_info_date));
                markerInfoDate.setText(String.format("Date: %s", extraValues.get("Date")));
            }
            return markerInfoView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        title="%%";
        description="%%";
        date="%%";

        /* set up view model */
        galleryViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);

        /* set up google map fragment */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* set up UI */
        mPopupView = getLayoutInflater().inflate(R.layout.search_popup, null);
        searchPopup = new PopupWindow(mPopupView);
        mRecyclerView = (RecyclerView) findViewById(R.id.imgStrip);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, 0, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MapsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mSearch = (Button) findViewById(R.id.imgSearch);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title="%y%";
//                searchPopup.showAtLocation(findViewById(R.id.main_map_layout), Gravity.CENTER, 32, 32);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException{
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        mMap.setInfoWindowAdapter(new MarkerInfoAdapter());
        /* set up observers */
        queueData();
    }

    @Override
    public void thumbnailClick(Photo photo){
        LatLng movLoc = new LatLng(photo.getImLat(), photo.getImLng());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(movLoc,21.0f));
    }

    private void populateMap(List<Photo> photos) {
        mMap.clear();
        for(Photo photo : photos) {
            /* only take photos with GPS info */
            if (photo.getImHasCoordinates() == false) {
                continue;
            }
            LatLng coords = new LatLng(photo.getImLat(), photo.getImLng());
            /* create 50x50 marker icon */
            Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gps_photo);
            Bitmap scaledMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, 100, 100, true);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(photo.getImTitle())
                    .icon(BitmapDescriptorFactory.fromBitmap(scaledMarkerBitmap)));
            HashMap<String, String> extraValues=new HashMap<>();
            extraValues.put("ThumbnailPath", photo.getImThumbPath());
            extraValues.put("Description", photo.getImDescription());
            extraValues.put("Date", photo.getImDateTime());
            marker.setTag(extraValues);
        }
    }

    private void queueData() {
        galleryViewModel.getFilteredPhotos(title, description, date).observe(this, new Observer<List<Photo>>(){
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                System.out.println("onChanged (getFilteredPhotos): size " + photos.size());
                mAdapter.resetDataset(photos);
                populateMap(photos);
            }});
    }
}
