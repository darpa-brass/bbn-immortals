package com.securboration.immortals.pojoapi;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by CharlesEndicott on 7/10/2017.
 */
public class OntologyCheck {

    private final static Logger logger = LoggerFactory
            .getLogger(OntologyCheck.class);

    private Map<String, Set<String>> analysisReport = new HashMap<>();
    private Set<String> proposedSchemas = new HashSet<>();
    private Set<String> currentSchemas = new HashSet<>();
    private Map<String, String> proposedInstantiations = new HashMap<>();
    
    private  Model currentOnt;
    private  Model  proposedOnt;

    public OntologyCheck(Model[] models) {
        analysisReport.put(WARNING_CLASS_DEFINITION, new HashSet<>());
        analysisReport.put(WARNING_PREDICATE_DEFINITION, new HashSet<>());
        analysisReport.put(WARNING_REDEFINE_SCHEMA, new HashSet<>());
        analysisReport.put(WARNING_UNDEFINED_CLASS, new HashSet<>());
        analysisReport.put(WARNING_UNDEFINED_PREDICATE, new HashSet<>());
        
        currentOnt = models[0];
        proposedOnt = models[1];
    }

    public static Model[] initialize(String pathToCurrentOnt, String pathToProposedOnt) {

        Model[] models = new Model[2];

        // read ontology from specified directories
        logger.warn("Reading in current ontology...");
        models[0] = readOntology(pathToCurrentOnt);
        logger.warn("Done. Reading in ontology to test...");
        models[1] = readOntology(pathToProposedOnt);

        // Current ontology fails the validation check due to range/domain issues
        logger.warn("Done. Checking models for surface-level logical errors...");
        //ModelValidator.validateModel(models[0], System.out);
        //ModelValidator.validateModel(models[1], System.out);
        logger.warn("Done.");

        return models;
    }

    private static Model readOntology(String pathToOnt) {
        byte[] encoded;
        Collection<File> collectFiles = FileUtils.listFiles(
                new File(pathToOnt),null , true);
        Model ontology = ModelFactory.createDefaultModel();

        for (File newFile : collectFiles) {
            try {
                encoded = Files.readAllBytes(Paths.get(newFile.getAbsolutePath()));
            } catch (IOException x) {
                return null;
            }
            ontology.read(new ByteArrayInputStream(encoded), null, "TURTLE");
        }
        return ontology;
    }

