package com.securboration.immortals.soot;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.constraint.InjectionImpact;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.xml.RetrievalStrategy;
import com.securboration.immortals.ontology.functionality.xml.XsltEmbedStrategy;
import com.securboration.immortals.ontology.resources.logical.LogicalResource;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import soot.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SootXmlTransformer {
    
    public static SootMethod generateXmlTransformMethod(GradleTaskHelper taskHelper, InjectionImpact injectionImpact,
                                                        BytecodeArtifactCoordinate currentProjCoord, ConstraintAssessment constraintAssessment, String projUUID) throws IOException, XmlPullParserException {

        String queryForXmlTransformers = "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                "prefix IMMoRTALS_property_impact: <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#>\n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select distinct ?dfu ?methodName ?className ?abstractAspect where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t    ?dfu a IMMoRTALS_dfu_instance:DfuInstance\n" +
                "\t\t; IMMoRTALS:hasFunctionalAspects ?aspectInstance .\n" +
                "\t\t\n" +
                "\t\t?aspectInstance IMMoRTALS:hasAbstractAspect ?abstractAspect\n" +
                "\t\t; IMMoRTALS:hasMethodPointer ?pointer .\n" +
                "\t\t\n" +
                "\t\t?abstractAspect IMMoRTALS:hasImpactStatements ?impacts .\n" +
                "\t\t\n" +
                "\t\t?impacts IMMoRTALS:hasApplicableResource ?applicableResource .\n" +
                "\t\t\n" +
                "\t\t?applicableResource a IMMoRTALS_property_impact:XmlResourceImpact\n" +
                "\t\t; IMMoRTALS:hasXmlResourceImpactType \"XML_INSTANCE_CHANGE\" .\n" +
                "\t\t\n" +
                "\t\t?classArt IMMoRTALS:hasClassModel ?aClass .\n" +
                "\t\t\n" +
                "\t\t?aClass IMMoRTALS:hasMethods ?methods\n" +
                "\t\t; IMMoRTALS:hasClassName ?className .\n" +
                "\t\t\n" +
                "\t\t?methods IMMoRTALS:hasBytecodePointer ?pointer\n" +
                "\t\t; IMMoRTALS:hasMethodName ?methodName .\n" +
                "\t}\n" +
                "}";
        queryForXmlTransformers = queryForXmlTransformers.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        GradleTaskHelper.AssertableSolutionSet dfuSolutions = new GradleTaskHelper.AssertableSolutionSet();

        taskHelper.getClient().executeSelectQuery(queryForXmlTransformers, dfuSolutions);
        if (!dfuSolutions.getSolutions().isEmpty()) {

            //TODO for now just take first one, later will likely want criteria for selecting "best" dfu instance
            GradleTaskHelper.Solution dfuSolution = dfuSolutions.getSolutions().get(0);
            String methodName = dfuSolution.get("methodName");
            String className = dfuSolution.get("className");
            String abstractAspect = dfuSolution.get("abstractAspect");
            String dfuUUID = dfuSolution.get("dfu");
            
            try {
                injectionImpact.setAspectImplemented((Class<? extends FunctionalAspect>) TriplesToPojo.convert(taskHelper.getGraphName(),
                        abstractAspect, taskHelper.getClient()));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                System.out.println("UNABLE TO RETRIEVE ASPECT IMPLEMENTED");
            }

            String getAspectConfigs = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "select ?configResources ?optional ?configResourceClass where {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\t\n" +
                    "\t\t<???ABSTRACT_ASPECT???> IMMoRTALS:hasAspectConfigurations ?configs .\n" +
                    "\t\t\n" +
                    "\t\t?configs IMMoRTALS:hasRequiredResource ?configResources\n" +
                    "\t\t; IMMoRTALS:hasOptional ?optional .\t\t\n" +
                    "\t\t\n" +
                    "\t\t?configResources a ?configResourceClass ." +
                    "\t}\n" +
                    "}";
            getAspectConfigs = getAspectConfigs.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ABSTRACT_ASPECT???", abstractAspect);
            GradleTaskHelper.AssertableSolutionSet aspectConfigSolutions = new GradleTaskHelper.AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getAspectConfigs, aspectConfigSolutions);

            List<Resource> aspectConfigResources = new ArrayList<>();
            if (!aspectConfigSolutions.getSolutions().isEmpty()) {
                //configurations might need to be set in order to proceed with adaptation surface generation

                for (GradleTaskHelper.Solution aspectConfigSolution : aspectConfigSolutions.getSolutions()) {

                   // String configUUID = aspectConfigSolution.get("configResources");
                    String configClassUUID = aspectConfigSolution.get("configResourceClass");
                    String optionalString = aspectConfigSolution.get("optional");
                    boolean optional = Boolean.parseBoolean(optionalString);

                    String requiredResourceUUID = getRequiredResource(taskHelper, configClassUUID, currentProjCoord);
                    if (requiredResourceUUID == null && !optional) {
                        throw new RuntimeException("REQUIRED CONFIG RESOURCE NOT FOUND");
                    } else if (requiredResourceUUID == null) {
                        //optional resource, it's okay if the user didn't provide resource
                        //TODO get default resource
                        continue;
                    } else {

                        String requiredResourceShort = requiredResourceUUID.substring(requiredResourceUUID.indexOf("#") + 1);

                        switch (requiredResourceShort) {
                            case "XsltEmbedStrategy":
                                RetrievalStrategy xsltRetrievalStrat = getRetrievalStratForXslt(taskHelper, requiredResourceUUID);
                                XsltEmbedStrategy xsltEmbedStrategy = new XsltEmbedStrategy();
                                xsltEmbedStrategy.setRetrievalStrategy(xsltRetrievalStrat);
                                aspectConfigResources.add(xsltEmbedStrategy);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            Pair<File, DfuDependency> newXsltDependency = SootTransformStage.getDfuModuleDependency(taskHelper, dfuUUID, projUUID);
            File newDependency = newXsltDependency.getLeft();
            Scene.v().setSootClassPath(Scene.v().getSootClassPath() + File.pathSeparator + newDependency.getAbsolutePath());

            SootClass owner = Scene.v().loadClassAndSupport(className.replace("/", "."));
            SootMethod dfuMethod = owner.getMethodByName(methodName);
            constraintAssessment.getAspectConfigResources().addAll(aspectConfigResources);

            return dfuMethod;
        }
       return null;
    }

    private static String getRequiredResource(GradleTaskHelper taskHelper, String configClassUUID, BytecodeArtifactCoordinate currentProjCoord) {

        String getRequiredResource = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?resources where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t\n" +
                "\t\t?coord IMMoRTALS:hasGroupId \"???GROUP_ID???\"\n" +
                "\t\t; IMMoRTALS:hasArtifactId \"???ARTIFACT_ID???\"\n" +
                "\t\t; IMMoRTALS:hasVersion \"???VERSION???\" .\n" +
                "\t\t\n" +
                "\t\t?arch IMMoRTALS:hasProjectCoordinate ?coord\n" +
                "\t\t; IMMoRTALS:hasAvailableResources ?resources .\n" +
                "\t\t\n" +
                "\t\t?resources a <???RESOURCE_CLASS???> .\n" +
                "\t}\n" +
                "}";
        getRequiredResource = getRequiredResource.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???GROUP_ID???",
                currentProjCoord.getGroupId()).replace("???ARTIFACT_ID???", currentProjCoord.getArtifactId()).replace("???VERSION???",
                currentProjCoord.getVersion()).replace("???RESOURCE_CLASS???", configClassUUID);
        GradleTaskHelper.AssertableSolutionSet requiredResourceSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getRequiredResource, requiredResourceSolutions);

        if (requiredResourceSolutions.getSolutions().isEmpty()) {
            return null;
        } else {
            return requiredResourceSolutions.getSolutions().get(0).get("resources");
        }
    }

    private static RetrievalStrategy getRetrievalStratForXslt(GradleTaskHelper taskHelper, String requiredResourceUUID) {

        String getRetrievalStrat = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?retrievalStrat where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???RESOURCE_UUID???> IMMoRTALS:hasRetrievalStrategy ?retrievalStrat .\n" +
                "\t}\n" +
                "}";
        getRetrievalStrat = getRetrievalStrat.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???RESOURCE_UUID???",
                requiredResourceUUID);
        GradleTaskHelper.AssertableSolutionSet retrievalStratSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getRetrievalStrat, retrievalStratSolutions);

        if (retrievalStratSolutions.getSolutions().isEmpty()) {
            return null;
        } else {
            String retrievalStrat = retrievalStratSolutions.getSolutions().get(0).get("retrievalStrat");
            return RetrievalStrategy.valueOf(retrievalStrat);
        }
    }

    public static DfuDependency searchForXsltRetriever(GradleTaskHelper taskHelper, String projectUUID, List<Resource> aspectConfigResources,
                                                       List<File> projectDependencies) throws IllegalAccessException {

        String getFileReaderQuery = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>\n" +
                "prefix IMMoRTALS_resources_xml: <http://darpa.mil/immortals/ontology/r2.0.0/resources/xml#>\n" +
                "prefix IMMoRTALS_functionality_imagecapture: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/imagecapture#>\n" +
                "prefix IMMoRTALS_functionality_datatype: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#>\n" +
                "prefix IMMoRTALS_functionality_imagecapture: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/imagecapture#>\n" +
                "\n" +
                "select distinct ?methodName ?className ?abstractAspect ?dfu ?resourceDependencies where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t    ?dfu a IMMoRTALS_dfu_instance:DfuInstance\n" +
                "\t\t; IMMoRTALS:hasFunctionalityAbstraction ?functionalityAbstract\n" +
                "\t\t; IMMoRTALS:hasFunctionalAspects ?aspectInstance .\n" +
                "\n" +
                "\t\tOPTIONAL {?functionalityAbstract IMMoRTALS:hasResourceDependencies ?resourceDependencies .} \n" +
                "\t\t?aspectInstance IMMoRTALS:hasAbstractAspect ?abstractAspect\n" +
                "\t\t; IMMoRTALS:hasMethodPointer ?pointer .\n" +
                "\t\t\n" +
                "\t\t?abstractAspect IMMoRTALS:hasInputs ?in\n" +
                "\t\t;IMMoRTALS:hasOutputs ?out .\n" +
                "\t\t\n" +
                "\t\t?in IMMoRTALS:hasType IMMoRTALS_functionality_imagecapture:FileHandle .\n" +
                "\t\t?out IMMoRTALS:hasType IMMoRTALS_functionality_datatype:Text .\n" +
                "\t\t\n" +
                "\t\t?classArt IMMoRTALS:hasClassModel ?aClass .\n" +
                "\t\t\n" +
                "\t\t?aClass IMMoRTALS:hasMethods ?methods\n" +
                "\t\t; IMMoRTALS:hasClassName ?className .\n" +
                "\t\t\n" +
                "\t\t?methods IMMoRTALS:hasBytecodePointer ?pointer\n" +
                "\t\t; IMMoRTALS:hasMethodName ?methodName .\n" +
                "\t}\n" +
                "}";
        getFileReaderQuery = getFileReaderQuery.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        GradleTaskHelper.AssertableSolutionSet fileReaderSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getFileReaderQuery, fileReaderSolutions);

        //need to know how to place/retrieve xslt
        RetrievalStrategy retrievalStrategy = null;
        if (aspectConfigResources.isEmpty()) {
            //go to default
            retrievalStrategy = RetrievalStrategy.FROM_LOCAL_FILE_SYSTEM;
        } else {
            for (Resource aspectConfigResource : aspectConfigResources) {
                if (aspectConfigResource instanceof LogicalResource) {
                    // could be retrieval strategyb
                    for (Field field : aspectConfigResource.getClass().getDeclaredFields()) {
                        if (field.getType().equals(RetrievalStrategy.class)) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            retrievalStrategy = (RetrievalStrategy) field.get(aspectConfigResource);
                            break;
                        }
                    }
                }
                if (retrievalStrategy != null) {
                    break;
                }
            }
        }

        for (GradleTaskHelper.Solution fileReaderSolution : fileReaderSolutions.getSolutions()) {

            String methodName = fileReaderSolution.get("methodName");
            String className = fileReaderSolution.get("className");
            String dfuUUID = fileReaderSolution.get("dfu");
            String resourceDependency = fileReaderSolution.get("resourceDependencies");
            String resourceShortName = resourceDependency.substring(resourceDependency.indexOf("#") + 1);

            if (resourceShortName.equals(FILE_SYSTEM_RESOURCE_ID) && !(retrievalStrategy.equals(RetrievalStrategy.FROM_LOCAL_FILE_SYSTEM))) {
                continue;
            } else if (resourceShortName.equals(CLASSPATH_RESOURCE_ID) && !(retrievalStrategy.equals(RetrievalStrategy.FROM_CLASSPATH_RESOURCE))) {
                continue;
            }

            try {
                Pair<File, DfuDependency> newDependencyPair = SootTransformStage.getDfuModuleDependency(taskHelper, dfuUUID, projectUUID);
                File newDependencyFile = newDependencyPair.getLeft();
                Scene.v().setSootClassPath(Scene.v().getSootClassPath() + File.pathSeparator + newDependencyFile.getAbsolutePath());

                SootClass owner = Scene.v().loadClassAndSupport(className.replace("/", "."));
                SootMethod fileReaderMethod = owner.getMethodByName(methodName);

                DfuDependency dfuDependency = newDependencyPair.getRight();//new DfuDependency(fileReaderMethod, newDependencyPair.getRight(), newDependency);
                dfuDependency.setInvokedDfuMethod(fileReaderMethod);
                dfuDependency.setDependencyFile(newDependencyFile);
                parseDfuDependencies(dfuDependency, projectDependencies);

                return dfuDependency;

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static void parseDfuDependencies(DfuDependency dfuDependency, List<File> projectDependencies) throws IOException, XmlPullParserException {

        Collection<BytecodeArtifactCoordinate> dependencyCoordinates = parseCoordinates(projectDependencies);
        for (DfuDependency dependencyOfDfu : dfuDependency.getDependenciesOfDfu()) {

            Pair<DependencyStatus, BytecodeArtifactCoordinate> dependencyStatus = getDependencyStatus(dependencyOfDfu, dependencyCoordinates);

            switch (dependencyStatus.getLeft()) {

                case PRESENT:
                    // dependency is already present, no action needed
                    break;
                case COLLISION:
                    //TODO resolve conflict
                    throw new RuntimeException("VERSION COLLISION BETWEEN DEPENDENCIES!");
                case ABSENT:
                    Optional<File> dfuModuleResourceOption = SootTransformStage.searchForMavenModule(
                            dependencyStatus.getRight().getRepositoryPath(), dependencyOfDfu);
                    //TODO dependency of a dependency of a dependency... recursive dependencies
                    if (!dependencyOfDfu.getDependenciesOfDfu().isEmpty()) {}

                    if (dfuModuleResourceOption.isPresent()) {
                        dependencyOfDfu.setDependencyFile(dfuModuleResourceOption.get());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static Collection<BytecodeArtifactCoordinate> parseCoordinates(List<File> projectDependencies) throws IOException, XmlPullParserException {

        List<BytecodeArtifactCoordinate> projectCoordinates = new ArrayList<>();

        for (File projectDependency : projectDependencies) {

            File dependencyDir = projectDependency.getParentFile();
            if (!dependencyDir.isDirectory()) {
                throw new RuntimeException("Parent of dependency should be a directory");
            }
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Optional<File> pomFileOption = FileUtils.listFiles(dependencyDir, new String[]{"pom"}, false).stream().findFirst();

            if (!pomFileOption.isPresent()) {
                continue;
                //throw new RuntimeException("UNABLE TO FIND DFU MODULE PROJECT-FILE");
            }

            File pomFile = pomFileOption.get();
            Model pomModel = mavenReader.read(new FileInputStream(pomFile));

            BytecodeArtifactCoordinate projectCoord = new BytecodeArtifactCoordinate();
            if (pomModel.getGroupId() != null) {
                projectCoord.setGroupId(pomModel.getGroupId());
            } else {
                projectCoord.setGroupId(pomModel.getParent().getGroupId());
            }

            projectCoord.setArtifactId(pomModel.getArtifactId());

            if (pomModel.getVersion() != null) {
                projectCoord.setVersion(pomModel.getVersion());
            } else {
                projectCoord.setVersion(pomModel.getParent().getVersion());
            }

            projectCoordinates.add(projectCoord);
        }

        return projectCoordinates;
    }

    private static Pair<DependencyStatus, BytecodeArtifactCoordinate> getDependencyStatus(DfuDependency dependencyOfDfu,
                                                                Collection<BytecodeArtifactCoordinate> currentDependencies) {

        BytecodeArtifactCoordinate dependencyCoord = dependencyOfDfu.getDependencyCoordinate();
        Optional<BytecodeArtifactCoordinate> currentCoordOption;

        currentCoordOption = currentDependencies.stream().filter(coord ->
                (dependencyCoord.getGroupId().equals(coord.getGroupId())) &&
                (dependencyCoord.getArtifactId().equals(coord.getArtifactId())) &&
                (dependencyCoord.getVersion().equals(coord.getVersion()))).findAny();

        if (currentCoordOption.isPresent()) {
            return new Pair<>(DependencyStatus.PRESENT, currentCoordOption.get());
        }

        currentCoordOption = currentDependencies.stream().filter(coord ->
                (dependencyCoord.getGroupId().equals(coord.getGroupId())) &&
                (dependencyCoord.getArtifactId().equals(coord.getArtifactId())) &&
                !(dependencyCoord.getVersion().equals(coord.getVersion()))).findAny();

        if (currentCoordOption.isPresent()) {
            return new Pair<>(DependencyStatus.COLLISION, currentCoordOption.get());
        }

        return new Pair<>(DependencyStatus.ABSENT, dependencyCoord);
    }

    public static final String FILE_SYSTEM_RESOURCE_ID = "FileSystemResource";
    public static final String CLASSPATH_RESOURCE_ID = "ClasspathResource";
}
