package com.bbn.marti.service;

import com.bbn.marti.immortals.SubmissionServiceFunctionalUnit;
import com.bbn.marti.immortals.converters.TcpInitializationDataToTcpSocketServer;
import com.bbn.marti.immortals.pipelines.TcpSocketServerToCotServerChannel;
import mil.darpa.immortals.analytics.protocols.log4j.Log4jAnalyticsEndpoint;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import mil.darpa.immortals.core.api.ApiHelper;
import mil.darpa.immortals.core.api.applications.AnalyticsConfig;
import mil.darpa.immortals.core.api.applications.AnalyticsTarget;
import mil.darpa.immortals.core.api.applications.MartiConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MartiMain {

    public static final String DEFAULT_CONFIG_FILE = "Marti-Config.json";
    
    public static int defaultPort = 3334;

    private SubscriptionManager subMgr;
    private CoreConfig coreConfig;
    private CoreMonitor monitor;
    private BrokerService brokerService;
    public static MartiConfig martiConfig;

    private SubmissionServiceFunctionalUnit submissionService;
    
    public static MartiConfig getConfig() {
        return martiConfig;
    }

    CoreConfig.ReadTcpConfigurationData readTcpConfigurationData;
    
    public void initializeAnalytics(@Nullable AnalyticsEndpointInterface analyticsEndpointInterface) {
        AnalyticsConfig analyticsConfig = martiConfig.analyticsConfig;
        if (analyticsConfig == null || analyticsConfig.target == AnalyticsTarget.DEFAULT ||
                analyticsConfig.target == AnalyticsTarget.STDOUT) {
            Analytics.initializeEndpoint(new AnalyticsEndpointInterface() {

                @Override
                public void log(AnalyticsEvent e) {
                    System.out.println("ImmortalsAnalytics: " + ApiHelper.getSharedGson().toJson(e));
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

        } else if (analyticsConfig.target == AnalyticsTarget.LOCAL_JSON_CONSUMER) {
            if (analyticsEndpointInterface == null) {
                throw new RuntimeException("Cannot use a local json consumer if none is provided!");
            }
            Analytics.initializeEndpoint(analyticsEndpointInterface);

        } else if (analyticsConfig.target == AnalyticsTarget.NET_LOG4J) {
            if (analyticsConfig.port <= 0 || analyticsConfig.url == null || analyticsConfig.url.equals("")) {
                throw new RuntimeException("NET_LOG4J logging configured but the url and port are not configured!");
            }
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Analytics.initializeEndpoint(new Log4jAnalyticsEndpoint(analyticsConfig.url, analyticsConfig.port));
                }
            });
            Analytics.registerThread(t);
            t.start();

        } else {
            throw new RuntimeException("Unexpected analytics backend '" + analyticsConfig.target + "Specified!");
        }

        if (analyticsConfig == null) {
            Analytics.setVerbosity(AnalyticsVerbosity.Metadata);
        } else {
            Analytics.setVerbosity(analyticsConfig.verbosity);
        }

        Analytics.setSourceIdentifier("MartiRouter");
    }

    public void loadConfig0(@Nullable MartiConfig config) throws IOException {
        if (config == null) {
            File inputFile = new File(DEFAULT_CONFIG_FILE);

            if (inputFile.exists()) {
                FileReader fr = new FileReader(inputFile);
                config = ApiHelper.getSharedGson().fromJson(fr, MartiConfig.class);
            } else {
                throw new RuntimeException("Configuration file must exist at location '" + inputFile.getAbsolutePath() + "'!");
            }
        }
        
        this.martiConfig = config;
        this.coreConfig = CoreConfig.getInstance();
        monitor = CoreMonitor.getInstance();
        this.martiConfig = config;
        
        File storage = new File(config.storageDirectory);
        if (!storage.exists()) {
            storage.mkdir();
        }
    }

    public void assemblePipes2() {
        // New Configuration reader to set up the servers
        readTcpConfigurationData = coreConfig.getNewReadTcpConfigurationData();


        // When the configuration is read in, pass it to the TcpSocketServer factory to create a new stcp server
        readTcpConfigurationData.setNext(
                // A factory to produce TcpSocketServers
                new TcpInitializationDataToTcpSocketServer(
                        // When a server socket is produced, produce a CotServerChannel from it
                        new TcpSocketServerToCotServerChannel(
                                // Then send that CotServerChannel to the SubmissionService
                                submissionService.addCotServerChannelToService()
                        )
                )
        );
    }

    public void setupRmi3() throws RemoteException {
        Registry localRegistry;
        int port = defaultPort;
        if (CoreConfig.getInstance().getAttributeInteger("network.rmiPort") != null) {
            port = CoreConfig.getInstance().getAttributeInteger("network.rmiPort");
        }
        try {
            localRegistry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            System.err.println("Couldn't set up RMI Registery on port " + port + "; trying again on random port");
            localRegistry = LocateRegistry.createRegistry(0);
        }

        localRegistry.rebind("SubMgr", subMgr);
        localRegistry.rebind("CoreConfig", coreConfig);
        localRegistry.rebind("CoreMonitor", monitor);
    }

    public void initializeServices1() throws IOException {
        // Initialize analytics
        // ???
        
        // New SubscriptionManager
        subMgr = new SubscriptionManager();

        // New SubmissionService
        submissionService = new SubmissionServiceFunctionalUnit(subMgr);

        // New Configuration reader to set up the servers
        readTcpConfigurationData = coreConfig.getNewReadTcpConfigurationData();

        brokerService = new BrokerService(subMgr);

        submissionService.addConsumer(brokerService);
    }

    public void startServices4() {
        // Trigger the pipeline by reading the configuration data, which sets up the servers
        readTcpConfigurationData.consume(null);
        submissionService.startService();
        brokerService.startService();
    }

    public static void main(String[] args) {
        try {
            MartiMain mm = new MartiMain();
            mm.loadConfig0(null);
            mm.initializeAnalytics(null);
            mm.initializeServices1();
            mm.assemblePipes2();
            mm.setupRmi3();
            mm.startServices4();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
