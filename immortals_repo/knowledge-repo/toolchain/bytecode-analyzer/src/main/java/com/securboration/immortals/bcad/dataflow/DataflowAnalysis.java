//package com.securboration.immortals.bcad.dataflow;
//
//import java.util.Map;
//
//import org.objectweb.asm.Type;
//import org.objectweb.asm.tree.AbstractInsnNode;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.MethodNode;
//
//public class DataflowAnalysis {
//    
//    public static Map<AbstractInsnNode,DataflowAnalysis> build(
//            ClassNode cn, 
//            MethodNode mn
//            ){
//        
//        return null;//TODO
//    }
//    
//    public static class DataflowAnalysisNode{
//        private final AbstractInsnNode instruction;
//        private final Action[] actions;
//        
//        public DataflowAnalysisNode(
//                AbstractInsnNode instruction,
//                Action...actions
//                ) {
//            super();
//            this.instruction = instruction;
//            this.actions = actions;
//        }
//        
//        
//    }
//    
//    public static class Action{
//        
//        private final String humanReadable;
//        private final ReadOrWrite type;
//        private final Location location;
//        
//        public Action(
//                String humanReadable, 
//                ReadOrWrite type,
//                Location location
//                ) {
//            super();
//            this.humanReadable = humanReadable;
//            this.type = type;
//            this.location = location;
//        }
//        
//        @Override
//        public String toString(){
//            return String.format(
//                "%s %s %s", 
//                type.name(), 
//                location.toString(),
//                humanReadable == null ? "" : humanReadable
//                );
//        }
//    }
//    
//    public static enum ReadOrWrite{
//        READ,
//        WRITE
//        ;
//    }
//    
//    public abstract static class Location{
//        
//        private final String humanReadable;
//        
//        @Override
//        public abstract String toString();
//
//        public Location(String humanReadable) {
//            this.humanReadable = humanReadable;
//        }
//
//        
//        public String getHumanReadable() {
//            return humanReadable;
//        }
//        
//    }
//    
//    public static class StackItem extends Location{
//        
//        private final Type type;
//
//        public StackItem(
//                String humanReadable,
//                Type type
//                ) {
//            super(humanReadable);
//            this.type = type;
//        }
//
//        @Override
//        public String toString() {
//            return String.format(
//                "stack item %s %s", 
//                type.getDescriptor(),
//                super.getHumanReadable()
//                );
//        }
//        
//    }
//    
//    public static class LocalVar extends Location{
//        
//        private final int varIndex;
//        private final Type type;
//        
//        public LocalVar(
//                String humanReadable,
//                Type type
//                ) {
//            super(humanReadable);
//            this.type = type;
//        }
//
//        @Override
//        public String toString() {
//            return String.format(
//                "stack item %s %s", 
//                type.getDescriptor(),
//                super.getHumanReadable()
//                );
//        }
//        
//    }
//    
//    public static class Field extends Location{
//        
//        private final String ownerClassName;
//        private final String fieldTypeDesc;
//        private final String fieldName;
//        private final boolean isStaticField;
//        
//        public Field(
//                String humanReadable, 
//                String ownerClassName,
//                String fieldTypeDesc, 
//                String fieldName, 
//                boolean isStaticField
//                ) {
//            super(humanReadable);
//            this.ownerClassName = ownerClassName;
//            this.fieldTypeDesc = fieldTypeDesc;
//            this.fieldName = fieldName;
//            this.isStaticField = isStaticField;
//        }
//
//        @Override
//        public String toString() {
//            return String.format(
//                "%sfield %s %s %s %s", 
//                isStaticField?"static ":"",
//                ownerClassName, 
//                fieldName, 
//                fieldTypeDesc,
//                super.getHumanReadable()
//                );
//        }
//        
//    }
//    
//    public static class MethodCall extends Location{
//        
//    }
//    
//    
//
//}
