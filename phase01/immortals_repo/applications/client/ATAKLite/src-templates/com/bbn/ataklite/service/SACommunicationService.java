package com.bbn.ataklite.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import com.bbn.ataklite.ATAKLite;
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
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.Coordinates;
import mil.darpa.immortals.dfus.images.BitmapReader;
import mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter;

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
$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-declaration

    private Dispatcher networkDispatcher = null;
    private SAIntentBroadcaster intentBroadcaster;

    Thread latestSAThread;
    Thread networkReadThread;

    private ObjectPipeMultiplexerHead<String, Location> imageSender;
    private ConsumingPipe<CoTMessage> cotSender;

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
$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-cleanup

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
        Analytics.log(Analytics.newEvent(AnalyticsEventType.MyImageSent, ATAKLite.getConfigInstance().callsign, CoordinateLocationConverter.toCoordinates(location)));
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
                        Coordinates coordinates = new Coordinates(
                                message.getLatitude(),
                                message.getLongitude(),
                                null,
                                null,
                                message.getTime(),
                                message.getHow()
                        );


                        byte[] imageData = message.getImageData();

                        if (imageData != null) {
                            Analytics.log(Analytics.newEvent(AnalyticsEventType.FieldImageReceived, message.getUid(), coordinates));

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
                            intentBroadcaster.broadcastImageUpdate(coordinates, message.getUid(), imagePath);

                        } else {
                            Analytics.log(Analytics.newEvent(AnalyticsEventType.FieldLocationUpdated, message.getUid(), coordinates));
                            intentBroadcaster.broadcastFieldUpdate(coordinates, message.getUid());
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
        if (config.imageBroadcastIntervalMS != null &&
                config.imageBroadcastIntervalMS > 0 &&
                config.imageBroadcastDelayMS != null) {

            testEventBroadcaster.startBroadcastingImage(this, config.imageBroadcastIntervalMS,
                    config.imageBroadcastDelayMS);

            intentBroadcaster.displayMessage("Automatically sending Images every " + Integer.toString(config.imageBroadcastIntervalMS) + " milliseconds.");
        }
    }

    private void initializeLocationProvider() {

        // Initialize the location provider
$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-init
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
coordinates = $BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-work

                    if (coordinates != null) {

                        Analytics.log(Analytics.newEvent(AnalyticsEventType.MyLocationProduced, Analytics.getOwnSourceIdentifier(), coordinates));


                        // TODO: This conversion should probably be a separately defined converter of sorts.
                        CoTMessage message = new CoTMessage(mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter.toLocation(coordinates), config.callsign);

                        // CO-work: 360469F1-8998-482F-8132-E51CB61B0753
                        cotSender.consume(message);
                        // CO-work-end

                        // Provide the image sender with the updated location
                        testEventBroadcaster.setCurrentLocation(coordinates);
                        
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
