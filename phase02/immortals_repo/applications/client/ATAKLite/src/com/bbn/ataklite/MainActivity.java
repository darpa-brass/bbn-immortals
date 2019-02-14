package com.bbn.ataklite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bbn.ataklite.entities.MonitoredEntityManager;
import com.bbn.ataklite.fragments.CustomMapFragment;
import com.bbn.ataklite.fragments.MonitoredEntityFragment;
import com.bbn.ataklite.service.SACommunicationService;
import com.bbn.ataklite.service.SAIntentReceiver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mil.darpa.immortals.core.analytics.*;
import mil.darpa.immortals.core.api.applications.AnalyticsTarget;

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements MonitoredEntityFragment.OnFragmentInteractionListener {

//    private static final String HISTORY_FILE = "ataklite/ATAKLite-TransactionData.json";

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private String mCurrentPhotoPath;

    private MonitoredEntityManager entityManager;

    private Location lastLocation = null;
    private TextView currentCoordinatesTextView;
    String currentCoordinatesTextViewFormattingString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Analytics.registerCurrentThread();

        final ATAKLiteConfig config = ATAKLiteConfig.loadConfig(this);

        if (config.analyticsConfig == null || config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.DEFAULT) {
            Analytics.initializeEndpoint(new AnalyticsEndpointInterface() {

                Gson gson = new GsonBuilder().create();

                @Override
                public void log(AnalyticsEvent e) {
                    Log.i("ImmortalsAnalytics", gson.toJson(e));
                }

                @Override
                public void shutdown() {
                    // Pass
                }

                @Override
                public void start() {
                    // pass
                }
            });

        } else if (config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.STDOUT) {
            Analytics.initializeEndpoint(new AnalyticsStdoutEndpoint());

        } else if (config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.ZEROMQ) {
            if (config.analyticsConfig.port <= 0 || config.analyticsConfig.url == null || config.analyticsConfig.url.equals("")) {
                throw new RuntimeException("ZEROMQ logging configured but the url and port are not configured!");
            }
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Analytics.initializeEndpoint(new AnalyticsMQEndpoint(config.analyticsConfig.url, config.analyticsConfig.port));
                };
            });
            Analytics.registerThread(t);
            t.start();

        } else {
            throw new RuntimeException("Unexpected analytics backend '" + config.analyticsConfig.target + "Specified!");
        }

        if (config.analyticsConfig == null) {
            Analytics.setVerbosity(AnalyticsVerbosity.Metadata);
        } else {
            Analytics.setVerbosity(config.analyticsConfig.verbosity);
        }

        String host = "";
        String port = "";
        String callID = "UNDEFINED";
        boolean startSA = false;
        if (config != null) {
            ATAKLiteConfig.ServerConfig sConfig = config.serverConfig;

            startSA = (config.broadcastSA == null ? false : true);
            host = (sConfig == null || sConfig.url == null) ? "" : sConfig.url;
            port = (sConfig == null || sConfig.port == null) ? "" : Integer.toString(sConfig.port);
            callID = config.callsign;
        }

        Analytics.setSourceIdentifier(callID);
        Analytics.log(Analytics.newEvent(AnalyticsEventType.ClientStart, Analytics.getOwnSourceIdentifier(), "MainActivity"));


        // Initialize the entity manager
        entityManager = new MonitoredEntityManager(callID, config);


        // Set the button to save the data from the entityManager
//        final Button saveHistoryButton = (Button) findViewById(R.id.saveTransactionHistory);
//        saveHistoryButton.setVisibility(View.INVISIBLE);
//        final Button loadHistoryButton = (Button) findViewById(R.id.loadTransactionHistory);
//        loadHistoryButton.setVisibility(View.INVISIBLE);
//
//        if ((new File(Environment.getExternalStorageDirectory(), "/" + HISTORY_FILE)).exists()) {
//            saveHistoryButton.setEnabled(false);
//            loadHistoryButton.setEnabled(true);
//        } else {
//            saveHistoryButton.setEnabled(true);
//            loadHistoryButton.setEnabled(false);
//        }
//
//        saveHistoryButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                saveHistoryButton.setEnabled(false);
//                loadHistoryButton.setEnabled(false);
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            saveHistory();
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    saveHistoryButton.setEnabled(true);
//                                }
//                            });
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//
//                        }
//                    }
//                }).start();
//            }
//        });
//
//
//        loadHistoryButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveHistoryButton.setEnabled(false);
//                loadHistoryButton.setEnabled(false);
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            boolean exists = loadHistory();
//
//                            if (exists) {
//                                displayToast("History successfully loaded.");
//
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        saveHistoryButton.setEnabled(true);
//                                    }
//                                });
//
//                            } else {
//                                displayToast("Could not load history since it does not exist!");
//                            }
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//
//            }
//        });

        // Grab items to be updated while the app runs
        currentCoordinatesTextView = (TextView) findViewById(R.id.currentCoordinates);
        currentCoordinatesTextViewFormattingString = getResources().getString(R.string.main_current_coordinates);

        SACommunicationServiceReceiver serviceReceiver = new SACommunicationServiceReceiver();

        boolean mapInitialized = false;
        CustomMapFragment mapDisplayFragment = null;
        // Try to attemptInit maps
        if (config == null || config.userInterface == null || config.userInterface != ATAKLiteConfig.UserInterface.ListView) {
            mapDisplayFragment = new CustomMapFragment();
            mapInitialized = mapDisplayFragment.attemptInit(this);
        }

        if (mapInitialized) {
            // If the fragment was constructed, use a map
            FragmentTransaction fragmentTransaction =
                    this.getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, mapDisplayFragment);
            fragmentTransaction.commit();
            entityManager.addEntityChangeListener(mapDisplayFragment);

        } else {
            // If it wasn't constructed, use the backup UI
            MonitoredEntityFragment listDisplayFragment = new MonitoredEntityFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, listDisplayFragment);
            fragmentTransaction.commit();
            listDisplayFragment.setEntityManager(entityManager);
            entityManager.addEntityChangeListener(listDisplayFragment);
        }

        TextView callsignTextView = (TextView) findViewById(R.id.myCallsign);
        callsignTextView.setText(String.format(getResources().getString(R.string.main_my_callsign), callID));

        if (startSA && host.trim().length() > 0 && port.trim().length() > 0 && callID.trim().length() > 0) {
            startService(new Intent(getBaseContext(), SACommunicationService.class));
        } else {
            this.displayToast("ATAKLite's situational awareness (SA) services are turned off or missing settings.");
            stopService(new Intent(getBaseContext(), SACommunicationService.class));
        }

        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(SAIntentReceiver.SELF_LOCATION_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, mStatusIntentFilter);

        IntentFilter fieldUpdateFilter = new IntentFilter(SAIntentReceiver.FIELD_LOCATION_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, fieldUpdateFilter);

        IntentFilter fieldImageUpdateFilter = new IntentFilter(SAIntentReceiver.FIELD_IMAGE_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, fieldImageUpdateFilter);

        IntentFilter displayMessageFilter = new IntentFilter(SAIntentReceiver.DISPLAY_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, displayMessageFilter);
    }

