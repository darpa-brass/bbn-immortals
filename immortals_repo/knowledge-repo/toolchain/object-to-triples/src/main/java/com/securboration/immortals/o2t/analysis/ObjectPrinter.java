package com.securboration.immortals.o2t.analysis;

import java.util.Stack;

public class ObjectPrinter {
    
    public static ObjectNodeVisitor getPrinterVisitor(){
        return new ObjectNodeVisitor(){

            @Override
            public void visitPrimitiveField(
                    ObjectNode primitiveFieldOwner,
                    ObjectNode primitiveFieldValue
                    ) {
                
                final String actualType = 
                        primitiveFieldValue.getActualType() == null ?
                                "?"
                                :
                                primitiveFieldValue.getActualType().getName();
                
                final String possibleType = 
                        primitiveFieldValue.getPossibleType().getName();
                
                String subclassString = 
                        "which is a subclass of [" + possibleType + "]";
                
                if(possibleType.equals(actualType)){
                    subclassString = "";
                }
                
                subclassString = "";
                
                System.out.println(
                        printPath(primitiveFieldValue) + " = \"" + primitiveFieldValue.getValue() + "\", a [" + actualType + "] " + subclassString);
            }
            
            private String printPath(ObjectNode o){
                return printPath(o,new Stack<>());
            }
            
            private String printPath(ObjectNode o,Stack<String> s){
                
                if(o.getParent() == null){
                    s.push("object_root");
                    
                    StringBuilder sb = new StringBuilder();
                    
                    while(!s.isEmpty()){
                        sb.append(s.pop());
                    }
                    
                    return sb.toString();
                }
                
                if(o.getArrayIndex() != null){
                    //it's an element of an array
                    s.push("["+o.getArrayIndex()+"]");
                    
                    return printPath(o.getParent(),s);
                } else if (o.getFieldName() != null){
                    //it's a simple field
                    s.push("."+o.getFieldName());
                    
                    return printPath(o.getParent(),s);
                } else {
                    throw new RuntimeException("unhandled corner case");
                }
            }

            @Override
            public void visitObjectField(ObjectNode objectFieldOwner,
                    ObjectNode objectFieldValue) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public ArrayElementVisitor visitArrayField(
                    ObjectNode arrayFieldOwner, ObjectNode arrayFieldValue) {
                // TODO Auto-generated method stub
                return new ArrayElementVisitor(){

                    @Override
                    public void visitArrayElement(ObjectNode array, int index,
                            ObjectNode elementAtIndex) {
                        // TODO Auto-generated method stub
                        
                    }
                    
                };
            }
        };
    }
    
}
