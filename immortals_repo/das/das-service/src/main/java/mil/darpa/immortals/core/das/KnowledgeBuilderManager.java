package mil.darpa.immortals.core.das;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.knowledgebuilders.IKnowledgeBuilder;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.SchemaDependencyKnowledgeBuilder;

public class KnowledgeBuilderManager {

	static final Logger logger = LoggerFactory.getLogger(KnowledgeBuilderManager.class);

	public static void initialize() {
		
		FileOutputStream out = null;
		
		try {
			//####Initialize SchemaDependencyKnowledgeBuilder#####
			//Get root folder of data DFUs
			String dataDFURoot = ImmortalsConfig.getInstance().globals.getImmortalsRoot()
					.resolve(DATA_DFU_SOURCE).toString();
			Map<String, Object> schemaParams = new HashMap<String, Object>();
			schemaParams.put(SchemaDependencyKnowledgeBuilder.PARAM_DATA_DFU_ROOT, dataDFURoot);
			
			//Invoke schema dependency knowledge builder so it generates a model of the data DFUs
			IKnowledgeBuilder kb = new SchemaDependencyKnowledgeBuilder();
			Model schemaModel = kb.buildKnowledge(schemaParams);
			
			//Write the model to the ingestion folder in TURTLE format
			Path knowledgeIngestionPath = ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();
    		File schemaKnowledgeFile = new File(knowledgeIngestionPath.toFile(), "schemaDependencies.ttl");
			
    		if (!schemaKnowledgeFile.exists()) {
				schemaKnowledgeFile.createNewFile();
			}
			
    		out = new FileOutputStream(schemaKnowledgeFile);

			schemaModel.write(out, "TURTLE");
			//#####################################################
		} catch (Exception e) {
			logger.error("Unexpected error initializing knowledge builders: " + e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					//Do nothing;releasing resources
				}
			}
		}
	}
	
	private static final String DATA_DFU_SOURCE = "shared/modules/dfus/TakServerDataManager/src/main/java/mil/darpa/immortals/dfus/TakServerDataManager/DFU";

}
