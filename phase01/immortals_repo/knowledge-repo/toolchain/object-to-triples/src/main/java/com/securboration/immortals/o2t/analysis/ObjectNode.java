package com.securboration.immortals.o2t.analysis;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph of a Java class instance (object)
 * 
 * @author jstaples
 *
 */
public class ObjectNode {

    private final ObjectNode parent;
    private final Object value;
    private final Class<?> actualType;
    private final Class<?> possibleType;
    
    //if it's a normal Java object, it will have these
    private Map<String, ObjectNode> fields = null;
    
    // ** OR ** //
    
    //if it's an array object it will have these
    private List<ObjectNode> arrayValues = null;
    
    /**
     * 
     * @return the index of the current node in an array, if it belongs to an
     *         array. Otherwise null
     */
    public Integer getArrayIndex(){
        
        if(parent.arrayValues == null){
            return null;
        }
        
        int index = parent.arrayValues.indexOf(this);
        
        if(index == -1){
            throw new RuntimeException("could not find array index!");
        }
        
        return index;
    }
    
    /**
     * 
     * @return the name of the field holding the current node, if it belongs to
     *         a field. Otherwise null
     */
    public String getFieldName(){
        
        if(parent.fields == null){
            return null;
        }
        
        for(String key:parent.fields.keySet()){
            if(parent.fields.get(key) == this){
                return key;
            }
        }
        
        throw new RuntimeException("couldn't find field name!");
    }

    
    private ObjectNode(Object value, ObjectNode parent,Class<?> possibleType,Class<?> actualType) {
        this.value = value;
        this.parent = parent;
        this.possibleType = possibleType;
        this.actualType = actualType;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        toStringInternal(sb);
        
        return sb.toString();
    }
    
    private void toStringInternal(StringBuilder sb){
        toStringRecursive(0,sb,this,new HashSet<>());
    }
    
    //printfs to the stringbuilder
    private static void printf(StringBuilder sb,int depth,String format,Object...args){
        for(int i=0;i<depth;i++){
            sb.append("  ");
        }
        sb.append(String.format(format, args));
    }
    
    private static void toStringRecursive(
            final int depth,
            StringBuilder sb,
            ObjectNode current,
            Set<ObjectNode> visited
            ){
        
        if(current.arrayValues != null || current.fields != null){
            printf(sb,depth,"object of type [%s] has hash [%d]\n",current.actualType.getName(),current.value.hashCode());
        }
        
        if(current.arrayValues != null){
            visited.add(current);
            
            printf(sb,depth+1,"array with length %d:\n",current.arrayValues.size());
            
            for(int i=0;i<current.arrayValues.size();i++){
                
                printf(sb,depth+1,"array element at index %d:\n",i);
                
                ObjectNode element = current.arrayValues.get(i);
                
                if(visited.contains(element)){
                    printf(sb,depth+2,"already visited this object (hash=[%d], type=[%s]), not printing to prevent an infinite cycle\n",element.value.hashCode(),element.value.getClass().getName());
                } else {
                    toStringRecursive(depth+2,sb,element,new HashSet<>(visited));
                }
            }
        } else if(current.fields != null){
            visited.add(current);
            
            for(String fieldName:current.fields.keySet()){
                ObjectNode field = current.fields.get(fieldName);
                
                printf(sb,depth+1,"field \"%s\" contains a [%s]\n",fieldName,field.possibleType);
                
                if(visited.contains(field)){
                    printf(sb,depth+2,"already visited this object (hash=[%d], type=[%s]), not printing to prevent an infinite cycle\n",field.value.hashCode(),field.value.getClass().getName());
                } else {
                    toStringRecursive(depth+2,sb,field,new HashSet<>(visited));
                }
            }
        } else {
            printf(sb,depth+1,"value=[%s] and actualType=[%s]\n",current.value,current.value==null?"?":current.value.getClass().getName());
        }
        
    }
    

