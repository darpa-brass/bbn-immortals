package com.securboration.immortals.service.vis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLOntologyWalker;

import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.Exporter;
import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.JsonGenerator;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.data.VowlData;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.entities.AbstractEntity;
import de.uni_stuttgart.vis.vowl.owl2vowl.model.ontology.OntologyMetric;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.owlapi.EntityCreationVisitor;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.owlapi.IndividualsVisitor;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.AnnotationParser;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.BaseIriCollector;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.EquivalentSorter;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.ImportedChecker;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.OntologyInformationParser;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.TypeSetter;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.classes.GenericClassAxiomVisitor;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.classes.HasKeyAxiomParser;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.classes.OwlClassAxiomVisitor;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.property.DataPropertyVisitor;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.property.DomainRangeFiller;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.property.ObjectPropertyVisitor;
import de.uni_stuttgart.vis.vowl.owl2vowl.parser.vowl.property.VowlSubclassPropertyGenerator;

/**
 * Abstract converter which processes the most part of the converting.
 * The sub classes can specify the source of the ontology or to some additional processing if necessary.
 */
public class VowlGenerator {
    private static final Logger logger = LogManager.getLogger(VowlGenerator.class);
    
    protected final JsonGenerator jsonGenerator = new JsonGenerator();
    protected String loadedOntologyPath;
    protected OWLOntologyManager manager;
    protected VowlData vowlData;
    protected OWLOntology ontology;
    protected boolean initialized = false;
    
    private final String rdf;
    private final String rdfIri;
    
    
    private VowlGenerator(
            final String data, 
            final String lang,
            final String documentIri
            ){
        this.rdf = getRdfForm(data,lang);
        this.rdfIri = documentIri;
    }
    
    public static void main(String[] args) throws Exception {
        
        final File dir = new File(
            "C:\\Users\\Securboration\\Desktop\\code\\immortals\\" +
            "trunk\\knowledge-repo\\vocabulary\\ontology-package\\" +
            "target\\classes\\ontology");
        
        StringBuilder sb = new StringBuilder();
        
        List<String> allTtl = new ArrayList<>();
        for(File f:FileUtils.listFiles(dir, new String[]{"ttl"}, true)){
            System.out.printf("%s\n", f.getName());
            System.out.printf("%s\n", f.getAbsolutePath());
            
            File outputPath = new File("output/" + f.getName().replace(".ttl", ".json").replace("_", "-"));
            
            
            String template = "                     <li><a href=\"#NAME\" id=\"NAME\">DESC</a></li>\n";
            template = template.replace("NAME", f.getName().replace("_", "-").replace(".ttl", ""));
            
            String name = f.getAbsolutePath().contains("/individuals/") ? 
                    "IMMORTALS individuals: " + f.getName()
                    :
                    "IMMORTALS vocabulary: " + f.getName()
                    ;
            template = template.replace("DESC", name);
            sb.append(template);
            
            
            try{
                allTtl.add(getTtlForm(FileUtils.readFileToString(f),"TURTLE"));
                
                FileUtils.writeStringToFile(//Todo
                    new File(outputPath.getAbsolutePath().replace(".json", ".ttl")), 
                    getTtlForm(FileUtils.readFileToString(f),"TURTLE"));
                
                //TODO: there is a weird when processing typed literal strings
                final String vowlJson = VowlGenerator.generate(
                    FileUtils.readFileToString(f).replace("^^<xsd:string>", ""),
                    "TURTLE",
                    "http://darpa.mil/immortals/ontology/r2.0.0"
                    );
                
                FileUtils.writeStringToFile(outputPath, vowlJson);
            } 
//            catch(RuntimeException e){
//                e.printStackTrace();//TODO
//            }
            finally {
                //do nothing
            }
        }
        
        {
            Model m = ModelFactory.createDefaultModel();
            for(String s:allTtl){
                m.read(new ByteArrayInputStream(s.getBytes()), null, "TURTLE");
            }
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            m.write(os, "TURTLE");
            
            FileUtils.writeStringToFile(
                new File("output/immortals-all.ttl"), 
                os.toString()
                );
            
            final String vowlJson = VowlGenerator.generate(
                os.toString().replace("^^<xsd:string>", ""),
                "TURTLE",
                "http://darpa.mil/immortals/ontology/r2.0.0"
                );
            
            FileUtils.writeStringToFile(
                new File("output/immortals-all.json"), 
                vowlJson
                );
            
            String template = "                     <li><a href=\"#NAME\" id=\"NAME\">DESC</a></li>\n";
            template = template.replace("NAME", "immortals-vocab-all");
            template = template.replace("DESC", "IMMoRTALS all-in-one vocabulary");
            sb.append(template);
        }
        
        System.out.println(sb.toString());
    }
    
//    public static void main(String[] args) throws Exception {
//        
//        final String path = 
//                "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\" +
//                "knowledge-repo\\toolchain\\visualization\\webvowl_1.0.3\\" +
//                "owl2vowl\\ontologies\\" +
//                "pizza.owl";
//                //"wine.rdf";
//                //"immortals_core.ttl";
//        
//        final String vowlJson = VowlGenerator.generate(
//            FileUtils.readFileToString(new File(path)),
//            "RDF/XML",
//            "http://darpa.mil/immortals/ontology/r2.0.0"
//            );
//        
//        FileUtils.writeStringToFile(
//            new File("vowl.json"), 
//            vowlJson
//            );
//    }
    
