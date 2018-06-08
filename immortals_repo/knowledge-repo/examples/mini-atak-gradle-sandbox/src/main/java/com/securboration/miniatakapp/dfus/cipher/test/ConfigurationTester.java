package com.securboration.miniatakapp.dfus.cipher.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import com.securboration.miniatakapp.dfus.cipher.Blockifier;
import com.securboration.miniatakapp.dfus.cipher.CipherImplApi;
import com.securboration.miniatakapp.dfus.cipher.CipherImplBouncyCrypto;
import com.securboration.miniatakapp.dfus.cipher.CipherImplJavaxCrypto;

/**
 * A simple test harness for testing the integrity of proxy-based IO stream 
 * transformations
 * 
 * @author jstaples
 *
 */
public class ConfigurationTester {
    
    private static final String[] messagesToSend = {//the messages to test
            "",//intentionally empty
            "message-0 ",
            "message-1 this is a message long enough to require the use of two blocks.  just needs to be a tiny bit larger",
//            "message-2 ",
//            "message-3 ",
//            "message-4 ",
            "message-5 ;) done. \u2202 \u03C0",
//            "message-6 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0",
//            "message-7 " + randomString(new Random(0L),117),
//            "message-8 " + randomString(new Random(0L),118),
            "message-9 " + randomString(new Random(0L),119),
            "message-10 " + randomString(new Random(0L),4096),
            "message-11 " + randomString(new Random(1L),1024*16),
//            "message-12 " + randomString(new Random(2L),1024*1024*4),
    };
    
    private static String randomString(Random rng,int numBytes){
        byte[] data = new byte[numBytes];
        rng.nextBytes(data);
        
        return (new String(data)).replace("\r\n","").replace("\n", "").replace("\r", "");
    }
    
    private static class Configuration{
        private final Class<?> cipherImpl;
        private final String alg;
        private final String padding;
        private final String chaining;
        private final int keyBytes;
        private final int blockSizeBytes;
        
        private final String keyGenerator;
        private final String ivGenerator;
        
