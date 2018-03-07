package com.bbn.ataklite.service;

import com.bbn.ataklite.*;
import com.bbn.ataklite.net.Dispatcher;
import com.bbn.ataklite.testhelpers.TestEventBroadcaster;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.synthesis.ObjectPipeMultiplexerHead;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.Coordinates;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.dfus.images.BitmapReader;
import mil.darpa.immortals.dfus.location.JavaTemporaryLocationProviderManualSimulated;
import mil.darpa.immortals.javatypeconverters.CotDataCoordinateConverter;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SACommunicationService {

    // Location determination declaration
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-declaration}
    private JavaTemporaryLocationProviderManualSimulated locationProvider = new JavaTemporaryLocationProviderManualSimulated();
    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

    private Dispatcher networkDispatcher = null;
    private SAIntentBroadcaster intentBroadcaster;

    private Thread latestSAThread;

    private ObjectPipeMultiplexerHead<String, Coordinates> imageSender;
    private ConsumingPipe<Event> cotSender;
    
    private ATAKLiteConfig config;

    private final TestEventBroadcaster testEventBroadcaster;

    public synchronized void start() {


        // Load the config.
        if (config == null) {
            config = ATAKLiteConfig.loadConfig();
        }

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

        initializeNetworkSenders(config);

        // Start broadcasting latestSA
        startTrackingLatestSA(config);

        // Load any test settings
        loadTestSettings(config);
    }

    public void addEventListener(SAIntentReceiverInterface listener) {
        intentBroadcaster.addListener(listener);
    }
    
    public void removeEventListener(SAIntentReceiverInterface listener) {
        intentBroadcaster.removeListener(listener);
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        if (intent != null && intent.getAction() != null) {
//            Log.i(TAG, "Start called: " + intent.getAction());
//            onHandleIntent(intent);
//        } else {
//            System.err.println("SACommunicationService recieved empty intent or action identifier!");
//        }
//
//        return START_STICKY;
//    }

    public synchronized void stop() {
        // Clean up the location provider
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-cleanup}
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

    public ATAKLiteConfig getConfig() {
        return config;
    }

    public SACommunicationService(ATAKLiteConfig config) {
        this.config = config;
        testEventBroadcaster = new TestEventBroadcaster(this);
        // Load the intent broadcaster to communicate with the UI
        intentBroadcaster = new SAIntentBroadcaster();
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_SEND_IMAGE.equals(action)) {
////                final Location location = intent.getParcelableExtra(SAIntentReceiver.EXTRA_LOCATION);
////                final String imageFilepath = intent.getStringExtra(SAIntentReceiver.EXTRA_IMAGE_URL);
////                handleActionSendImage(imageFilepath, location);
//            } else if (ACTION_BROADCAST_FIELD_UPDATE.equals(action)) {
//                final Location location = intent.getParcelableExtra(SAIntentReceiver.EXTRA_LOCATION);
//                final String locationID = intent.getStringExtra(SAIntentReceiver.EXTRA_ORIGIN_ID);
//                intentBroadcaster.broadcastFieldUpdate(CoordinateLocationConverter.toCoordinates(location), locationID);
//
//            } else if (ACTION_BROADCAST_IMAGE_UPDATE.equals(action)) {
//                final Location location = intent.getParcelableExtra(SAIntentReceiver.EXTRA_LOCATION);
//                final String locationID = intent.getStringExtra(SAIntentReceiver.EXTRA_ORIGIN_ID);
//                final String imageUrl = intent.getStringExtra(SAIntentReceiver.EXTRA_IMAGE_URL);
//                intentBroadcaster.broadcastImageUpdate(CoordinateLocationConverter.toCoordinates(location), locationID, imageUrl);
//
//            }
//        }
//    }

    /**
     * Handle action SEND_IMAGE in the provided background thread with the provided parameters.
     */
    public void handleActionSendImage(String imageFilepath, Coordinates coordinates) {
        Analytics.log(Analytics.newEvent(AnalyticsEventType.MyImageSent, config.callsign, coordinates));
        imageSender.write(imageFilepath, coordinates);
        intentBroadcaster.broadcastSelfImageUpdate(coordinates, imageFilepath);
    }
    
    public void handleActionSendCoordinates(Coordinates coordinates) {
        Analytics.log(Analytics.newEvent(AnalyticsEventType.MyLocationProduced, Analytics.getOwnSourceIdentifier(), coordinates));

        Event message = CotDataCoordinateConverter.toEvent(coordinates, config.callsign);

        // CO-work: 360469F1-8998-482F-8132-E51CB61B0753
        cotSender.consume(message);
        // CO-work-end

        // Provide the image sender with the updated location
        testEventBroadcaster.setCurrentLocation(coordinates);

        // Send to other interested parties (such as the UI) through an intent
        intentBroadcaster.broadcastSelfLocationUpdate(coordinates);
        
    }
    
    void handleFieldCotEventReceived(Event message) {

        Coordinates coordinates = new Coordinates(
                message.getPoint().getLat().doubleValue(),
                message.getPoint().getLon().doubleValue(),
                null,
                null,
                message.getTime().toGregorianCalendar().getTimeInMillis(),
                message.getHow()
        );


        if (message.getDetail() != null && message.getDetail().getImage() != null && message.getDetail().getImage().getValue() != null) {
            byte[] imageData = Base64.decodeBase64(message.getDetail().getImage().getValue().getBytes());

            Analytics.log(Analytics.newEvent(AnalyticsEventType.FieldImageReceived, message.getUid(), coordinates));

            String imageName = message.getUid() + "-" + System.currentTimeMillis() + ".jpg";

            File imageFile = new File(config.storageDirectory, imageName);


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

    private void initializeDispatcher(String takServerHost, int portAsInt) {

        try {
            if (takServerHost != null & takServerHost.trim().length() > 0 && portAsInt > 0) {
                networkDispatcher = new Dispatcher(InetAddress.getByName(takServerHost), portAsInt);
            }

            // c0ntrolp0int - NetworkReceiver
            final ByteCotifier byteCotifyer = new ByteCotifier(
                    networkDispatcher
            );

            final Event message;

            Runnable r = new Runnable() {

                @Override
                public void run() {
                    Event message;

                    while ((message = byteCotifyer.produce()) != null) {
                        handleFieldCotEventReceived(message);
                    }
                }
            };

            Thread networkReadThread = new Thread(r);
            Analytics.registerThread(networkReadThread);
            networkReadThread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void loadTestSettings(ATAKLiteConfig config) {
        if (config.imageBroadcastIntervalMS != null &&
                config.imageBroadcastIntervalMS > 0 &&
                config.imageBroadcastDelayMS != null) {

            testEventBroadcaster.startBroadcastingImage(config.imageBroadcastIntervalMS,
                    config.imageBroadcastDelayMS);

            intentBroadcaster.displayMessage("Automatically sending Images every " + Integer.toString(config.imageBroadcastIntervalMS) + " milliseconds.");
        }
    }

    private void initializeLocationProvider() {
        // Initialize the location provider
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-init}
        locationProvider.initialize();
        // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd
    }

    private void initializeNetworkSenders(ATAKLiteConfig config) {
        // c0ntrolp0int - ImageSender
        // The ImageCoterizer has Bitmap and Location pipes that it turns into a CoTMessage
        JavaImageCotifier imageCoterizer = new JavaImageCotifier(
                // The user's callsign
                config.callsign,
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
                new Passthrough<Coordinates>(imageCoterizer.input1)
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

                    Coordinates coordinates;

                    // Get the user's current location
                    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceWith{coordinates = $BCC0A94D-C2B1-40AD-8056-E3DDBD46585E-work}
                    coordinates = locationProvider.getLastKnownLocation();
                    // 2B997763-CB0F-426B-88D2-4E5995E79A8A-replaceEnd

                    if (coordinates != null) {
                        handleActionSendCoordinates(coordinates);
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
        Analytics.registerThread(latestSAThread);
        latestSAThread.start();
    }
}
