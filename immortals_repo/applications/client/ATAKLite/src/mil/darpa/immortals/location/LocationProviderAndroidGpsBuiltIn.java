package mil.darpa.immortals.dfus.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.securboration.immortals.ontology.functionality.locationprovider.CleanupAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.GetCurrentLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.resources.gps.GpsReceiverEmbedded;
import com.securboration.immortals.ontology.resources.gps.GpsSatelliteConstellation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.core.simulated.EnvironmentConfiguration;
import mil.darpa.immortals.datatypes.Coordinates;

/**
 * Created by awellman@bbn.com on 2/4/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = LocationProvider.class,
        resourceDependencies = {
                GpsSatelliteConstellation.class,
                GpsReceiverEmbedded.class
        }
)
public class LocationProviderAndroidGpsBuiltIn {

    private static final String PROFILE_IDENTIFIER = "LocationProviderAndroidGpsBuiltIn";
    private static final String HOW = "m-g";

    private String provider;

    private LocationManager locationManager;

    private Location mostRecentLocation;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLocation(location);
            mostRecentLocation = location;

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public LocationProviderAndroidGpsBuiltIn() {
    }

    @FunctionalAspectAnnotation(aspect = InitializeAspect.class)
    public void initialize(Context context) {
        EnvironmentConfiguration.getAndroidEnvironment().handleMissingResources(PROFILE_IDENTIFIER);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        provider = locationManager.getBestProvider(criteria, true);

        try {
            // TODO: Is this necessary to get a location from getLastKnownLoation?
            locationManager.requestLocationUpdates(provider, 5000, 0, this.locationListener);
        } catch (SecurityException e) {
            // TODO: Make this prettier? Indicates Marshmallow and selective permissions have disabled location.
            throw new RuntimeException(e);
        }
    }

    private synchronized void setLocation(Location location) {
        this.mostRecentLocation = location;
    }

    @FunctionalAspectAnnotation(aspect = GetCurrentLocationAspect.class)
    public synchronized Coordinates getLastKnownLocation() {
        try {
            Location location = mostRecentLocation;

            Coordinates coordinates;
            if (location != null) {
                coordinates = new Coordinates(
                        location.getLatitude(),
                        location.getLongitude(),
                        (location.hasAltitude() ? location.getAltitude() : null),
                        (location.hasAccuracy() ? location.getAccuracy() : null),
                        location.getTime(),
                        HOW
                );
            } else {
                coordinates = new Coordinates(
                        0,
                        0,
                        null,
                        null,
                        System.currentTimeMillis(),
                        HOW
                );
            }
            return coordinates;
        } catch (SecurityException e) {
            // TODO: Make this prettier? Indicates Marshmallow and selective permissions have disabled location.
            throw new RuntimeException(e);
        }
    }

    @FunctionalAspectAnnotation(aspect = CleanupAspect.class)
    public void onDestroy() {
        try {
            Log.i(PROFILE_IDENTIFIER, "Unregistering location update since SACommunicationService is stopping.");
            if (locationManager != null && this.locationManager != null) {
                locationManager.removeUpdates(this.locationListener);
            }
        } catch (SecurityException e) {
            // TODO: Make this prettier? Indicates Marshmallow and selective permissions have disabled location.
            throw new RuntimeException(e);
        }
    }
}
