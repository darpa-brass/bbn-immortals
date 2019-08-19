package com.securboration.test.xsdmut;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author jstaples
 *
 */
public class ConfigurationHelper {
    private static final String CONFIG_LOC_KEY = "Configuration.location";
    private static final String CONFIG_LOC_DEFAULT = "config.txt";
    
    public static synchronized <T extends ConfigurableTypeBase> T acquire(Class<T> configClass) {
        try {
            Constructor<T> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            
            T t = constructor.newInstance();
            init(t,null);
            return t;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void init(
            Object config,
            String configKeyValues
            ) throws IllegalArgumentException, IllegalAccessException {
        
        final Map<String,String> classpathKvs = getClasspathConfig();
        final Map<String,String> fileKvs = getFileSystemConfig();
        final Map<String,String> configKvs = configKeyValues == null ? new HashMap<>():getKvs(configKeyValues);
        
        final String prefix = config.getClass().getSimpleName() + ".";
        
        print(
            "populating configuration pojo of type %s:\n",
            config.getClass().getName()
            );
        for (Field f : config.getClass().getDeclaredFields()) {
            f.setAccessible(true);

            final String key = prefix + f.getName();
            final String value;
            final String source;
            {
                final String classpathConfigValue = classpathKvs.get(key);
                final String fileSystemConfigValue = fileKvs.get(key);
                final String systemPropertyValue = System.getProperty(key);
                final String manualValue = configKvs == null ? null : configKvs.get(key);
                
                String[] valueAndSource = getValueAndSource(
                    classpathConfigValue,
                    fileSystemConfigValue,
                    systemPropertyValue,
                    manualValue
                    );
                
                value = valueAndSource[0];
                source = valueAndSource[1];
            }

            ConfigurationProperty configAnnotation = f.getDeclaredAnnotation(ConfigurationProperty.class);
            
            if(configAnnotation == null) {//skip this field
                continue;
            }
            
            print("\t%s = %s (%s)\n", key, value, source);
            print("\t\tpurpose: %s\n", configAnnotation.desc());

            if (value == null) {
                String stringForm = ""+f.get(config);
                
                if(f.getType().equals(String[].class) && f.get(config) != null) {
                    stringForm = Arrays.asList((String[])f.get(config)).toString();
                }
                
                print(
                        "\t\tNOTE: no override for property, instead using default from POJO of \"%s\"\n",
                        stringForm
                        );

                continue;
            }

            if (f.getType().equals(int.class) || f.getType().equals(Integer.class)) {
                f.set(config, Integer.parseInt(value));
            } else if (f.getType().equals(long.class) || f.getType().equals(Long.class)) {
                f.set(config, Long.parseLong(value));
            } else if (f.getType().equals(float.class) || f.getType().equals(Float.class)) {
                f.set(config, Float.parseFloat(value));
            } else if (f.getType().equals(double.class) || f.getType().equals(Double.class)) {
                f.set(config, Double.parseDouble(value));
            } else if (f.getType().equals(String.class)) {
                f.set(config, value);
            } else if (f.getType().equals(String[].class)) {
                String[] v = value.split(",");
                for (int i = 0; i < v.length; i++) {
                    v[i] = v[i].trim();
                }
                f.set(config, v);
            } else if (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class)) {
                f.set(config, Boolean.parseBoolean(value));
            } else {
                throw new RuntimeException("unsupported field type: " + f.getType().getName());
            }
        }
    }
    
    
    public static String dumpConfig(Object config) throws IllegalArgumentException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        
        final String prefix = config.getClass().getSimpleName() + ".";
        
        for (Field f : config.getClass().getDeclaredFields()) {
            f.setAccessible(true);

            final String key = prefix + f.getName();
            final Object value = f.get(config);
            
            ConfigurationProperty humanReadable = f.getDeclaredAnnotation(ConfigurationProperty.class);
            
            if(humanReadable == null) {
                continue;
            }
            
            final String stringForm;
            {
                if(value == null) {
                    stringForm = null;
                } else if(value instanceof String[]) {
                    String[] v = (String[]) value;
                    
                    StringBuilder vsb = new StringBuilder();
                    for(int i=0;i<v.length;i++) {
                        if(i > 0) {
                            vsb.append(",");
                        }
                        
                        vsb.append(v[i]);
                    }
                    
                    stringForm = vsb.toString();
                } else {
                    stringForm = value.toString();
                }
            }
            
            sb.append("# ").append(humanReadable.desc()).append("\n");
            if(stringForm == null) {
                sb.append("# ");
            }
            sb.append(key).append(" = ").append(stringForm).append("\n");
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public static String printConfiguration(Object config) throws IllegalArgumentException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        
        final String prefix = config.getClass().getSimpleName() + ".";
        sb.append(config.getClass().getSimpleName()).append("\n");
        for (Field f : config.getClass().getDeclaredFields()) {
            f.setAccessible(true);

            final String key = prefix + f.getName();
            final String value = System.getProperty(key);

            ConfigurationProperty humanReadable = f.getDeclaredAnnotation(ConfigurationProperty.class);
            
            if(humanReadable == null) {
                continue;
            }
            
            sb.append(String.format("\t%s\n", key.replace(prefix, "."), value));
            sb.append(String.format("\t\t%s\n", humanReadable.desc()));

            {
                String stringForm = ""+f.get(config);
                
                if(f.getType().equals(String[].class) && f.get(config) != null) {
                    stringForm = Arrays.asList((String[])f.get(config)).toString();
                }
                
                sb.append(String.format(
                        "\t\tdefault = \"%s\"\n",
                        stringForm
                        ));
            }
        }
        
        return sb.toString();
    }
    
    private static Map<String,String> getKvs(String input){
        final String[] lines = input.split("\\n");
        
        Map<String,String> map = new HashMap<>();
        
        for(String line:lines) {
            line = line.trim();
            if(line.isEmpty()) {
                continue;
            }
            if(!line.contains("=")) {
                continue;
            }
            
            final int indexOfSplit = line.indexOf("=");
            final String keyPart = line.substring(0, indexOfSplit).trim();
            final String valuePart = line.substring(indexOfSplit+1).trim();
            
            map.put(keyPart, valuePart);
        }
        
        return map;
    }

    private static String[] getValueAndSource(
            String classpathValue, 
            String fileValue, 
            String propertiesValue,
            String manualValue
            ) {
        //order of precedence is
        // manual overrides all
        // properties override file values and properties values
        // file overrides classpath values
        // classpath override nothing
        
        String source = null;
        String value = null;
        
        if(value == null && manualValue != null) {
            value = manualValue;
            source = "manual override";
        }
        
        if(value == null) {
            value = propertiesValue;
            source = "a JVM system property";
        }
        
        if(value == null) {
            value = fileValue;
            source = CONFIG_LOC_KEY + " on filesystem";
        }
        
        if(value == null) {
            value = classpathValue;
            source = CONFIG_LOC_KEY + " on classpath";
        }
        
        if(value == null) {
            source = "the default value defined in the configuration POJO";
        }
        
        return new String[] {value,source};
    }
    
    
    
    
    private static synchronized Map<String,String> getFileSystemConfig(){
        String fileConfigLocation = System.getProperty(CONFIG_LOC_KEY);
        if(fileConfigLocation != null) {
            print("found override for %s with value %s",CONFIG_LOC_KEY,fileConfigLocation);
        } else {
            fileConfigLocation = CONFIG_LOC_DEFAULT;
            print("no override for %s so looking at default location ./%s",CONFIG_LOC_KEY,fileConfigLocation);
        }
        
        final File f = new File(fileConfigLocation);
        
        if(!f.exists()) {
            print("no config file found at %s, skipping file-based configuration",f.getAbsolutePath());
            return new HashMap<>();
        }
        
        print("performing file configuration using %s",f.getAbsolutePath());
        
        try(InputStream is = new FileInputStream(f)){
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            while(true) {
                byte[] buffer = new byte[1024];
                
                final int bytesRead = is.read(buffer);
                if(bytesRead == -1) {
                    break;
                }
                
                o.write(buffer, 0, bytesRead);
            }
            
            String s = new String(o.toByteArray());
            
            return getKvs(s);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static synchronized Map<String,String> getClasspathConfig(){
        try(InputStream is = ConfigurationHelper.class.getClassLoader().getResourceAsStream(CONFIG_LOC_KEY)){
            if(is == null) {
                print("no " + CONFIG_LOC_KEY + " resource found on classpath, skipping classpath configuration");
                return new HashMap<>();
            }
            
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            while(true) {
                byte[] buffer = new byte[1024];
                
                final int bytesRead = is.read(buffer);
                if(bytesRead == -1) {
                    break;
                }
                
                o.write(buffer, 0, bytesRead);
            }
            
            String s = new String(o.toByteArray());
            
            return getKvs(s);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void print(String format, Object...args) {
        System.out.printf(
            "[configuration setup][in thread \"%s\"] %s\n",
            Thread.currentThread().getName(),
            String.format(format, args)
            );
    }

}

