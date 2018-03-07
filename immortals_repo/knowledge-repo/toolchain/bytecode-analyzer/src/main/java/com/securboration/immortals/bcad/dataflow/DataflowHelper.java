package com.securboration.immortals.bcad.dataflow;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bca.tools.MethodPrinter;
import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class DataflowHelper {
    
    public static class LocalVariableSpec{
        private final int localIndex;
        private final String variableName;
        private final String typeDesc;
        private final Class<?> semanticType;
        private final AbstractInsnNode scopeBegin;
        private final AbstractInsnNode scopeEnd;
        private final boolean isArgument;
        
        public LocalVariableSpec(int localIndex, String variableName,
                String typeDesc, Class<?> semanticType, AbstractInsnNode scopeBegin,
                AbstractInsnNode scopeEnd, boolean isArgument) {
            super();
            this.localIndex = localIndex;
            this.variableName = variableName;
            this.typeDesc = typeDesc;
            this.semanticType = semanticType;
            this.scopeBegin = scopeBegin;
            this.scopeEnd = scopeEnd;
            this.isArgument = isArgument;
        }
        
//        @Override
//        public String toString(){
//            return String.format(
//                "\"%s\" @%d type=%s", 
//                variableName,
//                localIndex,
//                semanticType == null ? "?" : semanticType.getSimpleName()
//                );
//        }
        
        public boolean isImplicitArg(){
            return isArgument && variableName.equals("this");
        }
        
        public String print(MethodNode mn){
            return String.format(
                "local name=\"%s\" index=%d semanticType=%s scope={%s -> %s} sort=%s javaType=%s ", 
                variableName,
                localIndex,
                semanticType == null ? "?" : semanticType.getSimpleName(),
                MethodPrinter.print(mn,scopeBegin),
                MethodPrinter.print(mn,scopeEnd),
                isArgument ? isImplicitArg() ? "[implicit method arg]" : "[explicit method arg]" : "[local var]",
                typeDesc 
                );
        }

        
        public int getLocalIndex() {
            return localIndex;
        }

        
        public String getVariableName() {
            return variableName;
        }

        
        public String getTypeDesc() {
            return typeDesc;
        }

        
        public Class<?> getSemanticType() {
            return semanticType;
        }

        
        public AbstractInsnNode getScopeBegin() {
            return scopeBegin;
        }

        
        public AbstractInsnNode getScopeEnd() {
            return scopeEnd;
        }


        
        public boolean isArgument() {
            return isArgument;
        }
    }
    
    public static class ReturnValueSpec{
        private String javaTypeDesc;
        private Class<?> semanticType;
        public ReturnValueSpec(String javaTypeDesc, Class<?> semanticType) {
            super();
            this.javaTypeDesc = javaTypeDesc;
            this.semanticType = semanticType;
        }
        public ReturnValueSpec() {
            super();
        }
        
        public String getJavaTypeDesc() {
            return javaTypeDesc;
        }
        
        public void setJavaTypeDesc(String javaTypeDesc) {
            this.javaTypeDesc = javaTypeDesc;
        }
        
        public Class<?> getSemanticType() {
            return semanticType;
        }
        
        public void setSemanticType(Class<?> semanticType) {
            this.semanticType = semanticType;
        }
    }

    public static class ParameterSpec{
        private int parameterIndex;
        private String parameterName;
        private String javaTypeDesc;
        private Class<?> semanticType;
        
        public ParameterSpec(
                int parameterIndex, 
                String parameterName,
                String javaTypeDesc, 
                Class<?> semanticType
                ) {
            this.parameterIndex = parameterIndex;
            this.parameterName = parameterName;
            this.javaTypeDesc = javaTypeDesc;
            this.semanticType = semanticType;
        }

        public ParameterSpec() {}

        
        public int getParameterIndex() {
            return parameterIndex;
        }

        
        public void setParameterIndex(int parameterIndex) {
            this.parameterIndex = parameterIndex;
        }

        
        public String getParameterName() {
            return parameterName;
        }

        
        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        
        public String getJavaTypeDesc() {
            return javaTypeDesc;
        }

        
        public void setJavaTypeDesc(String javaTypeDesc) {
            this.javaTypeDesc = javaTypeDesc;
        }

        
        public Class<?> getSemanticType() {
            return semanticType;
        }

        
        public void setSemanticType(Class<?> semanticType) {
            this.semanticType = semanticType;
        }
        
        
    }
    
    public static ReturnValueSpec getReturnValue(Method m){
        ReturnValueSpec rv = new ReturnValueSpec();
        
        rv.setJavaTypeDesc(Type.getDescriptor(m.getReturnType()));
        rv.setSemanticType(
            getOneOrNull(
                getPojoTypeAnnotationsExtending(
                    DataType.class,
                    m.getDeclaredAnnotations()
                    )
                )
            );
        
        return rv;
    }
    
    public static ParameterSpec[] getArgumentTypes(Method m){
        
        ParameterSpec[] specs = new ParameterSpec[m.getParameters().length];
        
        int argIndex = 0;
        for(Parameter p:m.getParameters()){
            final String parameterName = p.getName();
            final int parameterIndex = argIndex;
            final Class<?> parameterType = p.getType();
            final Class<?> datatype = getOneOrNull(
                getPojoTypeAnnotationsExtending(
                    DataType.class,
                    p.getDeclaredAnnotations()
                    )
                );
            
            ParameterSpec spec = new ParameterSpec(
                parameterIndex,
                parameterName,
                Type.getDescriptor(parameterType),
                datatype
                );
            
            specs[parameterIndex]=spec;
            
            argIndex++;
        }
        
        return specs;
    }
    
    private static <T> T getOneOrNull(Collection<T> values){
        if(values.size() != 1){
            return null;
        }
        
        return values.iterator().next();
    }
    
    private static Set<Class<?>> getPojoTypeAnnotationsExtending(
            Class<?> base, 
            Annotation[] annotations
            ){
        
        Set<Class<?>> matches = new HashSet<>();
        if(annotations == null){
            System.out.println("no annots");
            return matches;
        }
        
        for(Annotation a:annotations){
            try {
                Field f = a.annotationType().getField("BACKING_POJO");
                
                if(!f.getType().equals(Class.class)){
                    continue;
                }
                
                Class<?> c = (Class<?>)f.get(null);
                
                if(base.isAssignableFrom(c)){
                    matches.add(c);
                }
            } catch (NoSuchFieldException e) {
                //do nothing
            } catch (SecurityException|IllegalArgumentException|IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return matches;
    }

}
