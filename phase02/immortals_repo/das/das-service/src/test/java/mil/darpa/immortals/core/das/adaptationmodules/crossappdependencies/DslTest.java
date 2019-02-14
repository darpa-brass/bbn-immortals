package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import com.google.gson.*;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by awellman@bbn.com on 7/13/18.
 */
public class DslTest {

    public static class RecordedCombinations {
        private static Set<CipherConfiguration> shared;
        private static Set<CipherConfiguration> android;
        private static Set<CipherConfiguration> jvm;
        private static Set<CipherConfiguration> oldCombinations;

        public static synchronized void init() {
            InputStream is = DslTest.class.getClassLoader().getResourceAsStream("bad_combinations.json");

            Gson gson = new GsonBuilder().create();
            JsonObject root = gson.fromJson(new InputStreamReader(is), JsonObject.class);

            shared = loadConfigsFromJsonArray(root.get("same").getAsJsonArray());
            android = loadConfigsFromJsonArray(root.get("android").getAsJsonArray());
            jvm = loadConfigsFromJsonArray(root.get("jvm").getAsJsonArray());
            
            
            InputStream oldCombinationStream = DslTest.class.getClassLoader().getResourceAsStream("old_combinations.json");
            JsonArray oldJson = gson.fromJson(new InputStreamReader(oldCombinationStream), JsonArray.class);
            oldCombinations = loadConfigsFromJsonArray(oldJson);
            
            System.out.println();
        }

        private static Set<CipherConfiguration> loadConfigsFromJsonArray(JsonArray ja) {
            Set<CipherConfiguration> rval = new HashSet<>();
            Iterator<JsonElement> iter = ja.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();
                if (!(obj instanceof JsonObject)) {
                    throw new RuntimeException("Bad File!");
                } else {
                    JsonObject jo = (JsonObject) obj;

                    boolean isJavax = "CipherImplBouncyCrypto".equals(jo.get("impl").getAsString());

                    rval.add(new CipherConfiguration(
                            true, true, true, true,
                            jo.get("alg").getAsString(),
                            Integer.valueOf(jo.get("keySize").getAsString().replaceAll("B", "")),
                            jo.get("padScheme").getAsString(),
                            jo.get("mode").getAsString(),
                            isJavax,
                            isJavax
                    ));
                }
            }
            return rval;
        }

        public static synchronized Boolean validateConfiguration(CipherConfiguration configuration) {
            if (existsInSet(shared, configuration)) {
                return true;
            } else if (existsInSet(android, configuration) || existsInSet(jvm, configuration)) {
                return false;
            } else if (existsInSet(oldCombinations, configuration)) {
                return null;
            } else {
                throw new RuntimeException("Could not find configuration '" + configuration.toString() + "' in any sets!");
            }

        }
        
        private static boolean existsInSet(Set<CipherConfiguration> configurationSet, CipherConfiguration candidate) {
            return configurationSet.stream().anyMatch(t ->
                    t.getCipherChainingMode().equals(candidate.getCipherChainingMode()) &&
                            t.getPaddingScheme().equals(candidate.getPaddingScheme()) &&
                            t.getKeyLength().equals(candidate.getKeyLength()) &&
                            t.getCipherAlgorithm().equals(candidate.getCipherAlgorithm())); 
        }
    }

//    private static List<CipherConfiguration> ciphersFromResults() throws IOException {
//        List<CipherConfiguration> rval = new LinkedList<>();
//        
//        InputStream is = DslTest.class.getClassLoader().getResourceAsStream("bad_combinations.json");
//
//        Gson gson = new GsonBuilder().create();
//        JsonArray array = gson.fromJson(new InputStreamReader(is), JsonArray.class);
//
//        Iterator iter = array.iterator();
//
//        while (iter.hasNext()) {
//            Object obj = iter.next();
//            if (!(obj instanceof JsonObject)) {
//                throw new RuntimeException("Bad File!");
//            } else {
//                JsonObject jo = (JsonObject) obj;
//
//                boolean isJavax = "CipherImplBouncyCrypto".equals(jo.get("impl").getAsString());
//
//                rval.add(new CipherConfiguration(
//                        true, true, true, true,
//                        jo.get("alg").getAsString(),
//                        Integer.valueOf(jo.get("keySize").getAsString().replaceAll("B", "")),
//                        jo.get("padScheme").getAsString(),
//                        jo.get("mode").getAsString(),
//                        isJavax,
//                        isJavax
//                ));
//            }
//        }
//        return rval;
//    }

    public enum SecurityStandard {
        AES_128("AES encryption algorithm with 128bit+ key", "AES", 16, null);
//        AES_256("AES encryption algorithm with 256bit+ key", "AES", 32, null),
//        Blowfish_128("Blowfish encryption algorithm with 128bit+ key", "Blowfish", 16, null);
//        DESEDE_128("DESEDE encryption algorithm with 128bit+ key", "DESEDE", 16, null),
//        DES_56("DES encryption algorithm with 56bit+ key", "DES", 7, null),
//        GCM_128("GCM encryption algorithm with 128bit+ key", "GCM", 16, null),
//        ARIA("ARIA encryption algorithm", "ARIA", null, null),
//        XTEA("XTEA encryption algorithm", "XTEA", null, null),
//        TWOFISH("Twofish encryption algorithm", "Twofish", null, null),
//        THREEFISH_512("Threefish encryption algorithm with 512+ key", "Threefish_512", 64, null);


        public final String description;
        public final String algorithm;
        public final Integer keySize;
        public final String cipherChainingMode;

        SecurityStandard(String description, String algorithm, Integer keySize, String cipherChainingMode) {
            this.description = description;
            this.algorithm = algorithm;
            this.keySize = keySize;
            this.cipherChainingMode = cipherChainingMode;
        }
    }

    public void testDslConfigs() {

    }

    public static void main(String[] args) {
        try {

            RecordedCombinations.init();
            
            TreeSet<CipherConfiguration> allConfigs = new TreeSet<>();

            for (SecurityStandard ss : SecurityStandard.values()) {
                CipherConfiguration[] configurations = {
                        new CipherConfiguration(true, true, true, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, true, true, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, true, false, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, true, false, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, false, true, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, false, true, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, false, false, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(true, false, false, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, true, true, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, true, true, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, true, false, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, true, false, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, false, true, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, false, true, false, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, false, false, true, ss.algorithm, ss.keySize, null, null),
                        new CipherConfiguration(false, false, false, false, ss.algorithm, ss.keySize, null, null),
                };

                for (CipherConfiguration partialConfiguration : configurations) {

                    String adaptationIdentifier = "TestMain";
                    DslAdapter da = DslAdapter.getInstance();
                    CipherConfiguration result = da.querySingleSolution(adaptationIdentifier, partialConfiguration);
                    allConfigs.add(result);
                    RecordedCombinations.validateConfiguration(result);
                    System.out.println(result.toString());
                }
            }
            System.out.println("FINISHED");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
