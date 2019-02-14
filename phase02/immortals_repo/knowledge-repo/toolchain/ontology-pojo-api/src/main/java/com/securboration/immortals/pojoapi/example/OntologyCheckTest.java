package com.securboration.immortals.pojoapi.example;

import com.securboration.immortals.pojoapi.OntologyCheck;
import org.apache.jena.rdf.model.Model;


import java.io.IOException;


import static com.securboration.immortals.pojoapi.OntologyCheck.initialize;

/**
 * Created by CharlesEndicott on 7/10/2017.
 */
public class OntologyCheckTest {
    
    public static void main(String[] args) throws IOException {

        
        Model[] models = initialize(DEFAULT_CURRENT_WORKING_ONTOLOGY, GOOD_ONTOLOGY);
        assert(models != null);
        OntologyCheck test = new OntologyCheck(models);
        test.checkProposedOntology();

        test.cleanAnalysisReport();

        models = initialize(DEFAULT_CURRENT_WORKING_ONTOLOGY, REDEFINE_SCHEMA);
        assert (models != null);
        test = new OntologyCheck(models);
        test.checkProposedOntology();

        test.cleanAnalysisReport();

        models = initialize(DEFAULT_CURRENT_WORKING_ONTOLOGY, NO_SCHEMA);
        assert (models != null);
        test = new OntologyCheck(models);
        test.checkProposedOntology();

    }
    
    private static final String DEFAULT_CURRENT_WORKING_ONTOLOGY = "knowledge-repo/vocabulary/ontology-package/target/classes/ontology/immortals-vocab";
    private static final String PATH_TO_TESTDIRS = "knowledge-repo/toolchain/ontology-pojo-api/src/main/java/com/securboration/immortals/pojoapi/example/krgp";
    private static final String GOOD_ONTOLOGY = PATH_TO_TESTDIRS + "/krgpGood";
    private static final String BAD_ONTOLOGY = PATH_TO_TESTDIRS + "/krgpBad";
    private static final String REDEFINE_SCHEMA = BAD_ONTOLOGY + "/krgpRedefine";
    private static final String NO_SCHEMA = BAD_ONTOLOGY + "/krgpNoSchema";

}
