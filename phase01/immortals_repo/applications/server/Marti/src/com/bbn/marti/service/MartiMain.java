package com.bbn.marti.service;

import com.bbn.marti.immortals.SubmissionServiceFunctionalUnit;
import com.bbn.marti.immortals.converters.TcpInitializationDataToTcpSocketServer;
import com.bbn.marti.immortals.pipelines.TcpSocketServerToCotServerChannel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MartiMain {

    public static int defaultPort = 3334;

    public static void main(String[] args) {
        if (args.length > 0) {
            CoreConfig.CONFIG_FILE = args[0];
        }
        CoreConfig config = CoreConfig.getInstance(); // force a load of config files
        CoreMonitor monitor = CoreMonitor.getInstance();

        SubscriptionManager subMgr;

        try {
            subMgr = new SubscriptionManager();

            // New SubmissionService
            SubmissionServiceFunctionalUnit ss = new SubmissionServiceFunctionalUnit(subMgr);

            // New Configuration reader to set up the servers
            CoreConfig.ReadTcpConfigurationData readTcpConfigurationData = config.getNewReadTcpConfigurationData();

            // A factory to produce TcpSocketServers
            TcpInitializationDataToTcpSocketServer serverConverter = new TcpInitializationDataToTcpSocketServer();

            // When a server socket is produced, produce a CotServerChannel from it
            TcpSocketServerToCotServerChannel tcpSocketServerToCotServerChannel = new TcpSocketServerToCotServerChannel();

            //// Construct the general pipeline for server initialization

            // When the configuration is read in, pass it to the TcpSocketServer factory to create a new stcp server
            readTcpConfigurationData.addSuccessor(serverConverter);

            // When the server is created, convert it to a CotServerChannel
            serverConverter.addSuccessor(tcpSocketServerToCotServerChannel);

            // Then send that CotServerChannel to the SubmissionService
            tcpSocketServerToCotServerChannel.addSuccessor(ss.addCotServerChannelToService());

            // Trigger the pipeline by reading the configuration data, which sets up the servers
            readTcpConfigurationData.init();

            BrokerService bs = new BrokerService(subMgr);
// TODO: IMMORTALS: Reenable
//			RepositoryService rs = new RepositoryService();
            // Submission -> Broker
            ss.addConsumer(bs);

// TODO: IMMORTALS: Reenable
//			// Submission -> Repository
//			ss.addConsumer(rs);
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
            localRegistry.rebind("CoreConfig", config);
            localRegistry.rebind("CoreMonitor", monitor);

            ss.startService();
            bs.startService();
// TODO: IMMORTALS: Reenable
//			rs.startService();

        } catch (RemoteException e) {
            System.err.println("Couldn't set up RMI Registery on port " + defaultPort);
        }


    }

}
