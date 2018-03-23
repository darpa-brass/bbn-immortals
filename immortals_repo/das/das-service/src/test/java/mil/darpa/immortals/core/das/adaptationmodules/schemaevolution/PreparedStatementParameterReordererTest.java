package mil.darpa.immortals.core.das.adaptationmodules.schemaevolution;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.DataLinkageMetadata;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;

public class PreparedStatementParameterReordererTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
        GradleKnowledgeBuilder gkb = new GradleKnowledgeBuilder();
        gkb.buildKnowledge(null);

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

	public void testStringParameterReorder() {
		        
        try {

    		LearnedQuery learnedQuery = new LearnedQuery();
    		
    		String learnedSql = "select s.source_id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley " + 
    				"from source s join cot_event ce on s.source_id = ce.source_id " + 
    				"join cot_event_position cep on ce.id = cep.cot_event_id " + 
    				"where servertime = ? and s.name = ?";
    		
    		List<Integer> parameterOrder = new ArrayList<>();
    		parameterOrder.add(new Integer(1));
    		parameterOrder.add(new Integer(0));
    		
    		DataLinkageMetadata dlm = new DataLinkageMetadata();
    		dlm.setClassName("mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForUid");
    		dlm.setSqlLineNumberEnd(50);
    		dlm.setSqlLineNumberStart(47);
    		
    		learnedQuery.setLearnedSql(learnedSql);
    		learnedQuery.setParameterOrder(parameterOrder);
    		learnedQuery.setDataLinkageMetadata(dlm);
    		
    		PreparedStatementParameterReorderer prx = new PreparedStatementParameterReorderer();
    		
    		Map<String, Object> parameters = new HashMap<>();
	        
	        DasAdaptationContext context = ContextManager.getContext("A001", "", "");
	
	        AdaptationTargetBuildInstance instance = GradleKnowledgeBuilder.getBuildInstance("TakServerDataManager", context.getAdaptationIdentifer());
	        Path adaptationSourceRoot = instance.getSourceRoot();
	        
	        Path sourceFile = adaptationSourceRoot.resolve("mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForUidAndInterval".replace(".", "/") + ".java");

			String DATA_DFU_SOURCE_FILE = ImmortalsConfig.getInstance().globals.getImmortalsRoot()
					.resolve("shared/modules/dfus/TakServerDataManager/src/main/java/mil/darpa/immortals/dfus/TakServerDataManager/DFU").toString();

			File dataDfuSourcePath = new File(DATA_DFU_SOURCE_FILE);

			CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(),
					new JavaParserTypeSolver(dataDfuSourcePath));

			ParserConfiguration parserConfiguration =
					new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

			JavaParser parser = new JavaParser(parserConfiguration);
			
			@SuppressWarnings("static-access")
			CompilationUnit cu = parser.parse(sourceFile);
			
			parameters.put(PreparedStatementParameterReorderer.PARAM_LEARNED_QUERY, learnedQuery);
			
			prx.resolvePattern(cu, typeSolver, parameters);

	        sourceFile.toFile().delete();
	        sourceFile.toFile().createNewFile();
	        
	        String code = cu.toString();
	        
	        Files.write(sourceFile, code.getBytes());
        } catch (Exception e) {
        	fail(e.getMessage());
        }
		
	}
	

}
