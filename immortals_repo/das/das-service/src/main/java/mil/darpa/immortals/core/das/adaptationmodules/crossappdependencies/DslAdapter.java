package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by awellman@bbn.com on 6/26/18.
 */
public class DslAdapter {

    public static List<CipherConfiguration> parseDslOutput(@Nonnull Path bestFilepath, CipherConfiguration partialConfig) throws IOException {
        List<CipherConfiguration> configurations = new LinkedList<>();

        List<String> allLines = Files.readAllLines(bestFilepath);

        CipherConfiguration currentConfiguration = null;

        for (String line : allLines) {
            if (line.startsWith("Solution")) {
                currentConfiguration = partialConfig.clone();
                configurations.add(currentConfiguration);
            } else if (!line.startsWith("Search stopped")) {
                line = line.trim().replace(" :: Bool", "");

                String identifier = line.substring(0, line.indexOf(" "));
                boolean valid = line.substring(line.lastIndexOf(" ")).endsWith("True");
                currentConfiguration.setValue(identifier, valid);
            }
        }
        return configurations;
    }

    private static DslAdapter instance;

    private DslAdapter() {
    }

    public static synchronized DslAdapter getInstance() {
        if (instance == null) {
            instance = new DslAdapter();
        }
        return instance;
    }

    public synchronized List<CipherConfiguration> query(@Nonnull String adaptationIdentifier, @Nonnull CipherConfiguration partialConfig, Boolean clientServerJavax) throws Exception {
        Path dslPath = ImmortalsConfig.getInstance().dasService.getResourceDslPath();
        LinkedList<String> command = new LinkedList<>();
        command.add("stack");
        command.add("exec");
        command.add("resource-dsl");
        command.add("--");
        command.add("check");

        List<String> onValues = new LinkedList<>();
        List<String> offValues = new LinkedList<>(CipherConfiguration.algorithmOmissions);

        if (partialConfig.getCipherAlgorithm() != null) {
            onValues.add(partialConfig.getCipherAlgorithm());
        }
        if (partialConfig.getKeyLength() != null) {
            onValues.add("KSZ" + Integer.toString(partialConfig.getKeyLength()));
        }
        if (partialConfig.getPaddingScheme() != null) {
            onValues.add(partialConfig.getPaddingScheme());
        }
        if (partialConfig.getCipherChainingMode() != null) {
            onValues.add(partialConfig.getCipherChainingMode());
        }

        if (clientServerJavax != null) {
            if (clientServerJavax) {
                onValues.add("ClientJavax");
                onValues.add("ServerJavax");
            } else {
                offValues.add("ClientJavax");
                offValues.add("ServerJavax");
            }
        }

        if (onValues.size() > 0) {
            command.add("--on");
            command.add("[\"" + String.join("\",\"", onValues) + "\"]");
        }

        if (!partialConfig.isUseServerAESNI()) {
            offValues.add("ServerAESNI");
        }
        if (!partialConfig.isUseServerSEP()) {
            offValues.add("ServerSEP");
        }
        if (!partialConfig.isUseClientAESNI()) {
            offValues.add("ClientAESNI");
        }
        if (!partialConfig.isUseClientSEP()) {
            offValues.add("ClientSEP");
        }

        // EHACK: Not this hack. Android 21 does not support higher keys.

        if (offValues.size() > 0) {
            command.add("--off");
            command.add("[\"" + String.join("\",\"", offValues) + "\"]");
        }

        ImmortalsProcessBuilder pb = new ImmortalsProcessBuilder(adaptationIdentifier, "resourceDsl");
        pb.command(command);
        pb.directory(dslPath.toFile());
        Process p = pb.start();
        p.waitFor(60000, TimeUnit.MILLISECONDS);

        if (p.exitValue() != 0) {
            throw new RuntimeException("The DSL execution returned with a non-zero exit status of " + Integer.toString(p.exitValue()) + "!");
        }

        return parseDslOutput(ImmortalsConfig.getInstance().dasService.getResourceDslPath().resolve("outbox/best.txt"), partialConfig);
    }

    public static void main(String[] args) {
        try {
            CipherConfiguration partialConfiguration = new CipherConfiguration(true, true, true, true,
                    "AES", 16, null, null);
            
            String adaptationIdentifier = "TestMain";

            DslAdapter da = DslAdapter.getInstance();
            List<CipherConfiguration> javaxConfigs = da.query(adaptationIdentifier, partialConfiguration, true);
            List<CipherConfiguration> bouncyCastleConfigs = da.query(adaptationIdentifier, partialConfiguration, false);
            List<CipherConfiguration> results = new LinkedList<>();
            results.addAll(javaxConfigs);
            results.addAll(bouncyCastleConfigs);
            Random r = new Random(34531254235412L);
            Collections.shuffle(results, r);

            for (CipherConfiguration cc : results) {
                System.out.println(
                        "KEYLEN=" + cc.getKeyLength() +
                        ", ALG=" + cc.getCipherAlgorithm() +
                                ", PADDING=" + cc.getPaddingScheme() +
                                
                                ", MODE=" + cc.getCipherChainingMode() + 
                                ", CJX=" + cc.isClientJavax() +
                                ", SJX=" + cc.isServerJavax()
                );
            }
            System.out.println("MEH");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
