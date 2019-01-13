package com6510.dcs.shef.ac.uk;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.Comparator;
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
    private FloatingActionButton mSearch;

    /* filter values */
    private String filter_title;
    private String filter_description;
    private String filter_date;

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
            markerInfoTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, markerInfoTitle.getTextSize()*1.2f);
            markerInfoTitle.setTypeface(null, Typeface.BOLD);
            TextView markerInfoDesc= ((TextView)markerInfoView.findViewById(R.id.marker_info_description));
            if(extraValues.get("Description").isEmpty()) {
                markerInfoDesc.setText("No Description set.");
            }
            else {
                if(extraValues.get("Description").length()>90) {
                    markerInfoDesc.setText(extraValues.get("Description").substring(0, 89)+"...");
                }
                else {
                    markerInfoDesc.setText(extraValues.get("Description"));
                }
            }
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

        /* no filters initially */
        filter_title = "";
        filter_date = "";
        filter_description = "";

        /* set up view model */
        galleryViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);

        /* set up google map fragment */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* set up UI */
        mRecyclerView = (RecyclerView) findViewById(R.id.imgStrip);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, 0, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MapsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mSearch = (FloatingActionButton) findViewById(R.id.imgSearch);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(getApplicationContext(), FilterActivity.class);
                filterIntent.putExtra("TitleFilter", filter_title);
                filterIntent.putExtra("DateFilter", filter_date);
                filterIntent.putExtra("DescFilter", filter_description);
                startActivityForResult(filterIntent, 0);
            }
        });

        /* auto hide floating button */
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    mSearch.hide();
                } else if (dx < 0) {
                    mSearch.show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException{
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        mMap.setInfoWindowAdapter(new MarkerInfoAdapter());
        /* set up observer */
        setFilteredObserver(filter_title, filter_description, filter_date);
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

    private void setFilteredObserver(String title, String description, String date) {
        galleryViewModel.getFilteredPhotos("%"+title+"%", "%"+description+"%", "%"+date+"%")
                .observe(this, new Observer<List<Photo>>(){
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                System.out.println("onChanged (getFilteredPhotos): size " + photos.size());

                /* sort photos by last modified time */
                Collections.sort(photos, (new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        if (o1.getImTimestamp() == o2.getImTimestamp()) {
                            return 0;
                        }
                        return o2.getImTimestamp() < o1.getImTimestamp() ? -1 : 1;
                    }
                }));

                mAdapter.resetDataset(photos);
                populateMap(photos);
            }});
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        System.out.println("Activity result called");
        if(res == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            filter_title = extras.getString("TitleFilter", "");
            filter_date = extras.getString("DateFilter", "");
            filter_description = extras.getString("DescFilter", "");
            setFilteredObserver(filter_title, filter_description, filter_date);
        }
    }
}
