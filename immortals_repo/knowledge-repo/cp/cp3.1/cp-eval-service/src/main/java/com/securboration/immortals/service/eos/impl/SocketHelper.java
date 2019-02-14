//package com.securboration.immortals.service.eos.impl;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Queue;
//import java.util.UUID;
//
//public class SocketHelper {
//    
//    private final Queue<ServerSocket> map = new ArrayDeque<>();
//    
//    public SocketHelper(final int numSocketsToAllocate){
//        for(int i=0;i<numSocketsToAllocate;i++){
//            map.pt
//        }
//    }
//    
//    public String allocate() throws IOException{
//        final String key = UUID.randomUUID().toString();
//        
//        map.put(key, new ServerSocket(0));
//        
//        return key;
//    }
//    
//    public int get(final String key){
//        ServerSocket s = map.get(key);
//        
//        return s.getLocalPort();
//    }
//    
//    private static int randomFreePort(){
//        try(ServerSocket s = new ServerSocket(0)){
//            return s.getLocalPort();
//        } catch(IOException e){
//            throw new RuntimeException(e);
//        }
//    }
//    
//    public static void main(String[] args){
//        
//        SocketHelper s = new SocketHelper()
//        
//        for(int i=0;i<1000;i++){
//            System.out.println(randomFreePort());
//        }
//    }
//
//}
