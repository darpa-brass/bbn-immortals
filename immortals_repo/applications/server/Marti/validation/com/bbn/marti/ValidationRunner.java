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
import mil.darpa.immortals.core.api.ApiHelper;
import mil.darpa.immortals.core.api.applications.AnalyticsTarget;
import mil.darpa.immortals.core.api.applications.MartiConfig;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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

    public long startTimeMS;

    public long timeoutMS = 5000;

    private final List<String> validatorIdentifiers;

    private ValidationResults results;

    private final Gson gson = new Gson();

    public String storageDirectory;

    private void setValidationResults(ValidationResults results) {
        this.results = results;
    }

    public MartiConfig getServerConfig() throws IOException {
        URL url = Tests.class.getClassLoader().getResource("Marti-Config.json");
        MartiConfig config = ApiHelper.getSharedGson().fromJson(new FileReader(url.getFile()), MartiConfig.class);
        config.analyticsConfig.target = AnalyticsTarget.LOCAL_JSON_CONSUMER;
        config.storageDirectory = storageDirectory;

        return config;
    }

    private ATAKLiteConfig getClientConfig(String identifier) {
        URL url = Tests.class.getClassLoader().getResource("ATAKLite-Config.json");
        ATAKLiteConfig config = ATAKLiteConfig.loadConfig(url.getFile());

        config.callsign = identifier;
        config.analyticsConfig.target = ATAKLiteConfig.AnalyticsTarget.LOCAL_JSON_CONSUMER;
        config.latestSABroadcastIntervalMS = 1000;
        config.imageBroadcastIntervalMS = 1000;
        config.imageBroadcastDelayMS = 0;
        config.latestSABroadcastDelayMS = 0;

        return config;
    }

    public MartiMain startServer(@Nullable MartiConfig config) {
        try {
            MartiMain mm = new MartiMain();

            // This Seemingly redundant way of starting it allows each new client to have its own thread/Analytics instance
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        mm.loadConfig0(config);
                        mm.initializeServices1();
                        mm.assemblePipes2();
                        mm.setupRmi3();
                        mm.startServices4();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            t.start();
            t.join();
            return mm;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private CLITAK startClient(@Nullable ATAKLiteConfig config, AnalyticsEndpointInterface analyticsEndpoint) {
        try {
            CLITAK tak = new CLITAK();

            // This Seemingly redundant way of starting it allows each new client to have its own thread/Analytics instance
            Thread t = new Thread(new Runnable() {
                public void run() {
                    tak.initialize(config, analyticsEndpoint);
                    tak.start();
                }
            });
            t.start();
            t.join();
            return tak;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public ValidationRunner(String... validatorIdentifiers) {
        System.setProperty("java.awt.headless", "true");
        this.storageDirectory = Long.toString(System.currentTimeMillis());
        this.validatorIdentifiers = Arrays.asList(validatorIdentifiers);
    }

    public ValidationResults execute() {
        startTimeMS = System.currentTimeMillis();
        try {
            CountDownLatch latch = new CountDownLatch(1);
            ValidationRunner vr = this;
            Analytics.initializeEndpoint(new AnalyticsEndpointInterface() {
                @Override
                public void start() {

                }

                @Override
                public void log(AnalyticsEvent analyticsEvent) {
                    if (analyticsEvent.type == AnalyticsEventType.Tooling_ValidationFinished) {
                        ValidationResults results = gson.fromJson(analyticsEvent.data, ValidationResults.class);
                        vr.setValidationResults(results);
                        latch.countDown();
                    }
                }

                @Override
                public void shutdown() {

                }
            });

            MartiConfig serverConfig = getServerConfig();

            ATAKLiteConfig clientConfig0 = getClientConfig("Client0");
            ATAKLiteConfig clientConfig1 = getClientConfig("Client1");
            ATAKLiteConfig clientConfig2 = getClientConfig("Client2");

            Set<String> clientIdentifiers = new HashSet<>();
            clientIdentifiers.add(clientConfig0.callsign);
            clientIdentifiers.add(clientConfig1.callsign);
            clientIdentifiers.add(clientConfig2.callsign);

            ValidatorManager vm = new ValidatorManager(clientIdentifiers, validatorIdentifiers, true);

            vm.start();

            MartiMain mm = startServer(serverConfig);
            CLITAK tak0 = startClient(clientConfig0, vm);
            CLITAK tak1 = startClient(clientConfig1, vm);
            CLITAK tak2 = startClient(clientConfig2, vm);

            String timeout = System.getenv("VALIDATION_SHORT_TIMEOUT_MS");
            if (timeout != null && !timeout.equals("")) {
                timeoutMS = Long.valueOf(timeout);
                latch.await(timeoutMS, TimeUnit.MILLISECONDS);
            } else {
                latch.await(5, TimeUnit.SECONDS);
            }
            if (results == null) {
                vm.shutdown();
                return vm.getResults();
            }
            return results;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
