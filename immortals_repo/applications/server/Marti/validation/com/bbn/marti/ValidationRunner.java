package com.bbn.marti;

import com.bbn.ataklite.ATAKLiteConfig;
import com.bbn.ataklite.CLITAK;
import com.bbn.marti.service.MartiMain;
import com.google.gson.Gson;
import mil.darpa.immortals.analytics.validators.ValidatorManager;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.api.applications.AnalyticsTarget;
import mil.darpa.immortals.core.api.applications.MartiConfig;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.validation.Validators;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by awellman@bbn.com on 8/3/17.
 */
public class ValidationRunner {

    private enum ValidationRunnerState {
        NOT_STARTED,
        STARTED,
        TERMINATING,
        FINISHED
    }


    public static class FailureListener implements ITestListener {

        @Override
        public void onTestStart(ITestResult result) {

        }

        @Override
        public void onTestSuccess(ITestResult result) {

        }

        @Override
        public void onTestFailure(ITestResult tr) {
            System.err.println("####HALTHALTHALT!");
            ValidationRunner.getInstance().terminate();
        }

        @Override
        public void onTestSkipped(ITestResult result) {

        }

        @Override
        public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

        }

        @Override
        public void onStart(ITestContext context) {

        }

        @Override
        public void onFinish(ITestContext context) {

        }
    }

    private long startTimeMS;

    private long timeoutMS = 5000;

    private final List<String> validatorIdentifiers;

    private ValidationResults results;

    private final Gson gson = new Gson();

    private final Path rootStorageDirectory;

    private final Path martiStorageDirectory;

    private ValidationRunnerState state = ValidationRunnerState.NOT_STARTED;

    private static final Set<Thread> threads = new HashSet<>();

    private static ValidationRunner instance;

    public static synchronized ValidationRunner getInstance() {
        if (instance == null) {
            String[] validators = {
                    Validators.CLIENT_LOCATION_SHARE.identifier,
                    Validators.CLIENT_IMAGE_SHARE.identifier
            };
            Path tmpPath = Paths.get(System.getProperty("java.io.tmpdir"));
            instance = new ValidationRunner(tmpPath, validators);
        }
        return instance;
    }

    public Path getMartiStorageDirectory() {
        return martiStorageDirectory;
    }

    public long getStartTimeMS() {
        return startTimeMS;
    }

    public long getTimeoutMS() {
        return timeoutMS;
    }

    private void setValidationResults(ValidationResults results) {
        this.results = results;
    }

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
        System.err.println("ERROR: " + e.getMessage());
        e.printStackTrace();
        Assert.fail("EXCEPTION: " + e.getMessage());
        ValidationRunner.getInstance().terminate();
    };

    private MartiConfig getServerConfig() {
        InputStream is = Tests.class.getClassLoader().getResourceAsStream("Marti-Config.json");
        MartiConfig config = gson.fromJson(new InputStreamReader(is), MartiConfig.class);

        config.storageDirectory = martiStorageDirectory.toString();

        config.analyticsConfig.target = AnalyticsTarget.LOCAL_JSON_CONSUMER;

        return config;
    }

    private ATAKLiteConfig getClientConfig(String identifier) {
        InputStream is = Tests.class.getClassLoader().getResourceAsStream("ATAKLite-Config.json");
        ATAKLiteConfig config = gson.fromJson(new InputStreamReader(is), ATAKLiteConfig.class);

        Path clientDir = rootStorageDirectory.resolve("clitak" + identifier + "StorageDirectory").toAbsolutePath();
        //noinspection ResultOfMethodCallIgnored
        clientDir.toFile().mkdir();
        config.storageDirectory = clientDir.toString();

        try {
            if (!clientDir.resolve("sample_image.jpg").toFile().exists()) {
                Files.copy(Tests.class.getClassLoader().getResourceAsStream("sample_image.jpg"),
                        clientDir.resolve("sample_image.jpg"));
            }


            if (!rootStorageDirectory.resolve("env.json").toFile().exists()) {
                Files.copy(ValidationRunner.class.getClassLoader().getResourceAsStream("env.json"),
                        rootStorageDirectory.resolve("env.json"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        config.callsign = identifier;
        config.analyticsConfig.target = ATAKLiteConfig.AnalyticsTarget.LOCAL_JSON_CONSUMER;
        config.latestSABroadcastIntervalMS = 1000;
        config.imageBroadcastIntervalMS = 1000;
        config.imageBroadcastDelayMS = 0;
        config.latestSABroadcastDelayMS = 0;

        return config;
    }

    @Nullable
    private MartiMain startServer(@Nullable final MartiConfig config) {
        try {
            final MartiMain mm = new MartiMain();

            // This Seemingly redundant way of starting it allows each new client to have its own thread/Analytics instance
            Thread t = new Thread(() -> {
                try {
                    mm.loadConfig0(config);
                    mm.initializeServices1();
                    mm.assemblePipes2();
                    mm.setupRmi3();
                    mm.startServices4();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            t.setDaemon(true);
            threads.add(t);
            t.start();
            t.join();
            return mm;
        } catch (InterruptedException e) {
            System.out.println("#HaltServer: " + Long.toString(System.currentTimeMillis()));
            if (state == ValidationRunnerState.TERMINATING) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        } finally {

        }
    }

    private CLITAK startClient(@Nonnull final ATAKLiteConfig config, @Nonnull final AnalyticsEndpointInterface analyticsEndpoint) {
        try {
            final CLITAK tak = new CLITAK();

            // This Seemingly redundant way of starting it allows each new client to have its own thread/Analytics instance
            Thread t = new Thread(() -> {
                tak.initialize(config, analyticsEndpoint);
                tak.start();
            });
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            t.setDaemon(true);
            threads.add(t);
            t.start();
            t.join();
            return tak;
        } catch (InterruptedException e) {
            System.out.println("#HaltClient: " + Long.toString(System.currentTimeMillis()));
            if (state == ValidationRunnerState.TERMINATING) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }

    }

    private ValidationRunner(Path storageDirectory, String... validatorIdentifiers) {
        System.setProperty("java.awt.headless", "true");

        this.rootStorageDirectory = storageDirectory.toAbsolutePath();
        this.martiStorageDirectory = storageDirectory.resolve("martiStorageDirectory").toAbsolutePath();
        //noinspection ResultOfMethodCallIgnored
        this.martiStorageDirectory.toFile().mkdir();
        this.validatorIdentifiers = Arrays.asList(validatorIdentifiers);
    }

    public TestResult execute(Validators validator) {
        ValidationResults results = execute();
        return results.results.stream().filter(x -> x.validatorIdentifier.equals(validator.identifier)).findAny().get();
    }

    public ValidationResults execute() {
        synchronized (threads) {
            if (state == ValidationRunnerState.NOT_STARTED) {
                state = ValidationRunnerState.STARTED;


                final CountDownLatch durationLatch = new CountDownLatch(1);

                String[] clientIdentifiers = {"Client0", "Client1", "Client2"};
                ValidatorManager validatorManager = new ValidatorManager(Arrays.asList(clientIdentifiers), validatorIdentifiers, true);

                System.out.println("STARTING: " + Long.toString(System.currentTimeMillis()).substring(0, 10));

                final ValidationRunner vr = this;
                Analytics.initializeEndpoint(new AnalyticsEndpointInterface() {
                    @Override
                    public void start() {

                    }

                    @Override
                    public void log(AnalyticsEvent analyticsEvent) {
                        if (analyticsEvent.type == AnalyticsEventType.Tooling_ValidationFinished) {
                            ValidationResults results = gson.fromJson(analyticsEvent.data, ValidationResults.class);
                            vr.setValidationResults(results);
                            durationLatch.countDown();
                        }
                    }

                    @Override
                    public void shutdown() {

                    }
                });

                MartiConfig serverConfig = getServerConfig();

                ATAKLiteConfig clientConfig0 = getClientConfig(clientIdentifiers[0]);
                ATAKLiteConfig clientConfig1 = getClientConfig(clientIdentifiers[1]);
                ATAKLiteConfig clientConfig2 = getClientConfig(clientIdentifiers[2]);

                validatorManager.start();
                startTimeMS = System.currentTimeMillis();

                startServer(serverConfig);
                startClient(clientConfig0, validatorManager);
                startClient(clientConfig1, validatorManager);
                startClient(clientConfig2, validatorManager);

                System.out.println("#WAITING: " + Long.toString(System.currentTimeMillis()).substring(0, 10));
                String timeout = System.getenv("VALIDATION_SHORT_TIMEOUT_MS");
                try {
                    if (timeout != null && !timeout.equals("")) {
                        timeoutMS = Long.valueOf(timeout);
                        durationLatch.await(timeoutMS, TimeUnit.MILLISECONDS);
                    } else {
                        durationLatch.await(5000, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    System.out.println("#HaltWaiting: " + Long.toString(System.currentTimeMillis()));
                    if (state != ValidationRunnerState.TERMINATING) {
                        throw new RuntimeException(e);
                    }
                }

                validatorManager.shutdown();
                results = validatorManager.getResults();
                state = ValidationRunnerState.FINISHED;
                System.out.println("#ResultsGotten: " + Long.toString(System.currentTimeMillis()).substring(0, 10));
            }
        }

        return results;
    }

    public void terminate() {
        synchronized (threads) {
            System.out.println("#HALT: " + Long.toString(System.currentTimeMillis()).substring(0, 10));
            state = ValidationRunnerState.TERMINATING;
            for (Thread t : threads) {
                t.interrupt();
            }
        }

    }
}
