package mil.darpa.immortals.modulerunner;

import android.app.Activity;
import android.os.Bundle;
import mil.darpa.immortals.datatypes.Coordinates;

public class MainActivity extends Activity {
    // Location determination declaration
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-declaration}
    private mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn locationProvider;
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-init}
        mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn locationProviderAndroidGPS = new mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn();
        locationProvider = locationProviderAndroidGPS;
        locationProvider.initialize(this);
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

        Coordinates coordinates;

        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{coordinates = $BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-work}
        coordinates = locationProvider.getLastKnownLocation();
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

        System.out.println(coordinates.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-cleanup}
        if (locationProvider != null) {
            locationProvider.onDestroy();
        }
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd
    }
}
