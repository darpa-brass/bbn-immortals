package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by awellman@bbn.com on 6/26/18.
 */
public class CipherConfiguration implements Comparator<CipherConfiguration>, Comparable<CipherConfiguration> {

    public static final HashSet<String> resources = new HashSet<>(Arrays.asList(
            "ServerAESNI",
            "ServerSEP",
            "ClientAESNI",
            "ClientSEP"
    ));

    // DSLHACK: The DSL should account for these.
    public static final HashSet<String> algorithmOmissions = new HashSet<>(Arrays.asList(
            "RC5_64",
            "SEEDWrap",
            "Serpent_128",
            "Threefish_1024"
    ));

    private static final HashSet<String> algorithms = new HashSet<>(Arrays.asList(
            "AES",
            "ARIA",
            "Blowfish",
            "Camellia",
            "CAST5",
            "CAST6",
            "DES",
            "DESede",
            "DSTU7624",
            "GCM",
            "GOST28147",
            "IDEA",
            "Noekeon",
            "RC2",
            "RC5",
            "RC6",
            "Rijndael",
            "SEED",
            "Skipjack",
            "SM4",
            "TEA",
            "Threefish_256",
            "Threefish_512",
            "Twofish",
            "XTEA"
    ));

    private static final HashSet<String> modes = new HashSet<>(Arrays.asList(
            "CBC", // IV
            "CFB", // IV
            "CTR", // IV
            "ECB",
            "CTS",
            "OFB", // IV
            "OpenPGPCFB",
            "PGPCFBBlock",
            "SICBlock"
    ));

    private static final HashSet<String> paddings = new HashSet<>(Arrays.asList(
            "ISO10126_2Padding",
            "ISO7816_4Padding",
            "NoPadding",
            "PKCS5Padding",
            "PKCS7Padding",
            "TBCPadding",
            "X923Padding",
            "ZeroBytePadding"
    ));

    private static final HashSet<String> keysizes = new HashSet<>(Arrays.asList(
            "KSZ16",
            "KSZ24",
            "KSZ32",
            "KSZ40",
            "KSZ48",
            "KSZ56",
            "KSZ64",
            "KSZ8"
    ));

    private final boolean useServerAESNI;
    private final boolean useServerSEP;
    private final boolean useClientAESNI;
    private final boolean useClientSEP;
    private String cipherAlgorithm;
    private Integer keyLength;
    private String paddingScheme;
    private String cipherChainingMode;
    private boolean serverJavax;
    private boolean clientJavax;

    private List<String> cipherAlgorithms = new LinkedList<>();
    private List<Integer> keyLengths = new LinkedList<>();
    private List<String> paddingSchemes = new LinkedList<>();
    private List<String> cipherChainingModes = new LinkedList<>();

    CipherConfiguration(boolean useServerAESNI, boolean useServerSEP, boolean useClientAESNI, boolean useClientSEP,
                        String cipherAlgorithm, Integer keyLength, String paddingScheme, String cipherChainingMode,
                        boolean serverJavax, boolean clientJavax) {
        this.useServerAESNI = useServerAESNI;
        this.useServerSEP = useServerSEP;
        this.useClientAESNI = useClientAESNI;
        this.useClientSEP = useClientSEP;
        this.cipherAlgorithm = cipherAlgorithm;
        this.keyLength = keyLength;
        this.paddingScheme = paddingScheme;
        this.cipherChainingMode = cipherChainingMode;
        this.serverJavax = serverJavax;
        this.clientJavax = clientJavax;
    }

    public String toString() {
        return "CipherConfiguration(useServerAESNI=" + useServerAESNI + ",useServerSEP=" + useServerSEP + ",useClientAESNI=" + useClientAESNI + ",useClientSEP=" + useClientSEP +
                ",cipherAlgorithm=" + cipherAlgorithm + ",keyLength=" + keyLength + ",paddingScheme=" + paddingScheme + ",mode=" + cipherChainingMode + ",serverJavax=" + serverJavax + ",clientJavax=" + clientJavax + ")";
    }

    public CipherConfiguration(boolean useServerAESNI, boolean useServerSEP, boolean useClientAESNI, boolean useClientSEP,
                               @Nullable String cipherAlgorithm, @Nullable Integer keyLength,
                               @Nullable String paddingScheme, @Nullable String cipherChainingMode) {
        this.useServerAESNI = useServerAESNI;
        this.useServerSEP = useServerSEP;
        this.useClientAESNI = useClientAESNI;
        this.useClientSEP = useClientSEP;
        this.cipherAlgorithm = cipherAlgorithm;
        this.keyLength = keyLength;
        this.paddingScheme = paddingScheme;
        this.cipherChainingMode = cipherChainingMode;
    }

    public boolean isUseServerAESNI() {
        return useServerAESNI;
    }

    public boolean isUseServerSEP() {
        return useServerSEP;
    }

    public boolean isUseClientAESNI() {
        return useClientAESNI;
    }

    public boolean isUseClientSEP() {
        return useClientSEP;
    }

    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public Integer getKeyLength() {
        return keyLength;
    }

    public String getPaddingScheme() {
        return paddingScheme;
    }

    public String getCipherChainingMode() {
        return cipherChainingMode;
    }

