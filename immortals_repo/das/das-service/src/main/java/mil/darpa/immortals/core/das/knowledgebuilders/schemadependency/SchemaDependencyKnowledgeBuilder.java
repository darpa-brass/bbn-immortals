package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;

import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;
import mil.darpa.immortals.core.das.knowledgebuilders.AbstractKnowledgeBuilder;
import mil.darpa.immortals.core.das.knowledgebuilders.generic.CodePatternResolver;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

public class SchemaDependencyKnowledgeBuilder extends AbstractKnowledgeBuilder {

	private static final String DAS_DB_USER = "immortals";
	private static final String DAS_DB_PWD = "immortals";
	private static final String DAS_DB_CURRENT_SCHEMA = "baseline";
	private static final String DAS_DB_URL = "jdbc:postgresql://localhost/immortals";	

	@Override
	public Model buildKnowledge(Map<String, Object> parameters) throws Exception {
		
		if (parameters == null || !parameters.containsKey(PARAM_DATA_DFU_ROOT)) {
			throw new InvalidOrMissingParametersException("Missing source directory for data DFUs.");
		}
		
		String dataDfuSource = (String) parameters.get(PARAM_DATA_DFU_ROOT);
		
		File dataDfuSourcePath = new File(dataDfuSource);

		CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(),
				new JavaParserTypeSolver(dataDfuSourcePath));

		ParserConfiguration parserConfiguration =
				new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

		SourceRoot sourceRoot = new SourceRoot(dataDfuSourcePath.toPath());
		sourceRoot.setParserConfiguration(parserConfiguration);
		
		List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse("");

		List<CompilationUnit> cus = parseResults.stream()
				.filter(ParseResult::isSuccessful)
				.map(r -> r.getResult().get()).collect(Collectors.toList());
		
		//System.out.println(new YamlPrinter(true).output(cu.findRootNode()));
		
		CodePatternResolver patternResolver = new PreparedStatementPatternResolver();
		
		dropAllTempDASTables();
		
		for (CompilationUnit cu : cus) {
			Map<String, Object> components = patternResolver.resolvePattern(cu, typeSolver);
			
			if (components != null && components.containsKey(PreparedStatementPatternResolver.DATA_ACCESS_DFU_METADATA)) {
				DataLinkageMetadata dataLinkageMetadata = 
					(DataLinkageMetadata) components.get(PreparedStatementPatternResolver.DATA_ACCESS_DFU_METADATA);

				Resource dataLinkage = createInstance(DATA_LINKAGE_CLASS);

				dataLinkage.addProperty(HAS_SQL, dataLinkageMetadata.getOriginalSql());
				
				String dfuClassName = cu.getPackageDeclaration().get().getChildNodes().get(0).toString() + 
						"." + cu.getPrimaryTypeName().get();
				
				dataLinkage.addProperty(HAS_CLASS_NAME, dfuClassName);

				dataLinkage.addLiteral(SQL_LINE_BEGIN, 
						dataLinkageMetadata.getSqlLineNumberStart());
				dataLinkage.addLiteral(SQL_LINE_END, 
						dataLinkageMetadata.getSqlLineNumberEnd());
				
				dataLinkage.addLiteral(HAS_SQL_VARIABLE_NAME, dataLinkageMetadata.getSqlVariableName());

				dataLinkageMetadata.setSqlMetadata(buildSqlMetadata(dataLinkageMetadata.getOriginalSql()));
				dataLinkage.addLiteral(CONTAINS_DISJUNCTIVE_FILTER, 
							dataLinkageMetadata.getSqlMetadata().isDisjunctiveFilter());
				
				dataLinkage.addLiteral(HAS_PROJECTION, 
						dataLinkageMetadata.getSqlMetadata().getProjectedIdentifiers()
							.stream().collect(Collectors.joining(",")));
				
				try (Connection conn = getDASConnection()) {
					SQLTransformer sqlT = new SQLTransformer();

					ResolvedQuery resolvedQuery = sqlT.getBoundQuery(dataLinkageMetadata, conn);
					String sampleSql = sqlT.getStableSampleSQL(dataLinkageMetadata, resolvedQuery.getResolvedQuery(), 
							POSITIVE_DATA_LIMIT);
					String positiveTrainingTable = sqlT.createTableForSQL(sampleSql, conn);
					dataLinkage.addLiteral(TRAINING_DATA_TABLE, positiveTrainingTable);
					
					String complementSql = sqlT.getComplementQuery(resolvedQuery.getResolvedQuery());
					String negativeSampleSql = sqlT.getStableSampleSQL(dataLinkageMetadata, 
							complementSql, NEGATIVE_DATA_LIMIT);
					String negativeTrainingTable = sqlT.createTableForSQL(negativeSampleSql, conn);
					dataLinkage.addLiteral(NEGATIVE_DATA_TABLE, negativeTrainingTable);
					
					int parameterIndex = 1;
					for (Parameter p : resolvedQuery.getParameters()) {
						Resource parameter = createInstance(PARAMETER);
						parameter.addLiteral(PARAMETER_VALUE, p.getValue().toString());
						parameter.addLiteral(PARAMETER_SQL_TYPE, p.getType());
						parameter.addLiteral(PARAMETER_POSITION, parameterIndex++);
						parameter.addLiteral(PARAMETER_COLUMN_NAME, p.getColumnName());
						dataLinkage.addProperty(HAS_PARAMETER, parameter);
					}
				}
			}
		}
		
