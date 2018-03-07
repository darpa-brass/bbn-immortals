package mil.darpa.immortals.das.sourcecomposer.dfucomposers;

import mil.darpa.immortals.das.hacks.configuration.applications.ControlPointProfile;
import mil.darpa.immortals.das.sourcecomposer.ApplicationInstance;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.GenericParameterConfiguration;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuSubstitutionInstance;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Composes substitutions
 * 
 * Created by awellman@bbn.com on 5/1/17.
 */
public class SubstitutionComposer {

    private static String methodStringGenerator(String method, List<GenericParameterConfiguration> config)
            throws CompositionException {

        StringBuilder parameterStringBuilder = new StringBuilder();
        for (GenericParameterConfiguration p : config) {
            if (parameterStringBuilder.length() > 0) {
                parameterStringBuilder.append(", ");
            }

            if (p.providedByApplication) {
                if (p.applicationVariableName != null) {
                    parameterStringBuilder.append(p.applicationVariableName);

                } else if (p.value[0] != null) {
                    return p.value[0];

                } else {
                    throw new CompositionException.SubstitutionUndefinedParameterException(method, p.classType, null);
                }

            } else {
                throw new CompositionException.SubstitutionUndefinedParameterException(method, p.classType, null);
            }
        }
        return method + "(" + parameterStringBuilder.toString() + ")";
    }

    private final DfuSubstitutionInstance dfuConfiguration;

    public SubstitutionComposer(DfuSubstitutionInstance dfuConfiguration) {
        this.dfuConfiguration = dfuConfiguration;
    }

    //    public void augmentApplication(ApplicationConfiguration applicationConfiguration)
    public void augmentApplication(ApplicationInstance applicationInstance)
            throws IOException, CompositionException {
        String substitute_identifier = "substitute_" + UUID.randomUUID().toString().substring(0, 8);

        String declaration = "public " + dfuConfiguration.substitutionConfiguration.classPackage + " " + substitute_identifier +
                " = new " + dfuConfiguration.substitutionConfiguration.classPackage + "();";

        String initialization = dfuConfiguration.substitutionConfiguration.initializationMethod == null ? "" :
                substitute_identifier + "." +
                        methodStringGenerator(dfuConfiguration.substitutionConfiguration.initializationMethod,
                                dfuConfiguration.substitutionConfiguration.initializationMethodParameters) + ";";

        String work = dfuConfiguration.substitutionConfiguration.workMethod == null ? "" :
                substitute_identifier + "." +
                        methodStringGenerator(dfuConfiguration.substitutionConfiguration.workMethod,
                                dfuConfiguration.substitutionConfiguration.workMethodParameters) + ";";

        String cleanup = dfuConfiguration.substitutionConfiguration.cleanupMethod == null ? "" :
                substitute_identifier + "." +
                        methodStringGenerator(dfuConfiguration.substitutionConfiguration.cleanupMethod,
                                dfuConfiguration.substitutionConfiguration.cleanupMethodParameters) + ";";

        String point_identifier = "$" + dfuConfiguration.controlPointUuid;
        String dp = point_identifier + "-declaration";
        String ip = point_identifier + "-init";
        String wp = point_identifier + "-work";
        String cp = point_identifier + "-cleanup";

        ControlPointProfile cpc = applicationInstance.profileConfiguration.getControlPointByUuid(dfuConfiguration.controlPointUuid);

        for (String filepath : cpc.synthesisTargetFiles) {

            List<String> sourceLines = Files.readAllLines(applicationInstance.getApplicationPath().resolve(filepath));
            List<String> targetLines = new LinkedList<>();

            for (String sourceLine : sourceLines) {
                if (sourceLine.contains(point_identifier)) {
                    if (sourceLine.contains(dp)) {
                        targetLines.add(sourceLine.replace(dp, declaration));

                    } else if (sourceLine.contains(ip)) {
                        targetLines.add(sourceLine.replace(ip, initialization));

                    } else if (sourceLine.contains(wp)) {
                        targetLines.add(sourceLine.replace(wp, work));

                    } else if (sourceLine.contains(cp)) {
                        targetLines.add(sourceLine.replace(cp, cleanup));

                    } else {
                        throw new CompositionException.SubstitutionImproperlyFormattedControlPoint(
                                applicationInstance.getApplicationPath().resolve(filepath).toString(), sourceLine);
                    }
                } else {
                    targetLines.add(sourceLine);
                }
            }

            Files.write(applicationInstance.getApplicationPath().resolve(filepath), targetLines);
        }

//        List<String> dependencyLines = new LinkedList<>();

//        if (applicationInstance.profileConfiguration.deploymentPlatform == DeploymentPlatform.Android) {
//            dependencyLines.add("apply plugin: 'com.android.application'");


//        } else if (applicationInstance.profileConfiguration.deploymentPlatform == DeploymentPlatform.Java) {
//            dependencyLines.add("apply plugin: 'java'");
//        }

        applicationInstance.addDependency(dfuConfiguration.substitutionConfiguration.dependencyString);

//        dependencyLines.add("{");
//        dependencyLines.add("compile '" + dfuConfiguration.substitutionConfiguration.dependencyString + "'");
//        dependencyLines.add("}");
//        Files.write(applicationInstance.getApplicationPath().resolve(applicationInstance.profileConfiguration.getGradleTargetFile()), dependencyLines);


        Path appPath = applicationInstance.getApplicationPath();
        for (String sourceFile : cpc.fileCopyMap.keySet()) {
            Files.copy(appPath.resolve(sourceFile), appPath.resolve(cpc.fileCopyMap.get(sourceFile)), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
