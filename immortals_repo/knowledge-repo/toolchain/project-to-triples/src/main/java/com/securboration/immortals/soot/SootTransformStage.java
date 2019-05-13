package com.securboration.immortals.soot;

import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.constraint.WrapperImplementationImpact;
import com.securboration.immortals.ontology.dfu.DfuModule;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureSolution;
import com.securboration.immortals.ontology.lang.SourceFile;
import com.securboration.immortals.ontology.lang.WrapperSourceFile;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.utility.GradleTaskHelper.AssertableSolutionSet;
import com.securboration.immortals.wrapper.Wrapper;
import com.securboration.immortals.wrapper.WrapperFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.securboration.immortals.aframes.AnalysisFrameAssessment.*;

public class SootTransformStage {

    private String fullyQualifiedName;

    private String projectUUID;

    private Set<File> projectSpecificDependencies;

    private Set<FunctionalAspectInstance> aspectsToIntroduce;

    private UserAppInfo userAppInfo;

    private AspectConfigureSolution chosenSolution;

    private SootTransformStage(String _fullyQualifiedName, String _projectUUID, Set<File> _projectSpecificDependencies,
                               Set<FunctionalAspectInstance> _aspectsToIntroduce, UserAppInfo _userAppInfo, AspectConfigureSolution _chosenSolution) {
        fullyQualifiedName = _fullyQualifiedName;
        projectUUID = _projectUUID;
        projectSpecificDependencies = _projectSpecificDependencies;
        aspectsToIntroduce = _aspectsToIntroduce;
        userAppInfo = _userAppInfo;
        chosenSolution = _chosenSolution;
    }

    public static Optional<SootTransformStage> constructStage(GradleTaskHelper taskHelper, MethodInvocationDataflowNode streamInitNode, String projectUUID,
                                                              Set<File> projectSpecificDependencies, FunctionalAspectInstance aspectToIntroduce,
                                                              Collection<SootTransformStage> currentStages, List<MethodInvocationDataflowNode> methodNodes,
                                                              String nodeClassOwner, AspectConfigureSolution aspectConfigureSolution) {

        AssertableSolutionSet ownerSourceSolutions = getWrapperInsertionSiteContext(taskHelper, nodeClassOwner);
        String applicationSource;
        List<String> applicationSourceLines = new ArrayList<>();
        if (!ownerSourceSolutions.getSolutions().isEmpty()) {
            applicationSource = ownerSourceSolutions.getSolutions().get(0).get("source");
            applicationSourceLines = Arrays.asList(applicationSource.split("\n"));
        }

        if (currentStages.isEmpty()) {

            Set<FunctionalAspectInstance> stageAspects = new HashSet<>();
            stageAspects.add(aspectToIntroduce);

            UserAppInfo userAppInfo = new UserAppInfo();
            userAppInfo.setInitializerMethodName(streamInitNode.getJavaMethodName());
            userAppInfo.setInitializerLineNumber(streamInitNode.getLineNumber());
            userAppInfo.setUserAppLines(applicationSourceLines);
            userAppInfo.setProblematicMethodNodes(methodNodes);
            userAppInfo.setInitNodeClassOwner(nodeClassOwner);

            return Optional.of(new SootTransformStage(streamInitNode.getJavaClassName(), projectUUID, projectSpecificDependencies, stageAspects,
                    userAppInfo, aspectConfigureSolution));

        } else {

            Collection<SootTransformStage> modifyExistingStage = currentStages.stream().filter(stage -> stage.projectUUID.equals(projectUUID) &&
                    stage.fullyQualifiedName.equals(streamInitNode.getJavaClassName())).collect(Collectors.toList());

            if (!modifyExistingStage.isEmpty()) {

                SootTransformStage existingStage = modifyExistingStage.iterator().next();
                if (existingStage.aspectsToIntroduce.stream().anyMatch(aspect -> aspect.getAbstractAspect()
                        == aspectToIntroduce.getAbstractAspect())) {
                    return Optional.empty();
                } else {
                    existingStage.addNewAspect(aspectToIntroduce);
                    existingStage.addNewMethodNodes(methodNodes);
                    return Optional.empty();
                }

            } else {

                Set<FunctionalAspectInstance> stageAspects = new HashSet<>();
                stageAspects.add(aspectToIntroduce);

                UserAppInfo userAppInfo = new UserAppInfo();
                userAppInfo.setInitializerMethodName(streamInitNode.getJavaMethodName());
                userAppInfo.setInitializerLineNumber(streamInitNode.getLineNumber());
                userAppInfo.setUserAppLines(applicationSourceLines);
                userAppInfo.setProblematicMethodNodes(methodNodes);
                userAppInfo.setInitNodeClassOwner(nodeClassOwner);

                return Optional.of(new SootTransformStage(streamInitNode.getJavaClassName(), projectUUID, projectSpecificDependencies,
                        stageAspects, userAppInfo, aspectConfigureSolution));
            }
        }
    }

