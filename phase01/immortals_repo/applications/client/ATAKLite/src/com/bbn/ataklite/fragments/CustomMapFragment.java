package com.bbn.ataklite.fragments;

import android.content.Context;
import android.location.Location;
import com.bbn.ataklite.entities.EntityChangeListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 2/8/16.
 */
public class CustomMapFragment extends MapFragment implements EntityChangeListener {

    private GoogleMap map;
    private Marker myMarker;
    private HashMap<String, Marker> entityMarkers = new HashMap<String, Marker>();

    public boolean attemptInit(Context context) {
        ConnectionResult googlePlayAvailability = new ConnectionResult(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context));

        if (googlePlayAvailability.isSuccess()) {
            this.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void onMyLocationChanged(@Nullable Location newLocation) {
        if (map != null) {
            // TODO: If the location went from available to null, maybe it would mean something...
            if (newLocation != null) {
                if (myMarker == null) {
                    myMarker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()))
                            .title("Android Client: 09"));
                    myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else {
                    myMarker.setPosition(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
                }
            }
        }
    }

    @Override
    public void onExternalEntityLocationAddedOrChanged(@Nonnull String entityIdentifier, @Nonnull Location newLocation) {
        if (newLocation != null && entityIdentifier != null) {
            Marker marker = entityMarkers.get(entityIdentifier);
            if (marker == null) {
                marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()))
                        .title(entityIdentifier));
                entityMarkers.put(entityIdentifier, marker);
            } else {
                marker.setPosition(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
            }
        }
    }

    @Override
    public void onExternalEntityImageAdded(@Nonnull String identifier, @Nonnull Location imageLocation, @Nonnull String imageUrl) {

    }

}
