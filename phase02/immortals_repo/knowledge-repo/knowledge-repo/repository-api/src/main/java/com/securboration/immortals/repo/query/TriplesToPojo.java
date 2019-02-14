package com.securboration.immortals.repo.query;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.securboration.immortals.j2t.analysis.ClassBytecodeHelper;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.repo.ontology.FusekiClient;

/**
 * Maps triples previously derived from POJOs back into POJOs.
 * 
 * @author jstaples
 *
 */
public class TriplesToPojo {
    
    private static final Logger logger = 
            LogManager.getLogger(TriplesToPojo.class);
    
    private TriplesToPojo(String graphName, FusekiClient client) {
        this.graphName = graphName;
        this.client = client;
    }
    
    private final Map<String,Object> urisToObjects = new HashMap<>();
    
    private final Set<String> problematicPropertyUris = new HashSet<>();
    
    private final String graphName;
    private final FusekiClient client;
    
    
    /**
     * Converts a single individual described completely in a single graph into
     * a Plain Old Java Object (POJO).
     * 
     * @param graphName
     *            the name of a graph containing all information about the
     *            indicated individual
     * @param individualUri
     *            the URI of an individual to convert into a POJO
     * @param client
     *            a client for interacting with Fuseki
     * @return a POJO representation of the individual
     * 
     * @throws ClassNotFoundException
     *             if one or more classes needed to populate the POJO cannot be
     *             located on the application classpath
     * @throws InstantiationException
     *             if one or more classes needed to populate the POJO do not
     *             have public default constructors
     * @throws IllegalAccessException
     *             if a security manager prevents us from setting fields
     *             reflectively
     * @throws NoSuchFieldException
     *             if we attempt to interact with a nonexistent field
     * @throws SecurityException
     *             if a security manager prevents us from setting fields
     *             reflectively
     */
    public static Object convert(
            final String graphName,
            final String individualUri,
            final FusekiClient client
            ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
        
        TriplesToPojo converter = new TriplesToPojo(graphName,client);
        
        final Class<?> pojoType = converter.getPojoType(individualUri);
        final Object pojo = pojoType.newInstance();
        
        converter.populate(
            pojo,
            individualUri
            );
        
        return pojo;
    }
    
    /**
     * Executes a SPARQL SELECT query. Individuals that bind to variables in the
     * results will be reconciled within a provided graph name.
     * 
     * @param sparqlSelectQuery
     *            a SELECT query to execute. The query may span multiple graphs,
     *            but note that individual information will be reconciled using
     *            only the triples in the graph below.
     * @param graphName
     *            the name of a graph containing information about the
     *            individuals that bind to results in the query
     * @param client
     *            a fuseki client
     * @param config
     *            a ObjectToTriples conversion context. This context permits the
     *            results of the SPARQL query to be used in subsequent calls to
     *            ObjectToTriples while preserving URI integrity.
     * @return a context for iterating through the results of the query and in
     *         which URI resources are represented as POJOs (literals are boxed
     *         into types such as Integer and Boolean). 
     *         <p><b>Note</b>: it is recommended
     *         that you close this context (otherwise, it may grow in size over
     *         time until it is at least as large as an in-memory representation
     *         of the graph).
     */
    public static SparqlPojoContext sparqlSelect(
            final String sparqlSelectQuery,
            final String graphName,
            final FusekiClient client,
            final ObjectToTriplesConfiguration config
            ){
        SparqlPojoContext context = new SparqlPojoContext(
            sparqlSelectQuery,
            graphName,
            client,
            config
            );
        
        return context;
    }
    
    public static class SparqlPojoContext implements Closeable, Iterable<Map<String,Object>>{
        
        private final TriplesToPojo converter;
        private final ObjectToTriplesConfiguration config;
        
        private final List<Map<String,RDFNode>> solutions = new ArrayList<>();
        
        private SparqlPojoContext(
                final String sparqlSelectQuery,
                final String graphName,
                final FusekiClient client,
                final ObjectToTriplesConfiguration config
                ){
            this.config = config;
            this.converter = new TriplesToPojo(graphName,client);
            
            ResultSetAggregator aggregator = new ResultSetAggregator();
            client.executeSelectQuery(sparqlSelectQuery, aggregator);
            
            solutions.addAll(aggregator.getSolutions());
            
        }

        @Override
        public void close() throws IOException {
            if(config == null){
                return;
            }
            
            //unbind from the ObjectToTriples context
            for(String uri:converter.urisToObjects.keySet()){
                Object o = converter.urisToObjects.get(uri);
                
                config.getNamingContext().clearNameForObject(o);
            }
        }
        
