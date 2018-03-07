package mil.darpa.immortals.configuration.sourcecomposer.paradigms;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 5/22/17.
 */
public abstract class AbstractParadigm {

    @Nonnull
    public final String dependencyString;

    @Nonnull
    public final String classPackage;

    @Nonnull

    public AbstractParadigm(@Nonnull String dependencyString, @Nonnull String classPackage) {
        this.dependencyString = dependencyString;
        this.classPackage = classPackage;
    }

}
