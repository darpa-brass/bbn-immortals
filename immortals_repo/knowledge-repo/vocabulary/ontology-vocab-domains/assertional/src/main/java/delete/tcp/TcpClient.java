package delete.tcp;

import java.io.IOException;

public class TcpClient {
    
    public static void main(String[] args) throws IOException, InterruptedException{
        TcpClientServer.main(new String[]{
                "client",
                "localhost",
                "24601"
                });
    }

}
