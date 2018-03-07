package com.bbn.ataklite;


import com.bbn.ataklite.service.SACommunicationService;
import com.bbn.ataklite.service.SAIntentCLIReceiver;
import com.bbn.ataklite.service.SAIntentReceiverInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CLITAK {

    public SACommunicationService saCommunicationService;

    public SAIntentReceiverInterface cliListener;
    
    private Logger logger = LoggerFactory.getLogger(CLITAK.class);

    public void initialize() {
        initialize(ATAKLiteConfig.loadConfig(), null);
    }

    public void initialize(@Nonnull ATAKLiteConfig atakLiteConfig, @Nullable AnalyticsEndpointInterface analyticsEndpointInterface) {
        final ATAKLiteConfig config = atakLiteConfig;
        
        saCommunicationService = new SACommunicationService(config);

        initLogging(config, analyticsEndpointInterface);

        Analytics.log(Analytics.newEvent(AnalyticsEventType.ClientStart, Analytics.getOwnSourceIdentifier(), "CLITAK"));

        cliListener = new SAIntentCLIReceiver();
        saCommunicationService.addEventListener(cliListener);
        
    }
    
    public void addEventListener(@Nonnull SAIntentReceiverInterface listener) {
        saCommunicationService.addEventListener(listener);
    }
    
    public void removeEventListener(@Nonnull SAIntentReceiverInterface listener) {
        saCommunicationService.removeEventListener(listener);
    }
    
    public void start() {
        saCommunicationService.start();
    }

    private void initLogging(@Nonnull ATAKLiteConfig config, @Nullable AnalyticsEndpointInterface analyticsEndpointInterface) {
        if (config.analyticsConfig == null ||
                config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.DEFAULT ||
                config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.STDOUT) {
            Analytics.initializeEndpoint(new AnalyticsEndpointInterface() {

                Gson gson = new GsonBuilder().create();

                @Override
                public void log(AnalyticsEvent e) {
                    logger.info("ImmortalsAnalytics" + gson.toJson(e));
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

        } else if (config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.LOCAL_JSON_CONSUMER) {
            if (analyticsEndpointInterface == null) {
                throw new RuntimeException("Cannot use a local json consumer if none is provided!");
            }
            Analytics.initializeEndpoint(analyticsEndpointInterface);

//        } else if (config.analyticsConfig.target == ATAKLiteConfig.AnalyticsTarget.NET_LOG4J) {
//            if (config.analyticsConfig.port <= 0 || config.analyticsConfig.url == null || config.analyticsConfig.url.equals("")) {
//                throw new RuntimeException("NET_LOG4J logging configured but the url and port are not configured!");
//            }
//            Analytics.initializeEndpoint(new Log4jAnalyticsEndpoint(config.analyticsConfig.url, config.analyticsConfig.port));

        } else {
            throw new RuntimeException("Unexpected analytics backend '" + config.analyticsConfig.target + "Specified!");
        }

        if (config.analyticsConfig == null) {
            Analytics.setVerbosity(AnalyticsVerbosity.Metadata);
        } else {
            Analytics.setVerbosity(config.analyticsConfig.verbosity);
        }

        Analytics.setSourceIdentifier(config.callsign);
    }
    
    public CLITAK getInstance() {
        return null;
    }
}
