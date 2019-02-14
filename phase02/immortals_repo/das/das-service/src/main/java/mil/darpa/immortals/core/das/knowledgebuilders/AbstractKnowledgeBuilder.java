package mil.darpa.immortals.core.das.knowledgebuilders;

import java.util.Map;
import java.util.UUID;

import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public abstract class AbstractKnowledgeBuilder implements IKnowledgeBuilder {
	
	public AbstractKnowledgeBuilder() {
		ARQ.init();
		model = ModelFactory.createDefaultModel();
	}
	
	protected Model getModel() {
		return model;
	}

	public abstract Model buildKnowledge(Map<String, Object> parameters) throws Exception;
	
	protected Resource createInstance(Resource resourceType) {
		
		Resource result = null;

		result = model.createResource(IMMORTALS_KNOWLEDGE_BUILDERS_NS + UUID.randomUUID().toString(), resourceType);
		
		return result;
	}
	
	private Model model = null;

	public static final String IMMORTALS_PREFIX = "http://darpa.mil/immortals/ontology/2.0-LOCAL";
	public static final String IMMORTALS_GENERAL_NS = IMMORTALS_PREFIX + "#";
	public static final String IMMORTALS_KNOWLEDGE_BUILDERS_NS = AbstractKnowledgeBuilder.IMMORTALS_PREFIX + "/KnowledgeBuilders#";

}
