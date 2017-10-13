package com.securboration.immortals.service.vis;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uni_stuttgart.vis.vowl.owl2vowl.converter.AbstractConverter;
import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.ConsoleExporter;
import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.Exporter;
import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.FileExporter;

public class Visualizer {
    private static final String IRI_OPTION_NAME = "iri";
    private static final String FILE_OPTION_NAME = "file";
    private static final String OUTPUT_OPTION_NAME = "output";
    private static final String DEPENDENCIES_OPTION_NAME = "dependencies";
    private static final String HELP_OPTION_NAME = "h";
    private static final String ECHO_OPTION_NAME = "echo";
    
    private static final Logger logger = LogManager.getLogger(Visualizer.class);
    
    private static class CustomConverter extends AbstractConverter{
        
        private final OWLOntologyManager ontologyManager;
        private final OWLOntology ontologyToDisplay;
        private final OWLOntology externalOntology;
        
        private CustomConverter(
                OWLOntologyManager ontologyManager,
                OWLOntology ontologyToDisplay,
                OWLOntology externalOntology
                ){
            this.ontologyManager = ontologyManager;
            this.ontologyToDisplay = ontologyToDisplay;
            this.externalOntology = externalOntology;
        }

        @Override
        protected void loadOntology() throws OWLOntologyCreationException {
        }
        
    }
    
    private static byte[] convertBetweenSemanticFormats(
            byte[] input, 
            String inputLang,
            String outputLang
            ) throws IOException{
        Model m = ModelFactory.createDefaultModel();
        
        m.read(new ByteArrayInputStream(input), null, inputLang);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.write(os, outputLang, null);
        
        return os.toByteArray();
    }

    public static void main(String[] args) throws OWLOntologyCreationException, IOException {
        
        final String path = 
                "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\" +
                "knowledge-repo\\toolchain\\visualization\\webvowl_1.0.3\\" +
                "owl2vowl\\ontologies\\" +
                "immortals_core.ttl";
        
        final byte[] input = FileUtils.readFileToByteArray(new File(path));
        
//        final byte[] ttl = convertBetweenSemanticFormats(input,"TTL","TTL");
        final byte[] rdf = convertBetweenSemanticFormats(
            input,
            "TTL",
            "RDF/XML"
            );
        
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology ont = m.loadOntologyFromOntologyDocument(
            new ByteArrayInputStream(rdf)
            );
        
        System.out.println("loaded " + ont.getAxiomCount() + " axioms");
        
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology displayThis = manager.loadOntologyFromOntologyDocument(
            new ByteArrayInputStream(rdf)
            );
        OWLOntology considerThis = manager.createOntology();
        
        
        
        CustomConverter c = new CustomConverter(
            manager,
            displayThis,
            considerThis
            );
        
        c.convert();
    }

    protected Options createOptions() {
        Options options = new Options();
        options.addOption(Option.builder(HELP_OPTION_NAME).desc("views this help text").build());

        OptionGroup inputOptions = new OptionGroup();
        inputOptions.setRequired(true);
        inputOptions.addOption(Option.builder(IRI_OPTION_NAME).argName("IRI").hasArg().desc("the iri of an ontology").build());
        inputOptions.addOption(Option.builder(FILE_OPTION_NAME).argName("PATH").hasArg().desc("the local path to an ontology").build());
        options.addOption(Option.builder(DEPENDENCIES_OPTION_NAME).argName("PATHS").hasArgs().desc("paths to dependencies of a local ontology").build());

        OptionGroup outputOptions = new OptionGroup();
        outputOptions.addOption(Option.builder(ECHO_OPTION_NAME).desc("prints the converted ontology on the console").build());
        outputOptions.addOption(Option.builder(OUTPUT_OPTION_NAME).argName("PATH").hasArg().desc("specify the path for the desired output location").build());

        options.addOptionGroup(inputOptions);
        options.addOptionGroup(outputOptions);

        return options;
    }

    protected void printHelpMenu(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar owl2vowl.jar", options);
    }

    protected Exporter createExporterFromOption(
            CommandLine line, 
            IRI ontologyIri
            ) {
        if (line.hasOption(ECHO_OPTION_NAME)) {
            return new ConsoleExporter();
        } else {
            String exportPath = null;

            if (line.hasOption(OUTPUT_OPTION_NAME)) {
                exportPath = line.getOptionValue(OUTPUT_OPTION_NAME);
            }

            return generateFileExporter(ontologyIri, exportPath);
        }
    }

    protected FileExporter generateFileExporter(IRI ontologyIri, String filePath) {
        String filename;

        if (filePath != null) {
            filename = filePath;
        } else {
            // catch empty remainder
            try {
                filename = FilenameUtils.removeExtension(ontologyIri.getRemainder().get()) + ".json";
            } catch (Exception e){
                System.out.println("Failed to extract filename from iri");
                System.out.println("Reason: "+e);
                String defaultName="default.json";
                System.out.println("Writing to '"+defaultName+"'");
                filename=defaultName;
            }
        }

        return new FileExporter(new File(filename));
    }
}
