package com6510.dcs.shef.ac.uk;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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

/**
 * Maps Activity, used for section 1.1.2. All images with locations are loaded onto this activity. Thumbnails are listed at the bottom.
 * All such images also have markers on the map. Clicking on a thumbnail zooms in on the marker. Clicking on the marker shows the details of the image.
 * The current location button is also provided on the top-left corner. There is also a filter option, which sends an intent to FilterActivity
 */
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
    private String filter_artist;
    private String filter_make;
    private String filter_model;

    /**
     * This class is to create a custom marker. We add in all details we feel are relevant to the marker and add the thumbnail as well.
     */
    public class MarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {

        private final View markerInfoView;

        /**
         * Constructor. Retrieve the relevant layout.
         */
        public MarkerInfoAdapter(){
            markerInfoView = getLayoutInflater().inflate(R.layout.marker_info_window, null);
        }

        /**
         * Null method
         * @param marker Marker
         * @return null Always
         */
        @Override
        public View getInfoWindow(Marker marker){
            return null;
        }

        /**
         * Method to override the default marker code with the custom marker
         * @param marker Marker in question
         * @return A View which shows the required values including the thumbnail.
         */
        @Override
        public View getInfoContents(Marker marker) {
            HashMap<String, String> extraValues = (HashMap<String, String>) marker.getTag();

            ImageView markerInfoThumbnail = markerInfoView.findViewById(R.id.marker_info_thumbnail);
            TextView markerInfoTitle = markerInfoView.findViewById(R.id.marker_info_title);
            TextView markerInfoDesc = markerInfoView.findViewById(R.id.marker_info_description);
            TextView markerInfoDate = markerInfoView.findViewById(R.id.marker_info_date);
            TextView markerInfoArtist = markerInfoView.findViewById(R.id.marker_info_artist);
            TextView markerInfoMake = markerInfoView.findViewById(R.id.marker_info_make);
            TextView markerInfoModel = markerInfoView.findViewById(R.id.marker_info_model);

            markerInfoThumbnail.setImageBitmap(BitmapFactory.decodeFile(extraValues.get("ThumbnailPath")));
            markerInfoTitle.setText("Title: " + Util.getPrettyTrimmedString(marker.getTitle(), 50));
            markerInfoDesc.setText("Desc: " + Util.getPrettyTrimmedString(extraValues.get("Description"), 50));
            markerInfoDate.setText("Date: " + Util.getPrettyTrimmedString(extraValues.get("Date"), 50));
            markerInfoArtist.setText("Artist: " + Util.getPrettyTrimmedString(extraValues.get("Artist"), 50));
            markerInfoMake.setText("Make: " + Util.getPrettyTrimmedString(extraValues.get("Make"), 50));
            markerInfoModel.setText("Model: " + Util.getPrettyTrimmedString(extraValues.get("Model"), 50));

            return markerInfoView;
        }
    }

    /**
     * On create for the Maps Activity
     * @param savedInstanceState State
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /* no filters initially */
        filter_title = "";
        filter_date = "";
        filter_description = "";
        filter_artist = "";
        filter_make = "";
        filter_model = "";

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
        /* Call the filter intent */
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(getApplicationContext(), FilterActivity.class);
                filterIntent.putExtra("TitleFilter", filter_title);
                filterIntent.putExtra("DateFilter", filter_date);
                filterIntent.putExtra("DescFilter", filter_description);
                filterIntent.putExtra("ArtistFilter", filter_artist);
                filterIntent.putExtra("MakeFilter", filter_make);
                filterIntent.putExtra("ModelFilter", filter_model);
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

    /**
     * Method to setup map after it's been loaded. Set the Marker Info Window to the custom marker, and begin observing for data.
     * @param googleMap Map Object
     * @throws SecurityException
     */
    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException{
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        mMap.setInfoWindowAdapter(new MarkerInfoAdapter());
        /* set up observer */
        setFilteredObserver(filter_title, filter_description, filter_date, filter_artist, filter_make, filter_model);
    }

    /**
     * Called on clicking on a thumbnail. Move the map to the image's location.
     * @param photo Photo whose thumbnail has been clicked
     */
    @Override
    public void thumbnailClick(Photo photo){
        LatLng movLoc = new LatLng(photo.getImLat(), photo.getImLng());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(movLoc,21.0f));
    }

    /**
     * Populate the map with photo markers.
     * @param photos List of hotos
     */
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
            HashMap<String, String> extraValues = new HashMap<>();
            extraValues.put("ThumbnailPath", photo.getImThumbPath());
            extraValues.put("Description", photo.getImDescription());
            extraValues.put("Date", photo.getImDateTime());
            extraValues.put("Artist", photo.getImArtist());
            extraValues.put("Make", photo.getImMake());
            extraValues.put("Model", photo.getImModel());
            marker.setTag(extraValues);
        }
    }

    /**
     * Setup the Live Data observer for the required data. Can also filter if needed, by any of the params.
     * @param title Title
     * @param description Description
     * @param date Date
     * @param artist Artist
     * @param make Make
     * @param model Model
     */
    private void setFilteredObserver(String title, String description, String date, String artist, String make, String model) {
        galleryViewModel.getFilteredPhotos("%"+title+"%",
                "%"+description+"%",
                "%"+date+"%",
                "%"+artist+"%",
                "%"+make+"%",
                "%"+model+"%")
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

    /**
     * Receive and process data from the Filter activity. Used to set filter params for the LiveData observer
     * @param req Request code
     * @param res Response code
     * @param data Data returned
     */
    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        System.out.println("Activity result called");
        if(res == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            filter_title = extras.getString("TitleFilter", "");
            filter_date = extras.getString("DateFilter", "");
            filter_description = extras.getString("DescFilter", "");
            filter_artist = extras.getString("ArtistFilter", "");
            filter_make = extras.getString("MakeFilter", "");
            filter_model = extras.getString("ModelFilter", "");
            setFilteredObserver(filter_title, filter_description, filter_date, filter_artist, filter_make, filter_model);
        }
    }
}
