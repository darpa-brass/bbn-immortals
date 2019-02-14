package mil.darpa.immortals.das.sourcecomposer.configuration.paradigms;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Parameter details for DFU source composition
 * Created by awellman@bbn.com on 5/1/17.
 */
@SuppressWarnings("WeakerAccess")
public class GenericParameterConfiguration {

    public final boolean providedByApplication;
    public final String applicationVariableName;
    public final String classType;
    public final String[] value;

    public GenericParameterConfiguration(boolean providedByApplication, @Nullable String applicationVariableName,
                                         @Nullable String classType, @Nullable String... value) {
        this.providedByApplication = providedByApplication;
        this.applicationVariableName = applicationVariableName;
        this.classType = classType;
        this.value = value;
    }

    private transient static HashMap<String, String> primitiveMap = new HashMap<>();

    static {
        primitiveMap.put("int", "java.lang.Integer");
        primitiveMap.put("byte", "java.lang.Byte");
        primitiveMap.put("short", "java.lang.Short");
        primitiveMap.put("double", "java.lang.Double");
        primitiveMap.put("float", "java.lang.Float");
        primitiveMap.put("long", "java.lang.Long");
        primitiveMap.put("int[]", "java.lang.Integer[]");
        primitiveMap.put("byte[]", "java.lang.Byte[]");
        primitiveMap.put("short[]", "java.lang.Short[]");
        primitiveMap.put("double[]", "java.lang.Double[]");
        primitiveMap.put("float[]", "java.lang.Float[]");
        primitiveMap.put("long[]", "java.lang.Long[]");
    }

    /**
     * Irons out those primitive kinks....
     *
     * @param parameterIdentifier The primitive parameter identifier
     * @return String the class type of the parameter
     */
    public static String parameterIroner(String parameterIdentifier) {
        if (primitiveMap.containsKey(parameterIdentifier)) {
            return primitiveMap.get(parameterIdentifier);
        }
        return parameterIdentifier;
    }

    public Set<GenericParameterConfiguration> produceAnalysisParameters() {
        Set<GenericParameterConfiguration> parameterList;

        if (value == null) {
            parameterList = new HashSet<>(1);
            parameterList.add(new GenericParameterConfiguration(providedByApplication, null, classType));

        } else {
            parameterList = new HashSet<>(value.length);
            for (String loopValue : value) {
                parameterList.add(new GenericParameterConfiguration(providedByApplication, null, classType, loopValue));
            }
        }

        return parameterList;
    }
}
