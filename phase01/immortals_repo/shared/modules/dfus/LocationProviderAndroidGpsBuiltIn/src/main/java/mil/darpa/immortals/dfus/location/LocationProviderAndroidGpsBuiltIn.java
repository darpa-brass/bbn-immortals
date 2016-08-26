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
import com.securboration.immortals.ontology.resources.gps.GpsReceiver;
import com.securboration.immortals.ontology.resources.gps.GpsSatellite;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/4/16.
 */
//@Dfu(
//        //Identifies the core functionality being performed by the DFU
//        functionalityUri = Semantics.Functionality_LocationProvider_AndroidGPS,
//
//        //Any resource dependencies that apply to all functional aspects of this DFU go here
//        //Indicates the resource dependencies specific to this functional aspect
//        resourceDependencies = @ResourceDependencies(
//                dependencyUris = {
//                        Semantics.Ecosystem_Platform_Android,
//                        Semantics.Ecosystem_Hardware_EmbeddedGPS,
//                        Semantics.Ecosystem_Environment_GPSSatellites
//                }
//        )
//)
@DfuAnnotation(
        functionalityBeingPerformed = LocationProvider.class,
        resourceDependencies = {
                GpsSatellite.class,
                GpsReceiver.class
        }
)
public class LocationProviderAndroidGpsBuiltIn {

    private static final String TAG = "LocationProviderAndroidGpsBuiltIn";

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

    //    @SynthesisInit
    @FunctionalAspectAnnotation(aspect = InitializeAspect.class)
    public void initialize(@Nonnull Context context) {
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


    //    @SynthesisWork
//    @FunctionalDfuAspect(
//            //Indicates the core DFU functionality to which this aspect applies.  This should match
//            // exactly one of the @DFU annotations on the owner class
//            functionalityUri = Semantics.Functionality_LocationProvider,
//
//            //Indicates the functional aspect of the DFU to which this method applies.
//            functionalAspectUri = Semantics.Functionality_LocationProvider_LastKnown
//    )
//    @SemanticTypeBinding(
//            //Here the type annotation applies to return type (output)
//            //It can also be placed on any args (inputs)
//            semanticType = Semantics.Datatype_Coordinates
//    )
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
                        TAG
                );
            } else {
                coordinates = new Coordinates(
                        0,
                        0,
                        null,
                        null,
                        System.currentTimeMillis(),
                        TAG
                );
            }
            return coordinates;
        } catch (SecurityException e) {
            // TODO: Make this prettier? Indicates Marshmallow and selective permissions have disabled location.
            throw new RuntimeException(e);
        }
    }

    //    @SynthesisCleanup
    @FunctionalAspectAnnotation(aspect = CleanupAspect.class)
    public void onDestroy() {
        try {
            Log.i(TAG, "Unregistering location update since SACommunicationService is stopping.");
            if (locationManager != null && this.locationManager != null) {
                locationManager.removeUpdates(this.locationListener);
            }
        } catch (SecurityException e) {
            // TODO: Make this prettier? Indicates Marshmallow and selective permissions have disabled location.
            throw new RuntimeException(e);
        }
    }
}