        /**
         * 
         * @return an iterator over individual solutions to the query. Literal
         *         values will be boxed into Java types (e.g., int -> Integer).
         *         URI resource nodes will be converted into their corresponding
         *         Java POJO.
         */
        @Override
        public Iterator<Map<String,Object>> iterator(){
            
            return new Iterator<Map<String,Object>>(){
                
                int currentIndex = 0;

                Map<String,Object> uriToObjectCache = new HashMap<>();

                @Override
                public boolean hasNext() {
                    return currentIndex < solutions.size();
                }

                /**
                 * 
                 * @return the next solution to the query with literal types
                 * boxed (e.g., int -> Integer) and URI nodes converted into
                 * their corresponding Java POJO.  After calling this method,
                 * all such POJOs are bound to URIs when they are encountered 
                 * in subsequent calls to
                 * {@link com.securboration.immortals.o2t.analysis.ObjectToTriples#convert(ObjectToTriplesConfiguration, Object) ObjectToTriples.convert}
                 */
                @Override
                public Map<String, Object> next() {
                    Map<String,RDFNode> next = solutions.get(currentIndex);
                    currentIndex++;
                    
                    Map<String,Object> converted = new HashMap<>();
                    
                    for(String var:next.keySet()){
                        RDFNode value = next.get(var);
                        
                        if(value.isLiteral()){
                            converted.put(var, value.asLiteral().getValue());
                        } else if(value.isURIResource()){
                            try {
                                final String uri = value.asResource().getURI();
                                
                                final Object pojo;
                                
                                if(converter.urisToObjects.containsKey(uri)){
                                    pojo = converter.urisToObjects.get(uri);
                                } else {
                                    final Class<?> pojoType = 
                                            converter.getPojoType(uri);
                                    
                                    pojo = pojoType.newInstance();
                                    
                                    converter.populate(
                                        pojo,
                                        uri
                                        );
                                }
                                
                                converted.put(var, pojo);
                                converted.put(var+"$uri", uri);
                            } catch (ClassNotFoundException
                                    | InstantiationException
                                    | IllegalAccessException
                                    | NoSuchFieldException
                                    | SecurityException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    
                    updateCache(converter.urisToObjects);
                    
                    return converted;
                }
                
                private void updateCache(
                        Map<String,Object> newMapping
                        ){
                    Map<String,Object> oldMapping = uriToObjectCache;
                    
                    Set<String> newKeys = new HashSet<>(newMapping.keySet());
                    newKeys.removeAll(oldMapping.keySet());
                    
                    Set<String> bothKeys = new HashSet<>(newMapping.keySet());
                    bothKeys.retainAll(oldMapping.keySet());
                    
                    //make sure that no old key maps to a different object than
                    // before
                    for(String bothKey:bothKeys){
                        Object oldObject = oldMapping.get(bothKey);
                        Object newObject = newMapping.get(bothKey);
                        
                        if(oldObject != newObject){
                            throw new RuntimeException(
                                "sanity check failed: redefinition of URI " + bothKey
                                );
                        }
                    }
                    
                    //any new key should map to an Object
                    for(String newKey:newKeys){
                        Object newObject = newMapping.get(newKey);
                        
                        uriToObjectCache.put(newKey, newMapping.get(newKey));
                        
                        if(config != null){
                            config.getNamingContext().setNameForObject(
                                newObject, 
                                newKey
                                );
                        }
                    }
                }
                
            };
        }
    }
    
    private void populate(
            final Object objectToPopulate,
            final String individualUri
            ) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException{
        
        //TODO: the performance is poor because many small SPARQL queries are 
        //      executed
        // a performance enhancement would be to execute a smaller number of 
        //      queries and cache the results for future use
        
        Map<Resource,List<RDFNode>> properties = getRelationships(
            graphName,
            individualUri,
            client
            );
        
        for(Resource property:properties.keySet()){
            logger.trace(
                String.format(
                    "\t%s -->\n", 
                    property.getURI()
                    )
                );
            
            populateFieldWithProperty(
                objectToPopulate,
                property,
                properties.get(property)
                );
        }
        
        urisToObjects.put(
            individualUri, 
            objectToPopulate
            );
    }
    
    private void populateFieldWithProperty(
            final Object template, 
            final Resource property, 
            final List<RDFNode> values
            ) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException{
        logger.trace(String.format(
            "searching for field in %s corresponding to %s (%d values will be injected)\n", 
            template.getClass().getName(), 
            property.toString(),
            values.size()
            ));
        logger.trace(
            String.format(
                "\tproperty name = %s\n", 
                getFieldNameFromProperty(property)
                )
            );
        
        if(problematicPropertyUris.contains(property.getURI())){
            logger.trace(
                String.format(
                    "Skipping property %s", 
                    property.getURI()
                )
            );
            
            return;
        }
        
        Field f = getFieldForProperty(
            template.getClass(),
            property
            );
        
        if(f == null){
            logger.warn(
                String.format(
                    "unable to find a field for (extraneous?) " +
                    "property %s, it will be skipped in the future", 
                    property.getURI()
                )
            );
            
            problematicPropertyUris.add(property.getURI());
            
            return;
        }
        
        //TODO: handle Map
        
        final Class<?> fieldType = f.getType();
        
        Object valueToSet = null;
        
        if(fieldType.equals(Class.class)){
            if(values.size() != 1){
                throw new RuntimeException("sanity check failed");
            }
            
            valueToSet = getPojoType(values.get(0).asResource().getURI());
        } else if(fieldType.equals(byte[].class)){
            if(values.size() != 1){
                throw new RuntimeException(
                    "expected 1 value but got " + values.size()
                    );
            }
            
            valueToSet = Base64.getDecoder().decode(
                values.get(0).asLiteral().getString()
                );
        } else if(fieldType.equals(char.class)){
            if(values.size() != 1){
                throw new RuntimeException(
                    "expected 1 value but got " + values.size()
                    );
            }
            
            valueToSet = values.get(0).asLiteral().getChar();
        } else if(fieldType.equals(short.class)){
            if(values.size() != 1){
                throw new RuntimeException(
                    "expected 1 value but got " + values.size()
                    );
            }
            
            valueToSet = values.get(0).asLiteral().getShort();
        } else if(fieldType.equals(byte.class)){
            if(values.size() != 1){
                throw new RuntimeException(
                    "expected 1 value but got " + values.size()
                    );
            }
            
            valueToSet = values.get(0).asLiteral().getByte();
        } else if(fieldType.isArray()||(Set.class.isAssignableFrom(fieldType))||(List.class.isAssignableFrom(fieldType))){
            Class<?> componentType = f.getType().getComponentType();
            if(componentType == null){
                componentType = Object.class;
            }
            
            if(Set.class.isAssignableFrom(fieldType)||List.class.isAssignableFrom(fieldType)){
                if(f.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType t = (ParameterizedType)f.getGenericType();
                    
                    Type[] types = t.getActualTypeArguments();
                    if(types.length != 1){
                        throw new RuntimeException(
                            "expected 1 but got " + types.length
                            );
                    }
                    componentType = Class.forName(types[0].getTypeName());
                } else {
                    throw new RuntimeException(
                        "unhandled case: Set or List with generic type " + 
                                f.getGenericType().getClass().getName());
                }
            }
            
            //the values should be placed in an array
            Object list = Array.newInstance(
                componentType, 
                values.size()
                );
            
            int index = -1;
            for(RDFNode value:values){
                index++;
                if(value.isLiteral()){
                    //the array element is a literal
                    Object literal = getLiteralForProperty(
                        componentType,
                        graphName,
                        property,
                        value.asLiteral(),
                        client
                        );
                    
                    logger.trace(
                        String.format(
                            "array[%d]=a %s (vs %s)\n", 
                            index,
                            literal.getClass().getName(), 
                            f.getType().getName()
                            )
                        );
                    
                    Array.set(list, index, literal);
                } else {
                    //the array element is an object ref
                    final String uri = value.asResource().getURI();
                    
                    Object instance = urisToObjects.get(uri);
                    if(instance != null){
                        valueToSet = instance;
                    } else {
                        Class<?> instanceType = getPojoType(uri);
                        
                        instance = instanceType.newInstance();
                        urisToObjects.put(uri, instance);
                        
                        //recurse
                        populate(
                            instance,
                            uri
                            );
                    }
                    
                    //if the array contains classes, get the instance's class
                    if(Class.class.isAssignableFrom(componentType)){
                        Array.set(list, index, instance.getClass());
                    } else {
                        Array.set(list, index, instance);
                    }
                }
            }
            
            valueToSet = list;
            
            if(Set.class.isAssignableFrom(fieldType)){
                Set<Object> set = new LinkedHashSet<>();
                for(int i=0;i<Array.getLength(list);i++){
                    set.add(Array.get(list, i));
                }

                valueToSet = set;
            } else if(List.class.isAssignableFrom(fieldType)){
                List<Object> aList = new ArrayList<>();
                for(int i=0;i<Array.getLength(list);i++){
                    aList.add(Array.get(list, i));
                }

                valueToSet = aList;
            }
        } else if(values.size() == 1){
            //the value should be placed directly in a field
            RDFNode value = values.get(0);
            
            if(value.isLiteral()){
                //the value is a literal
                valueToSet = getLiteralForProperty(
                    fieldType,
                    graphName,
                    property,
                    value.asLiteral(),
                    client
                    );
            } else if(value.isURIResource()) {
                //the value is an object
                final String uri = value.asResource().getURI();
                
                Object instance = urisToObjects.get(uri);
                if(instance != null){
                    valueToSet = instance;
                } else {
                    Class<?> instanceType = getPojoType(uri);
                    instance = instanceType.newInstance();
                    urisToObjects.put(uri, instance);
                    
                    //recurse
                    populate(
                        instance,
                        uri
                        );
                }
                
                valueToSet = instance;
            }
        } else {
            //do nothing
        }
        
        if(valueToSet != null){
            f.setAccessible(true);
            f.set(template, valueToSet);
        } else {
            logger.warn(
                String.format(
                    "WARNING: unable to populate field for property %s\n", 
                    property.getURI()
                    )
                );
        }
    }
    
    private static Object getLiteralForProperty(
            Class<?> expectedType,
            String graphName,
            Resource property,
            Literal literal,
            FusekiClient client
            ) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
        if(!literal.isLiteral()){
            throw new RuntimeException(
                "should only be invoked with literal node R values"
                );
        }
        
        if(!expectedType.isEnum()){
            return literal.getValue();
        } else {
            for(Object e:expectedType.getEnumConstants()){
                if(e.toString().equals(literal.getString())){
                    return e;
                }
            }
            
            throw new RuntimeException(
                "value " + 
                literal.getString() + 
                " is not part of enumeration " + expectedType.getName()
                );
        }
        
    }
    
    private static Field getFieldForProperty(Class<?> c, Resource property) throws NoSuchFieldException, SecurityException{
        try{
            Map<String,String> propertiesToFields = 
                    ClassBytecodeHelper.getPropertyToFieldMapping(c);
            
            String propertyName = property.getURI().substring(
                property.getURI().lastIndexOf("#")
                );
            
            String mapping = propertiesToFields.get(propertyName);
            
            if(mapping != null){
                logger.trace(
                    String.format(
                        "mapped %s to field %s.%s",
                        property.getURI(),
                        c.getName(),
                        mapping
                        )
                    );
                
                return c.getDeclaredField(mapping);
            }
            
            return c.getDeclaredField(
                getFieldNameFromProperty(property)
                );
        } catch(NoSuchFieldException e){//TODO: check for type hierarchy collisions.  E.g., super declares same field as child.
            if(c.getSuperclass() == null){
                return null;
            }
            
            return getFieldForProperty(c.getSuperclass(),property);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    
    private static String getFieldNameFromProperty(Resource property){
        //hasName
        //name
        final String uri = property.getURI();
        final String hasSubstring = uri.substring(uri.lastIndexOf("#")+1);
        final String name = hasSubstring.substring(3);
        
        final String fieldName = name.toLowerCase().charAt(0) + name.substring(1);
        
        return fieldName;   
    }
    
    private Class<?> getPojoType(
            final String individualUri
            ) throws ClassNotFoundException{
        
        RDFNode correspondingPojo = getCorrespondingPojo(
            graphName,
            individualUri,
            client
            );
        
        if(correspondingPojo != null){
            return Class.forName(
                correspondingPojo.asLiteral().getString()
                );
        }
        
        RDFNode type = getTypeOf(graphName,individualUri,client);
        if(type == null){
            throw new RuntimeException(
                "no type found for individual " + individualUri
                );
        }
        
        boolean stop = false;
        while(!stop){
            correspondingPojo = getCorrespondingPojo(
                graphName,
                type.asResource().getURI(),
                client
                );
            
            if(correspondingPojo != null){
                return Class.forName(
                    correspondingPojo.asLiteral().getString()
                    );
            }
            
            type = getSuperclassOf(
                graphName,
                type.asResource().getURI(),
                client
                );
            
            if(type == null){
                throw new RuntimeException(
                    "overshot type hierarchy looking for pojo anchor for " + individualUri
                    );
            }
        }
        
        throw new RuntimeException("should never be reached");
    }
    
    private static RDFNode getCorrespondingPojo(
            final String graphName, 
            final String type, 
            final FusekiClient client
            ){
        final String query = (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT DISTINCT ?pojo WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        <?type?> IMMoRTALS:hasPojoProvenance ?pojo .\r\n" +
                "    } .\r\n" + 
                "}"
                )
                .replace("?GRAPH?", graphName)
                .replace("?type?", type)
                ;
        
        RDFNode solution = getSingleValueOrNull(select(query,client));
        
        return solution;
    }
    
    private static RDFNode getTypeOf(
            final String graphName, 
            final String individual, 
            final FusekiClient client
            ){
        final String query = (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT DISTINCT ?type WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        <?individual?> a ?type .\r\n" +
                "    } .\r\n" + 
                "}"
                )
                .replace("?GRAPH?", graphName)
                .replace("?individual?", individual)
                ;
        
        return getSingleValueOrNull(select(query,client));
    }
    
    private static Map<Resource,List<RDFNode>> getRelationships(
            final String graphName, 
            final String individual, 
            final FusekiClient client
            ){
        final String query = (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT DISTINCT ?p ?o WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        <?individual?> ?p ?o .\r\n" +
                "    } .\r\n" + 
                "}"
                )
                .replace("?GRAPH?", graphName)
                .replace("?individual?", individual)
                ;
        
        Map<Resource,List<RDFNode>> map = new HashMap<>();
        
        for(Map<String,RDFNode> solution:select(query,client)){
            Resource p = solution.get("p").asResource();
            RDFNode rValue = solution.get("o");
            
            List<RDFNode> values = map.get(p);
            if(values == null){
                values = new ArrayList<>();
                map.put(p,values);
            }
            values.add(rValue);
        }
        
        return map;
    }
    
    private static RDFNode getSuperclassOf(
            final String graphName, 
            final String type, 
            final FusekiClient client
            ){
        
        /* retrieves only the *DIRECT* superclass of type */
        
        final String query = (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT DISTINCT ?parent WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        <?type?> <?directSubclassOf?> ?parent .\r\n" +
                "    } .\r\n" + 
                "}"
                )
                .replace("?GRAPH?", graphName)
                .replace("?type?", type)
                .replace("?directSubclassOf?", ReasonerVocabulary.directSubClassOf.getURI())
                ;
        
        /*
         * Could also do something like the following (which works on other 
         * non-Jena impls:
  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
  PREFIX owl: <http://www.w3.org/2002/07/owl#> 
  SELECT * { 
    <ind> rdf:type ?directType .
    FILTER NOT EXISTS {
      <ind> rdf:type ?type .
      ?type rdfs:subClassOf ?directType .
      FILTER NOT EXISTS {
         ?type owl:equivalentClass ?directType .
      }
    }
  }
         */
        //https://stackoverflow.com/questions/32935506/removing-unwanted-superclass-answers-in-sparql
        
        return getSingleValueOrNull(select(query,client));
    }
    
    private static RDFNode getSingleValueOrNull(List<Map<String,RDFNode>> solutions){
        List<RDFNode> values = getSingleValues(solutions);
        
        if(values.size() > 1){
            throw new RuntimeException(
                "expected exactly 1 value but found " + values.size() + " " + values.toString()
                );
        } else if(values.size() == 0){
            return null;
        }
        
        return values.get(0);
    }
    
    private static List<RDFNode> getSingleValues(List<Map<String,RDFNode>> solutions){
        List<RDFNode> values = new ArrayList<>();
        
        for(Map<String,RDFNode> solution:solutions){
            if(solution.size() > 1){
                throw new RuntimeException(
                    "expected at most 1 k-v pair but found " + 
                    solution.size() + " for keys " + 
                    solution.keySet()
                    );
            }
            
            values.addAll(solution.values());
        }
        
        return values;
    }
    
    private static List<Map<String,RDFNode>> select(
            final String sparql, 
            final FusekiClient client
            ){
        ResultSetAggregator r = new ResultSetAggregator();
        client.executeSelectQuery(sparql, r);
        
        List<Map<String,RDFNode>> solutions = r.getSolutions();
        
        logger.trace(
            String.format(
                "> QUERY \n%s\n> RETURNED %d results:\n",
                "\t"+sparql.replace("\n", "\n\t"),
                solutions.size()
                )
            );
        
        for(Map<String,RDFNode> s:solutions){
            for(String var:s.keySet()){
                logger.trace(
                    String.format(
                        "\t%s -> %s", 
                        var, 
                        s.get(var)
                        )
                    );
            }
            logger.trace("");
        }
        
        return solutions;
    }    

}
