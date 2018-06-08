package com.securboration.immortals.utility;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.jvm.AndroidRuntimeEnvironment;
import com.securboration.immortals.ontology.cp.jvm.JavaRuntimeEnvironment;
import com.securboration.immortals.ontology.cp.jvm.RuntimeEnvironment;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.functionality.ConfigurationBinding;
import com.securboration.immortals.ontology.functionality.alg.encryption.CipherAlgorithm;
import com.securboration.immortals.ontology.functionality.alg.encryption.CipherChainingMode;
import com.securboration.immortals.ontology.functionality.alg.encryption.CipherKeyLength;
import com.securboration.immortals.ontology.functionality.alg.encryption.PaddingScheme;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureSolution;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.gmei.DeploymentModel;
import com.securboration.immortals.ontology.resources.Device;
import com.securboration.immortals.ontology.resources.PlatformResource;

/**
 * Hard-coded heuristic checks for configuration validity in the context of a
 * given deployment model
 *
 * TODO: test this class
 *
 * @author jstaples
 *
 */
public class ConfigurationCheckerRules {

    private static final String NOOP_URI = "http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-NoopCipher";
    private static final String BOGO_URI = "http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-BogoCipher";
    private static final String JAVAX_URI = "http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-JavaxCipher";
    private static final String BOUNCY_URI = "http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance-BouncyCastleCipher";

    private static final Set<String> supportedImplUris = new HashSet<>(
            Arrays.asList(
                    NOOP_URI,
                    BOGO_URI,
                    JAVAX_URI,
                    BOUNCY_URI
            )
    );

    private static final Set<String> supportedPaddingSchemes = new HashSet<>(
            Arrays.asList(
                    "PKCS5Padding",
                    "NoPadding",
                    "ZeroBytePadding",
                    "PKCS7Padding",
                    "ISO10126-2Padding",
                    "ISO7816-4Padding",
                    "TBCPadding",
                    "X923Padding"
            )
    );

    private static final Set<String> supportedAlgorithms = new HashSet<>(
            Arrays.asList(
                    "AES", "Blowfish", "DES", "DESede", "RC2", "Rijndael",
                    "ARIA", "Camellia", "CAST5", "CAST6", "DSTU7624", "GCM",
                    "GOST28147", "IDEA", "Noekeon", "RC5", "RC6", "SEED",
                    "Skipjack", "SM4", "TEA", "Threefish-256", "Threefish-512",
                    "Twofish", "XTEA"
            )
    );

    private static final Set<String> supportedBlockModes = new HashSet<>(
            Arrays.asList(
                    "ECB", "CBC", "CTR", "CFB", "OFB", "CTS", "OpenPGPCFB",
                    "PGPCFBBlock", "SICBlock"
            )
    );

    private static final Set<String> supportedKeyLengthsBytes = new HashSet<>(
            Arrays.asList(
                    "16", "24", "32", "8", "40", "48", "56", "64"
            )
    );


    private static String checkAndReport(Set<String> validValues, String value){
        if(validValues.contains(value)){
            return null;
        }

        return $("value \"%s\" does not belong to set %s",value, validValues);
    }

    private static String checkAndReport(
            final int lowerBoundInclusive,
            final int upperBoundInclusive,
            final int value,
            final int...prohibited
    ){
        if(value < lowerBoundInclusive || value > upperBoundInclusive){
            return $("value %d outside range [%d,%d]",value,lowerBoundInclusive,upperBoundInclusive);
        }

        for(int prohibitedValue:prohibited){
            if(value == prohibitedValue){
                return $("value %d is prohibited",value);
            }
        }

        return null;
    }

    private static String getBinding(
            AspectConfigureSolution solution,
            Class<? extends DataType> configurationBinding
    ){
        for(ConfigurationBinding b:solution.getConfigurationBindings()){
            if(b.getSemanticType().equals(configurationBinding)){
                return b.getBinding();
            }
        }

        throw new RuntimeException("no key found of type " + configurationBinding);
    }