    @Deprecated
    public WrapperImplementationImpact transform(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config)
            throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException, XmlPullParserException {

        WrapperImplementationImpact analysisImpact = new WrapperImplementationImpact();
        analysisImpact.setProjectUUID(projectUUID);
        taskHelper.getPw().println("Instances found. Determining their design pattern...");
        boolean previouslyAugmented = false;

        Pair<File, BytecodeArtifactCoordinate> resourceFileToCoord = null;
        for (FunctionalAspectInstance aspectInstance : aspectsToIntroduce) {

            Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = AnalysisFrameAssessment.getDfuInstancePairMap(taskHelper, config, aspectInstance);

            String cipherImpl = null;
            String magicString = null;
            if (chosenSolution != null) {

                DfuInstance chosenInstance = chosenSolution.getChosenInstance();
                Pair<String, String> dfuUUIDToCipherImpl = null;
                for (DfuInstance dfuInstance : dfuInstanceStringMap.keySet()) {
                    if (chosenInstance.getClassPointer().equals(dfuInstance.getClassPointer())) {
                        dfuUUIDToCipherImpl = dfuInstanceStringMap.get(dfuInstance);
                    }
                }

                //TODO add cipher jar location
                String getUsageParadigmMagicString = getImplementationSpecificStrings(taskHelper,
                        dfuUUIDToCipherImpl.getLeft());
                GradleTaskHelper.AssertableSolutionSet usageParadigmSolutions = new GradleTaskHelper.AssertableSolutionSet();

                resourceFileToCoord = getDfuModuleDependency(taskHelper, dfuUUIDToCipherImpl.getLeft());
                projectSpecificDependencies.add(resourceFileToCoord.getLeft());

                System.err.println("MAGIC: " + getUsageParadigmMagicString == null ? "NULL" : getUsageParadigmMagicString);

                taskHelper.getClient().executeSelectQuery(getUsageParadigmMagicString, usageParadigmSolutions);

                magicString = usageParadigmSolutions.getSolutions().get(0).get("magicString");
                magicString = generateMagicString(taskHelper, magicString, chosenSolution, usageParadigmSolutions);
                cipherImpl = dfuUUIDToCipherImpl.getRight();
            } else {
                System.err.println("UNABLE TO FIND CHOSEN CONFIGURATION SOLUTION");
            }

            taskHelper.getPw().println("Instance with method pointer: " + aspectInstance.getMethodPointer() + " utilizes a stream design pattern." +
                    " Immortals will begin the process of wrapping the stream implementation in a custom class...");

            WrapperFactory wrapperFactory = null;
            Wrapper wrapper = null;
            WrapperSourceFile[] producedSourceFiles = new WrapperSourceFile[3];
            Optional<String> userSourceUUID = null;
            boolean plugin = analysisViaPlugin(taskHelper);

            switch (aspectInstance.getAbstractAspect().newInstance().getAspectId()) {

                case "cipherEncrypt": {

                    wrapperFactory = new WrapperFactory();
                    wrapper = initializeWrapper(fullyQualifiedName, projectSpecificDependencies.stream().map(
                            File::getAbsolutePath).collect(Collectors.toSet()), wrapperFactory, "java.io.OutputStream");

                    previouslyAugmented = wrapperFactory.wrapWithCipher(wrapper, cipherImpl, producedSourceFiles,
                            taskHelper, plugin, userAppInfo.getProblematicMethodNodes());
                    String cipherImplSyntaxSafe = cipherImpl.replace("/", ".");
                    cipherImplSyntaxSafe = cipherImplSyntaxSafe.substring(cipherImplSyntaxSafe.lastIndexOf(".") + 1);
                    wrapper.getCipherInfo().setCipherClassName(cipherImplSyntaxSafe);

                    userSourceUUID = augmentUserApplication(taskHelper, userAppInfo, wrapper);
                    checkForImplementationSpecificMethods(taskHelper, wrapper);
                    break;
                }

                case "cipherDecrypt": {

                    wrapperFactory = new WrapperFactory();
                    wrapper = initializeWrapper(fullyQualifiedName, projectSpecificDependencies.stream().map(
                            File::getAbsolutePath).collect(Collectors.toSet()), wrapperFactory, "java.io.InputStream");

                    previouslyAugmented = wrapperFactory.wrapWithCipher(wrapper, cipherImpl, producedSourceFiles,
                            taskHelper, plugin, userAppInfo.getProblematicMethodNodes());
                    String cipherImplSyntaxSafe = cipherImpl.replace("/", ".");
                    cipherImplSyntaxSafe = cipherImplSyntaxSafe.substring(cipherImplSyntaxSafe.lastIndexOf(".") + 1);
                    wrapper.getCipherInfo().setCipherClassName(cipherImplSyntaxSafe);

                    userSourceUUID = augmentUserApplication(taskHelper, userAppInfo, wrapper);
                    checkForImplementationSpecificMethods(taskHelper, wrapper);
                }
            }

            if (!previouslyAugmented) {
                String classFileLocation = wrapperFactory.produceWrapperClassFile(wrapper);
                File javaSource = decompileWrapperFoundation(taskHelper, wrapper, plugin,
                        classFileLocation);
                String source = FileUtils.readFileToString(javaSource);

                if (userSourceUUID.isPresent()) {
                    SourceFile userSourceFile = (SourceFile) TriplesToPojo.convert(taskHelper.getGraphName(),
                            userSourceUUID.get(), taskHelper.getClient());
                    analysisImpact.setAugmentedUserFile(userSourceFile);
                }

                WrapperSourceFile wrapperSourceFile = new WrapperSourceFile();
                wrapperSourceFile.setSource(source);
                wrapperSourceFile.setFileSystemPath(javaSource.getAbsolutePath());
                wrapperSourceFile.setFileName(javaSource.getName());
                producedSourceFiles[2] = wrapperSourceFile;
                analysisImpact.setProducedSourceFiles(producedSourceFiles);
                taskHelper.getClient().addToModel(ObjectToTriples.convert(config, wrapperSourceFile), taskHelper.getGraphName());
                //taskHelper.getClient().addToModel(ObjectToTriples.convert(config, analysisImpact), taskHelper.getGraphName());

                wrapper.getAspectsAdapted().add(aspectInstance.getAbstractAspect().newInstance());
                analysisImpact.setNewDependencies(Objects.requireNonNull(resourceFileToCoord).getRight());
                analysisImpact.setWrapperClassNameShort(wrapper.getWrapperClass().getShortJavaStyleName());

                if (magicString != null) {
                    // user specified configuration parameters, pass to code insertion stage
                    wrapper.getCipherInfo().setConfigurationParameters(Optional.of(magicString));
                } else {
                    wrapper.getCipherInfo().setConfigurationParameters(Optional.empty());
                }
                taskHelper.getWrappers().add(wrapper);

            } else {
                recordAspectAdaptation(taskHelper, aspectInstance.getAbstractAspect().newInstance(), wrapper);
            }
        }
        return analysisImpact;
    }