        private static List<Configuration> enumerate(
                Class<?>[] impls,
                String[] algs,
                String[] pads,
                String[] modes,
                int[] keyLengths,
                int[] blockSizes,
                String[] keyGenerators,
                String[] ivGenerators
                ){
            List<Configuration> configs = new ArrayList<>();
            
            for(final Class<?> cipher:impls){
                for(final String alg:algs){
                    for(final String mode:modes){
                        for(final int keyLength:keyLengths){
                            for(final int blockSize:blockSizes){
                                for(final String pad:pads){
                                    for(String keyGenerator:keyGenerators){
                                        for(String ivGenerator:ivGenerators){
                                            Configuration c = new Configuration(cipher,alg,pad,mode,keyLength,blockSize,keyGenerator,ivGenerator);
                                            configs.add(c);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                }
            }
            return configs;
        }
        
        private Configuration(
                Class<?> cipherImpl, 
                String alg, 
                String padding,
                String chaining, 
                int keyBytes, 
                int blockSizeBytes,
                String keyGenerator,
                String ivGenerator
                ) {
            super();
            this.cipherImpl = cipherImpl;
            this.alg = alg;
            this.padding = padding;
            this.keyBytes = keyBytes;
            this.blockSizeBytes = blockSizeBytes;
            this.chaining = chaining;
            this.keyGenerator = keyGenerator;
            this.ivGenerator = ivGenerator;
        }
        
        
        @Override
        public String toString(){
            return String.format(
                "{impl=%s, alg=%s, mode=%s, padScheme=%s, keySize=%dB, blockSize=%dB, keyGen=\"%s\", ivGen=%s}", 
                cipherImpl.getSimpleName(),
                alg,
                chaining,
                padding,
                keyBytes,
                blockSizeBytes,
                keyGenerator,
                ivGenerator == null ? null : "\"" + ivGenerator + "\""
                );
        }
    }
    
    public static void main(String[] args) throws Exception{
        
        final Class<?>[] impls = {

//                CipherImplNoop.class,
//                CipherImplBogo.class,
                CipherImplJavaxCrypto.class,
                CipherImplBouncyCrypto.class,
                
                };
        
//        final int[] keyBytesToTest = {32,64,128,192,256,384,512};
//        final int[] blockSizesToTest = {32,64,128,192,256,384,512};
//        final String[] algsToTest = {"DES","3DES","AES","Blowfish"};
//        final String[] paddingSchemes = {"PKCS5"};
//        final String[] modes = {"ECB","CBC"};
        
        
//        final int[] keyBytesToTest = {8,16,24,32,40,48,56,64};
//        final int[] blockSizesToTest = {8,16,24,32,40,48,56,64};
//        final String[] algsToTest = {"DES","3DES","AES","Blowfish"};
//        final String[] paddingSchemes = {"PKCS5Padding"};
//        final String[] modes = {"ECB","CBC","PCBC","CFB","OFB","CTR"};
        
        final String[] algsToTest = {"AES","ARIA","Blowfish","Camellia","CAST5","CAST6","DES","DESede","DSTU7624","GCM","GOST28147","IDEA","Noekeon","RC2","RC5","RC5-64","RC6","Rijndael","SEED","SEEDWrap","Serpent 128","Skipjack","SM4","TEA","Threefish-256","Threefish-512","Threefish-1024","Twofish","XTEA"};
        final String[] paddingSchemes = {"NoPadding","ZeroBytePadding","PKCS5Padding","PKCS7Padding","ISO10126-2Padding","ISO7816-4Padding","TBCPadding","X923Padding"};
        final String[] modes = {"ECB","CBC","CTR","CFB","CTS","OFB","OpenPGPCFB","PGPCFBBlock","SICBlock"};
        final int[] keyBytesToTest = {8,16,24,32,40,48,56,64};
        final int[] blockSizesToTest = {8,16,24,32,40,48,56,64};
        
        final String[] ivs = {"an IV generator string",null};
        final String[] keyPhrases = {"a key generator phrase"};
        
        final List<Configuration> configs = Configuration.enumerate(
            impls,
            algsToTest,
            paddingSchemes,
            modes,
            keyBytesToTest,
            blockSizesToTest,
            keyPhrases,
            ivs
            );
        
        Map<String,String> map = new LinkedHashMap<>();
        int count = 0;
        int passCount = 0;
        for(Configuration config:configs){
            count++;
            System.out.printf("[%05d of %05d (%3.4f%%)] %s\n", count,configs.size(), (count*100d) / (configs.size()*1d), config.toString());
            final String configTuple = config.toString();
            try{
                for(String message:messagesToSend){
                    testWithByteArrayStreams(config,message.getBytes());
                }
                
                map.put(configTuple, "PASS");
                passCount++;
            } catch(Exception e){
//                e.printStackTrace();
                map.put(configTuple, "FAIL");//TODO: dump exception 
            }
        }
        
        {
            StringBuilder sb = new StringBuilder();
            for(String key:map.keySet()){
                if(map.get(key).startsWith("PASS")){
                    sb.append(String.format("%s:  %s\n", map.get(key),key));
                }
            }
            
            sb.append("\n");
            {
                Map<String,Set<String>> validConfigurationParameters = collectPassingConfigs(map);
                
                for(String key:validConfigurationParameters.keySet()){
                    Set<String> validValues = validConfigurationParameters.get(key);
                    
                    sb.append(String.format("valid options for key \"%s\": %s\n", key, validValues));
                }
            }
            
            {
                sb.append(String.format("\n\n%d of %d configurations passed\n", passCount, count));
            }
            
            FileUtils.writeStringToFile(new File("./report.dat"), sb.toString());
        }
    }
    
    private static Map<String,String> extractKvs(String s){
        s = s.replace("{", "");
        s = s.replace("}", "");
        
        Map<String,String> map = new LinkedHashMap<>();
        for(String part:s.split(",")){
            if(!part.contains("=")){
                continue;
            }
            
            String[] kv = part.split("=");
            
            final String key = kv[0].trim();
            final String value = kv[1].trim();
            
            map.put(key, value);
        }
        
        return map;
    }
    
    private static Map<String,Set<String>> collectPassingConfigs(
            Map<String,String> map
            ){
        
        Map<String,Set<String>> summary = new LinkedHashMap<>();
        
        for(final String config:map.keySet()){
            final String value = map.get(config);
            
            Map<String,String> kvs = extractKvs(config);
            
            for(String key:kvs.keySet()){
                Set<String> valuesForKey = summary.get(key);
                
                if(valuesForKey == null){
                    valuesForKey = new LinkedHashSet<>();
                    summary.put(key, valuesForKey);
                }
                
                if(value.startsWith("PASS")){
                    valuesForKey.add(kvs.get(key));
                }
            }
        }
        
        return summary;
        
    }
    
    private static void nuke(String message){
        throw new RuntimeException(message);
    }
    
    private static OutputStream wrap(
            String tag,
            CipherImplApi cipher,
            OutputStream o
            ){
        return cipher.acquire(o);
    }
    
    private static InputStream wrap(
            String tag,
            CipherImplApi cipher,
            InputStream i
            ){
        return cipher.acquire(i);
    }
    
    private static CipherImplApi acquire(Configuration config) throws InstantiationException, IllegalAccessException{
        CipherImplApi cipher = (CipherImplApi)config.cipherImpl.newInstance();
        
        cipher.configure(
            config.alg, 
            config.keyBytes, 
            config.chaining, 
            config.padding, 
            config.keyGenerator, 
            config.ivGenerator
            );
        
        return cipher;
    }
    
    private static void testWithByteArrayStreams(
          final Configuration config,
          final byte[] transmitThis
          ) throws Exception {
        final ByteArrayOutputStream output = 
                new ByteArrayOutputStream();
        
        final OutputStream outputWrapped = 
                wrap("output",acquire(config),output);
        outputWrapped.write(transmitThis);
        outputWrapped.close();
        
        final InputStream input = 
                wrap("input",acquire(config),new ByteArrayInputStream(output.toByteArray()));
        
        final byte[] recovered = IOUtils.toByteArray(input);
        
        final String hex1 = Blockifier.hash(transmitThis);
        final String hex2 = Blockifier.hash(recovered);
        
        if(!hex1.equals(hex2)){
            System.out.println(hex1);
            System.out.println(hex2);
            nuke("FAIL: recovered decrypted result does not match original plaintext");
        }
    }

}
