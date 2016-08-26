package com.bbn.ataklite.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import com.bbn.ataklite.ATAKLiteConfig;
import com.bbn.ataklite.CoTMessage;
import com.bbn.ataklite.net.Dispatcher;
import com.bbn.ataklite.pipes.ByteCotifier;
import com.bbn.ataklite.pipes.CotByter;
import com.bbn.ataklite.pipes.ImageCotifier;
import com.bbn.ataklite.pipes.Passthrough;
import com.bbn.ataklite.testhelpers.TestEventBroadcaster;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.synthesis.ObjectPipeMultiplexerHead;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;
import mil.darpa.immortals.dfus.images.BitmapDownsizer;
import mil.darpa.immortals.dfus.images.BitmapReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SACommunicationService extends IntentService {


    private static final String TAG = "SACommunicationService";

    private Gson gson = (new GsonBuilder()).create();

    // Location determination declaration
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-declaration}
    Object locationProvider;
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

    private Dispatcher networkDispatcher = null;
    private SAIntentBroadcaster intentBroadcaster;

    Thread latestSAThread;
    Thread networkReadThread;

    private ObjectPipeMultiplexerHead<String, Location> imageSender;
    private WriteableObjectPipeInterface<CoTMessage> cotSender;

    private TestEventBroadcaster testEventBroadcaster = new TestEventBroadcaster();

    private static final String ACTION_SEND_IMAGE = "com.bbn.ataklite.action.SEND_IMAGE";
    private static final String ACTION_BROADCAST_FIELD_UPDATE = "com.bbn.ataklite.action.BROADCAST_FIELD_UPDATE";

    public static final String ACTION_BROADCAST_IMAGE_UPDATE = "com.bbn.ataklite.action.BROADCAST_IMAGE_UPDATE";


    @Override
    public void onCreate() {
        super.onCreate();

        // Load the intent broadcaster to communicate with the UI
        intentBroadcaster = new SAIntentBroadcaster(this);

        // Load the config.
        ATAKLiteConfig config = ATAKLiteConfig.loadConfig(this);

        if (config == null) {
            String message = "Unable to load configuration from filesystem or shared preferences. Please configure the application in the settings menu to the upper right.";
            intentBroadcaster.displayMessage(message);

        } else if (config.serverConfig == null) {
            String message = "No server configuration has been set. Please configure the application in the settings menu to the upper right.";
            intentBroadcaster.displayMessage(message);

        } else {
            initializeDispatcher(config.serverConfig.url, config.serverConfig.port);
            networkDispatcher.start();
        }

        initializeLocationProvider();

        initializeNetworkSenders();

        // Start broadcasting latestSA
        startTrackingLatestSA(config);

        // Load any test settings
        loadTestSettings(config);

        Log.i(TAG, "Created called.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            Log.i(TAG, "Start called: " + intent.getAction());
            onHandleIntent(intent);
        } else {
            System.err.println("SACommunicationService recieved empty intent or action identifier!");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        // Clean up the location provider
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-cleanup}
        if (locationProvider != null) {
            if (locationProvider instanceof mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn) {
                ((mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn) locationProvider).onDestroy();
            }
        }
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

        // Stop the latestSA broadcaster
        if (latestSAThread != null) {
            latestSAThread.interrupt();
        }


        // Stop the network dispatcher
        if (networkDispatcher != null) {
            networkDispatcher.stop();
        }

        Analytics.log(Analytics.newEvent(AnalyticsEventType.ClientShutdown, Analytics.getOwnSourceIdentifier(), null));
    }

    /**
     * Helper Method that starts this service with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSendImage(Context context, Location location, String imageFilepath) {
        Intent intent = new Intent(context, SACommunicationService.class);
        intent.setAction(ACTION_SEND_IMAGE);
        intent.putExtra(SAIntentReceiver.EXTRA_LOCATION, location);
        intent.putExtra(SAIntentReceiver.EXTRA_IMAGE_URL, imageFilepath);
        context.startService(intent);
    }

    public SACommunicationService() {
        super("SACommunicationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_IMAGE.equals(action)) {
                final Location location = intent.getParcelableExtra(SAIntentReceiver.EXTRA_LOCATION);
                final String imageFilepath = intent.getStringExtra(SAIntentReceiver.EXTRA_IMAGE_URL);
                handleActionSendImage(imageFilepath, location);
            } else if (ACTION_BROADCAST_FIELD_UPDATE.equals(action)) {
                final Location location = intent.getParcelableExtra(SAIntentReceiver.EXTRA_LOCATION);
                final String locationID = intent.getStringExtra(SAIntentReceiver.EXTRA_ORIGIN_ID);
                intentBroadcaster.broadcastFieldUpdate(mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter.toCoordinates(location), locationID);

            } else if (ACTION_BROADCAST_IMAGE_UPDATE.equals(action)) {
                final Location location = intent.getParcelableExtra(SAIntentReceiver.EXTRA_LOCATION);
                final String locationID = intent.getStringExtra(SAIntentReceiver.EXTRA_ORIGIN_ID);
                final String imageUrl = intent.getStringExtra(SAIntentReceiver.EXTRA_IMAGE_URL);
                intentBroadcaster.broadcastImageUpdate(mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter.toCoordinates(location), locationID, imageUrl);

            }
        }
    }

    /**
     * Handle action SEND_IMAGE in the provided background thread with the provided parameters.
     */
    private void handleActionSendImage(String imageFilepath, Location location) {
        imageSender.write(imageFilepath, location);
    }

    private void initializeDispatcher(String takServerHost, int portAsInt) {

        try {
            if (takServerHost != null & takServerHost.trim().length() > 0 && portAsInt > 0) {
                networkDispatcher = new Dispatcher(InetAddress.getByName(takServerHost), portAsInt);
            }

            // c0ntrolp0int - NetworkReceiver
            final ByteCotifier byteCotifyer = new ByteCotifier(
                    networkDispatcher
            );

            final CoTMessage message;

            Runnable r = new Runnable() {

                @Override
                public void run() {
                    CoTMessage message;

                    while ((message = byteCotifyer.produce()) != null) {

                        // TODO: add the provider to the CoTMessage object
                        Location location = new Location("");

                        location.setAltitude(message.getAltitude());
                        location.setLatitude(message.getLatitude());
                        location.setLongitude(message.getLongitude());


                        byte[] imageData = message.getImageData();

                        if (imageData != null) {

                            String imageName = message.getUid() + "-" + System.currentTimeMillis() + ".jpg";

                            File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageName);


                            try {
                                FileOutputStream fos = new FileOutputStream(imageFile.getPath());
                                fos.write(imageData);
                                fos.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            // Not currently used, will probably need to add additional parts of the control point and/or integrate it with the Image Output control point
                            String imagePath = imageFile.getAbsolutePath();
                            intentBroadcaster.broadcastImageUpdate(mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter.toCoordinates(location), message.getUid(), imagePath);

                        } else {
                            intentBroadcaster.broadcastFieldUpdate(mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter.toCoordinates(location), message.getUid());
                        }
                    }

                }
            };

            networkReadThread = new Thread(r);
            networkReadThread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void loadTestSettings(ATAKLiteConfig config) {
        if (config.testSettings != null) {
            if (config.testSettings.imageBroadcastIntervalMS != null &&
                    config.testSettings.imageBroadcastIntervalMS > 0 &&
                    config.testSettings.imageBroadcastDelayMS != null) {

                testEventBroadcaster.startBroadcastingImage(this, config.testSettings.imageBroadcastIntervalMS,
                        config.testSettings.imageBroadcastDelayMS);

                intentBroadcaster.displayMessage("Automatically sending Images every " + Integer.toString(config.testSettings.imageBroadcastIntervalMS) + " milliseconds.");
            }
        }
    }

    private void initializeLocationProvider() {

        // Initialize the location provider
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-init}
        // By default attempt to initialize the location system based on the assumption the Simulated and AndroidGPS DFUs are available
        java.io.File inputFile = new java.io.File(android.os.Environment.getExternalStorageDirectory(), "ataklite/LocationProviderManualSimulated.json");

        if (inputFile.exists()) {
            // If the simulated instruction file exists, use the simulated GPS
            mil.darpa.immortals.dfus.location.LocationProviderManualSimulated locationProviderManualSimulated = new mil.darpa.immortals.dfus.location.LocationProviderManualSimulated();
            locationProviderManualSimulated.initialize(this);
            this.locationProvider = locationProviderManualSimulated;
        } else {
            // Otherwise, use the internal one
            mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn locationProviderAndroidGPS = new mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn();
            locationProviderAndroidGPS.initialize(this);
            this.locationProvider = locationProviderAndroidGPS;
        }

        if (locationProvider != null) {
            String message = "Using '" + locationProvider.getClass().getSimpleName() + "' for location";
            intentBroadcaster.displayMessage(message);
        }
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd
    }

    private void initializeNetworkSenders() {

        // c0ntrolp0int - ImageSender
        // The ImageCoterizer has Bitmap and Location pipes that it turns into a CoTMessage
        ImageCotifier imageCoterizer = new ImageCotifier(
                // Which the CotByter turns into bytes
                new CotByter(
                        // Which are passed to the network
                        networkDispatcher
                )
        );

        // The head provides a unified interface for the two input pipelines
        imageSender = new ObjectPipeMultiplexerHead<>(
                // It passes the filepath to a BitmapReader
                new BitmapReader(
                        // Which passes it to the input that takes bitmaps on the ImageCoterizer
                        imageCoterizer.input0
                ),
                // And passes the location straight through to the ImageCoterizer
                new Passthrough<Location>(imageCoterizer.input1)
        );

        // c0ntrolp0int - NetworkSender
        cotSender = new CotByter(
                networkDispatcher
        );
    }

    private void startTrackingLatestSA(final ATAKLiteConfig config) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (config.latestSABroadcastDelayMS != null && config.latestSABroadcastDelayMS > 0) {
                    try {
                        Thread.sleep(config.latestSABroadcastDelayMS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                boolean interrupted = false;

                final int sleepInterval = (config.latestSABroadcastIntervalMS == null ? 5000 : config.latestSABroadcastIntervalMS);

                while (!interrupted) {

                    mil.darpa.immortals.datatypes.Coordinates coordinates;


                    // Get the user's current location
                    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{coordinates = $BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-work}

                    if (locationProvider instanceof mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn) {
                        coordinates = ((mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn) locationProvider).getLastKnownLocation();
                    } else if (locationProvider instanceof mil.darpa.immortals.dfus.location.LocationProviderManualSimulated) {
                        coordinates = ((mil.darpa.immortals.dfus.location.LocationProviderManualSimulated) locationProvider).getLastKnownLocation();
                    } else {
                        coordinates = null;
                    }
                    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

                    if (coordinates != null) {
                        // TODO: This conversion should probably be a separately defined converter of sorts.
                        CoTMessage message = new CoTMessage(mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter.toLocation(coordinates), config.callsign);

                        // CO-work: 360469F1-8998-482F-8132-E51CB61B0753
                        cotSender.consume(message);
                        // CO-work-end

                        // Send to other interested parties (such as the UI) through an intent
                        intentBroadcaster.broadcastSelfLocationUpdate(coordinates);
                    }

                    try {
                        Thread.sleep(sleepInterval);
                        interrupted = Thread.interrupted();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
        };

        latestSAThread = new Thread(runnable);
        latestSAThread.start();
    }
}
