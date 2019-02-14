package com.securboration.miniatakapp.dfus.cipher.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.compress.utils.IOUtils;

import com.securboration.miniatakapp.dfus.cipher.Blockifier;
import com.securboration.miniatakapp.dfus.cipher.CipherImplApi;
import com.securboration.miniatakapp.dfus.cipher.CipherImplBogo;
import com.securboration.miniatakapp.dfus.cipher.CipherImplBouncyCrypto;
import com.securboration.miniatakapp.dfus.cipher.CipherImplJavaxCrypto;
import com.securboration.miniatakapp.dfus.cipher.CipherImplNoop;
import com.securboration.miniatakapp.dfus.cipher.DataPaddingInputStream;
import com.securboration.miniatakapp.dfus.cipher.DataPaddingOutputStream;
import com.securboration.miniatakapp.dfus.cipher.PrintDelegateInputStream;
import com.securboration.miniatakapp.dfus.cipher.PrintDelegateOutputStream;

/**
 * A simple test harness for testing the integrity of proxy-based IO stream 
 * transformations
 * 
 * @author jstaples
 *
 */
public class TestHarness {
    
    private static final int KEY_LENGTH_BYTES = 16;//must match the cipher
    private static final int PADDED_BLOCK_SIZE_BYTES = 8192;//messages to be transmitted are broken into chunks this size
    
    private static final boolean youLikeWorkingSoftware = true;//false to disable padding which typically leaves trailing bytes dangling in the output stream
    private static final boolean youLikeReading = false;//true to print a verbose log of the various transformations applied to messages
    private static final boolean youLikeWaiting = false;//true to run a huge number of tests
    
    private static final boolean testUsingByteArrayStreams = true;//true iff you want to include tests using ByteArrayIOStream classes
    private static final boolean testUsingClientServer = true;//true iff you want to include client/server Socket-based tests
    
    private static final CipherAcquirer[] ciphersToTest = new CipherAcquirer[]{//the cipher impls to test
            Ciphers.NOOP,
            Ciphers.BOGO,
            Ciphers.JAVAX,
            Ciphers.BOUNCY
            };
    
    private static final String[] messagesToSend = {//the messages to test
            "",//intentionally empty
            "message-0 ",
            "message-1 this is a message long enough to require the use of two blocks.  just needs to be a tiny bit larger",
//            "message-2 ",
//            "message-3 ",
//            "message-4 ",
            "message-5 ;) done. \u2202 \u03C0",
//            "message-6 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0message-5 ;) done. \\u2202 \\u03C0",
//            "message-7 " + randomString(new Random(0L),117),
//            "message-8 " + randomString(new Random(0L),118),
            "message-9 " + randomString(new Random(0L),119),
            "message-10 " + randomString(new Random(0L),4096),
            "message-11 " + randomString(new Random(1L),1024*16),
//            "message-12 " + randomString(new Random(2L),1024*1024*4),
    };
    
    private static String randomString(Random rng,int numBytes){
        byte[] data = new byte[numBytes];
        rng.nextBytes(data);
        
        return (new String(data)).replace("\r\n","").replace("\n", "").replace("\r", "");
    }
    
    public interface CipherAcquirer{
        CipherImplApi acquire();
    }
    
    private static class Ciphers{
        private static final CipherAcquirer NOOP = new CipherAcquirer(){
            @Override
            public CipherImplApi acquire(){
                return new CipherImplNoop();
            }
        };
        
        private static final CipherAcquirer BOGO = new CipherAcquirer(){
            @Override
            public CipherImplApi acquire(){
                return new CipherImplBogo();
            }
        };
        
        private static final CipherAcquirer JAVAX = new CipherAcquirer(){
            @Override
            public CipherImplApi acquire(){
                CipherImplJavaxCrypto cipher = new CipherImplJavaxCrypto();
                
                cipher.configure(
                    "AES",KEY_LENGTH_BYTES,
                    "CBC","PKCS5Padding",
                    "a key ;)",
                    "an init vector ;)"
                    );
                
                return cipher;
            }
        };
        
        private static final CipherAcquirer BOUNCY = new CipherAcquirer(){
            @Override
            public CipherImplApi acquire(){
                CipherImplBouncyCrypto cipher = new CipherImplBouncyCrypto();
                
                cipher.configure(
                    "AES",KEY_LENGTH_BYTES,
                    "CBC","PKCS5Padding",
                    "a key ;)",
                    "an init vector ;)"
                    );
                
                return cipher;
            }
        };
    }
    