    /**
     *
     * @param context
     * @param solution
     * @return null iff the configuration is valid in the provided context. Else
     *         a message is returned describing why the configuration is
     *         invalid.
     */
    public static String checkConfiguration(
            final DeploymentModel context,
            final AspectConfigureSolution solution
    ){
        final DfuInstance dfu = solution.getChosenInstance();
        final String dfuUri = dfu.getDurableUri();
        final String algorithmName = getBinding(solution,CipherAlgorithm.class);
        final int keyLengthBytes = Integer.parseInt(getBinding(solution,CipherKeyLength.class));
        final int keyLengthBits = bytesToBits(keyLengthBytes);
        final String paddingSchemeName = getBinding(solution,PaddingScheme.class);
        final String cipherBlockModeName = getBinding(solution,CipherChainingMode.class);

        final boolean supportsStrongCrypto =
                isUnlimitedCryptoStrengthPossible(context);

        //all BOGO and NOOP configurations are valid regardless of any other
        // configuration settings
        {
            if(dfu.getDurableUri().contains("BOGO")){
                return null;
            }

            if(dfu.getDurableUri().contains("NOOP")){
                return null;
            }
        }

        // If here, we know the DFU uses java's crypto architecture and is 
        // limited in strength by export laws.  Check whether the platform 
        // supports strong crypto
        if(!supportsStrongCrypto && keyLengthBits > 128){
            return $(
                    "Platform crypto strength is limited to 128 bits by export " +
                            "constraints but attempted to use a key size of %d bits",
                    keyLengthBits
            );
        }

        //sanity check the dfu
        final String dfuReport =
                checkAndReport(supportedImplUris,dfu.getDurableUri());
        if(dfuReport != null){
            return dfuReport;
        }

        //sanity check the algorithm
        final String algReport =
                checkAndReport(supportedAlgorithms,algorithmName);
        if(algReport != null){
            return algReport;
        }

        //sanity check the key size
        final String keyReport =
                checkAndReport(supportedKeyLengthsBytes,""+keyLengthBytes);
        if(keyReport != null){
            return keyReport;
        }

        //sanity check the padding scheme
        final String paddingReport =
                checkAndReport(supportedPaddingSchemes,paddingSchemeName);
        if(paddingReport != null){
            return paddingReport;
        }

        //sanity check the mode
        final String modeReport =
                checkAndReport(supportedBlockModes,cipherBlockModeName);
        if(modeReport != null){
            return modeReport;
        }

        {//verify that the impl supports the selected algorithm
            if(JAVAX_URI.equals(dfuUri)){
                //  for cipher impl com.securboration.miniatakapp.dfus.cipher.CipherImplJavaxCrypto
                //   cipher algorithms allowed: [AES, Blowfish, DES, DESede, RC2, Rijndael], disallowed: [ARIA, Camellia, CAST5, CAST6, DSTU7624, GCM, GOST28147, IDEA, Noekeon, RC5, RC5-64, RC6, SEED, SEEDWrap, Serpent 128, Skipjack, SM4, TEA, Threefish-256, Threefish-512, Threefish-1024, Twofish, XTEA]
                //   padding schemes allowed: [PKCS5Padding, NoPadding], disallowed: [ZeroBytePadding, PKCS7Padding, ISO10126-2Padding, ISO7816-4Padding, TBCPadding, X923Padding]
                //   chaining modes allowed: [ECB, CBC, CTR, CFB, OFB, CTS], disallowed: [OpenPGPCFB, PGPCFBBlock, SICBlock]

                final String algReport2 =
                        checkAndReport(
                                new HashSet<>(Arrays.asList("AES", "Blowfish", "DES", "DESede", "RC2", "Rijndael")),
                                dfuUri
                        );
                if(algReport2 != null){
                    return algReport2;
                }

                final String paddingReport2 =
                        checkAndReport(
                                new HashSet<>(Arrays.asList("PKCS5Padding", "NoPadding")),
                                paddingSchemeName
                        );
                if(paddingReport2 != null){
                    return paddingReport2;
                }

                final String modeReport2 =
                        checkAndReport(
                                new HashSet<>(Arrays.asList("ECB", "CBC", "CTR", "CFB", "OFB", "CTS")),
                                paddingSchemeName
                        );
                if(modeReport2 != null){
                    return modeReport2;
                }
            } else if(BOUNCY_URI.equals(dfuUri)){
                //  for cipher impl com.securboration.miniatakapp.dfus.cipher.CipherImplBouncyCrypto
                //   cipher algorithms allowed: [AES, ARIA, Blowfish, Camellia, CAST5, CAST6, DES, DESede, DSTU7624, GCM, GOST28147, IDEA, Noekeon, RC2, RC5, RC6, Rijndael, SEED, Skipjack, SM4, TEA, Threefish-256, Threefish-512, Twofish, XTEA], disallowed: [RC5-64, SEEDWrap, Serpent 128, Threefish-1024]
                //   padding schemes allowed: [ZeroBytePadding, PKCS5Padding, PKCS7Padding, ISO10126-2Padding, ISO7816-4Padding, TBCPadding, X923Padding, NoPadding], disallowed: []
                //   chaining modes allowed: [ECB, CBC, CTR, CFB, CTS, OFB, OpenPGPCFB, PGPCFBBlock, SICBlock], disallowed: []

                final String algReport2 =
                        checkAndReport(
                                new HashSet<>(Arrays.asList("AES", "ARIA", "Blowfish", "Camellia", "CAST5", "CAST6", "DES", "DESede", "DSTU7624", "GCM", "GOST28147", "IDEA", "Noekeon", "RC2", "RC5", "RC6", "Rijndael", "SEED", "Skipjack", "SM4", "TEA", "Threefish-256", "Threefish-512", "Twofish", "XTEA")),
                                dfuUri
                        );
                if(algReport2 != null){
                    return algReport2;
                }

                final String paddingReport2 =
                        checkAndReport(
                                new HashSet<>(Arrays.asList("ZeroBytePadding", "PKCS5Padding", "PKCS7Padding", "ISO10126-2Padding", "ISO7816-4Padding", "TBCPadding", "X923Padding", "NoPadding")),
                                paddingSchemeName
                        );
                if(paddingReport2 != null){
                    return paddingReport2;
                }

                final String modeReport2 =
                        checkAndReport(
                                new HashSet<>(Arrays.asList("ECB", "CBC", "CTR", "CFB", "CTS", "OFB", "OpenPGPCFB", "PGPCFBBlock", "SICBlock")),
                                paddingSchemeName
                        );
                if(modeReport2 != null){
                    return modeReport2;
                }
            } else {
                throw new RuntimeException("unhandled case: " + dfuUri);
            }
        }

        {
            final String keyLengthReport;

            if(algorithmName.equals("AES")){
                //# key bytes allowed: [16, 24, 32], disallowed: [8, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,32,keyLengthBytes);
            } else if(algorithmName.equals("ARIA")){
                //# key bytes allowed: [16, 24, 32], disallowed: [8, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,32,keyLengthBytes);
            } else if(algorithmName.equals("Blowfish")){
                //# key bytes allowed: [16, 24, 32], disallowed: [8, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(8,64,keyLengthBytes);
            } else if(algorithmName.equals("Camellia")){
                //# key bytes allowed: [16, 24, 32], disallowed: [8, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,32,keyLengthBytes);
            } else if(algorithmName.equals("CAST5")){
                //# key bytes allowed: [8, 16], disallowed: [24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(8,16,keyLengthBytes);
            } else if(algorithmName.equals("CAST6")){
                //# key bytes allowed: [8, 16, 24, 32, 40, 48, 56, 64], disallowed: []
                keyLengthReport = checkAndReport(8,64,keyLengthBytes);
            } else if(algorithmName.equals("DES")){
                //# key bytes allowed: [8], disallowed: [16, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(8,8,keyLengthBytes);
            } else if(algorithmName.equals("DESede")){
                //# key bytes allowed: [24, 16], disallowed: [8, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,24,keyLengthBytes);
            } else if(algorithmName.equals("DSTU7624")){
                //# key bytes allowed: [16, 32], disallowed: [8, 24, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,32,keyLengthBytes,24);
            } else if(algorithmName.equals("GCM")){
                //# key bytes allowed: [16, 24, 32], disallowed: [8, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,32,keyLengthBytes);
            } else if(algorithmName.equals("GOST28147")){
                //# key bytes allowed: [32], disallowed: [8, 16, 24, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(32,32,keyLengthBytes);
            } else if(algorithmName.equals("IDEA")){
                //# key bytes allowed: [8, 16, 24, 32, 40, 48, 56, 64], disallowed: []
                keyLengthReport = checkAndReport(8,64,keyLengthBytes);
            } else if(algorithmName.equals("Noekeon")){
                //# key bytes allowed: [16, 24, 32, 40, 48, 56, 64], disallowed: [8]
                keyLengthReport = checkAndReport(16,64,keyLengthBytes);
            } else if(algorithmName.equals("RC2")){
                //# key bytes allowed: [8, 16, 24, 32, 40, 48, 56, 64], disallowed: []
                keyLengthReport = checkAndReport(8,64,keyLengthBytes);
            } else if(algorithmName.equals("RC5")){
                //# key bytes allowed: [8, 16, 24, 32, 40, 48, 56, 64], disallowed: []
                keyLengthReport = checkAndReport(8,64,keyLengthBytes);
            } else if(algorithmName.equals("RC5-64")){
                //# key bytes allowed: [], disallowed: [8, 16, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(0,0,keyLengthBytes);
            } else if(algorithmName.equals("RC6")){
                //# key bytes allowed: [8, 16, 24, 32, 40, 48, 56, 64], disallowed: []
                keyLengthReport = checkAndReport(8,64,keyLengthBytes);
            } else if(algorithmName.equals("Rijndael")){
                //# key bytes allowed: [16, 24, 32], disallowed: [8, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,32,keyLengthBytes);
            } else if(algorithmName.equals("SEED")){
                //# key bytes allowed: [16, 24, 32, 40, 48, 56, 64], disallowed: [8]
                keyLengthReport = checkAndReport(16,64,keyLengthBytes);
            } else if(algorithmName.equals("SEEDWrap")){
                //# key bytes allowed: [], disallowed: [8, 16, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(0,0,keyLengthBytes);
            } else if (algorithmName.equals("Serpent 128")) {
                // # key bytes allowed: [], disallowed: [8, 16, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(0, 0, keyLengthBytes);
            } else if(algorithmName.equals("Skipjack")){
                //# key bytes allowed: [16, 24, 32, 40, 48, 56, 64], disallowed: [8]
                keyLengthReport = checkAndReport(16,64,keyLengthBytes);
            } else if(algorithmName.equals("SM4")){
                //# key bytes allowed: [16], disallowed: [8, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,16,keyLengthBytes);
            } else if(algorithmName.equals("TEA")){
                //# key bytes allowed: [16], disallowed: [8, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,16,keyLengthBytes);
            } else if(algorithmName.equals("Threefish-256")){
                //# key bytes allowed: [32], disallowed: [8, 16, 24, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,16,keyLengthBytes);
            } else if(algorithmName.equals("Threefish-512")){
                //# key bytes allowed: [64], disallowed: [8, 16, 24, 32, 40, 48, 56]
                keyLengthReport = checkAndReport(64,64,keyLengthBytes);
            } else if(algorithmName.equals("Threefish-1024")){
                //# key bytes allowed: [], disallowed: [8, 16, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(0,0,keyLengthBytes);
            } else if(algorithmName.equals("Twofish")){
                //# key bytes allowed: [8, 16, 24, 32], disallowed: [40, 48, 56, 64]
                keyLengthReport = checkAndReport(8,32,keyLengthBytes);
            } else if(algorithmName.equals("XTEA")){
                //# key bytes allowed: [16], disallowed: [8, 24, 32, 40, 48, 56, 64]
                keyLengthReport = checkAndReport(16,16,keyLengthBytes);
            } else {
                throw new RuntimeException("unsupported algorithm: " + algorithmName);
            }

            if(keyLengthReport != null){
                return keyLengthReport;
            }
        }

        //if here, the configuration hasn't failed any checks and is assumed 
        // to be valid
        return null;
    }

