package com.securboration.immortals.instantiation.annotationparser.traversal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.securboration.immortals.instantiation.annotationparser.bytecode.AnnotationHelper;

public class ObjectBuilderVisitor {
    
    private static class Helper{
        
        private static Object sanitize(Object value){
            if(value instanceof Type){
                Type t = (Type) value;
                return AnnotationHelper.getClass(t.getDescriptor());
            }
            
            return value;
        }
        
        private static void setArrayField(
                Object instance,
                String fieldName,
                List<?> value
                ){
            Field f = getField(instance,fieldName);
            f.setAccessible(true);
            
            final Class<?> elementType = f.getType().getComponentType();
            
            Object array = Array.newInstance(elementType, value.size());
            
            for(int i=0;i<value.size();i++){
                
                Object element = value.get(i);
                element = sanitize(element);
                
                Object afterCast = elementType.cast(element);
                
                System.out.printf("@ index %d\n", i);
                System.out.printf("  array type: %s\n", elementType);
                System.out.printf("  element type: %s\n", afterCast.getClass());
                
                Array.set(array, i, afterCast);
            }
            
            setField(instance,fieldName,array);
            
        }
        
        private static void setField(
                Object instance,
                String fieldName,
                Object value
                ){
            
            value = sanitize(value);
            
            Field f = getField(instance,fieldName);
            
            f.setAccessible(true);
            try {
                f.set(instance, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(
                    "unable to set field " + fieldName + 
                    " in class " + instance.getClass().getName() + 
                    " to value " + value,
                    e
                    );
            }
            
        }
        
        private static Field getField(
                final Object template, 
                final String fieldName
                ){
            boolean stop = false;
            Class<?> current = template.getClass();
            List<Class<?>> tried = new ArrayList<>();
            tried.add(current);
            
            while(!stop){
                for(Field f:current.getDeclaredFields()){
                    if(f.getName().equals(fieldName)){
                        return f;
                    }
                }
                
                current = current.getSuperclass();
                
                tried.add(current);
                
                if(current == null){
                    stop = true;
                }
            }
            
            throw new RuntimeException(
                "couldn't find field " + fieldName + 
                " in classes " + tried.toString()
                );
        }
        
        private static Object getEnumConstant(
                final String desc, 
                final String value
                ){
            try{
                Class<?> c = Class.forName(Type.getType(desc).getClassName());
                
                try {
                    Field f = c.getField(value);
                    
                    return f.get(null);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(
                        "unable to access enum field " + value + 
                        " from " + c.getName()
                        );
                }
                
            } catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }
    }
    
    private static class ObjectBuilderContext{
        
        private final Object template;
        private final ObjectBuilderContext parent;
        
        private ObjectBuilderContext(
                final Object template
                ){
            this(null,template);
        }
        
        private ObjectBuilderContext(
                final ObjectBuilderContext parent,
                final Object template
                ){
            this.parent = parent;
            this.template = template;
        }
        
        public ObjectBuilderContext getFieldChild(String name) {
            return new ObjectBuilderContext(
                this,
                getTemplate(name)
                );
        }
        
        public ObjectBuilderContext getArrayChild(String name) {
            return new ObjectBuilderContext(
                this,
                new ArrayList<>()
                );
        }
        
//        private static String getTemplateClassName(ObjectBuilderContext c){
//            if(c == null){
//                return "[parent is null]";
//            }
//            
//            return c.template == null ? "[template is null]" : c.template.getClass().getName();
//        }
//        private static String getPathToRoot(ObjectBuilderContext c){
//            
//            StringBuilder sb = new StringBuilder();
//            
//            boolean stop = false;
//            while(!stop){
//                
//                sb.append(
//                    String.format(
//                        "%s has parent %s\n", 
//                        getTemplateClassName(c),
//                        getTemplateClassName(c.parent)
//                        )
//                    );
//                
//                c = c.parent;
//                
//                if(c == null){
//                    stop = true;
//                }
//            }
//            
//            return sb.toString();
//        }
        
        private Object getTemplate(
                final String name
                ) {
            
//            System.out.printf(
//                "acquiring template for %s.%s\n", 
//                template == null ? "[template is null]":template.getClass().getName(),
//                name
//                );
            
            if(template == null){
                throw new RuntimeException("template is null");
            }
            
            for(Field f:template.getClass().getDeclaredFields()){
                if(f.getName().equals(name)){
                    
                    try{
                        return f.getType().newInstance();
                    } catch(InstantiationException | IllegalAccessException e){
                        throw new RuntimeException(
                            "no default constructor for " + f.getType().getName());
                    }
                }
            }
            
            throw new RuntimeException(
                "could not find field with name " + name + 
                " in class " + template.getClass().getName()
                );
        }
        
