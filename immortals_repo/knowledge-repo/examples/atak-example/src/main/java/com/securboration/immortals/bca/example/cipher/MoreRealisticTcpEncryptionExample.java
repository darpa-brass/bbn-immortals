package com.securboration.immortals.bca.example.cipher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

/**
 * Code segment representative of the communication between ATAK/MARTI
 * 
 * There are three synthetic (inserted by IMMoRTALS) code regions:
 *  * configuration (sets up encryption parameters)
 *  * encrypt data sent to server from client
 *  * decrypt data arriving at server from client
 * 
 * @author jstaples
 *
 */
public class MoreRealisticTcpEncryptionExample {
    
    
    
    private static class Config {
        //ok to modify modify these
        
        //{ TODO: begin synthetic code region [CONFIGURE]
        private static final boolean CONFIG_ENCRYPT_SENT_TRAFFIC = true;
        private static final boolean CONFIG_DECRYPT_RCVD_TRAFFIC = true;
        
        private static final String CIPHER_ALG = "AES";//tested: AES or DES
        private static final int CIPHER_KEY_LENGTH_BYTES = 16;//tested: AES takes a 16-byte key, DES takes an 8-byte key
        private static final String CIPHER_CHAIN_MODE = "CBC";//tested: CBC, CTR
        private static final String CIPHER_PADDING_SCHEME = "PKCS5Padding";
        
        private static final String CIPHER_KEY = "a test password";
        private static final String CIPHER_IV = "an init vector";
        //} TODO: end synthetic code region [CONFIGURE]
        
        
        private static final int RECEIVE_CHUNK_SIZE = 7;//prime to make things interesting
        private static final double SENDS_PER_SECOND = 0.25;
        private static final double READS_PER_SECOND = 10;
        
        private static final byte[] ATAK_MESSAGE_SIGIL = "feedbabebeef".getBytes();
        
        //don't touch these (derived from the values above)
        private static final long SEND_DELAY = (long)(1000d / SENDS_PER_SECOND);
        private static final long READ_DELAY = (long)(1000d / READS_PER_SECOND);
    }
    
    public static void main(String[] args) throws IOException{
//        MockTcpSocketConnection socket = new MockTcpSocketConnection(
//            Config.MAX_TCP_QUEUE_LENGTH
//            );
        
        final int portNumber = Server.startServer();
        Client.startClient(portNumber);
        
        
        log("done with main thread");
    }
    
    private static class Server{
        
        private static int startServer() throws IOException{
            ServerSocket serverSocket = new ServerSocket(0);//random port
            
            Thread t = new Thread(()->{
                try {
                    Socket connected = serverSocket.accept();
                    serverAction(connected);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        serverSocket.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            
            t.setName("server");
            t.setDaemon(false);
            t.start();
            
            return serverSocket.getLocalPort();
        }
        
        private static void serverAction(Socket s) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException{
            log("started server on port %s/%d:%s/%d", s.getLocalAddress(),s.getLocalPort(), s.getInetAddress(),s.getPort());
            
            InputStream readFromHere = s.getInputStream();
            
            {//TODO: begin synthetic code region [CIPHER:DECRYPT]
                if(Config.CONFIG_DECRYPT_RCVD_TRAFFIC){
                    CipherImpl c = new CipherImpl(
                        Config.CIPHER_ALG,
                        Config.CIPHER_KEY_LENGTH_BYTES,
                        Config.CIPHER_CHAIN_MODE,
                        Config.CIPHER_PADDING_SCHEME,
                        Config.CIPHER_KEY,
                        Config.CIPHER_IV
                        );
                    
                    readFromHere = c.acquire(readFromHere);
                }
            }//TODO: end synthetic code region [CIPHER:DECRYPT]
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while(true){
                serverTask(buffer,readFromHere);
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
                InputStream s
                ) {
            final byte[] chunk = new byte[Config.RECEIVE_CHUNK_SIZE];
            
            int received;
            try {
                received = s.read(chunk);
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
                    byte[] messageBytes = new byte[sigilStartIndex];
                    System.arraycopy(dump, 0, messageBytes, 0, sigilStartIndex);
                    
                    byte[] subsequentNonSigilBytes = new byte[dump.length - messageBytes.length - Config.ATAK_MESSAGE_SIGIL.length];
                    System.arraycopy(dump, sigilStartIndex+Config.ATAK_MESSAGE_SIGIL.length, subsequentNonSigilBytes, 0, subsequentNonSigilBytes.length);
                    
                    b.reset();
                    for(byte data:subsequentNonSigilBytes){
                        b.write(data);
                    }
                    
                    receiveMessage(messageBytes);
                }
            }
            
        }
        
        private static void receiveMessage(byte[] message){
            log("received a %dB message:  %s",message.length,new String(message));
        }
        
    }
    
    private static class Client{
        private static void startClient(final int port) throws UnknownHostException, IOException{
            Socket s = new Socket("localhost", port);
            Thread t = new Thread(()->{
                try {
                    Client.clientAction(s);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            t.setName("client");
            t.setDaemon(false);
            t.start();
        }
        
        private static void clientAction(Socket s) throws IOException{
            log("started client on port %s/%d:%s/%d", s.getLocalAddress(),s.getLocalPort(), s.getInetAddress(),s.getPort());
            
            OutputStream writeToHere = s.getOutputStream();
            
            {//TODO: begin synthetic code region [CIPHER:ENCRYPT]
                if(Config.CONFIG_ENCRYPT_SENT_TRAFFIC){
                    try {
                        CipherImpl c = new CipherImpl(
                            Config.CIPHER_ALG,
                            Config.CIPHER_KEY_LENGTH_BYTES,
                            Config.CIPHER_CHAIN_MODE,
                            Config.CIPHER_PADDING_SCHEME,
                            Config.CIPHER_KEY,
                            Config.CIPHER_IV
                            );
                        
                        writeToHere = c.acquire(writeToHere);
                    } catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
            }//TODO: end synthetic code region [CIPHER:ENCRYPT]
            
            int count = 0;
            while(true){
                count++;
                clientTask("message-" + count,writeToHere);
                delay(Config.SEND_DELAY);
            }
        }
        
        private static void clientTask(
                String message, 
                OutputStream writeToHere
                ){
            byte[] messageBytes = message.getBytes();
            byte[] messageBytesPlusSigil = new byte[messageBytes.length + Config.ATAK_MESSAGE_SIGIL.length];
            
            System.arraycopy(messageBytes, 0, messageBytesPlusSigil, 0, messageBytes.length);
            System.arraycopy(Config.ATAK_MESSAGE_SIGIL, 0, messageBytesPlusSigil, messageBytes.length, Config.ATAK_MESSAGE_SIGIL.length);
            
            try {
                writeToHere.write(messageBytesPlusSigil);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            log(
                "transmitted %dB message: %s", 
                messageBytes.length,
                new String(messageBytes)
                );
        }
        
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
    
    

}
