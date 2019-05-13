package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.xml.RetrievalStrategy;
import com.securboration.immortals.ontology.java.project.AnalysisMetrics;
import com.securboration.immortals.ontology.resources.logical.LogicalResource;
import com.securboration.immortals.soot.ProjectInfo;
import com.securboration.immortals.utility.GradleTaskHelper;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;

import soot.G;
import soot.Scene;
import soot.SourceLocator;
import soot.options.Options;

import java.io.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class AdaptGradleTask extends ImmortalsGradleTask {

    @TaskAction
    public void adapt() throws Exception {

        Project p = getProject();
        int adaptTriples = 0;

        Project parentProj = null;
        while (parentProj == null || (parentProj.getParent() != null)) {
            parentProj = p.getParent();
        }

        try {
            String fusekiEndpoint = String.valueOf(p.getProperties().get("fusekiEndpoint"));
            initFuseki(fusekiEndpoint);
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE FUSEKI ENDPOINT, MAKE SURE PROPERTY IS SET");

        }

        String projArtifactId = p.getName();
        String projGroupId = p.getGroup().toString();
        String projVersion = p.getVersion().toString();

        String graphName =  projGroupId + "-" + projArtifactId + "-" + projVersion;
        BytecodeArtifactCoordinate currentProjectCoord = new BytecodeArtifactCoordinate();
        currentProjectCoord.setGroupId(projGroupId);
        currentProjectCoord.setArtifactId(projArtifactId);
        currentProjectCoord.setVersion(projVersion);
        String projectUUID = getProjectUUID(currentProjectCoord, graphName);

        String pluginOutput = null;
        //TODO get this from somewhere else
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        try  {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE PLUGIN OUTPUT DIR, MAKE SURE PROPERTY IS SET");
        }

        String xsdTranslationEndpoint = null;
        try {
            xsdTranslationEndpoint = String.valueOf(p.getProperties().get("xsdTranslationEndpoint"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE XSD TRANSLATION ENDPOINT, MAKE SURE PROPERTY IS SET");
        }

        String jarOutput = null;
        try {
            jarOutput = String.valueOf(p.getProperties().get("repairedJarOutputPath"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE JAR OUTPUT DIR, MAKE SURE PROPERTY IS SET");
        }

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, graphName, pluginOutput, p.getName());
        taskHelper.setPw(new PrintWriter(System.out));

        Long beginTime = System.currentTimeMillis();

        File userGradleHome = p.getGradle().getGradleUserHomeDir();
        userGradleHome = new File(userGradleHome.getAbsolutePath() + File.separator + "daemon" + File.separator +
                p.getGradle().getGradleVersion() + "/");
        initSootForDataflowAnalysis(projectUUID, p.getBuildDir().getAbsolutePath(), taskHelper, userGradleHome);
        ConstraintAssessment constraintAssessment = new ConstraintAssessment();
        File baseProjFile = parentProj == null ? p.getBuildDir() : parentProj.getProjectDir();

        List<File> projectDependencies = new ArrayList<>();
        Configuration configuration = p.getConfigurations().getByName("compile");
        configuration.forEach(file -> projectDependencies.add(file));

        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setBaseProjectFile(baseProjFile);
        projectInfo.setProjectCoordinate(currentProjectCoord);
        projectInfo.setProjectDependencies(projectDependencies);
        projectInfo.setProjectUUID(projectUUID);

        AnalysisMetrics analysisMetrics = constraintAssessment.injectAdaptationSurface(projectInfo,
                xsdTranslationEndpoint, taskHelper, adaptTriples, beginTime, config);

        RetrievalStrategy retrievalStrategy = null;
        for (Resource aspectConfigResource : constraintAssessment.getAspectConfigResources()) {
            if (aspectConfigResource instanceof LogicalResource) {
                // could be retrieval strategy
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

        Set<File> projectsEffected = new HashSet<>();
        File sootOutput = new File(SourceLocator.v().getOutputDir());
        if (!sootOutput.exists()) {
            throw new RuntimeException("no adaptation surfaces generated...");
        }

        Collection<File> newClassFiles = FileUtils.listFiles(sootOutput, new String[]{"class"}, true);
        Map<String, File> classFileToClassName = new HashMap<>();
        for (String adaptationSurface : analysisMetrics.getClassesModified()) {

            Optional<File> fileOption = newClassFiles.stream().filter(classFile -> classFile.getAbsolutePath()
                    .contains(adaptationSurface.replace(".", File.separator))).findFirst();
            fileOption.ifPresent(file -> classFileToClassName.put(adaptationSurface, file));

            String getAdaptationSurfaceOwner = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                    "\n" +
                    "select ?projectPath where {\n" +
                    "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t    \n" +
                    "\t\t?sourceFile IMMoRTALS:hasFullyQualifiedName \"???CLASS_NAME???\" .\n" +
                    "\t\t\n" +
                    "\t\t?sourceFileRepo IMMoRTALS:hasSourceFiles ?sourceFile .\n" +
                    "\t\t\n" +
                    "\t\t?javaProject IMMoRTALS:hasSourceCodeRepo ?sourceFileRepo\n" +
                    "\t\t; IMMoRTALS:hasBuildScript ?buildScript .\n" +
                    "\t\t\n" +
                    "\t\t?buildScript IMMoRTALS:hasProjectDir ?projectPath .\n" +
                    "\t\t\n" +
                    "\t}\n" +
                    "}";
            getAdaptationSurfaceOwner = getAdaptationSurfaceOwner.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                    .replace("???CLASS_NAME???", adaptationSurface);
            GradleTaskHelper.AssertableSolutionSet ownerSolutions = new GradleTaskHelper.AssertableSolutionSet();

            taskHelper.getClient().executeSelectQuery(getAdaptationSurfaceOwner, ownerSolutions);
            if (!ownerSolutions.getSolutions().isEmpty()) {

                String projectPath = ownerSolutions.getSolutions().get(0).get("projectPath");
                File projectPathFile = new File(projectPath);
                if (!projectPathFile.exists()) {
                    throw new RuntimeException("unable to find specified project path... make sure you are running all immortals analysis on same machine");
                }

                projectsEffected.add(projectPathFile);
            } else {
                throw new RuntimeException("Unable to find location of project that owns adaptation surface...");
            }
        }

        for(File projectEffected : projectsEffected) {
            Collection<File> jarFiles = FileUtils.listFiles(projectEffected, new String[]{"jar"}, true);
            File jarFile = null;
            if (jarFiles.size() > 1) {
                for (File tempJarFile : jarFiles) {
                    if (tempJarFile.getName().contains(projArtifactId) && !(tempJarFile.getName().contains("INST"))) {
                        jarFile = tempJarFile;
                        break;
                    }
                }
            } else {
                jarFile = jarFiles.iterator().next();
            }

            if (jarOutput.equals("null")) {
                jarOutput = jarFile.getParent();
                if (jarOutput.endsWith("/") || jarOutput.endsWith("\\")) {
                    jarOutput = jarOutput.substring(0, jarOutput.length() - 1);
                }
            }

            String newJarString = jarOutput + File.separator + jarFile.getName();
            newJarString = newJarString.substring(0, newJarString.lastIndexOf(".jar"));
            newJarString = (newJarString + "MODIFIED.jar");
            JarOutputStream newJar = new JarOutputStream(new FileOutputStream(newJarString));
            JarFile jar = new JarFile(jarFile);

            BufferedInputStream in = null;
            Enumeration<JarEntry> jarEntryEnumerator = jar.entries();
            Set<String> encounteredEntry = new HashSet<>();
            while (jarEntryEnumerator.hasMoreElements()) {
                JarEntry jarEntry = jarEntryEnumerator.nextElement();
                encounteredEntry.add(jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    newJar.putNextEntry(jarEntry);
                    newJar.closeEntry();
                } else {

                    String jarEntryName = jarEntry.getName().replace("/", ".");
                    Optional<String> classNameOption = Arrays.stream(analysisMetrics.getClassesModified()).filter(jarEntryName::contains).findFirst();
                    if (classNameOption.isPresent()) {

                        File classFile = classFileToClassName.get(classNameOption.get());
                        JarEntry newEntry = new JarEntry(jarEntry.getName());
                        newEntry.setTime(classFile.lastModified());
                        newJar.putNextEntry(newEntry);

                        in = new BufferedInputStream(new FileInputStream(classFile));
                        byte[] buffer = new byte[1024];
                        while (true)
                        {
                            int count = in.read(buffer);
                            if (count == -1)
                                break;
                            newJar.write(buffer, 0, count);
                        }

                        newJar.closeEntry();
                        continue;
                    }

                    newJar.putNextEntry(jarEntry);
                    in = new BufferedInputStream(jar.getInputStream(jarEntry));

                    byte[] buffer = new byte[1024];
                    while (true) {
                        int count = in.read(buffer);
                        if (count == -1) {
                            break;
                        }
                        newJar.write(buffer, 0, count);
                    }
                    newJar.closeEntry();
                }
            }

            for (File dfuJarFile : constraintAssessment.getNewDependencyFiles()) {

                JarFile dfuJar = new JarFile(dfuJarFile);
                Enumeration<JarEntry> dfuEnum = dfuJar.entries();
                while (dfuEnum.hasMoreElements()) {

                    JarEntry dfuEntry = dfuEnum.nextElement();
                    if (encounteredEntry.add(dfuEntry.getName())) {
                        if (dfuEntry.isDirectory()) {
                            newJar.putNextEntry(dfuEntry);
                            newJar.closeEntry();
                        } else {
                            newJar.putNextEntry(dfuEntry);
                            in = new BufferedInputStream(dfuJar.getInputStream(dfuEntry));

                            byte[] buffer = new byte[1024];
                            while (true) {
                                int count = in.read(buffer);
                                if (count == -1) {
                                    break;
                                }
                                newJar.write(buffer, 0, count);
                            }
                            newJar.closeEntry();
                        }
                    }
                }
            }

            if (retrievalStrategy.equals(RetrievalStrategy.FROM_CLASSPATH_RESOURCE)) {

                Optional<File> outgoingXsltOption = constraintAssessment.getXsltFileHandles().stream().filter(file -> file.getName().contains("outgoingXslt")).findFirst();
                if (!outgoingXsltOption.isPresent()) {
                    throw new RuntimeException("UNABLE TO FIND PRODUCED XSLT FILE");
                }

                File outgoingXslt = outgoingXsltOption.get();
                JarEntry outgoingXsltEntry = new JarEntry(outgoingXslt.getName());
                outgoingXsltEntry.setTime(outgoingXslt.lastModified());
                newJar.putNextEntry(outgoingXsltEntry);
                in = new BufferedInputStream(new FileInputStream(outgoingXslt));
                byte[] buffer = new byte[1024];
                while (true)
                {
                    int count = in.read(buffer);
                    if (count == -1)
                        break;
                    newJar.write(buffer, 0, count);
                }
                newJar.closeEntry();

                Optional<File> incomingXsltOption = constraintAssessment.getXsltFileHandles().stream().filter(file -> file.getName().contains("incomingXslt")).findFirst();
                if (!incomingXsltOption.isPresent()) {
                    throw new RuntimeException("UNABLE TO FIND PRODUCED XSLT FILE");
                }

                File incomingXslt = incomingXsltOption.get();
                JarEntry incomingXsltEntry = new JarEntry(incomingXslt.getName());
                incomingXsltEntry.setTime(incomingXslt.lastModified());
                newJar.putNextEntry(incomingXsltEntry);
                in = new BufferedInputStream(new FileInputStream(incomingXslt));
                buffer = new byte[1024];
                while (true)
                {
                    int count = in.read(buffer);
                    if (count == -1)
                        break;
                    newJar.write(buffer, 0, count);
                }
                newJar.closeEntry();
                incomingXslt.delete();

                in.close();
                outgoingXslt.delete();
                incomingXslt.delete();

            } else if (retrievalStrategy.equals(RetrievalStrategy.FROM_LOCAL_FILE_SYSTEM)) {

            }

            newJar.close();
        }
    }

    private final String ns = "${" + "immortalsNs"
            + ":"
            + "http://darpa.mil/immortals/ontology"
            + "}";

    private String getProjectUUID(BytecodeArtifactCoordinate coord, String graphName) {

        String getProjUUIDQuery = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?proj where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t?proj IMMoRTALS:hasCoordinate ?coord .\n" +
                "\t\n" +
                "\t?coord IMMoRTALS:hasGroupId \"???GROUP???\"\n" +
                "\t; IMMoRTALS:hasArtifactId \"???ARTIFACT???\"\n" +
                "\t; IMMoRTALS:hasVersion \"???VERSION???\" .\n" +
                "\t}\n" +
                "}";
        getProjUUIDQuery = getProjUUIDQuery.replace("???GRAPH_NAME???", graphName).replace("???GROUP???", coord.getGroupId())
                .replace("???ARTIFACT???", coord.getArtifactId()).replace("???VERSION???", coord.getVersion());
        GradleTaskHelper.AssertableSolutionSet projUUIDSolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(getProjUUIDQuery, projUUIDSolutions);

        if (!projUUIDSolutions.getSolutions().isEmpty()) {
            return projUUIDSolutions.getSolutions().get(0).get("proj");
        } else {
            return null;
        }
    }

    private void initSootForDataflowAnalysis(String projectUUID, String pathToProjClasses, GradleTaskHelper taskHelper, File gradleUserHome) throws IOException {

        copySootResources(gradleUserHome);
        G.reset();
        Options.v().set_whole_program(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_class);
        Options.v().set_no_writeout_body_releasing(true);
        //Options.v().set_java_version(Options.java_version_7);
        //TODO might need this PackManager.v().writeOutput();

        List<String> pathToDependencies = getProjectDependencies(projectUUID, taskHelper);
        StringBuilder sb = new StringBuilder();
        sb.append(Scene.v().getSootClassPath());
        sb.append(File.pathSeparatorChar);
        sb.append(pathToProjClasses);
        sb.append("/classes/java/main");
        sb.append(File.pathSeparatorChar);

        for (String pathToDependency : pathToDependencies) {
            sb.append(pathToDependency);
            sb.append(File.pathSeparatorChar);
        }

        //Scene.v().setSootClassPath(Scene.v().getSootClassPath()
               // + File.pathSeparatorChar + pathToProjClasses + "/classes/java/main/"
               // + File.pathSeparatorChar + pathToADSLJar);

        Scene.v().setSootClassPath(sb.toString());
        Scene.v().loadBasicClasses();
    }

    private void copySootResources(File gradleUserHome) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("javabase.txt");
        if (is == null) {
            throw new RuntimeException("ERROR LOADING SOOT RESOURCES");
        }

        File sootResources = new File(gradleUserHome.getAbsolutePath() + File.separator + "javabase.txt");
        sootResources.createNewFile();
        sootResources.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(sootResources)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private List<String> getProjectDependencies(String projectUUID, GradleTaskHelper taskHelper) {

        List<String> dependencyPaths = new ArrayList<>();

        String queryForProjectDependencies = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?dependencyPath where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t<???PROJ_UUID???> IMMoRTALS:hasDependencies ?dependencies .\n" +
                "\t\n" +
                "\t?dependencies IMMoRTALS:hasSystemPath ?dependencyPath .\n" +
                "\t}\n" +
                "}";
        queryForProjectDependencies = queryForProjectDependencies.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???PROJ_UUID???", projectUUID);
        GradleTaskHelper.AssertableSolutionSet dependencySolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(queryForProjectDependencies, dependencySolutions);

        if (!dependencySolutions.getSolutions().isEmpty()) {
            for (GradleTaskHelper.Solution dependencySolution : dependencySolutions.getSolutions()) {
                String dependencyPath = dependencySolution.get("dependencyPath");
                dependencyPaths.add(dependencyPath);
            }
        }

        return dependencyPaths;
    }

    private String performDataflowAnalysis(String pluginOutput, String projName) throws IOException {

        File dfDir = new File(pluginOutput + File.separator + projName + File.separator + "dataflows");
        if (dfDir.exists()) {
            return null;
        } else {
            dfDir.mkdir();
            return dfDir.getAbsolutePath();
        }
    }

}
