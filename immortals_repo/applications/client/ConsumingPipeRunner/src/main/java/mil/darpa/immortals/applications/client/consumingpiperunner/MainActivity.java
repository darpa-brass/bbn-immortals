package mil.darpa.immortals.applications.client.consumingpiperunner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.GenericConsumingPipeDfu;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import mil.darpa.immortals.core.synthesis.annotations.dfu.SynthesisAndroidContext;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends Activity {

    Thread moduleThread;

    @SynthesisAndroidContext
    public Context context;

    // Declaration
    // $B49219A8-493B-4639-8F94-73EEC496C523-declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;

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

        Analytics.initializeEndpoint(new AnalyticsEndpointInterface() {
            
            Gson gson = new GsonBuilder().create();
            
            @Override
            public void start() {
                
            }

            @Override
            public void log(AnalyticsEvent event) {
                Log.e("ImmortalsAnalytics", gson.toJson(event));
            }

            @Override
            public void shutdown() {
                // Pass
            }
        });

        Analytics.setVerbosity(AnalyticsVerbosity.Data);


        final ConsumingPipe consumingDfu = new GenericConsumingPipeDfu();


        // Initialization
        // $B49219A8-493B-4639-8F94-73EEC496C523-init


        moduleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Execution
                // $B49219A8-493B-4639-8F94-73EEC496C523-work
                consumingDfu.consume(null);
            }
        });
        Analytics.registerThread(moduleThread);
        moduleThread.start();
    }

    @Override
    protected void onDestroy() {
        // Cleanup
        // $B49219A8-493B-4639-8F94-73EEC496C523-cleanup

        super.onDestroy();
    }
}