    private static String $(String format, Object...args){
        return String.format(format, args);
    }

    private static int bytesToBits(final int bytes){
        return bytes*8;
    }


    private static boolean isUnlimitedCryptoStrengthPossible(
            DeploymentModel context
    ){
        Device d = getFirstResourceOfType(Device.class,context);

        RuntimeEnvironment r = getFirstResourceOfType(RuntimeEnvironment.class,d);

        final Boolean cryptoStrength;

        if(r instanceof AndroidRuntimeEnvironment){
            cryptoStrength = ((AndroidRuntimeEnvironment)r).getUnlimitedCryptoStrengh();
        } else if(r instanceof JavaRuntimeEnvironment){
            cryptoStrength = ((JavaRuntimeEnvironment)r).getUnlimitedCryptoStrengh();
        } else {
            throw new RuntimeException("unhandled case: " + r.getClass());
        }

        return (cryptoStrength != null) && (cryptoStrength == true);
    }

    private static <T extends PlatformResource> T getFirstResourceOfType(Class<T> type,Device d){
        for(PlatformResource p:d.getResources()){
            if(type.isAssignableFrom(p.getClass())){
                return (T)p;
            }
        }

        return null;
    }

    private static <T extends Resource> T getFirstResourceOfType(
            Class<T> type,
            DeploymentModel d
    ){
        for(Resource r:d.getAvailableResources()){
            if(type.isAssignableFrom(r.getClass())){
                return (T)r;
            }
        }

        return null;
    }

}