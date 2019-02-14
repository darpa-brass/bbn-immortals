package com.securboration.miniatakapp.dfus.cipher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Blockifier {
    
    public static String hex(byte[] data){
        return hex(data,0,data.length);
    }
    
    public static String hash(byte[] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(data);

            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    //TODO: why is the cipherOutputStream writing -1 (FF)???
    
    public static String hex(byte[] data, int offset, int length){
        if(length < 0){
            return "-1 was returned so no data";
        }
        
        StringBuilder sb = new StringBuilder();
        
        byte[] slice = new byte[length];
        sb.append(data.length).append(" bytes with hash ").append(hash(data));
        
        if(length < 1024){
            sb.append(": ");
            
            sb.append("[");
            if(offset > 0){
                sb.append(" ... ");
            }
            for(int i=0;i<length;i++){
                sb.append(String.format("%02x ", data[i + offset]));
                slice[i] = data[i+offset];
            }
            sb.append("]");
            
            sb.append(" = \"" + new String(slice).replace("\r\n", "RN").replace("\n", "N").replace("\r", "R").replace((char)0, '|') + "\"");
        }
        
        return sb.toString();
    }
    
    public static String print(
            final String tag, 
            final byte[] data
            ){
        return print(tag,data,0,data.length);
    }
    
    public static String print(
            final String tag, 
            final byte[] d,
            final int dataOffset,
            final int dataLength
            ){
        byte[] data = new byte[dataLength];
        for(int i=0;i<dataLength;i++){
            data[i]=d[i+dataOffset];
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(tag).append(" in thread \"" + Thread.currentThread().getName() + "\"\n");
        sb.append("\tbinary (" + dataLength + "B): ");
        for(byte b:data){
            sb.append(String.format("%02x ", b));
        }
        sb.append("\n");
        
        sb.append("\ttext (no newlines): ");
        sb.append(new String(data).replace("\n", "NEWLINE"));
        sb.append("\n");
        
        return sb.toString();
    }
    
    
    
    public static byte[] deBlockify(final byte[] input){
        
        int length = 0;
        length |= (input[0] & 0x000000FF) << 24;
        length |= (input[1] & 0x000000FF) << 16;
        length |= (input[2] & 0x000000FF) << 8;
        length |= (input[3] & 0x000000FF);
        
        byte[] data = new byte[length];
        for(int i=0;i<length;i++){
            data[i] = input[i+4];
        }
        
        return data;
    }
    
    public static byte[] blockify(
            final byte[] input, 
            final int offset, 
            final int length, 
            final int blockSizeBytes
            ){
        final byte[] data = new byte[length];
        for(int i=0;i<length;i++){
            data[i] = input[i+offset];
        }
        
        return blockify(data,blockSizeBytes);
    }
    
    public static int getPaddingBytes(
            final int blockSizeBytes, 
            final int dataLength
            ){
        return (blockSizeBytes - ((dataLength + 4) % blockSizeBytes)) % blockSizeBytes;
    }
    
    public static byte[] blockify(
            final byte[] input,
            final int blockSizeBytes
            ){
        final int paddingBytes = getPaddingBytes(blockSizeBytes,input.length);
        final int numBlocks = (paddingBytes + input.length + 4) / blockSizeBytes;
        
//        System.out.printf("input length = %dB\n", input.length);
//        System.out.printf("padding = %dB\n", paddingBytes);
//        System.out.printf("# %d-B blocks = %d\n", blockSizeBytes, numBlocks);
//        System.out.printf(
//            "'rithmetic: %d = %d\n", 
//            numBlocks * blockSizeBytes, 
//            paddingBytes + input.length + 4
//            );
        
        byte[] output = new byte[numBlocks*blockSizeBytes];
        
//        System.out.println(print(output));
        
        output[0] = (byte)((input.length & 0xFF000000) >> 24);
        output[1] = (byte)((input.length & 0x00FF0000) >> 16);
        output[2] = (byte)((input.length & 0x0000FF00) >> 8 );
        output[3] = (byte)((input.length & 0x000000FF) >> 0 );
        
        for(int i=0;i<input.length;i++){
            output[i+4] = input[i];
        }
        
//        System.out.println(print(input));
//        System.out.println(print(output));
//        System.out.println();
        
        return output;
    }

}
