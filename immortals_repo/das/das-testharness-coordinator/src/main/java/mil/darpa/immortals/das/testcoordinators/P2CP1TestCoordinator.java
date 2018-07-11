package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiRequirements;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabaseColumns;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabasePerturbation;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabaseTableConfiguration;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.das.TestCoordinatorExecutionInterface;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import mil.darpa.immortals.testadapter.SubmissionServices;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class P2CP1TestCoordinator implements TestCoordinatorExecutionInterface {

    private static final Logger logger = LoggerFactory.getLogger(P2CP1TestCoordinator.class);
    private static PGPoolingDataSource dataSource;
    private static final String COT_DATA_SOURCE = "TakDataSource";
    private static final String SERVER_NAME = "localhost";
    private static final String DATABASE_NAME = "immortals";
    private static final String USER = "immortals";
    private static final String PASSWORD = "immortals";
    private static final int MAXIMUM_NUMBER_CONNECTIONS = 4;
    private List<String> tableNames = new ArrayList<>();
    private static final String TAKRPT_SCHEMA = "takrpt";
    private static final String TAKRPTAQL_SCHEMA = "takrptaql";

    private String sourceTableDDL = "CREATE TABLE source(source_id VARCHAR(16) NOT NULL, " +
            "name VARCHAR(16), channel VARCHAR(16) NOT NULL, " +
            "CONSTRAINT source_pkey PRIMARY KEY (source_id));";

    static {

        try {
            logger.info("Initializing voltdb database driver.");
            String driver = "org.voltdb.jdbc.Driver";
            Class.forName(driver);
        } catch (Exception e) {
            logger.error("Exception in PC2CP1TestCoordinator static initializer while initializing voltdb driver", e);
            throw new ExceptionInInitializerError(e);
        }

        //Not the ideal pooling implementation, but should be fine for this project
        try {
            logger.info("Setting up postgres connection pool.");
            dataSource = new PGPoolingDataSource();
            dataSource.setDataSourceName(COT_DATA_SOURCE);
            dataSource.setServerName(SERVER_NAME);
            dataSource.setDatabaseName(DATABASE_NAME);
            dataSource.setUser(USER);
            dataSource.setPassword(PASSWORD);
            dataSource.setMaxConnections(MAXIMUM_NUMBER_CONNECTIONS);
        } catch (Exception e) {
            logger.error("Exception in PC2CP1TestCoordinator static initializer while initializing postgres connection pool.", e);
            throw new ExceptionInInitializerError(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Shutdown hook invoked.");
                if (dataSource != null) {
                    dataSource.close();
                }
            }
        });
    }


    @Override
    public Response execute(SubmissionModel submissionModel, boolean attemptAdaptation) {
        logger.info("PC2CP1TestCoordinator executed.");

        try {
            String adaptationIdentifier = submissionModel.sessionIdentifier;

            // Check if what this CP cares about is set
            boolean perturbEnvironment = submissionModel.martiServerModel != null &&
                    submissionModel.martiServerModel.requirements != null &&
                    submissionModel.martiServerModel.requirements.postgresqlPerturbation != null;

            logger.info("Adaptation identifier: " + adaptationIdentifier);


            // Validate the input submission model
            if (perturbEnvironment) {
                List<String> errors = validatePerturbation(submissionModel);
                String error = String.join("\n", errors);

                // If there are errors, log and return BAD_REQUEST
                if (!errors.isEmpty()) {
                    logger.error("Validation error in submission model for CP1.");
                    errors.forEach(e -> logger.error(e + System.lineSeparator()));
                    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
                }
            }

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (perturbEnvironment) {
                            logger.info("Perturbing Environment for CP1.");
                            setupChallengeProblem(submissionModel);
                        }

                        logger.info("Creating deployment model for CP1.");
                        String deploymentModel = getDeploymentModel(adaptationIdentifier);
                        logger.trace("Deployment Model: " + deploymentModel);

                        if (attemptAdaptation) {
                            logger.info("Submitting deployment model for CP1 to DAS for adaptation");
                            SubmissionServices.getDasSubmitter().submitAdaptationRequest(deploymentModel).execute();
                        } else {
                            logger.info("Submitting deployment model for CP1 to DAS for validation");
                            SubmissionServices.getDasSubmitter().submitValidationRequest(deploymentModel).execute();
                        }

                    } catch (Exception e) {
                        logger.error("Unexpected exception during adaptation request handling.", e);
                        ImmortalsErrorHandler.reportFatalException(e);
                    }
                }
            });

            t.setUncaughtExceptionHandler(ImmortalsErrorHandler.fatalExceptionHandler);
            t.start();
            if (attemptAdaptation) {
                logger.info("Adaptation entered running state.");
            } else {
                logger.info("Validation entered running state.");
            }
            return Response.ok().build();

        } catch (Exception e) {
            logger.error("Exception during initial submission to DAS.");

            return Response.serverError().build();
        }
    }

    private List<String> validatePerturbation(SubmissionModel submissionModel) {

        List<String> errors = new ArrayList<String>();
        Set<DatabaseColumns> submittedColumns = new HashSet<DatabaseColumns>();

        //Make sure the submissionModel includes all the columns in CotEvent and CotEventPosition
        LinkedList<DatabaseTableConfiguration> tables = submissionModel.martiServerModel.requirements.postgresqlPerturbation.tables;

        if (tables != null && tables.size() > 0) {
            tables.forEach(table -> submittedColumns.addAll(table.columns.stream().collect(Collectors.toList())));
        }

        for (DatabaseColumns column : DatabaseColumns.values()) {
            if (!submittedColumns.contains(column)) {
                errors.add(String.format("At least one column (%s) was missing in schema perturbation. All columns must be specified.",
                        column.toString()));
            }
        }

        return errors;

    }

    private String getModifiedSubmissionModel(SubmissionModel submissionModel) {
    	    	
        LinkedList<DatabaseTableConfiguration> tablesInModel = submissionModel.martiServerModel.requirements.postgresqlPerturbation.tables;

        int tableIndex = 0;
        
        JsonArrayBuilder tab = Json.createArrayBuilder();
        
        for (DatabaseTableConfiguration table : tablesInModel) {
        	JsonArrayBuilder cab = Json.createArrayBuilder();
        	table.columns.forEach(c -> cab.add(c.columnName));

        	tab.add(Json.createObjectBuilder()
        			.add("table", tableNames.get(tableIndex++))
        			.add("columns", cab));
        }
        
    	JsonObject jsonObject = Json.createObjectBuilder()
    			.add("permutation", Json.createObjectBuilder()
    					.add("martiServerModel", Json.createObjectBuilder()
    							.add("requirements", Json.createObjectBuilder()
    									.add("postgresqlPerturbation", Json.createObjectBuilder()
    											.add("tables", tab))))).build();
    	
    	return jsonObject.toString();
    }
    
    private void setupChallengeProblem(SubmissionModel submissionModel) throws Exception {

        //Create new tables from submissionModel

        Connection conn = null;
        FileWriter writer = null;

        try {
            conn = getVoltDBConnection();

            //Prepare voltdb database
            deleteAllProcedures(conn);
            logger.info("Deleted voltdb procedures.");

            deleteAllTables(conn);
            logger.info("Deleted voltdb tables.");

            prepareSourceTable(conn, submissionModel);
            logger.info("Prepared Source table in voltdb.");

            prepareMasterCotEvent(conn, submissionModel);
            logger.info("Prepared MasterCotEvent table in voltdb");

            String perturbedDdl = preparePerturbedSchema(conn, submissionModel);
            logger.info("Prepared voltdb perturbation DDL.");

            Path castorSubmissionFolder = ImmortalsConfig.getInstance().extensions.castor.getExecutionWorkingDirectory(submissionModel.sessionIdentifier);
            logger.trace("Castor submission folder is: " + castorSubmissionFolder.toString());
            
            //Write submission model as json
            Path jsonPath = castorSubmissionFolder.resolve("submissionModel.json");
            writer = new FileWriter(jsonPath.toFile(), false);
            writer.write(getModifiedSubmissionModel(submissionModel));
            writer.flush();
            writer.close();

            logger.info("Wrote submission model to Castor submission folder.");

            //Write perturbed schema ddl to file (to support dataModel generation by Castor)
            Path ddlPath = castorSubmissionFolder.resolve("ddl.sql");
            writer = new FileWriter(ddlPath.toFile(), false);
            writer.write(sourceTableDDL + " " + perturbedDdl);
            writer.flush();
            writer.close();

            logger.info("Wrote perturbation DDL to Castor submission folder.");
            logger.trace("Perturbation DDL: " + sourceTableDDL + " " + perturbedDdl);

            //Prepare postgres database
            deleteAllTablesTakRpt();
            deleteAllTablesTakRptAql();
            logger.info("Deleted postgres tables.");
            preparePerturbedSchemaTakRpt(submissionModel);
            preparePerturbedSchemaTakRptAql(submissionModel);
            logger.info("Perturbed postgres schema.");

            deleteMasterCotEvent(conn);
            logger.info("Cleaning up voltdb database.");
        } catch (Exception e) {
            logger.error("Exception during setup for CP1.", e);
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                logger.error("Error releasing resources.", e);
            }
        }
    }

    private void createMasterCotEventTable(Connection conn) throws SQLException {

        String sql = "CREATE TABLE master_cot_event " +
                "  (id VARCHAR(16) NOT NULL, " +
                "  source_id VARCHAR(16) NOT NULL, " +
                "  cot_type VARCHAR(16) NOT NULL, " +
                "  how VARCHAR(16) NOT NULL, " +
                "  detail VARCHAR(400) NOT NULL, " +
                "  servertime VARCHAR(16) NOT NULL, " +
                "  point_hae VARCHAR(16) NOT NULL, " +
                "  point_ce VARCHAR(16) NOT NULL, " +
                "  point_le VARCHAR(16) NOT NULL, " +
                "  tileX VARCHAR(16) NOT NULL, " +
                "  tileY VARCHAR(16) NOT NULL, " +
                "  longitude VARCHAR(24) NOT NULL, " +
                "  latitude VARCHAR(24) NOT NULL, " +
                "  CONSTRAINT master_cot_event_pkey PRIMARY KEY (id), " +
                "  CONSTRAINT master_source_pk FOREIGN KEY (source_id) " +
                "    REFERENCES source (source_id) MATCH SIMPLE " +
                "    ON UPDATE RESTRICT ON DELETE RESTRICT)";

        conn.createStatement().execute(sql);
    }

    private String preparePerturbedSchema(Connection conn, SubmissionModel submissionModel) throws SQLException {

        String result = "";

        LinkedList<DatabaseTableConfiguration> tables = submissionModel.martiServerModel.requirements.postgresqlPerturbation.tables;

        StringBuilder ddl = null;
        StringBuilder dataLoadDML = null;
        String currentTable = null;
        String currentColumns = null;
        String primaryCotEventTable = null;

        int keyIndex = 0;

        for (DatabaseTableConfiguration table : tables) {
            currentTable = "a" + UUID.randomUUID().toString().replaceAll("-", "");
            tableNames.add(currentTable);

            ddl = new StringBuilder();
            dataLoadDML = new StringBuilder();

            ddl.append("CREATE TABLE " + currentTable + " (");

            ddl.append("id varchar(16) NOT NULL, ");
            ddl.append(table.columns.stream()
                    .map(c -> c.getCastorFieldDefinition())
                    .collect(Collectors.joining(",")));
            ddl.append(", CONSTRAINT pkey" + keyIndex + " PRIMARY KEY (id)");

            if (table.columns.contains(DatabaseColumns.CotEvent_SourceId)) {
                //Add foreign key constraint
                ddl.append(", CONSTRAINT source_pk FOREIGN KEY (source_id) REFERENCES source(source_id) MATCH SIMPLE ");
                ddl.append(" ON UPDATE RESTRICT ON DELETE RESTRICT");
            }

            if (primaryCotEventTable != null) {
                ddl.append(", CONSTRAINT cot_event_fk" + keyIndex + " FOREIGN KEY (id) " +
                        "REFERENCES " + primaryCotEventTable + " (id) MATCH SIMPLE " +
                        "ON UPDATE RESTRICT ON DELETE RESTRICT");
            }

            ddl.append(");");

            conn.createStatement().execute(ddl.toString());

            if (primaryCotEventTable == null) {
                primaryCotEventTable = currentTable;
            }

            result += ddl;

            dataLoadDML.append("INSERT INTO " + currentTable + "(id,");

            currentColumns = table.columns.stream()
                    .map(c -> c.columnName)
                    .collect(Collectors.joining(","));

            dataLoadDML.append(currentColumns);
            dataLoadDML.append(") SELECT id, ");
            dataLoadDML.append(currentColumns);
            dataLoadDML.append(" FROM master_cot_event ");

            conn.createStatement().execute(dataLoadDML.toString());

            keyIndex++;
        }

        return result;
    }

    private void preparePerturbedSchemaTakRptAql(SubmissionModel submissionModel) throws SQLException {

        LinkedList<DatabaseTableConfiguration> tables = submissionModel.martiServerModel.requirements.postgresqlPerturbation.tables;

        StringBuilder ddl = null;
        StringBuilder dataLoadDML = null;
        String currentTable = null;
        String currentColumns = null;

        int keyIndex = 0;
        Connection conn = null;
        String primaryCotEventTable = null;

        try {
            conn = dataSource.getConnection();

            int tableIndex = 0;
            for (DatabaseTableConfiguration table : tables) {
                currentTable = tableNames.get(tableIndex++);

                ddl = new StringBuilder();
                dataLoadDML = new StringBuilder();

                ddl.append("CREATE TABLE " + TAKRPTAQL_SCHEMA + "." + currentTable + " (");

                ddl.append("id integer NOT NULL,");

                ddl.append(table.columns.stream()
                        .map(c -> c.getTakFieldDefinition())
                        .collect(Collectors.joining(",")));
                ddl.append(", CONSTRAINT pkey" + keyIndex + " PRIMARY KEY (id)");

                if (table.columns.contains(DatabaseColumns.CotEvent_SourceId)) {
                    //Add foreign key constraint
                    ddl.append(", CONSTRAINT source_pk FOREIGN KEY (source_id) REFERENCES " + TAKRPTAQL_SCHEMA + ".source(source_id) MATCH SIMPLE ");
                    ddl.append(" ON UPDATE RESTRICT ON DELETE RESTRICT");
                }

                if (primaryCotEventTable != null) {
                    ddl.append(", CONSTRAINT cot_event_fk" + keyIndex + " FOREIGN KEY (id) " +
                            "REFERENCES " + TAKRPTAQL_SCHEMA + "." + primaryCotEventTable + " (id) MATCH SIMPLE " +
                            "ON UPDATE RESTRICT ON DELETE RESTRICT");
                }

                ddl.append(");");

                conn.createStatement().execute(ddl.toString());

                if (primaryCotEventTable == null) {
                    primaryCotEventTable = currentTable;
                }

                dataLoadDML.append("INSERT INTO " + TAKRPTAQL_SCHEMA + "." + currentTable + "(id,");

                currentColumns = table.columns.stream()
                        .map(c -> c.columnName)
                        .collect(Collectors.joining(","));

                dataLoadDML.append(currentColumns);
                dataLoadDML.append(") SELECT id, ");
                dataLoadDML.append(currentColumns);
                dataLoadDML.append(" FROM " + TAKRPTAQL_SCHEMA + ".master_cot_event ");

                conn.createStatement().execute(dataLoadDML.toString());

                keyIndex++;
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
        }

    }

    private void preparePerturbedSchemaTakRpt(SubmissionModel submissionModel) throws SQLException {

        LinkedList<DatabaseTableConfiguration> tables = submissionModel.martiServerModel.requirements.postgresqlPerturbation.tables;

        StringBuilder ddl = null;
        StringBuilder dataLoadDML = null;
        String currentTable = null;
        String currentColumns = null;

        int keyIndex = 0;
        Connection conn = null;
        String primaryCotEventTable = null;
        try {
            conn = dataSource.getConnection();

            int tableIndex = 0;
            for (DatabaseTableConfiguration table : tables) {
                currentTable = tableNames.get(tableIndex++);

                ddl = new StringBuilder();
                dataLoadDML = new StringBuilder();

                ddl.append("CREATE TABLE " + TAKRPT_SCHEMA + "." + currentTable + " (");

                ddl.append("id varchar(16) NOT NULL,");

                ddl.append(table.columns.stream()
                        .map(c -> c.getCastorFieldDefinition())
                        .collect(Collectors.joining(",")));
                ddl.append(", CONSTRAINT pkey" + keyIndex + " PRIMARY KEY (id)");

                if (table.columns.contains(DatabaseColumns.CotEvent_SourceId)) {
                    //Add foreign key constraint
                    ddl.append(", CONSTRAINT source_pk FOREIGN KEY (source_id) REFERENCES " + TAKRPT_SCHEMA + ".source(source_id) MATCH SIMPLE ");
                    ddl.append(" ON UPDATE RESTRICT ON DELETE RESTRICT");
                }

                if (primaryCotEventTable != null) {
                    ddl.append(", CONSTRAINT cot_event_fk" + keyIndex + " FOREIGN KEY (id) " +
                            "REFERENCES " + TAKRPT_SCHEMA + "." + primaryCotEventTable + " (id) MATCH SIMPLE " +
                            "ON UPDATE RESTRICT ON DELETE RESTRICT");
                }

                ddl.append(");");

                conn.createStatement().execute(ddl.toString());

                if (primaryCotEventTable == null) {
                    primaryCotEventTable = currentTable;
                }

                dataLoadDML.append("INSERT INTO " + TAKRPT_SCHEMA + "." + currentTable + "(id,");

                currentColumns = table.columns.stream()
                        .map(c -> c.columnName)
                        .collect(Collectors.joining(","));

                dataLoadDML.append(currentColumns);
                dataLoadDML.append(") SELECT id, ");
                dataLoadDML.append(currentColumns);
                dataLoadDML.append(" FROM " + TAKRPT_SCHEMA + ".master_cot_event ");

                conn.createStatement().execute(dataLoadDML.toString());

                keyIndex++;
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
        }
    }


    private void prepareSourceTable(Connection conn, SubmissionModel submissionModel) throws Exception {

        conn.createStatement().execute(sourceTableDDL);

        String dataPath = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("database/server/data/source.csv").toString();
        String logPath = ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(submissionModel.sessionIdentifier).toString();

        //Load source table
        //csvloader --separator "," --skip 1 --file large_data_set/source.csv source -r ./log
        ProcessBuilder pb = new ProcessBuilder("csvloader", "--separator", "\",\"", "--skip", "1", "--file",
                dataPath, "source", "-r", "./log");
        pb.directory(new File(logPath));
        pb.inheritIO();
        Process p = null;
        int commandResult = 0;

        try {
            p = pb.start();
            //This is an upper bound on wait time for loading data (100 k records)
            //If the load completes sooner, the process continues
            if (p.waitFor(2, TimeUnit.MINUTES)) {
                commandResult = p.exitValue();
            }
        } catch (InterruptedException | IOException e) {
            logger.error("Exception loading source table in voltdb", e);
            throw e;
        }

        if (commandResult != 0) {
            throw new Exception("Voltdb csvloader returned error code: " + commandResult);
        }
    }

    private void prepareMasterCotEvent(Connection conn, SubmissionModel submissionModel) throws Exception {

        String dataPath = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("database/server/data/master_cot_event.csv").toString();
        String logPath = ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(submissionModel.sessionIdentifier).toString();

        //First create the master_cot_event table definition
        createMasterCotEventTable(conn);

        //Load master_cot_event table
        //csvloader --separator "," --skip 1 --file large_data_set/master_cot_event.csv source -r ./log
        ProcessBuilder pb = new ProcessBuilder("csvloader", "--separator", "\",\"", "--skip", "1", "--file",
                dataPath, "master_cot_event", "-r", "./log");
        pb.directory(new File(logPath));
        pb.inheritIO();
        Process p = null;
        int commandResult = 0;

        try {
            p = pb.start();
            //This is an upper bound on wait time for loading data (100 k records)
            //If the load completes sooner, the process continues
            if (p.waitFor(2, TimeUnit.MINUTES)) {
                commandResult = p.exitValue();
            }
        } catch (InterruptedException | IOException e) {
            logger.error("Exception with csvloader for loading MasterCotEvent table.", e);
            throw e;
        }

        if (commandResult != 0) {
            throw new Exception("Voltdb csvloader returned error code: " + commandResult);
        }

    }

    private void deleteAllProcedures(Connection conn) throws Exception {

        String systemCatalogCall = "{call @SystemCatalog(?)}";
        String updateClassesCall = "{call @UpdateClasses(?,?)}";

        ResultSet procedures = null;

        try (CallableStatement deleteProcedureStatement = conn.prepareCall(systemCatalogCall);
             CallableStatement updateClassesStatement = conn.prepareCall(updateClassesCall)) {

            deleteProcedureStatement.setString(1, "procedures");
            procedures = deleteProcedureStatement.executeQuery();
            while (procedures.next()) {
                conn.createStatement().execute("DROP PROCEDURE " + procedures.getString("PROCEDURE_NAME") + " IF EXISTS");
                updateClassesStatement.setBytes(1, null);
                updateClassesStatement.setString(2, "*." + procedures.getString("PROCEDURE_NAME"));
                updateClassesStatement.executeQuery();
            }
        } catch (Exception e) {
            logger.error("Unexpected error deleting voltdb procedures.", e);
            throw e;
        } finally {
            try {
                if (procedures != null) {
                    procedures.close();
                }
            } catch (Exception e) {
                //Ignore; releasing resources
            }
        }
    }

    private void deleteAllTables(Connection conn) throws Exception {

        String systemCatalogCall = "{call @SystemCatalog(?)}";

        ResultSet results = null;

        try (CallableStatement statement = conn.prepareCall(systemCatalogCall)) {
            statement.setString(1, "tables");
            results = statement.executeQuery();
            while (results.next()) {
                conn.createStatement().execute("DROP TABLE " + results.getString("TABLE_NAME") + " IF EXISTS CASCADE");
            }
        } catch (Exception e) {
            logger.error("Unexpected error deleting voltdb tables.", e);
            throw e;
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
            } catch (Exception e) {
                //Ignore; releasing resources
            }
        }
    }

    private void deleteMasterCotEvent(Connection conn) throws Exception {

        conn.createStatement().execute("DROP TABLE MASTER_COT_EVENT IF EXISTS CASCADE");
    }

    private void deleteAllTablesTakRpt() throws Exception {

        String getTablesSQL = "select table_schema || '.' || table_name as TABLE_NAME " +
                "from information_schema.tables " +
                "where table_type = 'BASE TABLE' " +
                "and table_schema = " + "'" + TAKRPT_SCHEMA + "' " +
                "and table_name not in ('master_cot_event', 'source')";

        ResultSet tables = null;
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            tables = conn.prepareStatement(getTablesSQL).executeQuery();
            while (tables.next()) {
                conn.createStatement().execute("DROP TABLE IF EXISTS " + tables.getString("TABLE_NAME") + " CASCADE");
            }
        } catch (Exception e) {
            logger.error("Unexpected error deleting postgres tables.", e);
            throw e;
        } finally {
            try {
                if (tables != null) {
                    tables.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Exception releasing resources.");
            }
        }
    }

    private void deleteAllTablesTakRptAql() throws Exception {

        String getTablesSQL = "select table_schema || '.' || table_name as TABLE_NAME " +
                "from information_schema.tables " +
                "where table_type = 'BASE TABLE' " +
                "and table_schema = " + "'" + TAKRPTAQL_SCHEMA + "' " +
                "and table_name not in ('master_cot_event', 'source')";

        ResultSet tables = null;
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            tables = conn.prepareStatement(getTablesSQL).executeQuery();
            while (tables.next()) {
                conn.createStatement().execute("DROP TABLE IF EXISTS " + tables.getString("TABLE_NAME") + " CASCADE");
            }
        } catch (Exception e) {
            logger.error("Unexpected error deleting postgres tables.", e);
            throw e;
        } finally {
            try {
                if (tables != null) {
                    tables.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Exception releasing resources.");
            }
        }
    }

    private Connection getVoltDBConnection() throws SQLException {

        String url = "jdbc:voltdb://localhost:21212";
        return DriverManager.getConnection(url);
    }

    private String getDeploymentModel(String adaptationIdentifier) throws Exception {

        //Load the default deployment model
        InputStream is = this.getClass().getResourceAsStream("/cp1_deployment_model.ttl");
        Model deploymentModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(deploymentModel, is, Lang.TTL);


        //Get the deployment model statement
        Selector selector = new SimpleSelector(null, RDF.type,
                deploymentModel.getResource("http://darpa.mil/immortals/ontology/r2.0.0/gmei#DeploymentModel"));
        StmtIterator iter = deploymentModel.listStatements(selector);

        if (!iter.hasNext()) {
            logger.error("DeploymentModel triple not found in deployment model template. Check cp1_deployment_model.ttl.");
            throw new Exception("Unable to create perturbed deployment model for DAS.");
        }

        org.apache.jena.rdf.model.Statement deploymentModelStatement = iter.next();

        deploymentModelStatement.getSubject().addLiteral(
                deploymentModel.getProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasSessionIdentifier"),
                adaptationIdentifier);


        // Inserting the Baseline Marti unique identifier (the source filepath)
        Resource martiServer = deploymentModel.getResource("http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.MartiServer");
        Property hasArtifactIdentifier = deploymentModel.getProperty("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#hasArtifactIdentifier");
        martiServer.addLiteral(hasArtifactIdentifier,
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/server/Marti").toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        deploymentModel.write(out, "TURTLE");

        return new String(out.toByteArray());
    }

    public AdaptationDetails submitAdaptationRequest(String deploymentModel) throws Exception {

        AdaptationDetails result = null;
        try {
            WebTarget repositoryService =
                    ClientBuilder.newClient(new ClientConfig()
                            .register(JacksonFeature.class))
                            .target("http://127.0.0.1:8080/bbn/das/");
            result = repositoryService.path("submitAdaptationRequest").request().post(
                    Entity.entity(deploymentModel, MediaType.TEXT_PLAIN), AdaptationDetails.class);
        } catch (Throwable e) {
            logger.error("Unexpected error submitting adaptation request to DAS.", e);
            throw e;
        }

        return result;
    }


    public static void main(String[] args) throws Exception {

        P2CP1TestCoordinator tc = new P2CP1TestCoordinator();

        SubmissionModel sm = new SubmissionModel();
        sm.sessionIdentifier = "A001";
        LinkedList<DatabaseTableConfiguration> tables = new LinkedList<DatabaseTableConfiguration>();

        sm.martiServerModel = new MartiSubmissionModel();
        sm.martiServerModel.requirements = new MartiRequirements();
        sm.martiServerModel.requirements.postgresqlPerturbation = new DatabasePerturbation();

        sm.martiServerModel.requirements.postgresqlPerturbation.tables = tables;

        DatabaseTableConfiguration table = new DatabaseTableConfiguration();
        table.columns = new LinkedList<DatabaseColumns>();

        table.columns.add(DatabaseColumns.CotEvent_SourceId);
        table.columns.add(DatabaseColumns.CotEvent_CotType);
        table.columns.add(DatabaseColumns.CotEvent_How);
        table.columns.add(DatabaseColumns.CotEvent_Detail);
        table.columns.add(DatabaseColumns.CotEvent_ServerTime);

        tables.add(table);

        table = new DatabaseTableConfiguration();
        table.columns = new LinkedList<DatabaseColumns>();

        table.columns.add(DatabaseColumns.Position_PointHae);
        table.columns.add(DatabaseColumns.Position_PointCE);
        table.columns.add(DatabaseColumns.Position_PointLE);
        table.columns.add(DatabaseColumns.Position_TileX);
        table.columns.add(DatabaseColumns.Position_TileY);
        table.columns.add(DatabaseColumns.Position_Longitude);
        table.columns.add(DatabaseColumns.Position_Latitude);

        tables.add(table);

        tc.execute(sm, true);
        //System.out.println(tc.getModifiedSubmissionModel(sm));
    }
}