		vacuumDASDB();
		
		return getModel();
	}
	
	private Connection getDASConnection() throws SQLException {

		Properties dbProps = new Properties();
		dbProps.setProperty("user", DAS_DB_USER);
		dbProps.setProperty("password", DAS_DB_PWD);
		dbProps.setProperty("currentSchema", DAS_DB_CURRENT_SCHEMA);
		
		Connection conn = DriverManager.getConnection(DAS_DB_URL, dbProps);
		
		Statement s = null;
		try {
			s = conn.createStatement();
			s.executeQuery("select setseed(" + SEED + ")");
		} finally {
			if (s != null) {
				s.close();
			}
		}

		return conn;
	}
	
	private void vacuumDASDB() throws SQLException {
		
		try (Connection conn = getDASConnection()) {
            conn.createStatement().execute("vacuum full analyze");			
		}
	}
	
	private void dropAllTempDASTables() throws Exception {
		
		String getTablesSQL = "select table_schema || '.' || table_name as TABLE_NAME " + 
				"from information_schema.tables " + 
				"where table_type = 'BASE TABLE' " + 
				"and table_schema = 'das' " + 
				"and table_name like 'temp_%'";

        try (Connection conn = getDASConnection(); ResultSet tables = conn.prepareStatement(getTablesSQL).executeQuery()) {
            while (tables.next()) {
                conn.createStatement().execute("DROP TABLE IF EXISTS " + tables.getString("TABLE_NAME") + " CASCADE");
            }
            conn.createStatement().execute("vacuum full analyze");
        }
	}
	
	private SQLMetadata buildSqlMetadata(String sql) throws JSQLParserException {
		
		SQLMetadata metadata = new SQLMetadata();

		CCJSqlParserManager pm = new CCJSqlParserManager();
		
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
		
		GenericSQLVisitor visitor = null;
		
		if (statement instanceof Select) {
			Select selectStatement = (Select) statement;
			visitor = new GenericSQLVisitor();

			selectStatement.getSelectBody().accept(visitor);
		}
		
		metadata.setDisjunctiveFilter(visitor.isDisjunctiveFilter());
		metadata.setParameters(visitor.getParameters());
		metadata.setProjectedIdentifiers(visitor.getProjection());
		
		return metadata;
	}
	
	public static final String PARAM_DATA_DFU_ROOT = "PARAM_DATA_DFU_ROOT";
	public static final int NEGATIVE_DATA_LIMIT = 5000;
	public static final int POSITIVE_DATA_LIMIT = 100_000;
	public static final double SEED = 0.5;
	
	public static final Model vocabulary = ModelFactory.createDefaultModel();
	
	//Existing hasClassName property; can be used to link to DFUs indirectly (thought if there are multiple
	//versions of the same class across different Java projects, this will be ambiguous)
	public static final Property HAS_CLASS_NAME = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_GENERAL_NS + "hasClassName");

	public static final Resource DATA_LINKAGE_CLASS = vocabulary.createResource(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "DataLinkage");
	public static final Resource PARAMETER = vocabulary.createResource(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "Parameter");
	
	public static final Property HAS_SQL = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasSQL");
	public static final Property CONTAINS_DISJUNCTIVE_FILTER = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "containDisjunctiveFilter");
	public static final Property SQL_LINE_BEGIN = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "startLineNumber");
	public static final Property SQL_LINE_END = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "endLineNumber");
	public static final Property TRAINING_DATA_TABLE = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "trainingDataTable");
	public static final Property NEGATIVE_DATA_TABLE = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "negativeDataTable");
	public static final Property PARAMETER_VALUE = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasParameterValue");
	public static final Property PARAMETER_SQL_TYPE = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasSQLType");
	public static final Property PARAMETER_POSITION = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasOrdinalPosition");
	public static final Property HAS_PARAMETER = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasParameter");
	public static final Property HAS_SQL_VARIABLE_NAME = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasSQLVariableName");
	public static final Property PARAMETER_COLUMN_NAME = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasColumnName");
	public static final Property HAS_PROJECTION = vocabulary.createProperty(AbstractKnowledgeBuilder.IMMORTALS_KNOWLEDGE_BUILDERS_NS + "hasProjection");
}