    public boolean isServerJavax() {
        return serverJavax;
    }

    public boolean isClientJavax() {
        return clientJavax;
    }

    void setValue(@Nonnull String identifier, boolean valid) {
        if (algorithms.contains(identifier)) {
            if (valid) {
                cipherAlgorithms.add(identifier);
            }

        } else if (modes.contains(identifier)) {
            if (valid) {
                cipherChainingModes.add(identifier);
            }

        } else if (paddings.contains(identifier)) {
            if (valid) {
                paddingSchemes.add(identifier);
            }

        } else if (keysizes.contains(identifier)) {
            if (valid) {
                keyLengths.add(Integer.valueOf(identifier.replace("KSZ", "")));
            }

        } else if (identifier.equals("ClientJavax")) {
            clientJavax = valid;

        } else if (identifier.equals("ServerJavax")) {
            serverJavax = valid;

        } else if (resources.contains(identifier)) {
            if ((identifier.equals("ServerAESNI") && valid && !useServerAESNI) ||
                    (identifier.equals("ServerSEP") && valid && !useServerSEP) ||
                    (identifier.equals("ClientAESNI") && valid && !useClientAESNI) ||
                    (identifier.equals("ClientSEP") && valid && !useClientSEP)
                    ) {
                throw new RuntimeException("Provided configuration for '" + identifier + "' violates initial constraint!");
            }
        } else if (algorithmOmissions.contains(identifier)) {
            if (valid) {
                throw new RuntimeException("Unsupported algorithm '" + identifier + "'!");
            }

        } else {
            throw new RuntimeException("Unknown DSL value '" + identifier + "'!");
        }
    }

    public void finalize(CipherConfiguration partialConfiguration) throws Exception {
        if (partialConfiguration.cipherAlgorithm == null) {
            cipherAlgorithm = cipherAlgorithms.get(0);
        } else {
            if (cipherAlgorithms.contains(partialConfiguration.cipherAlgorithm)) {
                cipherAlgorithm = partialConfiguration.cipherAlgorithm;
            } else {
                throw new RuntimeException("Required cipher algorithm '" + partialConfiguration.cipherAlgorithm + "' not found in solution!");
            }
        }

        if (partialConfiguration.keyLength == null) {
            keyLength = keyLengths.get(0);
        } else {
            if (keyLengths.contains(partialConfiguration.keyLength)) {
                keyLength = partialConfiguration.keyLength;
            } else {
                throw new RuntimeException("Required key length '" + Integer.toString(partialConfiguration.keyLength) + "' not found in solution!");
            }

        }

        if (partialConfiguration.cipherChainingMode == null) {
            cipherChainingMode = cipherChainingModes.get(0);
        } else {
            if (cipherChainingModes.contains(partialConfiguration.cipherChainingMode)) {
                cipherChainingMode = partialConfiguration.cipherChainingMode;
            } else {
                throw new RuntimeException("Required chaining mode '" + partialConfiguration.cipherChainingMode + "' not found in solution!");
            }

        }

        if (partialConfiguration.paddingScheme == null) {
            paddingScheme = paddingSchemes.get(0);
        } else {
            if (paddingSchemes.contains(partialConfiguration.paddingScheme)) {
                paddingScheme = partialConfiguration.paddingScheme;
            } else {
                throw new RuntimeException("Required padding scheme '" + partialConfiguration.paddingScheme + "' not found in configuration!");
            }
        }

        if (clientJavax != serverJavax) {
            throw new RuntimeException("clientJavax and serverJavax should be equal!");
        }
    }

    public CipherConfiguration clone() {
        return new CipherConfiguration(useServerAESNI, useServerSEP, useClientAESNI, useClientSEP,
                cipherAlgorithm, keyLength, paddingScheme, cipherChainingMode, serverJavax, clientJavax);
    }

    public static void main(String[] args) {

        List<String> algList = new LinkedList<>(algorithms);
        List<String> ksList = new LinkedList<>(keysizes);
        for (int i = 0; i < algList.size(); i++) {
            String algorithm = algList.get(i);

            for (int j = 0; j < ksList.size(); j++) {
                String keysize = ksList.get(j);

                String line = algorithm + "_" + keysize + "(\"" + algorithm + "-" + keysize + "\", \"" + algorithm + "\", " + keysize.replace("KSZ", "") + ", null)";
                if (i + 1 == algList.size() && j + 1 == ksList.size()) {
                    line += ";";
                } else {
                    line += ",";
                }
            }
        }
    }

    @Override
    public int compareTo(CipherConfiguration o) {
        return compare(this, o);
    }

    @Override
    public int compare(CipherConfiguration o1, CipherConfiguration o2) {
        if (o1 == null && o2 == null) {
            return 0;

        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else if (
                o1.cipherChainingMode.equals(o2.cipherChainingMode) &&
                        o1.paddingScheme.equals(o2.paddingScheme) &&
                        o1.keyLength.equals(o2.keyLength) &&
                        o1.clientJavax == o2.clientJavax &&
                        o1.serverJavax == o2.serverJavax
                ) {
            return 0;
        } else {
            return -1;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CipherConfiguration)) {
            return false;
        } else {
            CipherConfiguration cc = (CipherConfiguration) o;
            return compareTo(cc) == 0;
        }
    }
}
