package delete.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpClientServer {
    
    public static void main(String[] args) throws IOException, InterruptedException{
        
        if(args.length < 1){
            throw new RuntimeException("expected the following args:\n" +
                    "  \"server\" or \"client\"\n" +
                    "  [additional args]\n" +
                    "");
        }
        
        final boolean isServer = args[0].equals("server");
        
        if(isServer){
            if(args.length != 2){
                throw new RuntimeException("expected the following args:\n" +
                        "  \"server\" or \"client\"\n" +
                        "  a port # on which a server will be started\n" +
                        "");
            }
            final int localPort = Integer.parseInt(args[1]);
            
            System.out.printf(
                "running a server\n\tport=%d\n\n",
                localPort
                );
            
            try(ServerSocket socket = new ServerSocket(localPort)){
                
                Socket connection = socket.accept();
                
                serverLoop(connection);
            }
        } else {
            if(args.length != 3){
                throw new RuntimeException("expected the following args:\n" +
                        "  \"server\" or \"client\"\n" +
                        "  a host in server mode\n" +
                        "  the server's port\n" +
                        "");
            }
            final String remoteAddress = args[1];
            final int remotePort = Integer.parseInt(args[2]);
            
            System.out.printf(
                "running a client\n\tremote host=%s\n\tremote port=%d\n\n",
                remoteAddress,remotePort
                );
            
            try(Socket clientSocket = new Socket(remoteAddress, remotePort)){
                clientLoop(clientSocket);
            }
        }
    }
    
    private static void delay(long millis) throws InterruptedException{
        Thread.sleep(millis);
    }
    
    private static void serverLoop(
            Socket socket
            ) throws IOException, InterruptedException{
        
        socket.getOutputStream().write(0xFF);
        
        while(true){
            int received = socket.getInputStream().read();
            if(received == -1){
                System.out.println("stream has been closed remotely");
                return;
            }
            
            System.out.println("  connection is up @ " + System.currentTimeMillis());
            
            socket.getOutputStream().write(0xFF);
            
            delay(500);
        }
    }
    
    private static void clientLoop(
            Socket socket
            ) throws IOException, InterruptedException{
        
        while(true){
            int received = socket.getInputStream().read();
            if(received == -1){
                System.out.println("stream has been closed remotely");
                return;
            }
            
            System.out.println("  connection is up @ " + System.currentTimeMillis());
            
            socket.getOutputStream().write(0xFF);
        }
    }
    
    
    
    
//    private static void serverLoop(
//            String initialMessage,
//            Socket socket
//            ) throws IOException, InterruptedException{
//        
//        BufferedReader in = 
//                new BufferedReader(
//                    new InputStreamReader(
//                        socket.getInputStream()
//                        )
//                    );
//        
//        PrintStream out = 
//                new PrintStream(socket.getOutputStream());
//        
//        out.println(initialMessage);
//        out.flush();
//        
//        while(true){
//            String lineReceived = in.readLine();
//            
//            System.out.println("received " + lineReceived);
//            
//            out.print(lineReceived);
//            out.flush();
//            
//            delay(500);
//        }
//    }

}