    /**
     * Constructs an ObjectNode recursively from the provided object instance
     * 
     * @param value the object to build a model from
     * @return an object model that can be easily traversed to produce triples
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static ObjectNode build(Object value) throws IllegalArgumentException, IllegalAccessException {

        Class<?> possibleType = Object.class;
        Class<?> actualType = null;
        if(value != null){
            possibleType = value.getClass();
            actualType = value.getClass();
        }
        
        ObjectNode p = 
                new ObjectNode(
                        value, 
                        null, 
                        possibleType, 
                        actualType);

        diveInto(p, new HashMap<>(), new HashMap<>());

        return p;
    }
    
    private static class KeyValue{
        Object key;
        Object value;
        
        KeyValue(Object k,Object v){
            this.key = k;
            this.value = v;
        }
    }
    
    
    private static Object arrayify(
            Object input,
            Map<Object,Object> arrayifiedObjects
            ){
        
        if(arrayifiedObjects.containsKey(input)){
            return arrayifiedObjects.get(input);
        }
        
        List<Object> resultArray = new ArrayList<>();
        
        if(input instanceof Collection){
            Collection<?> c = (Collection<?>)input;
            
            for(Object o:c){
                resultArray.add(arrayify(o,arrayifiedObjects));
            }
        } else if(input instanceof Map){
            Map<?,?> m = (Map<?,?>)input;
            
            for(Object key:m.keySet()){
                Object value = m.get(key);
                
                resultArray.add(
                        new KeyValue(
                                arrayify(key,arrayifiedObjects),
                                arrayify(value,arrayifiedObjects)));
            }
        } else {
            return input;
        }
        
        Object[] array = resultArray.toArray();
        
        arrayifiedObjects.put(input, array);
        
        return array;
    }
    
    private static ObjectNode getNodeForObject(
            Object object,
            Class<?> possibleType,
            ObjectNode parent,
            Map<Object,ObjectNode> visited,
            Map<Object,Object> arrayifiedObjects,
            List<ObjectNode> recursivelyVisitThese
            ) throws IllegalArgumentException, IllegalAccessException{

        Class<?> actualType = null;
        
        if(object != null){
            actualType = object.getClass();
        }
        
        if(object == null || isPrimitive(object) || visited.containsKey(object)){
            
            return new ObjectNode(object,parent,possibleType,actualType);
        }
        
        Object convertedObject = 
                arrayify(object,arrayifiedObjects);
        
        ObjectNode newNode = new ObjectNode(convertedObject,parent,possibleType,actualType);
        
        recursivelyVisitThese.add(newNode);
        
        visited.put(object, newNode);
        
        return newNode;
    }
    
    private static void collectFieldsRecursive(Class<?> current,Collection<Field> fields){
        
        if(current == null){
            return;
        }
        
        for(Field f:current.getDeclaredFields()){
            fields.add(f);
        }
        
        collectFieldsRecursive(current.getSuperclass(),fields);
    }
    
    private static Collection<Field> getFields(Class<?> c){
        List<Field> fields = new ArrayList<>();
        
        collectFieldsRecursive(c,fields);
        
        return fields;
    }

    private static void diveInto(
            ObjectNode o, 
            Map<Object,ObjectNode> visited,
            Map<Object,Object> arrayifiedObjects
            ) throws IllegalArgumentException, IllegalAccessException {
        
        Object value = o.value;
        
        //if the value is null, stop here
        if(value == null){
            return;
        }
        
        //if it's a primitive, stop here
        if(isPrimitive(value)) {
            return;
        }
        
        //treat Class<?> fields like primitives
        if(value instanceof Class){
            return;
        }
        
        List<ObjectNode> diveIntoThese = new ArrayList<>();
        
        if(value.getClass().isArray()){
            
            //it's an array, so dive into each element
            o.arrayValues = new ArrayList<>();
            
            for(int i=0;i<Array.getLength(value);i++){
                
                ObjectNode arrayValue = 
                        getNodeForObject(
                                Array.get(value,i),
                                value.getClass().getComponentType(),
                                o,
                                visited,
                                arrayifiedObjects,
                                diveIntoThese);
                
                o.arrayValues.add(arrayValue);
            }
        } else {
            
            //it's a non-primitive object with fields, so dive into each field
            o.fields = new HashMap<>();    
            
            for (Field f : getFields(value.getClass())) {
                if (!shouldProcessField(f)) {
                    continue;
                }
                f.setAccessible(true);
                
                ObjectNode fieldValue = 
                        getNodeForObject(
                                f.get(value),
                                f.getType(),
                                o,
                                visited,
                                arrayifiedObjects,
                                diveIntoThese
                                );
                
                o.fields.put(
                        f.getName(), 
                        fieldValue);
            }
        }
        
        //dive into each new object
        for(ObjectNode newNode:diveIntoThese){
            
            diveInto(newNode,visited,arrayifiedObjects);
        }
        
        return;
    }
    
    
    private static final Class<?>[] primitiveBoxes = new Class<?>[]{
        Integer.class,
        Float.class,
        Long.class,
        Double.class,
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        
        byte[].class
      };
      
    private static boolean shouldProcessField(Field f) {
        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
            return false;
        }

        return true;
    }

    private static boolean isPrimitive(Object o) {
        if (o.getClass().isPrimitive()) {
            return true;
        }

        for (Class<?> c : primitiveBoxes) {
            if (c.isAssignableFrom(o.getClass())) {
                return true;
            }
        }

        if (o instanceof String) {
            return true;
        }

        return false;
    }
    
    /**
     * Recursively walks through the object structure, issuing listener events
     * to the provided listener where appropriate. Note that for objects with
     * loopy referential structure (i.e., a depth-first dive into the structure
     * yields two objects with the same reference), only the first visit will be
     * issued to prevent infinite recursion.
     * 
     * @param visitor
     *            the visitor to issue notifications to
     */
    public void accept(ObjectNodeVisitor visitor){
        
        if(visitor.shouldDiveInto(this)){
            visitRecursive(visitor,this,new HashSet<>());
        }
    }
    
