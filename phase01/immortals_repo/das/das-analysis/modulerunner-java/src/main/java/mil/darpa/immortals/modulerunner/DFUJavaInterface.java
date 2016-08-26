package mil.darpa.immortals.modulerunner;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public enum DFUJavaInterface {
    InputStream(java.io.InputStream.class),
    OutputStream(java.io.OutputStream.class),
    InputStreamReader(java.io.InputStreamReader.class),
    OutputStreamWriter(java.io.OutputStreamWriter.class),
    DiscreteBytes(byte[].class);

    private final List<Class> validClassList;

    private DFUJavaInterface(@Nonnull Class validClassTypes) {
        validClassList = Arrays.asList(validClassTypes);
    }

    public boolean isMethodValid(@Nonnull Method method) {
        Class testClass = method.getReturnType();


        for (Class clazz : validClassList) {
            if (clazz.isAssignableFrom(testClass)) {
                return true;
            }
        }
        return false;
    }
}