    public static String generate(
            String artifact, 
            String lang,
            String artifactIri
            ) throws Exception{
        VowlGenerator g = new VowlGenerator(
            artifact,
            lang,
            artifactIri
            );
        
        g.convert();
        final String[] s = new String[]{null};
        g.export(new Exporter(){

            @Override
            public void write(String text) throws Exception {
                s[0] = text;
            }
            
        });
        
        return s[0];
    }
    
    private static String getRdfForm(String input,String lang){
        Model m = ModelFactory.createDefaultModel();
        
        m.read(
            new ByteArrayInputStream(input.getBytes()), 
            null, 
            lang
            );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.write(os, "RDF/XML", null);
        
        return os.toString();
    }
    
    private static String getTtlForm(String input,String lang){
        Model m = ModelFactory.createDefaultModel();
        
        m.read(
            new ByteArrayInputStream(input.getBytes()), 
            null, 
            lang
            );
        
        for(String key:new HashSet<>(m.getNsPrefixMap().keySet())){
            m.removeNsPrefix(key);//TODO
        }
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.write(os, "TURTLE", null);
        
        return os.toString();
    }

    private void preLoadOntology() throws OWLOntologyCreationException, IOException {
        loadOntology();
        initialized = true;
    }

    private void loadOntology() throws OWLOntologyCreationException, IOException{
        //TODO
        
//        File tempFile = new File("tmp");
        
//        FileUtils.writeStringToFile(tempFile,getTtlForm(rdf,"RDF/XML"));//TODO
        
        manager = OWLManager.createOWLOntologyManager();
        
        manager.setIRIMappers(new HashSet<>(Arrays.asList(new OWLOntologyIRIMapper(){
            
            private static final long serialVersionUID = 1L;

            @Override
            public IRI getDocumentIRI(IRI ontologyIRI) {
                System.out.println(ontologyIRI);
                return ontologyIRI;//TODO
            }
            
        })));
//        manager.setOntologyLoaderConfiguration(OWLOntologyLoaderConfiguration.);
        
//        ontology = manager.loadOntology(
//            IRI.create("https://www.w3.org/TR/owl-guide/wine.rdf")
//            );
        
//        ontology = manager.loadOntologyFromOntologyDocument(tempFile);
        
//        .loadOntologyFromOntologyDocument(
//            IRI.create(tempFile)
//            IRI.create(new File(path))
//            new ByteArrayInputStream(rdf.getBytes())
//            );
        
//        manager.setOntologyDocumentIRI(ontology, IRI.create(rdfIri));
        
        ontology = manager.loadOntologyFromOntologyDocument(
            new ByteArrayInputStream(rdf.getBytes())
            );
        
        System.out.println("loaded " + ontology.getAxiomCount() + " axioms");
        
//        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//        OWLOntology displayThis = manager.loadOntologyFromOntologyDocument(
//            new ByteArrayInputStream(rdf)
//            );

        
        loadedOntologyPath = rdfIri;
    }