    private static void visitRecursive(
            ObjectNodeVisitor visitor,
            ObjectNode current,
            Set<ObjectNode> visited
            ){
        
        if(current.arrayValues != null){
            
            visited.add(current);
            
            ArrayElementVisitor arrayVisitor = 
                    visitor.visitArrayField(
                            current.parent, 
                            current
                            );
            
            for(int i=0;i<current.arrayValues.size();i++){
                
                ObjectNode element = current.arrayValues.get(i);
                
                arrayVisitor.visitArrayElement(
                        current, 
                        i, 
                        element
                        );
                
                if(visited.contains(element)){
                    //do nothing
                } else {
                    if(visitor.shouldDiveInto(element)){
                        visitRecursive(
                            visitor,
                            element,
                            new HashSet<>(visited)
                            );
                    }
                }
            }
        } else if(current.fields != null){
            
            visited.add(current);
            
            for(String fieldName:current.fields.keySet()){
                ObjectNode field = current.fields.get(fieldName);
                
                visitor.visitObjectField(
                        current, 
                        field
                        );
                
                if(visited.contains(field)){
                    //do nothing
                } else {
                    
                    if(visitor.shouldDiveInto(field)){
                        visitRecursive(visitor,field,new HashSet<>(visited));
                    }
                    
                }
            }
        } else {
            
            visitor.visitPrimitiveField(
                    current.parent, 
                    current
                    );
        }
        
    }

    /**
     * 
     * @return the parent of this node, of one exists
     */
    public ObjectNode getParent() {
        return parent;
    }

    /**
     * 
     * @return the actual value of this node
     */
    public Object getValue() {
        return value;
    }

    /**
     * 
     * @return the actual type of the value in this node
     */
    public Class<?> getActualType() {
        return actualType;
    }

    /**
     * 
     * @return the declared type of the value of this node. For example, a value
     *         in an Integer field can be null, but its type is still known to
     *         be Integer
     */
    public Class<?> getPossibleType() {
        return possibleType;
    }
}
