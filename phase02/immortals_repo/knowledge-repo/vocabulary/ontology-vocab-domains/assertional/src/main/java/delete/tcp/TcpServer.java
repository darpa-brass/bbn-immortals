package delete.tcp;

import java.io.IOException;

public class TcpServer {
    
    public static void main(String[] args) throws IOException, InterruptedException{
        TcpClientServer.main(new String[]{
                "server",
                "[a message to bounce]",
                "24601"
                });
    }

}
