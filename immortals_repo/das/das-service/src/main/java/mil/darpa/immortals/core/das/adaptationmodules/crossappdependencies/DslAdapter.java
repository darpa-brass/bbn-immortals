package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by awellman@bbn.com on 6/26/18.
 */
public class DslAdapter {

    private static final HashSet<String> aesDesBadPaddings = new HashSet<>(Arrays.asList(
            "ISO10126_2Padding",
            "ISO7816_4Padding",
            "PKCS7Padding",
            "TBCPadding",
            "X923Padding",
            "ZeroBytePadding"
    ));

    private static final HashSet<String> aesDesBadModes = new HashSet<>(Arrays.asList(
            "CBC",
            "CFB",
            "CTR",
            "ECB",
            "CTS",
            "OFB"
    ));

    private static final HashSet<String> rijndaelBadNoPaddingModes = new HashSet<>(Arrays.asList(
            "CFB",
            "CTR",
            "OFB"
    ));

    private static final HashSet<String> rijndaelBadNoPkcs5Modes = new HashSet<>(Arrays.asList(
            "CBC",
            "CFB",
            "CTR",
            "ECB",
            "OFB"
    ));

    public boolean isCombinationValid(CipherConfiguration cc) {
        if (cc.isServerJavax() || cc.isClientJavax()) {
            if (cc.getCipherAlgorithm().equals("AES")) {

                if ("CTS".equals(cc.getCipherChainingMode()) && "PKCS5Padding".equals(cc.getPaddingScheme())) {
                    return false;

                } else if (aesDesBadModes.contains(cc.getCipherChainingMode())) {
                    if (aesDesBadPaddings.contains(cc.getPaddingScheme())) {
                        return false;
                    }
                }

            } else if (cc.getCipherAlgorithm().equals("DES")) {
                if ("CTS".equals(cc.getCipherChainingMode()) && "NoPadding".equals(cc.getPaddingScheme())) {
                    return false;
                } else if (aesDesBadModes.contains(cc.getCipherChainingMode())) {
                    if (aesDesBadPaddings.contains(cc.getPaddingScheme())) {
                        return false;
                    }
                }
            } else if (cc.getCipherAlgorithm().equals("Rijndael")) {
                if (cc.getPaddingScheme().equals("PKCS5Padding")) {
                    if (rijndaelBadNoPkcs5Modes.contains(cc.getCipherChainingMode())) {
                        return false;
                    }
                } else if (cc.getPaddingScheme().equals("NoPadding")) {
                    if (rijndaelBadNoPaddingModes.contains(cc.getCipherChainingMode())) {
                        return false;
                    }
                }
            }
        } else if (!cc.isServerJavax() || !cc.isClientJavax()) {
            if ("AES".equals(cc.getCipherAlgorithm()) || "DES".equals(cc.getCipherAlgorithm()) || "Rijndael".equals(cc.getCipherAlgorithm())) {
                if ("NoPadding".equals(cc.getPaddingScheme()) && "PGPCFBBlock".equals(cc.getCipherChainingMode())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static final Logger logger = LoggerFactory.getLogger(DslAdapter.class);

    public static List<CipherConfiguration> parseDslOutput(@Nonnull Path bestFilepath, CipherConfiguration partialConfig) throws Exception {
        List<CipherConfiguration> configurations = new LinkedList<>();

        List<String> allLines = Files.readAllLines(bestFilepath);

        CipherConfiguration currentConfiguration = null;

        if ("No solutions found.".equals(allLines.get(0))) {
            return new LinkedList<>();
        }

        for (String line : allLines) {
            if (line.startsWith("Solution")) {
                if (currentConfiguration != null) {
                    currentConfiguration.finalize(partialConfig);
                    configurations.add(currentConfiguration);
                }
                currentConfiguration = partialConfig.clone();

            } else if (!line.startsWith("Search stopped") && !line.startsWith("Found")) {
                line = line.trim().replace(" :: Bool", "");

                String identifier = line.substring(0, line.indexOf(" "));
                boolean valid = line.substring(line.lastIndexOf(" ")).endsWith("True");
                currentConfiguration.setValue(identifier, valid);
            } else {
                if (currentConfiguration != null) {
                    currentConfiguration.finalize(partialConfig);
                    configurations.add(currentConfiguration);
                }
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

    public CipherConfiguration querySingleSolution(@Nonnull String adaptationIdentifier, @Nonnull CipherConfiguration partialConfiguration) throws Exception {
        // Feed it to the DSL and get back complete candidate configurations
        DslAdapter da = DslAdapter.getInstance();
        List<CipherConfiguration> configs = new LinkedList<>();
        
        List<CipherConfiguration> javaxConfigs = da.query(adaptationIdentifier, partialConfiguration, true, false);
        configs.addAll(javaxConfigs);
        
        List<CipherConfiguration> javaxConfigsNoPadding = da.query(adaptationIdentifier, partialConfiguration, true, true);
        configs.addAll(javaxConfigsNoPadding);
        
        List<CipherConfiguration> bouncyCastleConfigs = da.query(adaptationIdentifier, partialConfiguration, false, false);
        configs.addAll(bouncyCastleConfigs);
        
        List<CipherConfiguration> bouncyCastleConfigsNoPadding = da.query(adaptationIdentifier, partialConfiguration, false, true);
        configs.addAll(bouncyCastleConfigsNoPadding);

        Random r = new Random(34531244354312L);
        Collections.shuffle(configs, r);

        CipherConfiguration cc = null;

        if (configs.size() == 0) {
            String msg = "No solution found: " + partialConfiguration.toString();
            logger.info(msg);
//            throw new RuntimeException(msg);
        } else {
            for (CipherConfiguration candidate : configs) {
                if (isCombinationValid(candidate)) {
                    cc = candidate;
                    logger.info("Solution found: " + cc.toString());
                    break;
                }
            }
        }
        return cc;
    }

    private synchronized List<CipherConfiguration> query(@Nonnull String adaptationIdentifier, @Nonnull CipherConfiguration partialConfig, boolean clientServerJavax, boolean noPadding) throws Exception {
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

        if (clientServerJavax) {
            onValues.add("ClientJavax");
            onValues.add("ServerJavax");
        } else {
            offValues.add("ClientJavax");
            offValues.add("ServerJavax");
        }

        if (noPadding) {
            onValues.add("NoPadding");
            offValues.add("CBC");
//            onValues.add("CFB");
//            onValues.add("CTR");
            offValues.add("ECB");
//            onValues.add("CTS");
//            onValues.add("OFB");
//            onValues.add("OpenPGPCFB");
            offValues.add("PGPCFBBlock");
//            onValues.add("SICBlock");

        } else {
            offValues.add("NoPadding");
//            onValues.add("CBC");
            offValues.add("CFB");
            offValues.add("CTR");
//            onValues.add("ECB");
            offValues.add("CTS");
            offValues.add("OFB");
            offValues.add("OpenPGPCFB");
//            onValues.add("PGPCFBBlock");
            offValues.add("SICBlock");
        }

        if (onValues.size() > 0) {
            command.add("--on");
            command.add("[\"" + String.join("\",\"", onValues) + "\"]");
        }

        if (!partialConfig.isUseClientAESNI() || !partialConfig.isUseServerAESNI()) {
            offValues.add("ServerAESNI");
            offValues.add("ClientAESNI");
        }

        if (!partialConfig.isUseClientSEP() || !partialConfig.isUseServerSEP()) {
            offValues.add("ServerSEP");
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
            // No results. I haven't seen a return code that doesn't mean no results found...
//            throw new RuntimeException("The DSL execution returned with a non-zero exit status of " + Integer.toString(p.exitValue()) + "!");
            return new LinkedList<>();
        }
        return parseDslOutput(ImmortalsConfig.getInstance().dasService.getResourceDslPath().resolve("outbox/best.txt"), partialConfig);
    }
}
