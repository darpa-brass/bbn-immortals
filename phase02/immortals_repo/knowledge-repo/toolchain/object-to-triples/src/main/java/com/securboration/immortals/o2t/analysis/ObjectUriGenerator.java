package com.securboration.immortals.o2t.analysis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.securboration.immortals.o2t.ObjectTranslatorImpl;

/**
 * Generates a random-looking UUID from an Object.  
 * Two identical objects will have the same UUID
 * 
 * @author jstaples
 *
 */
public class ObjectUriGenerator {
    
    private final Map<Object,String> cache = new HashMap<>();
    
    private boolean verbose = false;
    
    private static final boolean enforceArrayOrdering = true;
    
    /**
     * Generates a String that looks like a UUID for the given object. This
     * pseudo-UUID is consistent in that two identical objects will have the
     * same UUID.
     * 
     * @param o
     *            an object to generate a UUID for
     * @return a String that looks like a UUID (but isn't). Whereas a UUID uses
     *         only hex characters, the returned pseudo-UUID uses the base64
     *         alphabet
     */
    public synchronized String generateUuid(
            Object o
            ) throws IllegalArgumentException, IllegalAccessException {
        if(cache.containsKey(o)){
            return cache.get(o);
        }
        
        String uuid = generateUuidInternal(o);
        
        cache.put(o, uuid);
        
        return uuid;
    }
    
    private String generateUuidInternal(Object o) throws IllegalArgumentException, IllegalAccessException {
        ObjectTranslatorImpl translator = new ObjectTranslatorImpl();
        ObjectNode node = ObjectNode.build(translator, o);
        
        UriGeneratorVisitor visitor = new UriGeneratorVisitor();
        
        node.accept(visitor);
        
        return hash(visitor.objectStream.toByteArray());
    }
    
    private static String convert256bitHashToUuid(byte[] hash){
        final int targetLengthBytes = 192 / 8;
        
        if(hash.length < targetLengthBytes){
            throw new RuntimeException(
                "hash is too short to convert to a UUID, length = " 
                + 
                hash.length
                );
        }
        
        byte[] result = new byte[targetLengthBytes];
        System.arraycopy(hash, 0, result, 0, result.length);
        
        for(int i=result.length;i<hash.length;i++){
            int resultIndex = i%result.length;
            result[resultIndex] = (byte)(result[resultIndex] ^ hash[i]);
        }
        
        String base64Form = Base64.getEncoder().encodeToString(result);
        
        {
            //sanitize the base64 value since it will be used in a URI
            base64Form = base64Form.replace("+", "-");
            base64Form = base64Form.replace("/", "~");
            base64Form = base64Form.replace("=", "_");
        }
        
//        0-8      8-12 12-16 16-20 20-32
//examples of UUIDs:
//        01234567 8901 2345 6789 012345678901
//        782a4ae7-25d7-4187-95f4-e62661dd9ff7
//        b9364d7c-04e0-11e7-93ae-92361f002671
//example of a pseudo-UUID:
//        eqHuxjhR-MiVj-NTtl-vfpk-rCBM7HsWkJGE
        
        return String.format(
            "%s-%s-%s-%s-%s",
            base64Form.substring(0, 8),
            base64Form.substring(8, 12),
            base64Form.substring(12, 16),
            base64Form.substring(16, 20),
            base64Form.substring(20, 32)
            );
    }
    
    /**
     * Converts a hash > 192 bits in length into a String that looks like a
     * UUID.
     * 
     * @param data
     *            a cryptographic hash
     * @return a String that looks like a UUID (but isn't). Whereas a UUID uses
     *         only hex characters, the returned value uses the base64 alphabet
     */
    private static String hash(byte[] data){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-384");
            md.update(data);
            
            //now we have to make a 384-bit hash look like a UUID.  
            //This involves: 
            // 1) XOR-wrapping the hash to obtain a 192-bit value
            // 2) base64 encoding the 192-bit value to obtain a 256-bit value
            // 3) formatting these 32 bytes in accordance with the UUID style
            return convert256bitHashToUuid(md.digest());
//            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    private class UriGeneratorVisitor implements ObjectNodeVisitor{
            
        final ByteArrayOutputStream objectStream = 
                new ByteArrayOutputStream();
        
        private boolean isPrimitive(Object o){
            Class<?> c = o.getClass();
            
            if(c.isArray()){
                return false;
            }
            
            if(c.isPrimitive()){
                return true;
            }
            
            if(c.equals(Class.class)){return true;}
            if(c.equals(String.class)){return true;}
            
            if(c.equals(Boolean.class)){return true;}
            if(c.equals(Integer.class)){return true;}
            if(c.equals(Long.class)){return true;}
            if(c.equals(Float.class)){return true;}
            if(c.equals(Double.class)){return true;}
            if(c.equals(Byte.class)){return true;}
            if(c.equals(Character.class)){return true;}
            if(c.equals(Short.class)){return true;}
            
            return false;
        }

        @Override
        public void visitPrimitiveField(
                ObjectNode primitiveFieldOwner,
                ObjectNode primitiveFieldValue
                ) {
            if(primitiveFieldValue.getValue() == null){
                return;
            }
            
            if(!isPrimitive(primitiveFieldValue.getValue())){
//                throw new RuntimeException(
//                    primitiveFieldValue.getValue().getClass() 
//                    + 
//                    " is not a primitive type"
//                    );
                return;
            }
            
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
            
            final String message = 
                    printPath(primitiveFieldValue) 
                    + 
                    " = \"" 
                    + 
                    primitiveFieldValue.getValue() 
                    + 
                    "\", a [" 
                    + 
                    actualType 
                    + 
                    "] " 
                    + 
                    subclassString
                    ;
            
            if(verbose){
                System.out.println(message);
            }
            
            write(message);
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
                
                if(enforceArrayOrdering){
                    s.push("["+o.getArrayIndex()+"]");
                } else {
                    s.push("[array]");
                }
                
//                s.push("["+o.getArrayIndex()+"]");
                
                return printPath(o.getParent(),s);
            } else if (o.getFieldName() != null){
                //it's a simple field
                s.push("."+o.getFieldName());
                
                return printPath(o.getParent(),s);
            } else {
                throw new RuntimeException("unhandled corner case");
            }
        }
        
        private void write(String s){
            try {
                objectStream.write(s.getBytes("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void visitObjectField(
                ObjectNode objectFieldOwner,
                ObjectNode objectFieldValue
                ) {
            String message = String.format(printPath(objectFieldValue) + " is a " + objectFieldValue.getActualType());
            
            if(verbose){
                System.out.println(message);
            }
            
            write(message);
        }

        @Override
        public ArrayElementVisitor visitArrayField(
                ObjectNode arrayFieldOwner, 
                ObjectNode arrayFieldValue
                ) {
            return new ArrayElementVisitor(){

                @Override
                public void visitArrayElement(
                        ObjectNode array, 
                        int index,
                        ObjectNode elementAtIndex
                        ) {
                    // arrays are handled elsewhere
                }
                
            };
        }
    }

    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
}
