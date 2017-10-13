package com.securboration.immortals.j2t.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.ontology.pojos.markup.PojoProperty;

public class ClassBytecodeHelper {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(ClassBytecodeHelper.class);
    
    public static Set<String> getPropertiesForField(
            Object ownerInstance,
            String field
            ) throws IOException{
        Map<String,Set<String>> fieldsToProperties = getFieldNameMapping(
            ownerInstance.getClass()
            );
        
        if(fieldsToProperties.containsKey(field)){
            return fieldsToProperties.get(field);
        }
        
        return new HashSet<>(Arrays.asList(field));
    }
    
    public static Map<String,String> getPropertyToFieldMapping(
            Class<?> classToAnalyze
            ) throws IOException{
        Map<String,Set<String>> fieldToProperties = 
                getFieldNameMapping(classToAnalyze);
        
        Map<String,String> result = new HashMap<>();
        
        for(String fieldName:fieldToProperties.keySet()){
            Set<String> propertiesForField = fieldToProperties.get(fieldName);
            
            for(String property:propertiesForField){
                if(result.containsKey(property)){
                    throw new RuntimeException("unhandled case");
//                    logger.warn(
//                        String.format(
//                            "ambiguous mapping--the following properties " +
//                            "map to field %s: %s and %s\n" , 
//                            ));
                }
                
                result.put(
                    property, 
                    fieldName
                    );
                
                result.put(
                    "#has" + Character.toUpperCase(property.charAt(0)) + property.substring(1), 
                    fieldName
                    );
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param classToAnalyze
     *            a class containing fields
     * @return a mapping of field names to the names of properties associated
     *         with that field. The names of these properties are determined by
     *         the interfaces implemented by the class analyzed. The mapping is
     *         (somewhat naively) determined by looking at the field referenced
     *         by the implementation of the interface.
     * @throws IOException
     *             if the bytecode of the class to analyze cannot be loaded from
     *             the classpath
     */
    public static Map<String,Set<String>> getFieldNameMapping(
            Class<?> classToAnalyze
            ) throws IOException{
        Map<String,Set<String>> fieldToProperty = new HashMap<>();
        
        for(Class<?> c:collectPojoPropertyInterfaces(classToAnalyze)){
            
            for(Method m:c.getMethods()){
                MethodNode mn = 
                        getInterfaceImplementationModel(
                            classToAnalyze,
                            m.getName(),
                            m.getParameterTypes()
                            );
                
                FieldInsnNode fn = getOnlyFieldInsnNodeOrNull(mn);
                
                if(fn == null){
                    logger.warn(
                        String.format(
                            "unable to identify a single field-accessing " +
                            "instruction in method %s %s %s from interface %s" +
                            " (skipping it)",
                            classToAnalyze.getName(),
                            mn.name,
                            mn.desc,
                            c.getName()
                            )
                        );
                    continue;
                }
                
                final String actualFieldName = 
                        fn.name;
                
                final String propertyName = 
                        getFieldNameFromInterfaceImplementation(c,m);
                
                Set<String> propertyNames = fieldToProperty.get(actualFieldName);
                
                if(propertyNames == null){
                    propertyNames = new HashSet<>();
                    fieldToProperty.put(actualFieldName,propertyNames);
                }
                
                propertyNames.add(propertyName);
                
                logger.debug(
                    String.format(
                        "%s -> %s\n",
                        actualFieldName,
                        propertyName
                        )
                    );
            }
        }
        
        return fieldToProperty;
    }
    
    public static Map<String,Method> getPropertyToInterfaceMethodMapping(
            Class<?> classToAnalyze
            ) throws IOException{
        Map<String,Method> interfaceToField = new HashMap<>();
        
        for(Class<?> c:collectPojoPropertyInterfaces(classToAnalyze)){
            for(Method m:c.getMethods()){
                final String propertyName = 
                        getFieldNameFromInterfaceImplementation(c,m);
                
                interfaceToField.put(propertyName, m);
            }
        }
        
        return interfaceToField;
    }
    
    private static String getFieldNameFromInterfaceImplementation(
            Class<?> implementer, 
            Method interfaceMethod
            ){
        String interfaceName = interfaceMethod.getName();
        
        if(interfaceName.startsWith("is") && Character.isUpperCase(interfaceName.charAt(2))){
            interfaceName = Character.toLowerCase(interfaceName.charAt(2)) + interfaceName.substring(3);
        } else if((interfaceName.startsWith("get")||interfaceName.startsWith("has")) && Character.isUpperCase(interfaceName.charAt(3))){
            interfaceName = Character.toLowerCase(interfaceName.charAt(3)) + interfaceName.substring(4);
        } else {
            throw new RuntimeException(
                "unexpected interface method name: " + interfaceName
                );
        }
        
        return interfaceName;
    }
    
    private static Set<Class<?>> collectPojoPropertyInterfaces(Class<?> c){
        Set<Class<?>> interfaces = new HashSet<>();
        
        boolean stop = false;
        while(!stop){
            for(Class<?> i:c.getInterfaces()){
                interfaces.add(i);
            }
            
            c = c.getSuperclass();
            
            if(c == null){
                stop = true;
            }
        }
        
        Set<Class<?>> filtered = new HashSet<>();
        for(Class<?> interfaceClass:interfaces){
            PojoProperty p = interfaceClass.getAnnotation(PojoProperty.class);
            
            if(p != null){
                filtered.add(interfaceClass);
            }
        }
        
        return filtered;
    }
    
    private static ClassNode getBytecode(Class<?> c) throws IOException{
        InputStream stream = 
                c.getClassLoader().getResourceAsStream(
                    c.getName().replace(".", "/") + ".class"
                    );
        
        ClassReader r = new ClassReader(stream);
        
        ClassNode cn = new ClassNode();
        r.accept(cn, 0);
        
        return cn;
    }
    
    private static MethodNode getInterfaceImplementationModel(
            Class<?> baseClass,
            final String interfaceMethodName,
            final Class<?>[] args
            ) throws IOException {
        
        boolean stop = false;
        while(!stop){
            for(Method m:baseClass.getMethods()){
                boolean match = 
                        m.getName().equals(interfaceMethodName)
                        &&
                        m.getParameterTypes().length == args.length;
                
                if(match){
                    for(int i=0;i<m.getParameterTypes().length;i++){
                        final Class<?> expected = args[i];
                        final Class<?> actual = m.getParameterTypes()[i];
                        
                        if(!expected.isAssignableFrom(actual)){
                            match = false;
                        }
                        
                    }
                }
                
                if(match){
                    return getMethod(
                        baseClass,
                        m.getName(),
                        Type.getMethodDescriptor(m)
                        );
                }
            }
            
            baseClass = baseClass.getSuperclass();
            if(baseClass == null){
                stop = true;
            }
        }
        throw new RuntimeException("no impl found for " + interfaceMethodName);
    }
    
    private static MethodNode getMethod(
            final Class<?> baseClass,
            final String name, 
            final String desc
            ) throws IOException{
        
        boolean stop = false;
        Class<?> currentClass = baseClass;
        while(!stop){
            ClassNode cn = getBytecode(currentClass);
            
            for(MethodNode mn:cn.methods){
                if(mn.name.equals(name) && mn.desc.equals(desc)){
                    return mn;
                }
            }
            
            currentClass = currentClass.getSuperclass();
            
            if(currentClass == null){
                stop = true;
            }
        }
        
        throw new RuntimeException(
            "could not find " + name + " " + desc + " in " + baseClass.getName()
            );
        
    }
    
    private static FieldInsnNode getOnlyFieldInsnNodeOrNull(MethodNode mn){
        
        Set<FieldInsnNode> insns = new HashSet<>();
        
        for(AbstractInsnNode i:mn.instructions.toArray()){
            if(i.getType() == AbstractInsnNode.FIELD_INSN){
                FieldInsnNode f = (FieldInsnNode)i;
                
                insns.add(f);
            }
        }
        
        if(insns.size() != 1){
            return null;
        }
        
        return insns.iterator().next();
    }

}
