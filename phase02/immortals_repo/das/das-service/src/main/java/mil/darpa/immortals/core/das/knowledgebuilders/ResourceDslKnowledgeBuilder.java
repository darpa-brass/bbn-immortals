package mil.darpa.immortals.core.das.knowledgebuilders;

import mil.darpa.immortals.config.ImmortalsConfig;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 6/27/18.
 */
public class ResourceDslKnowledgeBuilder implements IKnowledgeBuilder {
    
    private Logger logger = LoggerFactory.getLogger(ResourceDslKnowledgeBuilder.class);
    
    @Override
    public Model buildKnowledge(Map<String, Object> parameters) throws Exception {
        if (!ImmortalsConfig.getInstance().extensions.immortalizer.isPerformDslCompilation()) {
            logger.info("Skipping Resource DSL Compilation since it is disabled.");
            return null;
        }
        
        logger.info("Executing DSL Build.");
        
        Path workingDir = ImmortalsConfig.getInstance().dasService.getResourceDslPath();
        Path stackConfig = workingDir.resolve("stack.yaml");
        if (!Files.exists(stackConfig)) {
            throw new RuntimeException("Could not find expected resource dsl build configuration '" + stackConfig.toString() + "'!");
        }
        
        String[][] commands = {
                {"stack", "exec", "resource-dsl", "--", "example", "crossapp", "--dict", "--model", "--reqs"},
                {"stack", "exec", "resource-dsl", "--", "example", "crossapp", "--config-all"},
                {"stack", "exec", "resource-dsl", "--", "example", "crossapp", "--init-all"},
                {"stack", "exec", "resource-dsl", "--", "run"}
        };
        
        for (String[] command : commands) {
            ProcessBuilder pb = new ProcessBuilder(command);
            logger.info("EXEC: `" + pb.command().stream().collect(Collectors.joining(" ")) + "`");
            pb.inheritIO();
            pb.directory(workingDir.toFile());
            Process p = pb.start();
            p.waitFor(60, TimeUnit.MINUTES);
            if (p.isAlive()) {
                throw new RuntimeException("Command " + String.join(" ", command) + " has taken too long!"); 
            } else if (p.exitValue() != 0) {
                throw new RuntimeException("Command " + String.join(" ", command) + "Had a non-zero exit status!");
            }
        }
        return null;
    }
    
    public static void main(String[] args) {
        try {
            ResourceDslKnowledgeBuilder dkb = new ResourceDslKnowledgeBuilder();
            dkb.buildKnowledge(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
}
