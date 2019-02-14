package com.securboration.immortals.p2t.gradle.plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.tasks.TaskAction;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ratiocinate.engine.RatiocinationEngine;
import com.securboration.immortals.ratiocinate.engine.RatiocinationReport;
import com.securboration.immortals.repo.model.build.JarIngestor;
import com.securboration.immortals.utility.GradleTaskHelper;

public class IngestGradleTask extends ImmortalsGradleTask {

    @TaskAction
    public void ingest() throws Exception {

        Project p = getProject();

        String projArtifactId = p.getName();
        String projGroupId = p.getGroup().toString();
        String projVersion = p.getVersion().toString();

        List<File> files = new ArrayList<>();
        String pluginOutput = null;
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        try  {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE PLUGIN OUTPUT DIR, MAKE SURE PROPERTY IS SET");
        }

        try {
            String fusekiEndpoint = String.valueOf(p.getProperties().get("fusekiEndpoint"));
            initFuseki(fusekiEndpoint);
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE FUSEKI ENDPOINT, MAKE SURE PROPERTY IS SET");

        }

        final String version = "r2.0.0";

        {//get a list of all TTL files to process
            File pathFile = new File(pluginOutput);

            if(!pathFile.exists()){
                throw new RuntimeException("no file found for path " + pluginOutput);
            }

            if(pathFile.isDirectory()){
                files.addAll(
                        FileUtils.listFiles(pathFile, new String[]{"ttl"}, true)
                );
            } else if(pathFile.isFile()) {
                files.add(pathFile);
            } else {
                throw new RuntimeException(pluginOutput + " is not a normal file or dir");
            }
        }

        final String graphName = projGroupId + "-" + projArtifactId + "-" + projVersion;
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
                    } catch (FileNotFoundException exc) {
                        // relative path specified
                        m.read(
                                new ByteArrayInputStream(FileUtils.readFileToByteArray(f.getAbsoluteFile())),
                                null,
                                "TURTLE"
                        );
                    } catch (Exception exc) {
                        if (f.getParentFile().getName().contains("Pax")) {
                            System.out.println("read pax file...");
                            continue;
                        } else {
                            exc.printStackTrace();
                        }
                    }
                    client.addToModel(m, graphName);
                } else {
                    System.err.println("File: " + f.getName() + " is too large for analysis, skipping...");
                }
            }
        }

        InputStream jarStream =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ontology/immortals-ontologies-package-" + version + ".jar");

        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        IOUtils.copy(jarStream, jarBytes);

        int baseVocabTriples = JarIngestor.ingest(client, jarBytes.toByteArray(), ns, version, graphName, ".ttl");

        int inferenceTriplesAdded = 0;
        {
            RatiocinationEngine engine =
                    new RatiocinationEngine(client,graphName);

            RatiocinationReport report = engine.execute();
            inferenceTriplesAdded+=report.getTriplesAdded();
        }

        int domainTriplesAdded = 0;
        String domainKnowledge = null;
        try  {
            domainKnowledge = String.valueOf(p.getProperties().get("domainKnowledge"));

            File domainKnowledgeFile = new File(domainKnowledge);
            if (!domainKnowledgeFile.exists()) {
                throw new Exception();
            }

            Model m = ModelFactory.createDefaultModel();
            m.read(
                    new ByteArrayInputStream(FileUtils.readFileToByteArray(domainKnowledgeFile)),
                    null,
                    "TURTLE"
            );
            domainTriplesAdded+=m.getGraph().size();
            client.addToModel(m , graphName);
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE DOMAIN KNOWLEDGE TTL, MAKE SURE PROPERTY IS SET");
        }

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, graphName);
        taskHelper.setPw(new PrintWriter(System.out));
        ConstraintAssessment.architectureAnalysis(
                taskHelper, inferenceTriplesAdded, domainTriplesAdded, baseVocabTriples, config);
    }

    private final String ns = "${" + "immortalsNs"
            + ":"
            + "http://darpa.mil/immortals/ontology"
            + "}";

}
