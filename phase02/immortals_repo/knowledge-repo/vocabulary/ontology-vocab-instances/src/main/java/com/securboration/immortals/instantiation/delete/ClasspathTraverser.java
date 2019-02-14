package com.securboration.immortals.instantiation.delete;
//package com.securboration.immortals.instantiation;
//
//import org.objectweb.asm.tree.ClassNode;
//
//
//public interface ClasspathTraverser {
//    
//    public static class ClassInformation{
//        
//        private final String originUrl;
//        private final byte[] bytecode;
//        private final ClassNode classModel;
//        public ClassInformation(String originUrl, byte[] bytecode,
//                ClassNode classModel) {
//            super();
//            this.originUrl = originUrl;
//            this.bytecode = bytecode;
//            this.classModel = classModel;
//        }
//        public String getOriginUrl() {
//            return originUrl;
//        }
//        public byte[] getBytecode() {
//            return bytecode;
//        }
//        public ClassNode getClassModel() {
//            return classModel;
//        }
//    }
//    
//    public void visitClass(ClassInformation c);
//    
//    public ClassInformation requestClass(final String internalName);
//
//}
