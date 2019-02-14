package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class OutputUberGradleTask extends ImmortalsGradleTask {

    @TaskAction
    public void outputUberGraph() {

        Project p = getProject();

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

        try {
            String uberOutputDir = String.valueOf(p.getProperties().get("uberOutput"));

            Model m = ModelFactory.createDefaultModel();
            m.add(this.client.getModel(graphName));

            GradleTaskHelper.recordCPElement(OntologyHelper.serializeModel(m, "TTL", false), uberOutputDir
                    + File.separator + "uberOutput-" + projArtifactId + ".ttl");

        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO PRODUCE UBER OUTPUT FILE, MAKE SURE PROPERTY IS SET");
        }
    }
}
