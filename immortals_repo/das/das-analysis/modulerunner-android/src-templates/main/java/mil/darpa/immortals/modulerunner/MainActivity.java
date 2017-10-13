package mil.darpa.immortals.modulerunner;

import android.app.Activity;
import android.os.Bundle;
import mil.darpa.immortals.datatypes.Coordinates;

public class MainActivity extends Activity {
    // Location determination declaration
$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-declaration


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-init

        Coordinates coordinates;

coordinates = $BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-work

        System.out.println(coordinates.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-cleanup
    }
}