//    private void saveHistory() throws IOException {
//        LatestSaFileOutputStream os = new LatestSaFileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + HISTORY_FILE);
//        OutputStreamWriter osw = new OutputStreamWriter(os);
//        GsonHelper.createGsonInstance().toJson(entityManager, osw);
//        osw.flush();
//        osw.close();
////        os.flush();
////        os.close();
//    }
//
//    private boolean loadHistory() throws IOException {
//        File inputFile = new File(Environment.getExternalStorageDirectory(), HISTORY_FILE);
//
//        if (inputFile.exists()) {
//            InputStreamReader fr = new FileReader(inputFile);
//            MonitoredEntityManager mem = GsonHelper.createGsonInstance().fromJson(fr, MonitoredEntityManager.class);
//            fr.close();
//            HashMap<String<LinkedList<MonitoredEntity>>>
//
//            entityManager.loadPreviousEntityManagerData(mem);
//            return true;
//
//        } else {
//            return false;
//        }
//    }

    private void showSettings() {
        Intent showSettings = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(showSettings);
    }

    private void dispatchTakePictureIntent() {

        if (lastLocation == null) {
            this.displayToast("ATAKLite cannot send an image at this time since no location information is available.");

        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    this.displayToast("Exception getting camera image.");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showSettings();
            return true;
        } else if (id == R.id.action_camera) {
            dispatchTakePictureIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Analytics.log(Analytics.newEvent(AnalyticsEventType.ClientShutdown, Analytics.getOwnSourceIdentifier(), "MainActivity"));
        entityManager.shutdown();
        super.onDestroy();
        stopService(new Intent(getBaseContext(), SACommunicationService.class));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            SACommunicationService.startActionSendImage(this, lastLocation, mCurrentPhotoPath);

            this.displayToast("Image captured and sent with current location information.");

            //Uncomment to save image to gallery
            //Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //File f = new File(mCurrentPhotoPath);
            //Uri contentUri = Uri.fromFile(f);
            //mediaScanIntent.setData(contentUri);
            //this.sendBroadcast(mediaScanIntent);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void displayToast(@Nonnull final String displayMessage) {
        final Activity self = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(self, displayMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setLastLocation(@Nonnull Location location) {
        this.lastLocation = location;
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    public class SACommunicationServiceReceiver extends BroadcastReceiver {

        @Override
        public synchronized void onReceive(@Nonnull Context context, @Nonnull Intent intent) {
            String action = intent.getAction();

            if (SAIntentReceiver.SELF_LOCATION_UPDATE.equals(action)) {
                Location location = (Location) intent.getExtras().get(SAIntentReceiver.EXTRA_LOCATION);

                lastLocation = location;

                String lat = Double.toString(location.getLatitude());
                String lon = Double.toString(location.getLongitude());

                lat = lat.substring(0, Math.min(lat.indexOf(".") + 8, lat.length()));
                lon = lon.substring(0, Math.min(lon.indexOf(".") + 8, lon.length()));

                currentCoordinatesTextView.setText(String.format(currentCoordinatesTextViewFormattingString, lat, lon));
                entityManager.updateMyLocation(location);

            } else if (SAIntentReceiver.FIELD_LOCATION_UPDATE.equals(action)) {
                String origin = intent.getExtras().getString(SAIntentReceiver.EXTRA_ORIGIN_ID);
                Location location = (Location) intent.getExtras().get(SAIntentReceiver.EXTRA_LOCATION);

                entityManager.addOrUpdateExternalEntityLocation(origin, location);

            } else if (SAIntentReceiver.FIELD_IMAGE_UPDATE.equals(action)) {
                String origin = intent.getExtras().getString(SAIntentReceiver.EXTRA_ORIGIN_ID);
                Location location = (Location) intent.getExtras().get(SAIntentReceiver.EXTRA_LOCATION);
                String imageUrl = intent.getExtras().getString(SAIntentReceiver.EXTRA_IMAGE_URL);

                entityManager.addOrUpdateExternalEntityImages(origin, location, imageUrl);

            } else if (SAIntentReceiver.DISPLAY_MESSAGE.equals(action)) {
                final String displayMessage = intent.getExtras().getString(SAIntentReceiver.EXTRA_TEXT);
                System.out.println(displayMessage);
                displayToast(displayMessage);
            }
        }
    }
}
