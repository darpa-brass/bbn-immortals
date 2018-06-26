package mil.darpa.immortals.core.das.adaptationmodules.schemaevolution;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.AbstractAdaptationModule;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.DataLinkageMetadata;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.Parameter;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.SQLMetadata;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.SQLTransformer;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.SchemaDependencyKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.DataDFU;
import mil.darpa.immortals.core.das.sparql.SchemaMigrationTarget;
import mil.darpa.immortals.das.context.DasAdaptationContext;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SchemaEvolutionAdapter extends AbstractAdaptationModule {

    private static PGPoolingDataSource dataSource;
    private static final String COT_DATA_SOURCE = "TakDataSource";
    private static final String SERVER_NAME = "localhost";
    private static final String DATABASE_NAME = "immortals";
    private static final String USER = "immortals";
    private static final String PASSWORD = "immortals";
    private static final int MAXIMUM_NUMBER_CONNECTIONS = 4;
    private static final String DEFAULT_SCHEMA = "takrpt";
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
        dataSource.setCurrentSchema(DEFAULT_SCHEMA);

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

    private LearnedQuery learnQuery(DataLinkageMetadata dataLinkageMetadata, DasAdaptationContext dac, SQLTransformer sqlT) throws Exception {

    	LearnedQuery result = null;
    	
        LearnedQuery learnedQuery = new LearnedQuery();
        learnedQuery.setDataLinkageMetadata(dataLinkageMetadata);
        
        String learnedSql = null;

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
            learnedSql = null;
        } else {
            learnedSql = readCastorOutput(castorSubmissionFolder);

            if (learnedSql != null && learnedSql.trim().length() > 0) {
                learnedQuery.setLearnedSql(learnedSql);

                if (dataLinkageMetadata.getSqlMetadata().isParameterized()) {
                	if (determineParameterOrder(dataLinkageMetadata, learnedQuery)) {
                		result = learnedQuery;
                	}
                } else {
                    String sampleSql = sqlT.getRandomSampleSQL(learnedSql,
                            SchemaDependencyKnowledgeBuilder.POSITIVE_DATA_LIMIT);
                    if (compareQueryToValidationData(sampleSql, dataLinkageMetadata)) {
                    	result = learnedQuery;
                    }
                }
            }
        }

        return result;
    }
    
    private boolean determineParameterOrder(DataLinkageMetadata dataLinkageMetadata, LearnedQuery learnedQuery) throws Exception {
    	
    	boolean result = false;
    	
        //Generate sql combinations, then we'll iterate the different parameter assignments for each combination
        SQLMetadata learnedSQLMetadata = SQLMetadata.buildSqlMetadata(learnedQuery.getLearnedSql());

        if (learnedSQLMetadata.getLiteralReferencesInFilter().size() < dataLinkageMetadata.getSqlMetadata().getParameters().size()) {
            logger.info("The learned query does not support parameter substitution); there are too few literals in filter.");
        } else {
            SQLTransformer st = new SQLTransformer();
            List<String> sqlCombinations = st.generateParameterizedCombinations(
                    dataLinkageMetadata.getSqlMetadata().getParameters().size(),
                    learnedQuery.getLearnedSql(), learnedSQLMetadata.getLiteralReferencesInFilter().size());

            //For each parameterized statement, try different permutations of parameter assignments
            List<Integer> correctPermutation = null;

            for (String combination : sqlCombinations) {
                Set<List<Integer>> paramPermutations = st.generatePermutations(
                        dataLinkageMetadata.getSqlMetadata().getParameters().size());
                for (List<Integer> permutation : paramPermutations) {
                    String candidate = st.resolveParameters(combination, dataLinkageMetadata, permutation);
                    //Does this candidate query produce the same validation data as original
                    String sampleSql = st.getRandomSampleSQL(candidate,
                            SchemaDependencyKnowledgeBuilder.POSITIVE_DATA_LIMIT);
                    if (compareQueryToValidationData(sampleSql, dataLinkageMetadata)) {
                        correctPermutation = permutation;
                        break;
                    }
                }

                if (correctPermutation != null) {
                    learnedQuery.setLearnedSql(combination);
                    learnedQuery.setParameterOrder(correctPermutation);
                    result = true;
                    break;
                }
            }
        }
        
        return result;

    }

    private void build(DasAdaptationContext dac) throws Exception {

        AdaptationTargetBuildInstance takServerDataManagerInstance =
                GradleKnowledgeBuilder.getBuildInstance("TakServerDataManager",
                        dac.getAdaptationIdentifer());

        // Build and publish the new TakServerDataManager instance
        takServerDataManagerInstance.executeCleanAndBuild();

    }

    private String readCastorOutput(Path castorSubmissionFolder) throws Exception {

        final String SQL_MARKER = "SQL FORMAT:";
        String result = null;
        String sql = "";

        if (castorSubmissionFolder == null) {
            throw new InvalidOrMissingParametersException("castorSubmissionFolder");
        }

        try {
            Path definition = castorSubmissionFolder.resolve("definition.txt");
            String rawData = new String(Files.readAllBytes(definition));
            if (rawData != null || rawData.trim().length() > 0) {
                int sqlMarker = rawData.indexOf(SQL_MARKER);
                if (sqlMarker != -1) {
                    String rawSql = rawData.substring(sqlMarker + SQL_MARKER.length() + 1);
                    if (rawSql != null && rawSql.length() > 0) {
                        rawSql = rawSql.trim();
                        String[] learnedStatements = rawSql.split(";");
                        for (int x = 0; x < learnedStatements.length; x++) {
                            sql += learnedStatements[x].trim();
                            if (x < learnedStatements.length - 1) {
                                sql += " union ";
                            }
                        }
                    }
                }
            }
            if (sql == null || sql.trim().length() == 0) {
                throw new Exception("Unable to read learned SQL from Castor definition.txt file.");
            } else {
                result = sql;
            }
        } catch (NoSuchFileException e1) {
            logger.info("Definition file does not exist; Castor may have not learned query.");
        } catch (Exception e) {
            logger.error("Unable to read castor output file (definition.txt) in submission folder.", e);
            throw e;
        }

        return result;
    }

    private int runCastor(Path castorSubmissionFolder, DataLinkageMetadata dataLinkageMetadata) throws Exception {

        Path immortalsRoot = ImmortalsConfig.getInstance().globals.getImmortalsRoot();
        String castorScript = immortalsRoot.resolve("castor/takserver/castor_experiments/generic/run_from_das.sh").toString();

        int classIndex = dataLinkageMetadata.getClassName().lastIndexOf(".");

        ProcessBuilder pb = new ProcessBuilder(castorScript, castorSubmissionFolder.toString(),
                dataLinkageMetadata.getClassName().substring(classIndex + 1));
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

        String command = "\\COPY (" + sql + " ) to '" + fileName + "' WITH (FORMAT CSV, DELIMITER ',', HEADER TRUE, QUOTE '\"')";
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

    private void modifySourceCode(DasAdaptationContext context, LearnedQuery learnedQuery) throws Exception {

        // Get a build instance, which copies and creates the new artifact
        AdaptationTargetBuildInstance instance = GradleKnowledgeBuilder.getBuildInstance("TakServerDataManager", context.getAdaptationIdentifer());
        Path adaptationSourceRoot = instance.getSourceRoot();

        Path sourceFile = adaptationSourceRoot.resolve(learnedQuery.getDataLinkageMetadata().getClassName().replace(".", "/") + ".java");

        List<String> codeLines = Files.readAllLines(sourceFile);

        for (int x = learnedQuery.getDataLinkageMetadata().getSqlLineNumberStart() - 1; x < learnedQuery.getDataLinkageMetadata().getSqlLineNumberEnd(); x++) {
            codeLines.set(x, "//" + codeLines.get(x));
        }

        String newLine = "String " + learnedQuery.getDataLinkageMetadata().getSqlVariableName() + "=\"" + learnedQuery.getLearnedSql() + "\";";

        codeLines.add(learnedQuery.getDataLinkageMetadata().getSqlLineNumberEnd(), newLine);
        codeLines.add(learnedQuery.getDataLinkageMetadata().getSqlLineNumberEnd(), "//DAS Adaptation (Context: " + context.getAdaptationIdentifer() + ")");

        sourceFile.toFile().delete();
        sourceFile.toFile().createNewFile();

        String code = codeLines.stream().collect(Collectors.joining(System.lineSeparator()));

        Files.write(sourceFile, code.getBytes());

        if (learnedQuery.getDataLinkageMetadata().getSqlMetadata().isParameterized() &&
                learnedQuery.getParameterOrder() != null && !learnedQuery.getParameterOrder().isEmpty()) {
            //Need to potentially re-order the parameter set statements in the source
            String dataDFURoot = ImmortalsConfig.getInstance().globals.getImmortalsRoot()
                    .resolve(SchemaDependencyKnowledgeBuilder.DATA_DFU_SOURCE).toString();

            File dataDfuSourcePath = new File(dataDFURoot);

            CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(),
                    new JavaParserTypeSolver(dataDfuSourcePath));

            ParserConfiguration parserConfiguration =
                    new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));

            JavaParser parser = new JavaParser(parserConfiguration);

            @SuppressWarnings("static-access")
            CompilationUnit cu = parser.parse(sourceFile);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put(PreparedStatementParameterReorderer.PARAM_LEARNED_QUERY, learnedQuery);
            PreparedStatementParameterReorderer prx = new PreparedStatementParameterReorderer();

            prx.resolvePattern(cu, typeSolver, parameters);

            sourceFile.toFile().delete();
            sourceFile.toFile().createNewFile();

            code = cu.toString();

            Files.write(sourceFile, code.getBytes());
        }
    }

    @SuppressWarnings("unused")
	private boolean compareQueryToTrainingData(String query, DataLinkageMetadata dataLinkageMetadata) {

        boolean result = false;

        StringBuilder comparisonSQL = new StringBuilder();

        //we can use sql itself to verify the two result sets are equivalent,
        //though this is not the most efficient way to do this; also duplicate
        //records in one set are not detected using this approach
        comparisonSQL.append("(" + query + ") ");
        comparisonSQL.append(" except ");
        comparisonSQL.append(" (select * from " + dataLinkageMetadata.getPositiveTrainingDataTableName() + ") ");
        comparisonSQL.append(" union ");
        comparisonSQL.append(" (select * from " + dataLinkageMetadata.getPositiveTrainingDataTableName() + ") ");
        comparisonSQL.append(" except ");
        comparisonSQL.append("(" + query + ") ");

        CachedRowSet rowset = null;

        try (Connection conn = getDASConnection(true);
             ResultSet rs = conn.prepareStatement(comparisonSQL.toString()).executeQuery()) {
            rowset = RowSetProvider.newFactory().createCachedRowSet();
            rowset.populate(rs);
            result = !rowset.next();
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    private boolean compareQueryToValidationData(String query, DataLinkageMetadata dataLinkageMetadata) {

        boolean result = false;

        StringBuilder comparisonSQL = new StringBuilder();

        //we can use sql itself to verify the two result sets are equivalent,
        //though this is not the most efficient way to do this; also duplicate
        //records in one set are not detected using this approach
        comparisonSQL.append("(" + query + ") ");
        comparisonSQL.append(" except ");
        comparisonSQL.append(" (select * from " + dataLinkageMetadata.getValidationDataTableName() + ") ");
        comparisonSQL.append(" union ");
        comparisonSQL.append(" (select * from " + dataLinkageMetadata.getValidationDataTableName() + ") ");
        comparisonSQL.append(" except ");
        comparisonSQL.append("(" + query + ") ");

        CachedRowSet rowset = null;

        try (Connection conn = getDASConnection(true);
             ResultSet rs = conn.prepareStatement(comparisonSQL.toString()).executeQuery()) {
            rowset = RowSetProvider.newFactory().createCachedRowSet();
            rowset.populate(rs);
            result = !rowset.next();
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    @Override
    public void apply(DasAdaptationContext context) throws Exception {
    	
        if (ImmortalsConfig.getInstance().debug.isUseMockExtensionSchemaEvolution()) {
            mockApply(context);
            return;
        }

        this.context = context;
        int numberLearnedQueries = 0;

        LinkedList<String> errorMessages = new LinkedList<>();
        LinkedList<String> detailMessages = new LinkedList<>();
        Map<String, String> aqlProducedQueries = null;

        if (isApplicable(context)) {

            //Check each data DFU for impact
            //Impact is determined by executing the query with the same parameters used to create the training data
            //The returned data is compared to the training data and if there are any differences (or exceptions during execution)
            //the data DFU is deemed to be impacted by the schema change

            List<DataLinkageMetadata> dataLinkages = DataDFU.select(context.getKnowldgeUri());
            
            if (dataLinkages != null && !dataLinkages.isEmpty()) {

                try {
                	aqlProducedQueries = getAqlQueries(context, dataLinkages);
                } catch (Exception e) {
                	aqlProducedQueries.clear();
                	logger.error("AQL process invocation failed.", e);
                }

                SQLTransformer sqlT = new SQLTransformer();
                boolean adaptationRequired, castorValidation, aqlValidation;
                LearnedQuery learnedQuery;
                
                for (DataLinkageMetadata dataLinkageMetadata : dataLinkages) {
                    
                	String sampleSql = sqlT.getRandomSampleSQL(dataLinkageMetadata.getResolvedQuery(),
                            SchemaDependencyKnowledgeBuilder.POSITIVE_DATA_LIMIT);

                    learnedQuery = null;
                    adaptationRequired = castorValidation = aqlValidation = false;
                    
                    boolean sqlEquivalent = this.compareQueryToValidationData(sampleSql, dataLinkageMetadata);
                    
                    if (!sqlEquivalent) {
                    	adaptationRequired = true;
                    	
                        learnedQuery = learnQuery(dataLinkageMetadata, context, sqlT);
                        
                        if (learnedQuery == null) {
                        	logger.info("Castor did not learn query for: " + dataLinkageMetadata.getClassName());
                        } else {
                        	castorValidation = true;
                            //Modify Java code with new sql
                            modifySourceCode(context, learnedQuery);
                            numberLearnedQueries++;
                        }

                		String aqlSQL = aqlProducedQueries.get(dataLinkageMetadata.getClassName());
                		
                        if (aqlSQL != null) {
                    		if (dataLinkageMetadata.getSqlMetadata().isParameterized()) {
                    			SQLMetadata aqlSqlMetadata = SQLMetadata.buildSqlMetadata(aqlSQL);
                    			if (aqlSqlMetadata.isParameterized() &&
                    					aqlSqlMetadata.getParameters().size() == dataLinkageMetadata.getSqlMetadata().getParameters().size()) {
                    				for (Parameter p : dataLinkageMetadata.getSqlMetadata().getParameters()) {
                    					aqlSQL = aqlSQL.replaceFirst("\\?", Parameter.formatToString(p));
                    				}
                    			} else {
                    				aqlSQL = null;
                    			}
                    		}

                    		if (aqlSQL != null) {
	                    		sampleSql = sqlT.getRandomSampleSQL(aqlSQL,
	                                    SchemaDependencyKnowledgeBuilder.POSITIVE_DATA_LIMIT);
	                            if (compareQueryToValidationData(sampleSql, dataLinkageMetadata)) {
	                            	aqlValidation = true;
	                            }
                    		}
                    	}
                    } else {
                    	adaptationRequired = false;
                    }
                    
                    //Write results for castor and aql
                    recordResultsCSV(dataLinkageMetadata, context, ((learnedQuery != null) ? learnedQuery.getLearnedSql() : null), 
                    		aqlProducedQueries.get(dataLinkageMetadata.getClassName()),
                    		castorValidation, aqlValidation, adaptationRequired);
                }
            }

            DasOutcome outcome;

            try {
                build(context);
                outcome = DasOutcome.SUCCESS;
                String status = "" + numberLearnedQueries + " of " + dataLinkages.size() + " data DFUs repaired..";
                detailMessages.add(status);
            } catch (Exception e) {
                outcome = DasOutcome.ERROR;
                String error = "Unexpected build error after SchemaEvolutionAdapter is applied.";
                logger.error(error, e);
                errorMessages.add(error + " Message: " + e.getMessage());
            }
            AdaptationDetails ad = new AdaptationDetails(getClass().getName(), outcome, context.getAdaptationIdentifer());
            context.submitAdaptationStatus(ad);
        }
    }

    @SuppressWarnings("unused")
	private void recordResults(DataLinkageMetadata metadata, DasAdaptationContext context, String castorQuery, 
    		String aqlQuery, boolean castorValidation, boolean aqlValidation, boolean adaptationRequired) {
    	
    	Path submissionFolder = ImmortalsConfig.getInstance().globals.getAdaptationWorkingDirectory(context.getAdaptationIdentifer());
        Path resultsPath = submissionFolder.resolve("schemaEvolutionResults_" + UUID.randomUUID().toString() + ".json");

        FileWriter writer = null;
        JsonReader jsonReader = null;
        StringReader stringReader = null;
        
        try {
        	writer = new FileWriter(resultsPath.toFile(), false);

        	String submissionModelJson = getSchemaPerturbation(context);
        	stringReader = new StringReader(submissionModelJson);
        	jsonReader = Json.createReader(stringReader);

        	JsonObject submissionModel = jsonReader.readObject();
        
    		JsonObject jsonObject = Json.createObjectBuilder()
    			.add("submissionModel", submissionModel)
    			.add("castorQuery", (castorQuery != null) ? castorQuery : "null")
    			.add("aqlQuery", (aqlQuery != null) ? aqlQuery : "null")
    			.add("castorValidation", castorValidation)
    			.add("aqlValidation", aqlValidation)
    			.add("adaptationRequired", adaptationRequired)
    			.add("className",  metadata.getClassName())
    			.add("originalQuery", metadata.getOriginalSql())
    			.build();

			writer.write(jsonObject.toString());
	        writer.flush();
        } catch (Exception e) {
        	logger.error("Could not record schema evolution results.");
        	//swallow exception for now (we could also fail the entire test)
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (jsonReader != null) {
					jsonReader.close();
				}
				if (stringReader != null) {
					stringReader.close();
				}
			} catch (Exception e1) {
				//Do nothing;releasing resources
				logger.warn("Exception releasing json resources.");
			}
		}
    }
    
    private void recordResultsCSV(DataLinkageMetadata metadata, DasAdaptationContext context, String castorQuery, 
    		String aqlQuery, boolean castorValidation, boolean aqlValidation, boolean adaptationRequired) {
    	
    	Path submissionFolder = ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(context.getAdaptationIdentifer());
        Path resultsPath = submissionFolder.resolve("schemaEvolutionResults.csv");

        try (FileWriter writer = new FileWriter(resultsPath.toFile(), true)) {
        	writer.append("\"" + getSchemaPerturbation(context).replace("\"", "\"\"") + "\",");
        	writer.append("\"" + ((castorQuery != null) ? castorQuery : "null") + "\",");
        	writer.append("\"" + ((aqlQuery != null) ? aqlQuery : "null") + "\",");
        	writer.append("\"" + Boolean.toString(castorValidation) + "\",");
        	writer.append("\"" + Boolean.toString(aqlValidation) + "\",");
        	writer.append("\"" + Boolean.toString(adaptationRequired) + "\",");
        	writer.append("\"" + metadata.getClassName() + "\",");
        	writer.append("\"" + metadata.getOriginalSql() + "\",");
        	writer.append("\"" + context.getAdaptationIdentifer() + "\"");
        	writer.append(System.lineSeparator());
	        writer.flush();
		} catch (Exception e) {
        	logger.error("Could not record schema evolution results.");
        	//swallow exception for now (we could also fail the entire test)
		}
    }
    
    
    public void mockApply(DasAdaptationContext context) {
        try {

            AdaptationDetails starting = new AdaptationDetails(
                    getClass().getName(),
                    DasOutcome.RUNNING,
                    context.getAdaptationIdentifer()
            );
            context.submitAdaptationStatus(starting);
            Thread.sleep(1000);

            LinkedList<String> detailMessages = new LinkedList<>();
            detailMessages.add("Mock Success!");

            AdaptationDetails update = starting.produceUpdate(DasOutcome.SUCCESS, new LinkedList<>(), detailMessages);
            context.submitAdaptationStatus(update);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getSchemaPerturbation(DasAdaptationContext dac) throws Exception {
    	
        Path submissionModel = ImmortalsConfig.getInstance().extensions.castor
        		.getExecutionWorkingDirectory(dac.getAdaptationIdentifer()).resolve("submissionModel.json");

        String schemaPerturbation = new String(Files.readAllBytes(submissionModel));
        
        return schemaPerturbation;

    }
        
    public Map<String, String> getAqlQueries(DasAdaptationContext dac, List<DataLinkageMetadata> dataLinkages) throws Exception {

    	Map<String, String> results = new HashMap<String, String>();
    	
        String queryMappings = null;
        
        String schemaPerturbation = getSchemaPerturbation(dac);

        logger.debug("Submitting schema perturbation to aql service.");

    	aqlService = ClientBuilder.newClient(new ClientConfig()
                .register(JacksonFeature.class))
                .target(AQL_SERVICE_CONTEXT_ROOT);
        queryMappings = aqlService.request().post(
                Entity.entity(schemaPerturbation, MediaType.APPLICATION_JSON), String.class);

        if (queryMappings == null || queryMappings.trim().length() == 0) {
            logger.error("Could not retrieve query mappings from AQL service.");
            //Don't throw exception for now
        } else {
        	//Convert queryMappings to native Java structure
        	try(StringReader stringReader = new StringReader(queryMappings);
        		JsonReader jsonReader = Json.createReader(stringReader)) {
        		
        		JsonObject queryResults = jsonReader.readObject().getJsonObject("query");
        		for (DataLinkageMetadata dataLinkage : dataLinkages) {
        			JsonObject query = queryResults.getJsonObject(dataLinkage.getClassName());
        			if (query != null) {
        				JsonString sql = query.getJsonObject("t1").getJsonString("sql");
        				if (sql != null) {
        					String sqlString = sql.getString().trim();
        					if (sqlString.endsWith(";")) {
        						sqlString = sqlString.substring(0, sqlString.length()-1);
        					}
            				results.put(dataLinkage.getClassName(), sqlString);
        				}
        			}
        		}
        	} catch (Exception e) {
        		
        	} finally {
        	}
        }
        
        return results;
    }
    
	private Connection getDASConnection(boolean setRandomSeed) throws SQLException {

		Connection conn = dataSource.getConnection();
		
		if (setRandomSeed) {
			try (Statement s = conn.createStatement()) {
				s.executeQuery("select setseed(" + SEED + ")");
			}
		}

		return conn;
	}


	public static final double SEED = 0.5;
    private static WebTarget aqlService;
    private static final String AQL_SERVICE_CONTEXT_ROOT = ImmortalsConfig.getInstance().extensions.aqlbrass.getFullUrl().resolve("/brass/p2/c1/json").toString();
    
}
