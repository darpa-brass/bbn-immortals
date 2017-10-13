package mil.darpa.immortals.modulerunner;

import android.app.Activity;
import android.os.Bundle;
import mil.darpa.immortals.datatypes.Coordinates;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {
    // Location determination declaration
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$222B2DFF-35D6-47D3-9979-6814D5043E97-declaration}
    private mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn locationProvider;
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ("dummyEnv".equals(BuildConfig.BUILD_TYPE)) {

            File targetFile = new File("/sdcard/ataklite/env.json");
            if (!targetFile.exists()) {
                try {
                    int rid = getResources().getIdentifier("env", "raw", getPackageName());
                    InputStream is = getResources().openRawResource(rid);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    List<String> lines = new LinkedList<>();

                    String line = br.readLine();

                    while (line != null) {
                        lines.add(line);
                        line = br.readLine();
                    }
                    br.close();

                    File f = new File("/sdcard/ataklite");
                    if (!f.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        f.mkdir();
                    }

                    FileWriter fw = new FileWriter(targetFile);

                    for (String l : lines) {
                        fw.write(l);
                    }
                    fw.flush();
                    fw.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$222B2DFF-35D6-47D3-9979-6814D5043E97-init}
        @SuppressWarnings("UnnecessaryLocalVariable")
        mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn locationProviderAndroidGPS = new mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn();
        locationProvider = locationProviderAndroidGPS;
        locationProvider.initialize(this);
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

        Coordinates coordinates;

        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{coordinates = $222B2DFF-35D6-47D3-9979-6814D5043E97-work}
        coordinates = locationProvider.getLastKnownLocation();
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

        System.out.println(coordinates.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$222B2DFF-35D6-47D3-9979-6814D5043E97-cleanup}
        if (locationProvider != null) {
            locationProvider.onDestroy();
        }
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd
    }
}