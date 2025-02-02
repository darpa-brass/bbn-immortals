package com.securboration.immortals.service.api;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ratiocinate.engine.RatiocinationEngine;
import com.securboration.immortals.ratiocinate.engine.RatiocinationReport;
import com.securboration.immortals.repo.model.build.JarIngestor;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;

import java.io.*;
import java.util.*;

/**
 * Second iteration of the knowledge repository service
 *
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/krs")
public class ConstraintAssessmentService {

    private static final Logger logger =
            LoggerFactory.getLogger(ConstraintAssessmentService.class);

    @Autowired(required = true)
    private ImmortalsServiceProperties properties;

    @Autowired(required = true)
    private RepositoryUnsafe repository;

    /**
     * Ingests a bag of triples relevant to a challenge problem from a local
     * file system location readable by this process. Generates constraint
     * violations and for each attempts to find a repair strategy that will
     * remove the violation. Each repair strategy specifies the use of DFUs
     * fulfilling some abstract functional role but does not determine how to
     * configure those DFUs. Note therefore that <b>this process stops short of
     * actually synthesizing an adaptation using the repair strategies
     * discovered</b>. Returns the name of a Fuseki graph containing all of the
     * discovered triples and additionally the repair strategies generated.
     *
     * @param path
     *            a path to a single uber turtle file or an arbitrarily nested
     *            directory containing turtle files that is readable by this
     *            process into a bag of triples. The resultant bag of triples
     *            will include:
     *            <ol>
     *            <li>bytecode analysis results emitted from previous runs of
     *            the project analyzer plugin (from ATAK, third party libs,
     *            MARTI, etc.)</li>
     *            <li>everything currently in the static ontology dir</li>
     *            <li>the deployment model (aka GmeInterchangeFormat)</li>
     *            <li>the UCR analysis artifact</li>
     *            </ol>
     * @return the name of the graph in which the resultant triples are saved
     * @throws Exception
     *             if something goes awry
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/ingest",
            produces=MediaType.TEXT_PLAIN_VALUE
    )
    public String ingest(
            @RequestBody
                    String path
    ) throws Exception {
        List<File> files = new ArrayList<>();

        final String version = properties.getImmortalsVersion();

        {//get a list of all TTL files to process
            File pathFile = new File(path);

            if(!pathFile.exists()){
                throw new RuntimeException("no file found for path " + path);
            }

            if(pathFile.isDirectory()){
                files.addAll(
                        FileUtils.listFiles(pathFile, new String[]{"ttl"}, true)
                );
            } else if(pathFile.isFile()) {
                files.add(pathFile);
            } else {
                throw new RuntimeException(path + " is not a normal file or dir");
            }
        }

        final String graphName = Helper.getImmortalsUuid(properties);

        {//load everything into fuseki
            for(File f:files){
                System.out.println("READING IN: " + f.getName());
                if (!(f.length() > 50000000)) {
                    Model m = ModelFactory.createDefaultModel();
                    
                    try {
                        m.read(
                                new ByteArrayInputStream(FileUtils.readFileToByteArray(f)),
                                null,
                                "TURTLE"
                        );
                    } catch (Exception exc) {
                        if (f.getParentFile().getName().contains("Pax")) {
                            System.out.println("read pax file...");
                            continue;
                        } else {
                            return null;
                        }
                    }
                    repository.appendToGraph(m, graphName);
                } else {
                    logger.warn("File: " + f.getName() + " is too large for analysis, skipping...");
                }
            }
        }

        InputStream jarStream =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ontology/immortals-ontologies-package-" + version + ".jar");

        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        IOUtils.copy(jarStream, jarBytes);

        JarIngestor.ingest(repository, jarBytes.toByteArray(), properties.getImmortalsNs(), version, graphName, ".ttl");

        {
            FusekiClient client = repository.getFusekiClient();
            RatiocinationEngine engine =
                    new RatiocinationEngine(client,graphName);

            RatiocinationReport report = engine.execute();

            logger.info(report.getReportText());//TODO: something else?
        }

        {//trigger the krgp analysis (adds new triples to graph)
            ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");
            GradleTaskHelper taskHelper = new GradleTaskHelper(repository.getFusekiClient(), graphName);
            taskHelper.setPw(new PrintWriter(System.out));
            ConstraintAssessment.constraintAnalysis(
                    taskHelper, config);
           // String assessmentUUID = ConstraintAssessment.createAdaptationSurface(taskHelper, config, Helper.getThirdPartyDependencies(taskHelper));

        }
        return graphName;
    }

    /**
     * Augments an existing model with new triples. Critically, this method
     * re-runs the inferencing engine after adding the provided triples to the
     * model.
     *
     * @param graphName
     *            the name returned by a previous call to
     *            {@link #ingest(String)}
     * @param ttlContent
     *            a TTL document containing knowledge that augments the
     *            indicated graph. E.g., this document might contain a
     *            configuration specification for some abstract functionality
     * @return the name of the graph provided
     * @throws Exception
     *             if something goes awry
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/append/{graphName:.+}",
            produces=MediaType.TEXT_PLAIN_VALUE
    )
    public String append(
            @PathVariable("graphName")
                    String graphName,

            @RequestBody
                    String ttlContent
    ) throws Exception {
        Model m = ModelFactory.createDefaultModel();
        m.read(
                new ByteArrayInputStream(ttlContent.getBytes()),
                null,
                "TURTLE"
        );
        repository.appendToGraph(m, graphName);
        
        return graphName;
    }

    /**
     * Attempts to synthesize adaptations to any discovered evolutionary
     * pressures while taking into account the knowledge identified after an
     * initial call to {@link #ingest(String)} that was provided via
     * {@link #append(String, String)}.
     *
     * @param graphName
     *            the name returned by a previous call to
     *            {@link #ingest(String)}
     * @return the URI of a newly created concept instance within the indicated
     *         graph that specifies the corrective actions to take to enact the
     *         change. E.g., this solution might describe changes that need to
     *         be made to several source files.
     * @throws Exception
     *             if something goes awry
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/adapt/{graphName:.+}",
            produces=MediaType.TEXT_PLAIN_VALUE
    )
    public String adapt(
            @PathVariable("graphName")
                    String graphName
    ) throws Exception {
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");
        GradleTaskHelper taskHelper = new GradleTaskHelper(repository.getFusekiClient(), graphName);
        taskHelper.setPw(new PrintWriter(System.out));
        String assessmentUUID = null;//ConstraintAssessment.createAdaptationSurface(taskHelper, config);
        return assessmentUUID;
    }

    private static class Helper{

        private static String getImmortalsUuid(ImmortalsServiceProperties p){
            return UUID.randomUUID().toString() +
                    "-IMMoRTALS-" +
                    p.getImmortalsVersion();
        }

        protected static String getLogPath(String projectPath, GradleTaskHelper.TaskType taskType) {
            String logPath = "";

            new File(projectPath +"/reports").mkdirs();
            if (taskType == GradleTaskHelper.TaskType.CONSTRAINT) {
                logPath = projectPath + "/reports/constraintLog" + ".txt";
            } else if (taskType == GradleTaskHelper.TaskType.ANALYSIS_FRAME) {
                logPath = projectPath + "/reports/frameLog" + ".txt";
            }
            return logPath;
        }

        protected static Set<File> getDependencies(GradleTaskHelper taskHelper) {

            Set<File> dependencies = new HashSet<>();

            String getDependencyPaths = "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                    "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n\n" +
                    "select ?filePaths where {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\n" +
                    "\t\t?project IMMoRTALS:hasClasspaths ?classPaths .\n" +
                    "\t\t\n" +
                    "\t\t?classPaths IMMoRTALS:hasClasspathName ?classPathName\n" +
                    "\t\t; IMMoRTALS:hasElementHashValues ?hashes .\n" +
                    "\n\t\tFILTER regex(?classPathName, \"compile\") .\n" +
                    "\t\t?jars a IMMoRTALS_bytecode:JarArtifact\n" +
                    "\t\t; IMMoRTALS:hasHash ?hashes\n" +
                    "\t\t; IMMoRTALS:hasFileSystemPath ?filePaths .\n" +
                    "\t}\n" +
                    "}";
            getDependencyPaths = getDependencyPaths.replace("???GRAPH_NAME???", taskHelper.getGraphName());
            GradleTaskHelper.AssertableSolutionSet dependencySolutions = new GradleTaskHelper.AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getDependencyPaths, dependencySolutions);

            for (GradleTaskHelper.Solution dependencySolution : dependencySolutions.getSolutions()) {
                dependencies.add(new File(dependencySolution.get("filePaths")));
            }

            return dependencies;
        }
    }

}


