package com.securboration.immortals.bca.example.cipher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.crypto.NoSuchPaddingException;

public class TcpEncryptionExample {
    
    
    
    private static class Config {
        //ok to modify modify these
        private static final int MAX_TCP_QUEUE_LENGTH = 487;//prime to make things interesting
        private static final int RECEIVE_CHUNK_SIZE = 7;//prime to make things interesting
        
        private static final double SENDS_PER_SECOND = 10;
        private static final double READS_PER_SECOND = 0.5;
        
        private static final String CIPHER_ALG = "AES";//tested: AES or DES
        private static final int CIPHER_KEY_LENGTH_BYTES = 16;//tested: AES takes a 16-byte key, DES takes an 8-byte key
        private static final String CIPHER_CHAIN_MODE = "CBC";//tested: CBC, CTR
        private static final String CIPHER_PADDING_SCHEME = "PKCS5Padding";
        
        private static final String CIPHER_KEY = "a test password";
        private static final String CIPHER_IV = "an init vector";
        
        private static final byte[] ATAK_MESSAGE_SIGIL = "feedbabebeef".getBytes();
        
        //don't touch these (derived from the values above)
        private static final long SEND_DELAY = (long)(1000d / SENDS_PER_SECOND);
        private static final long READ_DELAY = (long)(1000d / READS_PER_SECOND);
    }
    
    public static void main(String[] args){
        MockTcpSocketConnection socket = new MockTcpSocketConnection(
            Config.MAX_TCP_QUEUE_LENGTH
            );
        
        Client.startClient(socket);
        Server.startServer(socket);
        
        log("done with main thread");
    }
    
    private static class Server{
        
        private static void startServer(MockTcpSocketConnection c){
            Thread t = new Thread(()->{
                serverAction(c);
            });
            
            t.setName("server");
            t.setDaemon(false);
            t.start();
        }
        
        private static void serverAction(MockTcpSocketConnection c){
            log("started server");
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while(true){
                serverTask(buffer,c);
                delay(Config.READ_DELAY);
            }
        }
        
        private static int getFirstSigilStartIndex(byte[] data){
              //meh, horribly inefficient
              for(int candidateStartIndex=0;candidateStartIndex<data.length;candidateStartIndex++){
                  
                  boolean match = true;
                  for(int testIndex=0;testIndex<Config.ATAK_MESSAGE_SIGIL.length;testIndex++){
                      if(testIndex+candidateStartIndex >= data.length){
                          match = false;
                          break;
                      }
                      
                      final int dataIndex = candidateStartIndex + testIndex;
                      
                      if(data[dataIndex] != Config.ATAK_MESSAGE_SIGIL[testIndex]){
                          match = false;
                          break;
                      }
                  }
                  
                  if(match){
                      return candidateStartIndex;
                  }
              }
              
              return -1;
          }
        