    public static void main(String[] args) throws Exception{
        
        int testCounter = 0;
        
        int iterations = 1;
        
        if(youLikeWaiting){
            iterations = 32;
        }
        
        for(int i=0;i<iterations;i++){
            for(CipherAcquirer cipher:ciphersToTest){
                CipherImplApi impl = cipher.acquire();
                
                byte[][] data = new byte[messagesToSend.length][];
                int counter = 0;
                for(String message:messagesToSend){
                    data[counter] = message.getBytes();
                    counter++;
                    
                    if(testUsingByteArrayStreams){
                        testCounter++;
                        System.out.printf(
                            ">>>> testing impl %s with message \"%s\"\n", 
                            impl.getClass().getSimpleName(), 
                            message.substring(0,Math.min(message.length(), 1024))
                            );
                        
                        testWithByteArrayStreams(cipher,message.getBytes());
                        
                        System.out.println();
                    }
                }
                
                if(testUsingClientServer){
                    testCounter++;
                    System.out.printf(
                        ">>>> testing impl %s using %s\n", 
                        impl.getClass().getSimpleName(), 
                        Blockifier.hex(flatten(data))
                        );
                    testWithSockets(
                        cipher,
                        data
                        );
                }
            }
        }
                
        System.out.printf("All %d tests PASS\n",testCounter);
    }
    
    private static void nuke(String message){
        
        try{
            Thread.sleep(1000L);
        } catch(InterruptedException e){
            //do nothing
        }
        
        new Exception(message).printStackTrace();
        
        System.exit(-1);//TODO: obviously remove this if used in unit test
    }
    
    private static OutputStream wrap(
            String tag,
            CipherImplApi cipher,
            OutputStream o
            ){
        if(!youLikeWorkingSoftware){
            return cipher.acquire(o);
        }
        
        if(youLikeReading){
            PrintDelegateOutputStream printEncrypted = 
                    new PrintDelegateOutputStream(tag + "-encrypted",o);
            
            OutputStream encryptingStream = 
                    cipher.acquire(printEncrypted);
            
            PrintDelegateOutputStream printPadded = 
                    new PrintDelegateOutputStream(tag + "-padded",encryptingStream);
            
            
            OutputStream paddingStream = 
                    new DataPaddingOutputStream(
                        PADDED_BLOCK_SIZE_BYTES,
                        printPadded
                        );
            
            PrintDelegateOutputStream printPlaintext = 
                    new PrintDelegateOutputStream(tag + "-plaintext",paddingStream);
            
            return printPlaintext;
        }
        
        OutputStream encryptingStream = 
                cipher.acquire(o);
        
        OutputStream paddingStream = 
                new DataPaddingOutputStream(
                    PADDED_BLOCK_SIZE_BYTES,
                    encryptingStream
                    );
        
        return paddingStream;
    }
    
    private static InputStream wrap(
            String tag,
            CipherImplApi cipher,
            InputStream i
            ){
        if(!youLikeWorkingSoftware){
            return cipher.acquire(i);
        }
        
        if(youLikeReading){
            
            PrintDelegateInputStream readEncrypted = 
                    new PrintDelegateInputStream(tag + "-encrypted",i);
            InputStream cipherStream = 
                    cipher.acquire(readEncrypted);
            
            PrintDelegateInputStream readPadded = 
                    new PrintDelegateInputStream(tag + "-padded",cipherStream);
            InputStream paddedStream = 
                    new DataPaddingInputStream(PADDED_BLOCK_SIZE_BYTES,readPadded);
            
            PrintDelegateInputStream readPlaintext = 
                    new PrintDelegateInputStream(tag + "-plaintext",paddedStream);
            
            return readPlaintext;
            
        }
        
        InputStream cipherStream = cipher.acquire(i);
        InputStream paddedStream = new DataPaddingInputStream(
            PADDED_BLOCK_SIZE_BYTES,
            cipherStream
            );
        
        return paddedStream;
    }
    
    private static byte[] toArray(List<Byte> list){
        byte[] data = new byte[list.size()];
        
        for(int i=0;i<list.size();i++){
            data[i] = list.get(i);
        }
        
        return data;
    }
    