    @Deprecated
    private Pair<File, BytecodeArtifactCoordinate> getDfuModuleDependency(GradleTaskHelper taskHelper, String dfuUUID) throws IOException, XmlPullParserException {


        String getPathToProjectRepo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?repoPath where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???PROJECT_UUID???> IMMoRTALS:hasDfuModuleRepo ?dfuRepo .\n" +
                "\t\t\n" +
                "\t\t?dfuRepo IMMoRTALS:hasPathToRepo ?repoPath .\n" +
                "\n" +
                "\t}\n" +
                "}";
        getPathToProjectRepo = getPathToProjectRepo.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???PROJECT_UUID???", projectUUID);
        AssertableSolutionSet repoPathSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getPathToProjectRepo, repoPathSolutions);

        String repoPath = null;
        if (!repoPathSolutions.getSolutions().isEmpty()) {
            repoPath = repoPathSolutions.getSolutions().get(0).get("repoPath");
        } else {
            System.err.println("UNABLE TO FIND REPOSITORY PATH(S)");
        }

        String getDfuModuleInfo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?artifact ?group ?version where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t<???DFU_INSTANCE???> IMMoRTALS:hasClassPointer ?dfuHash .\n" +
                "\t\n" +
                "\t?dfuModule IMMoRTALS:hasCompiledSourceHash ?dfuHash\n" +
                "\t; IMMoRTALS:hasCoordinate ?coord .\n" +
                "\t\n" +
                "\t?coord IMMoRTALS:hasArtifactId ?artifact\n" +
                "\t; IMMoRTALS:hasGroupId ?group\n" +
                "\t; IMMoRTALS:hasVersion ?version .\n" +
                "\t\n" +
                "\t}\n" +
                "}";
        getDfuModuleInfo = getDfuModuleInfo.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                replace("???DFU_INSTANCE???", dfuUUID);
        AssertableSolutionSet dfuModuleInfoSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getDfuModuleInfo, dfuModuleInfoSolutions);


        BytecodeArtifactCoordinate dfuModuleCoordinate = new BytecodeArtifactCoordinate();
        dfuModuleCoordinate.setRepositoryPath(repoPath);

        if (!dfuModuleInfoSolutions.getSolutions().isEmpty()) {

            GradleTaskHelper.Solution dfuModuleInfoSolution = dfuModuleInfoSolutions.getSolutions().get(0);

            dfuModuleCoordinate.setArtifactId(dfuModuleInfoSolution.get("artifact"));
            dfuModuleCoordinate.setGroupId(dfuModuleInfoSolution.get("group"));
            dfuModuleCoordinate.setVersion(dfuModuleInfoSolution.get("version"));

        } else {

            //TODO dfu module might belong to a jar instead of user-project
            getDfuModuleInfo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "select ?artifact ?group ?version where {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\n" +
                    "\t<???DFU_INSTANCE???> IMMoRTALS:hasClassPointer ?pointer .\n" +
                    "\t\n" +
                    "\t\t?aClass IMMoRTALS:hasBytecodePointer ?pointer .\n" +
                    "\t\n" +
                    "\t\t?classArtifact IMMoRTALS:hasClassModel ?aClass .\n" +
                    "\t\n" +
                    "\t?dfuModule IMMoRTALS:hasJarContents ?classArtifact\n" +
                    "\t; IMMoRTALS:hasHash ?dfuHash\n" +
                    "\t; IMMoRTALS:hasCoordinate ?coord .\n" +
                    "\t\n" +
                    "\t?coord IMMoRTALS:hasArtifactId ?artifact\n" +
                    "\t; IMMoRTALS:hasGroupId ?group\n" +
                    "\t; IMMoRTALS:hasVersion ?version .\n" +
                    "\t\n" +
                    "\t}\n" +
                    "}";
            getDfuModuleInfo = getDfuModuleInfo.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                    replace("???DFU_INSTANCE???", dfuUUID);
            dfuModuleInfoSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getDfuModuleInfo, dfuModuleInfoSolutions);

            if (!dfuModuleInfoSolutions.getSolutions().isEmpty()) {
                GradleTaskHelper.Solution dfuModuleInfoSolution = dfuModuleInfoSolutions.getSolutions().get(0);

                dfuModuleCoordinate.setArtifactId(dfuModuleInfoSolution.get("artifact"));
                dfuModuleCoordinate.setGroupId(dfuModuleInfoSolution.get("group"));
                dfuModuleCoordinate.setVersion(dfuModuleInfoSolution.get("version"));

            } else {
                System.err.println("UNABLE TO OBTAIN DFU MODULE INFO");
            }
        }

       // Optional<File> dfuModuleResourceOption = searchForMavenModule(repoPath, dfuModuleCoordinate);

        return null;
    }

    public static Pair<File, DfuDependency> getDfuModuleDependency(GradleTaskHelper taskHelper, String dfuUUID,
                                                               String projectUUID) throws IOException, XmlPullParserException {

        String getPathToProjectRepo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?repoPath where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???PROJECT_UUID???> IMMoRTALS:hasDfuModuleRepo ?dfuRepo .\n" +
                "\t\t\n" +
                "\t\t?dfuRepo IMMoRTALS:hasPathToRepo ?repoPath .\n" +
                "\n" +
                "\t}\n" +
                "}";
        getPathToProjectRepo = getPathToProjectRepo.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???PROJECT_UUID???", projectUUID);
        AssertableSolutionSet repoPathSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getPathToProjectRepo, repoPathSolutions);

        String repoPath = null;
        if (!repoPathSolutions.getSolutions().isEmpty()) {
            repoPath = repoPathSolutions.getSolutions().get(0).get("repoPath");
        } else {
            System.err.println("UNABLE TO FIND REPOSITORY PATH(S)");
        }

        String getDfuModuleInfo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                "\n" +
                "select ?artifact ?group ?version where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t<???DFU_INSTANCE???> IMMoRTALS:hasClassPointer ?dfuHash .\n" +
                "\t\n" +
                "\t{\n" +
                "     ?classArt a IMMoRTALS_bytecode:ClassArtifact\n" +
                "\t ; IMMoRTALS:hasHash ?dfuHash .\n" +
                "      \n" +
                "      ?jarArt IMMoRTALS:hasJarContents ?classArt\n" +
                "\t ; IMMoRTALS:hasCoordinate ?coord .\n" +
                "    }\n" +
                "\tUNION\t\n" +
                "    { \n" +
                "      ?dfuModule IMMoRTALS:hasCompiledSourceHash ?dfuHash\n" +
                "\t  ; IMMoRTALS:hasCoordinate ?coord . \n" +
                "    }\n" +
                "\t\n" +
                "\t?coord IMMoRTALS:hasArtifactId ?artifact\n" +
                "\t; IMMoRTALS:hasGroupId ?group\n" +
                "\t; IMMoRTALS:hasVersion ?version .\n" +
                "\t\n" +
                "\t}\n" +
                "}";
        getDfuModuleInfo = getDfuModuleInfo.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                replace("???DFU_INSTANCE???", dfuUUID);
        AssertableSolutionSet dfuModuleInfoSolutions = new AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getDfuModuleInfo, dfuModuleInfoSolutions);

        BytecodeArtifactCoordinate dfuModuleCoordinate = new BytecodeArtifactCoordinate();
        dfuModuleCoordinate.setRepositoryPath(repoPath);
        if (!dfuModuleInfoSolutions.getSolutions().isEmpty()) {

            GradleTaskHelper.Solution dfuModuleInfoSolution = dfuModuleInfoSolutions.getSolutions().get(0);

            dfuModuleCoordinate.setArtifactId(dfuModuleInfoSolution.get("artifact"));
            dfuModuleCoordinate.setGroupId(dfuModuleInfoSolution.get("group"));
            dfuModuleCoordinate.setVersion(dfuModuleInfoSolution.get("version"));

        } else {

            //TODO dfu module might belong to a jar instead of user-project
            getDfuModuleInfo = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "select ?artifact ?group ?version where {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\n" +
                    "\t<???DFU_INSTANCE???> IMMoRTALS:hasClassPointer ?pointer .\n" +
                    "\t\n" +
                    "\t\t?aClass IMMoRTALS:hasBytecodePointer ?pointer .\n" +
                    "\t\n" +
                    "\t\t?classArtifact IMMoRTALS:hasClassModel ?aClass .\n" +
                    "\t\n" +
                    "\t?dfuModule IMMoRTALS:hasJarContents ?classArtifact\n" +
                    "\t; IMMoRTALS:hasHash ?dfuHash\n" +
                    "\t; IMMoRTALS:hasCoordinate ?coord .\n" +
                    "\t\n" +
                    "\t?coord IMMoRTALS:hasArtifactId ?artifact\n" +
                    "\t; IMMoRTALS:hasGroupId ?group\n" +
                    "\t; IMMoRTALS:hasVersion ?version .\n" +
                    "\t\n" +
                    "\t}\n" +
                    "}";
            getDfuModuleInfo = getDfuModuleInfo.replace("???GRAPH_NAME???", taskHelper.getGraphName()).
                    replace("???DFU_INSTANCE???", dfuUUID);
            dfuModuleInfoSolutions = new AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getDfuModuleInfo, dfuModuleInfoSolutions);

            if (!dfuModuleInfoSolutions.getSolutions().isEmpty()) {
                GradleTaskHelper.Solution dfuModuleInfoSolution = dfuModuleInfoSolutions.getSolutions().get(0);

                dfuModuleCoordinate.setArtifactId(dfuModuleInfoSolution.get("artifact"));
                dfuModuleCoordinate.setGroupId(dfuModuleInfoSolution.get("group"));
                dfuModuleCoordinate.setVersion(dfuModuleInfoSolution.get("version"));

            } else {
                System.err.println("UNABLE TO OBTAIN DFU MODULE INFO");
            }
        }

        DfuDependency dfuDependency = new DfuDependency();
        dfuDependency.setDependencyCoordinate(dfuModuleCoordinate);
        Optional<File> dfuModuleResourceOption = searchForMavenModule(repoPath, dfuDependency);

        return new Pair<>(dfuModuleResourceOption.orElse(null), dfuDependency);
    }

    public static Optional<File> searchForMavenModule(String repoPath, DfuDependency dfuDependency) throws IOException, XmlPullParserException {

        File repoPathFile = new File(repoPath);

        if (!repoPathFile.exists()) {
            System.err.println("UNABLE TO ACCESS PROVIDED REPO");
            return Optional.empty();
        }

        BytecodeArtifactCoordinate moduleCoordinate = dfuDependency.getDependencyCoordinate();

        String[] groupSplits = moduleCoordinate.getGroupId().split("\\.");
        Optional<File> repoProbeOption = Optional.empty();

        for (String groupSplit : groupSplits) {
            if (repoProbeOption.isPresent()) {

                File repoProbe = repoProbeOption.get();

                repoProbeOption = Arrays.stream(Objects.requireNonNull(repoProbe.listFiles((current, name) ->
                        new File(current, name).isDirectory()))).filter(dfuFile -> dfuFile.getName().equals(groupSplit)).findAny();

                if (!repoProbeOption.isPresent()) {
                    return Optional.empty();
                }

            } else {

                repoProbeOption = Arrays.stream(Objects.requireNonNull(repoPathFile.listFiles((current, name) ->
                        new File(current, name).isDirectory()))).filter(dfuFile -> dfuFile.getName().equals(groupSplit)).findAny();

                if (!repoProbeOption.isPresent()) {
                    return Optional.empty();
                }
            }
        }

        File repoProbe = repoProbeOption.get();

        repoProbeOption = Arrays.stream(Objects.requireNonNull(repoProbe.listFiles((current, name) ->
                new File(current, name).isDirectory()))).filter(dfuFile -> dfuFile.getName().equals(moduleCoordinate.getArtifactId())).findAny();

        if (!repoProbeOption.isPresent()) {
            System.err.println("UNABLE TO LOCATE PROVIDED DFU MODULE");
            return Optional.empty();
        }

        repoProbe = repoProbeOption.get();

        if (moduleCoordinate.getVersion().equals("+")) {
            // get latest version available
            Collection<File> availableVersions = FileUtils.listFiles(repoProbe, new String[]{}, false).stream().filter(
                    File::isDirectory).collect(Collectors.toList());
            File latestVersion = null;
            int version = -1;
            for (File availableVersion : availableVersions) {
                try {
                    int tempVersion = Integer.parseInt(availableVersion.getName());
                    if (version == -1) {
                        latestVersion = availableVersion;
                    } else {
                        if (tempVersion > version) {
                            latestVersion = availableVersion;
                        }
                    }
                } catch (NumberFormatException exc) {
                    continue;
                }
            }

            repoProbeOption = Optional.of(latestVersion);
        } else {
            repoProbeOption = Arrays.stream(Objects.requireNonNull(repoProbe.listFiles((current, name) ->
                    new File(current, name).isDirectory()))).filter(dfuFile -> dfuFile.getName().equals(moduleCoordinate.getVersion())).findAny();
        }

        if (!repoProbeOption.isPresent()) {
            System.err.println("UNABLE TO LOCATE PROVIDED DFU MODULE");
            return Optional.empty();
        }

        repoProbe = repoProbeOption.get();

        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        Optional<File> pomFileOption = FileUtils.listFiles(repoProbe, new String[]{"pom"}, false).stream().findFirst();

        if (!pomFileOption.isPresent()) {
            System.err.println("UNABLE TO FIND DFU MODULE PROJECT-FILE");
            return Optional.empty();
        }

        File pomFile = pomFileOption.get();
        Model pomModel = mavenReader.read(new FileInputStream(pomFile));

        List<DfuDependency> dfuDependencies = new ArrayList<>();
        for (Dependency mavenDependency : pomModel.getDependencies()) {

            DfuDependency dependencyOfDfu = new DfuDependency();
            BytecodeArtifactCoordinate dfuCoord = new BytecodeArtifactCoordinate();

            dfuCoord.setRepositoryPath(repoPath);
            dfuCoord.setGroupId(mavenDependency.getGroupId());
            dfuCoord.setArtifactId(mavenDependency.getArtifactId());
            dfuCoord.setVersion(mavenDependency.getVersion());

            dependencyOfDfu.setDependencyCoordinate(dfuCoord);
            dfuDependencies.add(dependencyOfDfu);
        }

        dfuDependency.setDependenciesOfDfu(dfuDependencies);

        switch (pomModel.getPackaging()) {

            case "jar":
                File pomDir = pomFile.getParentFile();
                FileFilter fileSuffixFilter = new SuffixFileFilter("jar");
                File[] jarFiles = pomDir.listFiles(fileSuffixFilter);

                if (jarFiles.length > 0) {

                    for (File jarFile : jarFiles) {
                        if (jarFile.getName().contains("sources")) {
                            continue;
                        } else {
                            return Optional.of(jarFile);
                        }
                    }

                } else {
                    System.out.println("UNABLE TO FIND DFU MODULE RESOURCE");
                    return Optional.empty();
                }

                break;
            default:
                break;
        }

        return Optional.empty();
    }

    private void addNewAspect(FunctionalAspectInstance newAspect) {
        aspectsToIntroduce.add(newAspect);
    }

    private void addNewMethodNodes(List<MethodInvocationDataflowNode> newMethodNodes) {
        userAppInfo.getProblematicMethodNodes().addAll(newMethodNodes);
    }
}