    private Set<String> queryModelOneVariable(String queryString, Model modelToQuery) {

        Set<String> queryResults = new HashSet<>();

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, modelToQuery)){
            ResultSet results = qexec.execSelect();
            while(results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                queryResults.add(soln.get("c").toString());
            }
        }
        return queryResults;
    }

    private Map<String, String> queryModelTwoVariables(String queryString, Model modelToQuery) {

        Map<String, String> queryResults = new HashMap<>();

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, modelToQuery)){
            ResultSet results = qexec.execSelect();
            while(results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                queryResults.put(soln.get("x").toString(),soln.get("c").toString());
            }
        }
        return queryResults;
    }

    public void checkProposedOntology() throws IOException {
        
        int size = 0;

        // find all definitions for classes, new or already defined
        logger.warn("Checking ontology for class definitions...");
        proposedSchemas = queryModelOneVariable(CLASS_DEFINITION_QUERY, proposedOnt);
        if (proposedSchemas.size() != 0) {
            logger.warn(WARNING_CLASS_DEFINITION);
            for (String schema : proposedSchemas) {
                logger.warn(schema);
                analysisReport.get(WARNING_CLASS_DEFINITION).add(schema);
            }
            size = proposedSchemas.size();
            logger.warn("Found total of: " + size + " class definitions");
        } else {
            logger.warn("PASS. Proposed ontology does not define new classes. ");
        }

        // find all definitions for predicates, new or already defined
        logger.warn("Checking ontology for predicate definitions...");
        if (proposedSchemas.addAll(queryModelOneVariable(PREDICATE_DEFINITION_QUERY, proposedOnt))) {
            logger.warn(WARNING_PREDICATE_DEFINITION);
            for (String predicate : queryModelOneVariable(PREDICATE_DEFINITION_QUERY, proposedOnt)) {
                logger.warn(predicate);
                analysisReport.get(WARNING_PREDICATE_DEFINITION).add(predicate);
            }
            logger.warn("Found total of: " + (proposedSchemas.size() - size) + " predicate definitions");
        } else {
            logger.warn("PASS. Proposed ontology does not define new predicates. ");
        }

        // set of current definitions
        currentSchemas = queryModelOneVariable(CLASS_DEFINITION_QUERY, currentOnt);
        currentSchemas.addAll(queryModelOneVariable(PREDICATE_DEFINITION_QUERY, currentOnt));
        
        logger.warn("Checking proposed concepts...");
        proposedInstantiations = queryModelTwoVariables(INSTANTIATIONS_QUERY, proposedOnt);
        size = proposedInstantiations.size();
        logger.warn("Found total of: " + size + " non-schema instantiations.");
       // for (String schema : proposedSchemas) {
           // proposedInstantiations.remove(schema);
       // }

        // look at types that are to be instantiated and compare them with schemas
        proposedInstantiations.values().stream().distinct().forEach((type)->{
            inspectType(type);
        });

        HashSet<String> processedProps = new HashSet<>();
        for (String instance : proposedInstantiations.keySet()) {
           boolean checkPoint = true;
            Set<String> properties = queryModelOneVariable(getProperties(proposedInstantiations.get(instance), instance), proposedOnt);
            for (String property : properties) {
                if (currentSchemas.contains(property) && proposedSchemas.contains(property)) {
                    //TODO, analyze domain, range, etc. of each property to see if consistent
                } else if (!currentSchemas.contains(property) && proposedSchemas.contains(property)) {
                    //TODO ^^^analysis
                } else if (currentSchemas.contains(property) && !proposedSchemas.contains(property)) {
                    //TODO ^^^analysis
                } else {
                    if (checkPoint) {
                        size--;
                        checkPoint = false;
                    }
                    if (processedProps.add(property)) {
                        analysisReport.get(WARNING_UNDEFINED_PREDICATE).add(property);
                        logger.warn(WARNING_UNDEFINED_PREDICATE + property);
                    }
                }
            }
        }
        
        logger.warn("Of the original instantiations, " + size + " were found compliant with rules.");
        
        
        // print a summary of all the issues encountered
        logger.warn("\r\n\r\n================\r\nDone. Analysis report for proposed model is: ");
        for (String warning : analysisReport.keySet()) {
            logger.warn("Found " + analysisReport.get(warning).size() + " instance(s) violating rule: " + warning.substring(6));
            for (String report : analysisReport.get(warning)) {
                logger.warn(report);
            }
        }
        logger.warn("\r\n================\r\n\r\n");
    }
    
    
    private void inspectType(String type) {

        if (currentSchemas.contains(type) && proposedSchemas.contains(type)) {

            Set<String> proposedProperties = queryModelOneVariable(getPropertiesSchema(type), proposedOnt);
            Set<String> currentProperties = queryModelOneVariable(getPropertiesSchema(type), currentOnt);

            //TODO analyze property in-depth, e.g. compare domains/ranges for consistency
            // see if they are the same definition, if not let the user know
            // something has happened to their version of the definition
            proposedProperties.removeAll(currentProperties);
            if (proposedProperties.size() != 0) {
                logger.warn(WARNING_REDEFINE_SCHEMA);
                analysisReport.get(WARNING_REDEFINE_SCHEMA).add(type);
                logger.warn(type + ", with additional properties: ");
                for (String property : proposedProperties) {
                    logger.warn(property);
                }
            }


        } else if (currentSchemas.contains(type) &&
                !proposedSchemas.contains(type)) {
        } else if (!currentSchemas.contains(type) &&
                proposedSchemas.contains(type)) {
            // If only the proposed schema has it, warn the user that it's defining a new concept and then
            // see if it's derived from the current ontology
            Set<String> superTypes = queryModelOneVariable(getSuperType(type), proposedOnt);
            logger.warn("Proposed model uses unknown type: " + type + " , analyzing to see if subclass of known type.");
            
            for(String superType : superTypes) {
                inspectType(superType);
            }
        } else {
            boolean subClass = true;
            // If neither ontology has the definition of the instance, then we warn the user and continue
            logger.warn(WARNING_UNDEFINED_CLASS);
            for (Map.Entry entry : proposedInstantiations.entrySet()) {
                if (type.equals(entry.getValue())) {
                    logger.warn(entry.getKey().toString());
                    analysisReport.get(WARNING_UNDEFINED_CLASS).add(entry.getKey().toString());
                    subClass = false;
                }
            }
            
            if (subClass) {
                logger.warn("Reached ancestor ceiling. An instance attempts to subclass: " + type +
                " , which is not a subclass of any known class");
            }

        }
    }
    
    //TODO no doubt these queries can be optimized...
    private static String getProperties(String type, String instance) {
        String query = 
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                       "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                       "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                       "\n" +
                       "\n" +
                       "SELECT DISTINCT ?c WHERE {\n" +
                       "    \n" +
                       "   { <?t?> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?superClass filter (isBlank(?superClass))\n" +
                       "      ?superClass owl:onProperty ?c }\n" +
                       "    UNION {<?t?> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?superClass .\n" +
                       "      ?superClass ?c ?x filter (!isBlank(?x)) filter (str(?c) != 'http://www.w3.org/2000/01/rdf-schema#subClassOf') filter (str(?c) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type') FILTER (!regex(str(?c), 'http://www.w3.org/2002/07/owl.*')) filter (!regex(str(?c), 'http://www.w3.org/2000/01/rdf-schema.*')) } \n" +
                       "      UNION { <?i?> ?c ?x filter (!isBlank(?x))\n" +
                       "    filter (str(?c) != 'http://www.w3.org/2000/01/rdf-schema#subClassOf') filter (str(?c) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type') filter (!regex(str(?c), 'http://www.w3.org/2002/07/owl.*')) filter (!regex(str(?c), 'http://www.w3.org/2000/01/rdf-schema.*'))\n" +
                       "    }\n" +
                       "\n" +
                       "}"
                ;
        return query.replace("?t?", type).replace("?i?", instance);
    }
    
    private static String getPropertiesSchema(String type) {
        String query =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                        "\n" +
                        "\n" +
                        "SELECT DISTINCT ?c WHERE {\n" +
                        "    \n" +
                        "   { <?t?> rdfs:subClassOf ?superClass filter (isBlank(?superClass))\n" +
                        "    ?superClass owl:onProperty ?c } UNION { <?t?> ?c ?x filter (!isBlank(?x))\n" +
                        "    filter (str(?c) != 'http://www.w3.org/2000/01/rdf-schema#subClassOf') filter (str(?c) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')\n" +
                        "    }\n" +
                        "}"
                ;
        return query.replace("?t?", type);
    }
    
    private static String getSuperType(String type) {
        
        String query = 
                "" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "SELECT ?c " +
                        "WHERE { " +
                        "" +
                        "<?t?> rdfs:subClassOf ?c filter (!isBlank(?c)) " +
                        "}"
        
                ;
        return query.replace("?t?", type);
    }

    public void cleanAnalysisReport() {
        for (String warning : analysisReport.keySet()) {
            analysisReport.get(warning).clear();
        }
    }

    public static void main(String[] args) throws IOException {

        
        String pathToCurrent = args[0];
        String pathToProposed =  args[1];
        
        Model[] models = initialize(pathToCurrent, pathToProposed);
        OntologyCheck check = new OntologyCheck(models);
        
        check.checkProposedOntology();
        
        check.cleanAnalysisReport();
        
    }
    
    // TODO do multiple filters with regex if possible
    private final String CLASS_DEFINITION_QUERY = "SELECT ?c WHERE { { ?c a <http://www.w3.org/2002/07/owl#Class> } UNION { ?c a <http://www.w3.org/2000/01/rdf-schema#Class> } filter (!isBlank(?c)) }";
    private final String PREDICATE_DEFINITION_QUERY = "SELECT ?c WHERE { { ?c a <http://www.w3.org/2002/07/owl#ObjectProperty> } UNION { ?c a <http://www.w3.org/2002/07/owl#DatatypeProperty> } UNION {?c a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>} }";
    private final String INSTANTIATIONS_QUERY = "SELECT DISTINCT ?x ?c WHERE { ?x a ?c filter (str(?c) != 'http://www.w3.org/2002/07/owl#Class') filter (!isBlank(?x)) filter(str(?c) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#Property' ) " +
            "filter(str(?c) != 'http://www.w3.org/2002/07/owl#DatatypeProperty') filter (str(?c) != 'http://www.w3.org/2002/07/owl#ObjectProperty') }";
    private final String PREDICATES_QUERY = "SELECT DISTINCT ?p WHERE {  ?x ?p ?c filter (str(?p) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')  }";

    private final String WARNING_CLASS_DEFINITION = "FAIL. Proposed model has class definitions:";
    private final String WARNING_PREDICATE_DEFINITION = "FAIL. Proposed model has predicate definitions:";
    private final String WARNING_REDEFINE_SCHEMA = "FAIL. Proposed model attempts to augment schema:";
    private final String WARNING_UNDEFINED_CLASS = "FAIL. Proposed model attempts to use undefined class(es):";
    private final String WARNING_UNDEFINED_PREDICATE = "FAIL. Proposed model attempts to use undefined predicate:";
}