    private static void testWithByteArrayStreams(
          final CipherAcquirer cipher,
          final byte[] transmitThis
          ) throws Exception {
        
        final ByteArrayOutputStream output = 
                new ByteArrayOutputStream();
        
        final OutputStream outputWrapped = 
                wrap("output",cipher.acquire(),output);
        outputWrapped.write(transmitThis);
        outputWrapped.close();
        
        final long startTime = System.currentTimeMillis();
        
        final byte[] receivedData = output.toByteArray();
        
        final CountDownLatch senderClosesStream = new CountDownLatch(1);
        final CountDownLatch receiverHadASlightChance = new CountDownLatch(1);
        final CountDownLatch receiverHadAChance = new CountDownLatch(1);
        final CountDownLatch receiverShouldBeDone = new CountDownLatch(1);
        
        final AtomicBoolean receiverFinishedNormally = new AtomicBoolean(false);
        
        {
            Thread t = new Thread(){
                @Override
                public void run(){
                    try{
                        {//wait a tiny bit, capture receiver's buffer
                            Thread.sleep(250L);
                            receiverHadASlightChance.countDown();
                        }
                        
                        {//wait a bit, then capture the receiver's buffer
                            Thread.sleep(2000L);
                            receiverHadAChance.countDown();
                        }
                        
                        {//wait a bit, then close the output stream
                            Thread.sleep(2000L);
                            outputWrapped.close();
                            senderClosesStream.countDown();
                        }
                        
                        {//wait a bit more, then recapture the receiver's buffer
                            Thread.sleep(2000L);
                            receiverShouldBeDone.countDown();
                        }
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
            };
            t.setDaemon(true);
            t.start();
        }
        
        final List<Byte> recoveredData = new ArrayList<>();
        {
            Thread t = new Thread(){
                @Override
                public void run(){
                    try{
                        {//verify that the cipher even works
                            final InputStream in = 
                                    wrap(
                                        "test-in",
                                        cipher.acquire(),
                                        new ByteArrayInputStream(receivedData)
                                        );
                            
                            ByteArrayOutputStream recovered = new ByteArrayOutputStream();
                            
                            IOUtils.copy(in, recovered);
                            
                            final byte[] decrypted = recovered.toByteArray();
                            
                            final String hex1 = Blockifier.hex(transmitThis);
                            final String hex2 = Blockifier.hex(decrypted);
                            
                            if(!hex1.equals(hex2)){
                                System.out.println(hex1);
                                System.out.println(hex2);
                                nuke("FAIL: recovered decrypted result does not match original plaintext");
                            }
                        }
                        
                        final ByteArrayInputStream input = 
                                new ByteArrayInputStream(receivedData);
                        
                        final InputStream inputWrapped = 
                                wrap("input",cipher.acquire(),input);
                        
                        while(true){
                            
                            final byte[] tempBuffer = new byte[711];//intentionally weird buffer size
                            final int bytesRead = inputWrapped.read(tempBuffer);
                            
                            if(bytesRead == -1){
                                break;
                            }
                            
                            List<Byte> temp = new ArrayList<>();
                            
                            for(int i=0;i<bytesRead;i++){
                                temp.add(tempBuffer[i]);
                            }
                            
                            synchronized(recoveredData){
                                recoveredData.addAll(temp);
                            }
                            
                            if(bytesRead == 0){
                                Thread.sleep(100L);
                            }
                        }
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                    
                    receiverFinishedNormally.set(true);
                }
            };
            t.setDaemon(true);
            t.start();
        }
        
        //wait a tiny bit
        receiverHadASlightChance.await();
        
        final String recoveredRightAway;
        synchronized(recoveredData){
            recoveredRightAway = Blockifier.hex(toArray(recoveredData));
        }
        final long recoveredRightAwayTime = System.currentTimeMillis();
        
        //wait for the receiver to get a look
        receiverHadAChance.await();
        
        final String recoveredBeforeStreamClose;
        synchronized(recoveredData){
            recoveredBeforeStreamClose = Blockifier.hex(toArray(recoveredData));
        }
        final long recoveredBeforeStreamCloseTime = System.currentTimeMillis();
        
        //wait for the sender to close the stream
        senderClosesStream.await();
        
        final String recoveredAfterStreamClose;
        synchronized(recoveredData){
            recoveredAfterStreamClose = Blockifier.hex(toArray(recoveredData));
        }
        final long recoveredAfterStreamCloseTime = System.currentTimeMillis();
        
        final String transmitted = Blockifier.hex(transmitThis);
        
        System.out.printf("\t@ T+%5dms original:      %s\n", 0, transmitted);
        System.out.printf("\t@ T+%5dms recovered (0): %s\n", recoveredRightAwayTime - startTime, recoveredRightAway);
        System.out.printf("\t@ T+%5dms recovered (1): %s\n", recoveredBeforeStreamCloseTime - startTime, recoveredBeforeStreamClose);
        System.out.printf("\t@ T+%5dms recovered (2): %s\n", recoveredAfterStreamCloseTime - startTime, recoveredAfterStreamClose);
        System.out.printf("\treceiver finished normally ? %s\n", receiverFinishedNormally.get());
        
        if(!transmitted.equals(recoveredBeforeStreamClose)){
            nuke("original bytes != recovered bytes before stream closed (this is unexpected, you may need to tweak various latencies in this test case)");
        }
        
        if(!transmitted.equals(recoveredAfterStreamClose)){
            nuke("original bytes != recovered bytes after stream closed (this likely means the cipher isn't working properly)");
        }
        
        if(!receiverFinishedNormally.get()){
            nuke("receiver thread did not finish normally");
        }
        
        System.out.printf("\tPASS\n");
    }
    
    
    private static void print(String format, Object...args){
        System.out.printf(
            "\t[%s]  %s\n", 
            Thread.currentThread().getName(), 
            String.format(format, args).replace("\n", "\n\t")
            );
    }
    
    private static byte[] flatten(byte[][] a){
        int length = 0;
        for(byte[] b:a){
            length += b.length;
        }
        
        byte[] f = new byte[length];
        
        int index = 0;
        for(byte[] b:a){
            for(int i=0;i<b.length;i++){
                f[index] = b[i];
                index++;
            }
        }
        
        return f;
    }
    
    private static final class StringContainer{
        private String s;
        private long t;
    }
    
    private static void testWithSockets(
            final CipherAcquirer cipher,
            final byte[][] messages
            ) throws Exception {
        
        final byte[] expected = flatten(messages);
        
        final ServerSocket receiverSocket = new ServerSocket(0);
        
        print(
            "server has been allocated port %d", 
            receiverSocket.getLocalPort()
            );
          
          final long startTime = System.currentTimeMillis();
          
          final CountDownLatch clientClosesStream = new CountDownLatch(1);
          final CountDownLatch clientClosedStream = new CountDownLatch(1);
          final CountDownLatch orchestratorDone = new CountDownLatch(1);
          
          final AtomicBoolean clientFinishedNormally = new AtomicBoolean(false);
          final AtomicBoolean serverFinishedNormally = new AtomicBoolean(false);
          
          final List<Byte> serverReceived = new ArrayList<>();
          
          
          final StringContainer recoveredBeforeStreamClose = new StringContainer();
          final StringContainer recoveredAfterStreamClose = new StringContainer();
          final StringContainer recoveredWellAfterStreamClose = new StringContainer();
          
          
          {//orchestrator thread
              Thread t = new Thread(){
                  @Override
                  public void run(){
                      try{
                          {//wait a tiny bit, capture receiver's buffer
                              Thread.sleep(1000L);
                              
                              synchronized(serverReceived){
                                  recoveredBeforeStreamClose.s = Blockifier.hex(toArray(serverReceived));
                                  recoveredBeforeStreamClose.t = System.currentTimeMillis();
                              }
                          }
                          
                          {//shut down the client's stream
                              Thread.sleep(2000L);
                              clientClosesStream.countDown();
                              clientClosedStream.await();
                          }
                          
                          {//capture the buffer just after shutdown
                              synchronized(serverReceived){
                                  recoveredAfterStreamClose.s = Blockifier.hex(toArray(serverReceived));
                                  recoveredAfterStreamClose.t = System.currentTimeMillis();
                              }
                          }
                          
                          {//wait a bit more and snapshot the buffer again
                              Thread.sleep(1000L);
                              
                              synchronized(serverReceived){
                                  recoveredWellAfterStreamClose.s = Blockifier.hex(toArray(serverReceived));
                                  recoveredWellAfterStreamClose.t = System.currentTimeMillis();
                              }
                          }
                          
                          orchestratorDone.countDown();
                      }catch(Exception e){
                          throw new RuntimeException(e);
                      }
                  }
              };
              t.setName("orchestrator");
              t.setDaemon(true);
              t.start();
          }//end orchestrator thread
          
          {//server thread listens to messages
              Thread t = new Thread(){
                  @Override
                  public void run(){
                      try{
                          final Socket s = receiverSocket.accept();
                          
                          final InputStream in = wrap(
                              "server",
                              cipher.acquire(),
                              s.getInputStream()
                              );
                          
                          final byte[] tempBuffer = new byte[1024];
                          while(true){
                              final int bytesRead = in.read(tempBuffer);
                              
                              if(bytesRead <= -1){
                                  break;
                              } else if(bytesRead == 0){
                                  Thread.sleep(200L);
                              } else {
                                  List<Byte> buffer = new ArrayList<>();
                                  for(int i=0;i<bytesRead;i++){
                                      buffer.add(tempBuffer[i]);
                                  }
                                  
                                  synchronized(serverReceived){
                                      serverReceived.addAll(buffer);
                                  }
                              }
                          }
                          
                      }catch(Exception e){
                          throw new RuntimeException(e);
                      }
                      
                      serverFinishedNormally.set(true);
                  }
              };
              t.setName("server");
              t.setDaemon(true);
              t.start();
          }//end server thread
          

          {//client thread transmits to server
              Thread t = new Thread(){
                  @Override
                  public void run(){
                      try{
                          final Socket s =  new Socket(
                              "localhost",
                              receiverSocket.getLocalPort()
                              );
                          
                          final OutputStream o = wrap(
                              "client",
                              cipher.acquire(),
                              s.getOutputStream()
                              );
                          
                          for(byte[] message:messages){
                              o.write(message);
                              Thread.sleep(40L);
                          }
                          
                          clientClosesStream.await();
                          o.close();
                          clientClosedStream.countDown();
                          
                      }catch(Exception e){
                          throw new RuntimeException(e);
                      }
                      
                      clientFinishedNormally.set(true);
                  }
              };
              t.setName("client");
              t.setDaemon(true);
              t.start();
          }//end client thread
          
          //wait for the orchestrator
          orchestratorDone.await();
          
          //close the server socket
          receiverSocket.close();
          
          final String transmitted = Blockifier.hex(expected);
          
          final String beforeClose;final long beforeCloseTime;
          final String justAfterClose;final long justAfterCloseTime;
          final String wellAfterClose;final long wellAfterCloseTime;
          
          synchronized(serverReceived){
              beforeClose = recoveredBeforeStreamClose.s;
              justAfterClose = recoveredAfterStreamClose.s;
              wellAfterClose = recoveredWellAfterStreamClose.s;
              
              beforeCloseTime = recoveredBeforeStreamClose.t;
              justAfterCloseTime = recoveredAfterStreamClose.t;
              wellAfterCloseTime = recoveredWellAfterStreamClose.t;
          }
          
          System.out.printf("\t@ T+%5dms original          : %s\n", 0, transmitted);
          System.out.printf("\t@ T+%5dms before close()    : %s\n", beforeCloseTime - startTime, beforeClose);
          System.out.printf("\t@ T+%5dms just after close(): %s\n", justAfterCloseTime - startTime, justAfterClose);
          System.out.printf("\t@ T+%5dms well after close(): %s\n", wellAfterCloseTime - startTime, wellAfterClose);
          
          if(!transmitted.equals(recoveredAfterStreamClose.s)){
              nuke("original bytes != recovered bytes after stream closed (this likely means the cipher isn't working properly)");
          }
          
          if(!transmitted.equals(recoveredWellAfterStreamClose.s)){
              nuke("original bytes != recovered bytes well after stream closed (this likely means the cipher isn't working properly)");
          }
          
          if(!clientFinishedNormally.get()){
              nuke("client thread did not finish normally");
          }
          
          if(!serverFinishedNormally.get()){
              nuke("server thread did not finish normally");
          }
          
          System.out.printf("\tPASS\n");
      }
    

}