        private static void serverTask(
                ByteArrayOutputStream b,
                MockTcpSocketConnection c
                ) {
            final byte[] chunk = new byte[Config.RECEIVE_CHUNK_SIZE];
            
            int received;
            try {
                received = c.readFromHere.read(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            if(received == 0){
                return;
            }
            
            {
                byte[] raw = new byte[received];
                System.arraycopy(chunk, 0, raw, 0, received);
                log("received chunk of %dB: \"%s\"", received, new String(raw));
            }
            
            b.write(chunk,0,received);
            
            {
                final byte[] dump = b.toByteArray();
                
                final int sigilStartIndex = getFirstSigilStartIndex(dump);
                if(sigilStartIndex >= 0){
//                    log("found sigil @ %d of %d", sigilStartIndex, dump.length);
                    
                    byte[] messageBytes = new byte[sigilStartIndex];
                    System.arraycopy(dump, 0, messageBytes, 0, sigilStartIndex);
                    
                    byte[] subsequentNonSigilBytes = new byte[dump.length - messageBytes.length - Config.ATAK_MESSAGE_SIGIL.length];
                    System.arraycopy(dump, sigilStartIndex+Config.ATAK_MESSAGE_SIGIL.length, subsequentNonSigilBytes, 0, subsequentNonSigilBytes.length);
                    
                    b.reset();
                    for(byte data:subsequentNonSigilBytes){
                        b.write(data);
                    }
                    
//                    log(
//                        "found message of length %dB with remainder %dB", 
//                        messageBytes.length, 
//                        subsequentNonSigilBytes.length
//                        );
                    
                    receiveMessage(messageBytes);
                }
            }
            
        }
        
        private static void receiveMessage(byte[] message){
            log("received a %dB message:  %s",message.length,new String(message));
        }
        
    }
    
    private static class Client{
        private static void startClient(MockTcpSocketConnection c){
            Thread t = new Thread(()->{
                Client.clientAction(c);
            });
            
            t.setName("client");
            t.setDaemon(false);
            t.start();
        }
        
        private static void clientAction(MockTcpSocketConnection c){
            log("started client");
            
            int count = 0;
            while(true){
                count++;
                clientTask("message-" + count,c);
                delay(Config.SEND_DELAY);
            }
        }
        
        private static void clientTask(
                String message, 
                MockTcpSocketConnection c
                ){
            final byte[] chunk = message.getBytes();
            try {
                c.writeToHere.write(chunk);
                c.writeToHere.write(Config.ATAK_MESSAGE_SIGIL);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            log(
                "transmitted %dB message: %s", 
                chunk.length,
                new String(chunk)
                );
        }
        
//        private static void clientTask(
//                String message, 
//                MockTcpSocketConnection c
//                ){
//            final byte[] chunk = new byte[Config.MESSAGE_SIZE];
//            final byte[] messageBytes = message.getBytes();
//            System.arraycopy(messageBytes, 0, chunk, 0, messageBytes.length);
//            
//            for(int i=messageBytes.length;i<chunk.length;i++){
//                chunk[i] = ' ';
//            }
//            
//            try {
//                c.send(chunk);
//                c.send(Config.SIGIL);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            
//            log(
//                "transmitted %dB message: %s", 
//                chunk.length,
//                new String(chunk)
//                );
//        }
        
    }
    
    private static void log(String format, Object...args){
        String formatted = String.format(format, args);
        System.out.printf(
            "[%s]: %s\n", 
            Thread.currentThread().getName(),
            formatted
            );
    }
    
    private static void delay(final long delay){
        
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    
    
    private static class MockTcpSocketConnection {
        
        private final ArrayBlockingQueue<Byte> buffer;
        
        private InputStream readFromHere;
        private OutputStream writeToHere;
        
        public MockTcpSocketConnection(
                int maxBuffSize
                ) {
            super();
            this.buffer = new ArrayBlockingQueue<Byte>(maxBuffSize);
            
            this.readFromHere = new InputStream(){

                @Override
                public int read() throws IOException {
                    try {
                        return buffer.take();
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                }
                
            };
            
            this.writeToHere = new OutputStream(){
                @Override
                public void write(int b) throws IOException {
                    try {
                        buffer.put((byte)b);
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                }
            };
            
            {//TODO BEGIN transformation generated by IMMoRTALS
                try {
                    final CipherImpl cipher = new CipherImpl(
                        Config.CIPHER_ALG,
                        Config.CIPHER_KEY_LENGTH_BYTES,
                        Config.CIPHER_CHAIN_MODE,
                        Config.CIPHER_PADDING_SCHEME,
                        Config.CIPHER_KEY,
                        Config.CIPHER_IV
                        );
                    this.readFromHere = cipher.acquire(this.readFromHere);
                    this.writeToHere = cipher.acquire(this.writeToHere);
                    
                } catch (
                        InvalidKeyException | InvalidAlgorithmParameterException | 
                        NoSuchAlgorithmException | NoSuchPaddingException 
                        e
                        ) {
                    throw new RuntimeException(e);
                }
            }//TODO END transformation generated by IMMoRTALS
        }
        
    }
    
    

}