        private void visitPrimitive(
                final String name, 
                final Object value
                ){
            
            Helper.setField(
                template, 
                name, 
                value
                );
            
        }
        
        private void visitEnum(
                final String name, 
                final String desc,
                final String value
                ){
            
            Helper.setField(
                template, 
                name, 
                Helper.getEnumConstant(desc, value)
                );
            
        }
        
        private void visitArray(final String name, final List<?> array){
            
            Helper.setArrayField(
                parent.template, 
                name, 
                array
                );
            
        }
        
    }
    
    private static interface Finisher{
        public void finish(ObjectBuilderContext c);
    }
    
    private static class ArrayVisitor extends Visitor{
        
        final List<Object> arrayValues = new ArrayList<>();
        
        private ArrayVisitor(
                final String pathSoFar,
                final String name,
                final ObjectBuilderContext parent
                ) {
            super(
                pathSoFar,
                parent.getArrayChild(name)
                );

            super.finisher = 
                    (c)->{
                        c.visitArray(name, arrayValues);
                        System.out.println("done setting array @" + pathSoFar + ".");
                    };
        }
        
        @Override
        public void visit(String name, Object value) {
            //visit a primitive value
            
            System.out.printf(
                "found a primitive element:\n\t%s[%d] = %s (a %s)\n", 
                pathSoFar, 
                arrayValues.size(), 
                value,
                value.getClass().getName()
                );
            
            arrayValues.add(value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            //visit an enumeration value
            
            System.out.printf(
                "found an enum element:\n\t%s[%d] = %s (a %s)\n", 
                pathSoFar, 
                arrayValues.size(), 
                value,
                desc
                );
            
            arrayValues.add(
                Helper.getEnumConstant(desc, value)
                );
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            //visit a nested annotation
            
            System.out.printf(
                "found an annotation element:\n\t%s[%d] is an @%s\n", 
                pathSoFar, 
                arrayValues.size(), 
                desc
                );
            
            final Object template;
            
            try {
                Class<?> annotationClass = 
                        AnnotationHelper.getClass(desc);
                
                Class<?> backingClass = 
                        AnnotationHelper.getBackingPojo(annotationClass);
                
                template = backingClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            
            Visitor v = new Visitor(
                pathSoFar + "." + name,
                new ObjectBuilderContext(builder,template)
                );
            
            v.finisher = (c)->arrayValues.add(template);
            
            return v;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            //visit an array
            
            System.out.printf(
                "found an array element:\n\t%s[%d] is an array\n", 
                pathSoFar, 
                arrayValues.size()
                );
            
            throw new RuntimeException(
                "unexpected case: nested array detected"
                );
        }

        @Override
        public void visitEnd() {
            //encounter the end of the annotation
            super.visitEnd();
        }
    }
    
    public static class Visitor extends AnnotationVisitor{
        
        final protected ObjectBuilderContext builder;
        final String pathSoFar;
        
        Finisher finisher = (c)->{System.out.println("done.");};

        public Visitor(final Object o){
            this(
                "$",
                new ObjectBuilderContext(o)
                );
            
            System.out.printf("c=%s, t=%s\n", builder, builder.template);
        }
        
        private Visitor(
                final String pathSoFar,
                final ObjectBuilderContext builder
                ) {
            super(Opcodes.ASM5);
            
            this.pathSoFar = pathSoFar;
            this.builder = builder;
        }

        @Override
        public void visit(String name, Object value) {
            //visit a primitive value
            
            System.out.printf(
                "found a primitive field:\n\t%s.%s = %s (a %s)\n", 
                pathSoFar, 
                name, 
                value,
                value.getClass().getName()
                );
            
            builder.visitPrimitive(
                name, 
                value
                );
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            //visit an enumeration value
            
            System.out.printf(
                "found an enum value:\n\t%s.%s = %s (a %s)\n", 
                pathSoFar, 
                name, 
                value,
                desc
                );
            
            builder.visitEnum(name, desc, value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            //visit a nested annotation
            
            System.out.printf(
                "found a nested annotation value:\n\t%s.%s is an @%s\n", 
                pathSoFar, 
                name, 
                desc
                );
            
            Visitor v = new Visitor(
                pathSoFar + "." + name,
                builder.getFieldChild(name)
                );
            
            v.finisher = (c)->{
                c.visitPrimitive(name,v.builder.template);
                System.out.println(
                    "done setting non-primitive field @" + pathSoFar + ".");
            };
            
            return v;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            //visit an array
            
            System.out.printf(
                "found an array value:\n\t%s.%s\n", 
                pathSoFar, 
                name
                );
            
            return new ArrayVisitor(
                pathSoFar + "." + name,
                name,
                builder
                );
        }

        @Override
        public void visitEnd() {
            if(finisher != null){
                finisher.finish(builder);
            }
        }
        
    }

}
