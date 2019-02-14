package mil.darpa.immortals.core.das.knowledgebuilders;

import java.util.Map;

import org.apache.jena.rdf.model.Model;

public interface IKnowledgeBuilder {

	public Model buildKnowledge(Map<String, Object> parameters) throws Exception;
	
}