    private void preParsing(OWLOntology ontology, VowlData vowlData, OWLOntologyManager manager) {
        @SuppressWarnings("deprecation")
        OWLOntologyWalker walker = new OWLOntologyWalker(ontology.getImportsClosure());
        EntityCreationVisitor ecv = new EntityCreationVisitor(vowlData);
        
        try {
            walker.walkStructure(ecv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        new OntologyInformationParser(vowlData, ontology).execute();
    }

    private void parsing(OWLOntology ontology, VowlData vowlData, OWLOntologyManager manager) {
        processClasses(ontology, vowlData);
        processObjectProperties(ontology, vowlData);
        processDataProperties(ontology, vowlData);
        processIndividuals(ontology, vowlData, manager);
        processGenericAxioms();
    }

    @SuppressWarnings("deprecation")
    private void processIndividuals(OWLOntology ontology, VowlData vowlData, OWLOntologyManager manager) {
        // TODO check all classes
        ontology.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            for (OWLOntology owlOntology : manager.getOntologies()) {
                EntitySearcher.getIndividuals(owlClass, owlOntology).forEach(owlIndividual -> owlIndividual.accept(new IndividualsVisitor(vowlData,
                        owlIndividual, owlClass, manager)));
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void processObjectProperties(OWLOntology ontology, VowlData vowlData) {
        for (OWLObjectProperty owlObjectProperty : ontology.getObjectPropertiesInSignature(Imports.INCLUDED)) {
            for (OWLObjectPropertyAxiom owlObjectPropertyAxiom : ontology.getAxioms(owlObjectProperty, Imports.INCLUDED)) {
                try {
                    owlObjectPropertyAxiom.accept(new ObjectPropertyVisitor(vowlData, owlObjectProperty));
                } catch (Exception e){
                    System.out.println("          @WORKAROUND: Failed to accept property with HAS_VALUE OR  SubObjectPropertyOf ... SKIPPING THIS"  );
                    System.out.println("          propertyName: "+owlObjectProperty);
                    System.out.println("          propertyAxiom: "+owlObjectPropertyAxiom);
                    continue;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void processDataProperties(OWLOntology ontology, VowlData vowlData) {
        for (OWLDataProperty property : ontology.getDataPropertiesInSignature(Imports.INCLUDED)) {
            for (OWLDataPropertyAxiom propertyAxiom : ontology.getAxioms(property, Imports.INCLUDED)) {
                propertyAxiom.accept(new DataPropertyVisitor(vowlData, property));
            }
        }
    }

    private void createSubclassProperties(VowlData vowlData) {
        new VowlSubclassPropertyGenerator(vowlData).execute();
    }

    private void fillDomainRanges(VowlData vowlData) {
        new DomainRangeFiller(vowlData, vowlData.getProperties()).execute();
    }

    @SuppressWarnings("deprecation")
    private void processClasses(OWLOntology ontology, VowlData vowlData) {
        for (OWLClass owlClass : ontology.getClassesInSignature(Imports.INCLUDED)) {
            for (OWLClassAxiom owlClassAxiom : ontology.getAxioms(owlClass, Imports.INCLUDED)) {
                owlClassAxiom.accept(new OwlClassAxiomVisitor(vowlData, owlClass));
            }

            HasKeyAxiomParser.parse(ontology, owlClass, vowlData);
        }
    }

    @SuppressWarnings("deprecation")
    private void processGenericAxioms() {
        ontology.getGeneralClassAxioms().forEach(owlClassAxiom -> owlClassAxiom.accept(new GenericClassAxiomVisitor(vowlData)));
    }

    private void parseAnnotations(VowlData vowlData, OWLOntologyManager manager) {
        AnnotationParser annotationParser = new AnnotationParser(vowlData, manager);
        annotationParser.parse();
    }

    private void setCorrectType(Collection<AbstractEntity> entities) {
        for (AbstractEntity entity : entities) {
            entity.accept(new TypeSetter());
        }
    }

    private void postParsing(OWLOntology loadedOntology, VowlData vowlData, OWLOntologyManager manager) {
        setCorrectType(vowlData.getEntityMap().values());
        parseAnnotations(vowlData, manager);
        fillDomainRanges(vowlData);
        createSubclassProperties(vowlData);
        new ImportedChecker(vowlData, manager, loadedOntology, loadedOntologyPath).execute();
        vowlData.getEntityMap().values().forEach(entity -> entity.accept(new EquivalentSorter(ontology.getOntologyID().getOntologyIRI().orElse(IRI
                .create(loadedOntologyPath)), vowlData)));
        new BaseIriCollector(vowlData).execute();
    }

    /**
     * Executes the complete conversion to the webvowl compatible json format.
     * Normally is only called ones during export. But if a new conversion is required just call this method before exporting.
     * <p>
     *     The parsing is separated in three steps: The pre parsing -> normal parsing -> post parsing.
     *     This is necessary because we can access some properties of the entities only if we have the corresponding entity.
     *     For this example the pre parsing can be used to retrieve all entities without any additional special components.
     *     After that we can access the more special properties of the entity with helper classes like {@link EntitySearcher}.
     * </p>
     */
    private void convert() {
        if (!initialized) {
            try {
                preLoadOntology();
            } catch (OWLOntologyCreationException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        vowlData = new VowlData();
        vowlData.setOwlManager(manager);
        // TODO Probably the parsing could be automatized via class annotation and annotation parsing.
        // e.q. @PreParsing, @Parsing, @PostParsing just as an idea for improvement
        preParsing(ontology, vowlData, manager);
        parsing(ontology, vowlData, manager);
        postParsing(ontology, vowlData, manager);
        processMetrics();
    }

    private void processMetrics() {
        OntologyMetric metrics = new OntologyMetric(ontology);
        metrics.calculate(vowlData);
        vowlData.setMetrics(metrics);
    }

    /**
     * Exports the generated data according to the implemented {@link Exporter}.
     *
     * @param exporter The exporter.
     * @throws Exception Any exception during json generation.
     */
    private void export(Exporter exporter) throws Exception {
        if (vowlData == null) {
            convert();
        }
        
        jsonGenerator.execute(vowlData);
        jsonGenerator.export(exporter);
    }
}
