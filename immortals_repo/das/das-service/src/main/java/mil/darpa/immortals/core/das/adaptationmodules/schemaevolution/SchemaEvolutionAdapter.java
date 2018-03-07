package mil.darpa.immortals.core.das.adaptationmodules.schemaevolution;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationmodules.AbstractAdaptationModule;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.DataLinkageMetadata;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.SQLTransformer;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.SchemaDependencyKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.DataDFU;
import mil.darpa.immortals.core.das.sparql.SchemaMigrationTarget;
import mil.darpa.immortals.das.context.DasAdaptationContext;

public class SchemaEvolutionAdapter extends AbstractAdaptationModule {
	
	private static PGPoolingDataSource dataSource;
	private static final String COT_DATA_SOURCE = "TakDataSource";
	private static final String SERVER_NAME = "localhost";
	private static final String DATABASE_NAME = "immortals";
	private static final String USER = "immortals";
	private static final String PASSWORD = "immortals";
	private static final int MAXIMUM_NUMBER_CONNECTIONS = 4;
	private static final String BASELINE_SCHEMA = "baseline";
	private DasAdaptationContext context = null;
    private static final Logger logger = LoggerFactory.getLogger(SchemaEvolutionAdapter.class);
    
	static {
		//Not the ideal pooling implementation, but should be fine for this project
		dataSource = new PGPoolingDataSource();
		dataSource.setDataSourceName(COT_DATA_SOURCE);
		dataSource.setServerName(SERVER_NAME);
		dataSource.setDatabaseName(DATABASE_NAME);
		dataSource.setUser(USER);
		dataSource.setPassword(PASSWORD);
		dataSource.setMaxConnections(MAXIMUM_NUMBER_CONNECTIONS);
		dataSource.setCurrentSchema(BASELINE_SCHEMA);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (dataSource != null) {
					dataSource.close();
				}
			}
		});
	}

	@Override
	public boolean isApplicable(DasAdaptationContext context) throws Exception {
		
		//Check the deployment model at URI specified in the context to see if schema has been updated
		//and possibly whether the update requires adaptation (based on the new version and existing version
		//the data DFUs depend on
		
		String migrationTarget = SchemaMigrationTarget.select(context.getDeploymentModelUri());

		return (migrationTarget != null && migrationTarget.trim().length() > 0);
	}
	
	private String learnQuery(DataLinkageMetadata dataLinkageMetadata) throws Exception {
		
		String learnedQuery = null;
		
		// This gets the per-submission folder for Castor
		Path castorSubmissionFolder = ImmortalsConfig.getInstance().extensions.castor.getExecutionWorkingDirectory(context.getAdaptationIdentifer());

		// This gets the per-das-startup folder for Castor
		//Path dasStartupForCastor = ImmortalsConfig.getInstance().extensions.castor.getWorkingDirectory();
		
		//Write positive training data to castor submission folder
		String positiveDataSQL = "select * from " + dataLinkageMetadata.getPositiveTrainingDataTableName();
		String negativeDataSQL = "select * from " + dataLinkageMetadata.getNegativeTrainingDataTableName();
		
		String positiveDataFile = castorSubmissionFolder.resolve("positive_data.csv").toString();
		String negativeDataFile = castorSubmissionFolder.resolve("negative_data.csv").toString();
		
		createDataFileFromSQL(positiveDataFile, positiveDataSQL);
		createDataFileFromSQL(negativeDataFile, negativeDataSQL);
		
		String recall = "1.0";
		if (dataLinkageMetadata.getSqlMetadata().isDisjunctiveFilter()) {
			recall = "0.1";
		}
		
		writeCastorParameters(recall, castorSubmissionFolder);
		
		int castorResult = runCastor(castorSubmissionFolder, dataLinkageMetadata);
		
		if (castorResult != 0) {
			logger.info("Castor exited with result code: " + castorResult);
			learnedQuery = null;
		} else {
			learnedQuery = readCastorOutput(castorSubmissionFolder);			
		}
		
			
		return learnedQuery;
	}
	
	private String readCastorOutput(Path castorSubmissionFolder) throws Exception {
		
		final String SQL_MARKER = "SQL FORMAT:";
		String sql = null;
		
		if (castorSubmissionFolder == null) {
			throw new InvalidOrMissingParametersException("castorSubmissionFolder");
		}
		
		try {
			Path definition = castorSubmissionFolder.resolve("definition.txt");
			String rawData = new String(Files.readAllBytes(definition));
			if (rawData != null || rawData.trim().length() > 0) {
				int sqlMarker = rawData.indexOf(SQL_MARKER);
				if (sqlMarker !=-1) {
					sql = rawData.substring(sqlMarker + SQL_MARKER.length() + 1);
					if (sql != null && sql.length() > 0) {
						sql = sql.trim();
						if (sql.endsWith(";")) {
							sql = sql.substring(0, sql.length()-1);
						}
					}
				}
			}
			if (sql == null || sql.trim().length()== 0) {
				throw new Exception("Unable to read learned SQL from Castor definition.txt file.");
			}
		} catch (NoSuchFileException e1) {
			logger.info("Definition file does not exist; Castor may have not learned query.");
			sql = null;
		} catch (Exception e) {
			logger.error("Unable to read castor output file (definition.txt) in submission folder.", e);
			throw e;
		}
				
		return sql;
	}
	
	private int runCastor(Path castorSubmissionFolder, DataLinkageMetadata dataLinkageMetadata) throws Exception {
		
		Path immortalsRoot = ImmortalsConfig.getInstance().globals.getImmortalsRoot();
		String castorScript = immortalsRoot.resolve("castor/takserver/castor_experiments/generic/run_from_das.sh").toString();
		
		int classIndex = dataLinkageMetadata.getClassName().lastIndexOf(".");
		
		ProcessBuilder pb = new ProcessBuilder(castorScript, castorSubmissionFolder.toString(),
				dataLinkageMetadata.getClassName().substring(classIndex+1));
		pb.inheritIO();
		pb.directory(immortalsRoot.resolve("castor/takserver/castor_experiments/generic").toFile());
		Process p = null;
		int commandResult = 0;
		
		try {
			p = pb.start();
			if (p.waitFor(60, TimeUnit.MINUTES)) {
				commandResult = p.exitValue();
			} else {
				commandResult = -1;
				logger.info("Castor timed out.");
				//Need to kill it now
				ProcessBuilder pbkill = new ProcessBuilder("pkill", "-9", "-f", "java.*castor");
				pbkill.start();
			}
		} catch (InterruptedException | IOException e) {
			logger.error("Castor process encountered an exception.", e);
		}

		
		return commandResult;
	}
	
	private void writeCastorParameters(String recall, Path castorSubmissionFolder) throws IOException {
		
		if (recall == null || recall.length() == 0) {
			recall = "1.0";
		}
		
		String template = "{" + 
				"	\"createStoredProcedure\": true," + 
				"	\"iterations\": 1," + 
				"	\"minprec\": 1.0," + 
				"	\"minrec\": " + recall + "," + 
				"	\"maxterms\": 100," + 
				"	\"sample\": 20," + 
				"	\"beam\": 3," + 
				"	\"minimizeBottomClause\": false," + 
				"	\"threads\": 1," + 
				"	\"reductionMethod\": \"consistency\"," + 
				"	\"recall\": 10," + 
				"	\"groundRecall\": 100," + 
				"	\"useInds\": true," + 
				"	\"dbURL\": \"localhost\"" + 
				"}";
		
		Path paramPath = castorSubmissionFolder.resolve("parameters.json");
        FileWriter writer = new FileWriter(paramPath.toFile(), false);
		writer.write(template);
		writer.flush();
		writer.close();

	}
	
	private void createDataFileFromSQL(String fileName, String sql) throws Exception {

		String command="\\COPY (" + sql + " ) to '" + fileName + "' WITH (FORMAT CSV, DELIMITER ',', HEADER TRUE, QUOTE '\"')";
		ProcessBuilder pb = new ProcessBuilder("psql", "-a", "-d", "immortals", "-c", command);
		pb.inheritIO();
		Process p = null;
		int commandResult = 0;
		
		try {
			p = pb.start();
			if (p.waitFor(2, TimeUnit.MINUTES)) {
				commandResult = p.exitValue();
			}
		} catch (InterruptedException | IOException e) {
			logger.error(e.getMessage());
		}

		if (commandResult != 0) {
			throw new Exception("Unable to create data file from SQL.");
		}
		
		
	}
	
	private void modifySourceCode(DasAdaptationContext context, String newSql, DataLinkageMetadata dataLinkage) throws Exception {
		
        GradleKnowledgeBuilder gkb = new GradleKnowledgeBuilder();
        gkb.buildKnowledge(null);

        // Get a build instance, which copies and creates the new artifact
        AdaptationTargetBuildInstance instance = gkb.getBuildInstance("TakServerDataManager", context.getAdaptationIdentifer());
        Path adaptationSourceRoot = instance.getSourceRoot();
        
        Path sourceFile = adaptationSourceRoot.resolve(dataLinkage.getClassName().replace(".", "/") + ".java");

        List<String> codeLines = Files.readAllLines(sourceFile);
        
        for (int x = dataLinkage.getSqlLineNumberStart()-1; x < dataLinkage.getSqlLineNumberEnd(); x++) {
        	codeLines.set(x, "//" + codeLines.get(x));
        }
        
        String newLine = "String " + dataLinkage.getSqlVariableName() + "=\"" + newSql + "\";";

        codeLines.add(dataLinkage.getSqlLineNumberEnd(), newLine);
        codeLines.add(dataLinkage.getSqlLineNumberEnd(), "//DAS Adaptation (Context: " + context.getAdaptationIdentifer() + ")");
        
        sourceFile.toFile().delete();
        sourceFile.toFile().createNewFile();
        
        String code = codeLines.stream().collect(Collectors.joining(System.lineSeparator()));
        
        Files.write(sourceFile, code.getBytes());

	}

	@Override
	public void apply(DasAdaptationContext context) throws Exception {
		
		this.context = context;
		
		if (isApplicable(context)) {
			
			//Check each data DFU for impact
			//Impact is determined by executing the query with the same parameters used to create the training data
			//The returned data is compared to the training data and if there are any differences (or exceptions during execution)
			//the data DFU is deemed to be impacted by the schema change
			
			List<DataLinkageMetadata> dataLinkages = DataDFU.select(context.getKnowldgeUri());

			if (dataLinkages != null && !dataLinkages.isEmpty()) {
				
				for (DataLinkageMetadata dataLinkageMetadata : dataLinkages) {
					boolean sqlEquivalent = false;
					//For each data linkage, check if the original query can be executed without exception
					//and that the return results match the training data created during analysis
					CachedRowSet rowset = null;
					
					StringBuilder comparisonSQL = new StringBuilder();
					SQLTransformer sqlT = new SQLTransformer();
					
					String sampleSql = sqlT.getStableSampleSQL(dataLinkageMetadata, dataLinkageMetadata.getResolvedQuery(), 
							SchemaDependencyKnowledgeBuilder.POSITIVE_DATA_LIMIT);
					
					//we can use sql itself to verify the two result sets are equivalent,
					//though this is not the most efficient way to do this; also duplicate
					//records in one set are not detected using this approach
					comparisonSQL.append("(" + sampleSql + ") ");
					comparisonSQL.append(" except ");
					comparisonSQL.append(" (select * from " + dataLinkageMetadata.getPositiveTrainingDataTableName() + ") ");
					comparisonSQL.append(" union ");
					comparisonSQL.append(" (select * from " + dataLinkageMetadata.getPositiveTrainingDataTableName() + ") ");
					comparisonSQL.append(" except ");
					comparisonSQL.append("(" + sampleSql + ") ");

					try (	Connection conn = dataSource.getConnection();
							ResultSet rs = conn.prepareStatement(comparisonSQL.toString()).executeQuery()) {
						rowset = RowSetProvider.newFactory().createCachedRowSet();
						rowset.populate(rs);
						sqlEquivalent = !rowset.next();
					} catch (Exception e) {
						sqlEquivalent = false;
					}
					
					if (sqlEquivalent) {
						String newSql = learnQuery(dataLinkageMetadata);
						if (newSql == null || newSql.trim().length() == 0) {
							logger.info("Castor did not learn query for: " + dataLinkageMetadata.getClassName());
						} else {
							//Modify Java code with new sql
							modifySourceCode(context, newSql, dataLinkageMetadata);
						}
					}
				}
			}
			
		}
	}
}
