package mil.darpa.immortals.harness;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.harness.configuration.targets.ClientConfiguration;
import mil.darpa.immortals.harness.configuration.targets.ServerConfiguration;
import mil.darpa.immortals.ipc.StringMessageListener;
import mil.darpa.immortals.ipc.WebSocketClient;
import org.apache.commons.codec.binary.Base64;

import javax.websocket.DeploymentException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Stack;
import java.util.UUID;

/**
 * Created by awellman@bbn.com on 11/1/16.
 */
public class ScenarioConductor implements StringMessageListener {

    public static class ProcessRouter {

        private static final Stack<Process> runningProcesses = new Stack<>();

        private static final Thread shutdownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (runningProcesses) {
                    for (Process p : runningProcesses) {
                        p.destroy();
                    }
                }
            }
        });

        static {
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }

        public static Process startProcess(String... command) throws IOException {
            synchronized (runningProcesses) {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.inheritIO();
                Process p = pb.start();
                runningProcesses.add(p);
                return p;
            }
        }
    }

    private static final String executable = "scenarioconductor.py";

    @Override
    public void receiveMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void channelClosed() {
        System.out.println("CLOSED");

    }

    @Override
    public void channelOpened() {
        System.out.println("OPENED");

    }

    public static interface ScenarioRunnerListener {
        public void MessageReceived(String message);
    }

    private static final Gson gson = new GsonBuilder().create();

    private final ScenarioRunnerConfiguration configuration;

    public ScenarioConductor(ScenarioRunnerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void execute() {
        try {
            EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();
            String configurationJson = gson.toJson(configuration, ScenarioRunnerConfiguration.class);
            String config = Base64.encodeBase64String(configurationJson.getBytes());

            ProcessRouter.startProcess("python2.7",
                    ec.getScenarioComposeerPath().resolve(executable).toAbsolutePath().toString(),
                    "--port", Integer.toString(ec.getIpcPort(executable)));

            WebSocketClient wsc = new WebSocketClient(ec.getIpcPort(executable));
            wsc.setListener(this).start();

//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            wsc.sendMessage(gson.toJson(configuration));
//            wsc.sendMessage("{ \"value\": \"HELLO\"}");

        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        EnvironmentConfiguration.initializeDefaultEnvironmentConfiguration();

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.bandwidth = 1000000;
        ClientConfiguration cc = new ClientConfiguration();
        cc.count = 2;
        cc.imageBroadcastIntervalMS = 5000;
        cc.latestSABroadcastIntervalMS = 1000;

        ScenarioConfiguration scenarioConfiguration = new ScenarioConfiguration();
        scenarioConfiguration.clients.add(cc);
        scenarioConfiguration.server = serverConfiguration;
        scenarioConfiguration.sessionIdentifier = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);

        LinkedList<String> scenarioIdentifiers = new LinkedList<>();
        scenarioIdentifiers.add("client-test-location");
        scenarioIdentifiers.add("client-analysis-dynamic");
        scenarioIdentifiers.add("client-run-location");
        scenarioIdentifiers.add("client-analysis-static");
        scenarioIdentifiers.add("client-test-images");
        scenarioIdentifiers.add("client-run-images");

        ScenarioRunnerConfiguration src = ScenarioRunnerConfiguration.loadDefaultConfiguration(
                scenarioConfiguration,
                30,
                scenarioIdentifiers
        );

        ScenarioConductor sr = new ScenarioConductor(src);
        sr.execute();

        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
