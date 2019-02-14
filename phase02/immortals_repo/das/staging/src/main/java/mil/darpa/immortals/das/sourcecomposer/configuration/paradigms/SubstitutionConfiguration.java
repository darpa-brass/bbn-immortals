package mil.darpa.immortals.das.sourcecomposer.configuration.paradigms;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 5/1/17.
 */
public class SubstitutionConfiguration extends AbstractParadigm {

    public final String initializationMethod;
    public final LinkedList<GenericParameterConfiguration> initializationMethodParameters;
    public final String workMethod;
    public final LinkedList<GenericParameterConfiguration> workMethodParameters;
    public final String cleanupMethod;
    public final LinkedList<GenericParameterConfiguration> cleanupMethodParameters;

    public SubstitutionConfiguration(String dependencyString, String classPackage, String initializationMethod,
                                     LinkedList<GenericParameterConfiguration> initializationMethodParameters, String workMethod,
                                     LinkedList<GenericParameterConfiguration> workMethodParameters, String cleanupMethod,
                                     LinkedList<GenericParameterConfiguration> cleanupMethodParameters) {
        super(dependencyString, classPackage);
        this.initializationMethod = initializationMethod;
        this.initializationMethodParameters = initializationMethodParameters;
        this.workMethod = workMethod;
        this.workMethodParameters = workMethodParameters;
        this.cleanupMethod = cleanupMethod;
        this.cleanupMethodParameters = cleanupMethodParameters;
    }
}
