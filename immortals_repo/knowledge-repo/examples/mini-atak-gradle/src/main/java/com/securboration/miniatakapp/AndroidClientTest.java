package com.securboration.miniatakapp;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.InputStream;
import mil.darpa.immortals.annotation.dsl.ontology.types.CotMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class AndroidClientTest {
    
    public void enterClient() {
        
    }
    
    public void sendToServer(@BinaryData byte[] message, @mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.OutputStream OutputStream outputStream) {
        
    }
    
    public void receiveFromClient(@BinaryData byte[] message, @InputStream java.io.InputStream inputStream) {
        
    }
    
    public void exitServer(@CotMessage String message) {
        
    }
    
    public static void main(String[] args) throws Exception {

        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("172.25.240.1",8088));
        OutputStream outputStream = channel.socket().getOutputStream();
        OutputStream out;
        try {
            CipherImpl c = new CipherImpl(
                    "AES",
                    16,
                    "CBC",
                    "PKCS5Padding",
                    "a test password",
                    "an init vector"
            );
            out =  c.acquire(outputStream);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
        ByteBuffer currentWriteBuffer = ByteBuffer.allocate(20);
        currentWriteBuffer.put("Hello there!".getBytes());
        out.write(currentWriteBuffer.array());
        //out.flush();
        out.close();
        channel.close();
    }
}
