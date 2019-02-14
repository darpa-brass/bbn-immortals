package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import mil.darpa.immortals.das.testing.DeploymentTests;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.darpa.immortals.config.ImmortalsConfig;
import org.junit.experimental.categories.Category;

public class SchemaDependencyKnowledgeBuilderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Category(DeploymentTests.class)
	@Test
	public void testDataDFUs() {

		SchemaDependencyKnowledgeBuilder kb = new SchemaDependencyKnowledgeBuilder();

		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(SchemaDependencyKnowledgeBuilder.PARAM_DATA_DFU_ROOT, DATA_DFU_SOURCE_DIR);
			Model model = kb.buildKnowledge(parameters);
			
			model.write(System.out, "TURTLE");
			
			//Verify one of the Data DFUs
			Selector selector = new SimpleSelector(null, SchemaDependencyKnowledgeBuilder.HAS_CLASS_NAME, 
					"mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantChannelJoin");
			StmtIterator iter = model.listStatements(selector);

			assert(iter.hasNext());
			Statement stmt = iter.next();
			Resource subject = stmt.getSubject();
			Property p = null;
			RDFNode n = null;
			iter = model.listStatements(subject, p, n);
			Map<String, String> propertyValues = new HashMap<String, String>();
			while (iter.hasNext()) {
				stmt = iter.next();
				propertyValues.put(stmt.getPredicate().getURI(), stmt.getObject().toString());
			}
			
			//Ground truth for "mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantChannelJoin"
			String sql = "select s.name, ce.id, ce.cot_type, ce.servertime " + 
						"from source s join cot_event ce on s.source_id = ce.source_id " + 
						"where channel = 7";
			String start = "38^^http://www.w3.org/2001/XMLSchema#long";
			String end = "40^^http://www.w3.org/2001/XMLSchema#long";
			String booleanFalse = "false^^http://www.w3.org/2001/XMLSchema#boolean";
			
			assert(propertyValues.get(SchemaDependencyKnowledgeBuilder.CONTAINS_DISJUNCTIVE_FILTER.getURI()).equals(booleanFalse));
			assert(propertyValues.get(RDF.type.getURI()).equals(SchemaDependencyKnowledgeBuilder.DATA_LINKAGE_CLASS.toString()));
			assert(propertyValues.get(SchemaDependencyKnowledgeBuilder.HAS_SQL.getURI()).equals(sql));
			assert(propertyValues.get(SchemaDependencyKnowledgeBuilder.SQL_LINE_BEGIN.getURI()).equals(start));
			assert(propertyValues.get(SchemaDependencyKnowledgeBuilder.SQL_LINE_END.getURI()).equals(end));
		} catch (Exception e) {
			fail(e.toString());
		}

	}
	
	private final String DATA_DFU_SOURCE_DIR = ImmortalsConfig.getInstance().globals.getImmortalsRoot()
			.resolve("shared/modules/dfus/TakServerDataManager/src/main/java/mil/darpa/immortals/dfus/TakServerDataManager/DFU").toString();

}
