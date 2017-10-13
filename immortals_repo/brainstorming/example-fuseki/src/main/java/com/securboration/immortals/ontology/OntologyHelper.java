package com.securboration.immortals.ontology;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple utility class for reading/writing semantic models
 * 
 * @author jstaples
 *
 */
public class OntologyHelper {
    private static Logger logger = LoggerFactory
            .getLogger(OntologyHelper.class);

    /**
     * Serializes the provided model to the provided language
     * 
     * @param m
     *            a model to serialize
     * @param outputLanguage
     *            a language to serialize to (e.g., RDF/XML)
     * @return the serialized model
     * @throws IOException
     */
    public static String serializeModel(Model m, String outputLanguage)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        m.write(output, outputLanguage);

        return new String(output.toByteArray());
    }

    /**
     * Loads a non-inferencing model from an input stream
     * 
     * @param input
     *            the stream to read from
     * @param inputLanguage
     *            the language to read from the stream
     * @return the model read
     * @throws IOException
     */
    public static Model loadModel(final InputStream input,
            final String inputLanguage) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(input, inputLanguage);

        return model;
    }

    /**
     * Prints a vaguely human readable representation of triples in the provided
     * model to the provided printstream
     * 
     * @param model
     *            the model whose triples we will print
     * @param p
     *            where to print
     */
    public static void printTriples(Model model, PrintStream p) {
        StmtIterator statements = model.listStatements();

        int count = 0;
        while (statements.hasNext()) {
            Statement s = statements.next();

            p.printf("%07d: [%s] [%s] [%s]\n", count, s.getSubject(),
                    s.getPredicate(), s.getObject());

            count++;
        }
    }
}