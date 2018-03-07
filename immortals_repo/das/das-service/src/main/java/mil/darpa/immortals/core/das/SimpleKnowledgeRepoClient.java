package mil.darpa.immortals.core.das;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import mil.darpa.immortals.config.ImmortalsConfig;

public class SimpleKnowledgeRepoClient {

	//private static final String urlBase = "http://localhost:9999/immortalsRepositoryService/";
	private static final String urlBase = "http://localhost:9999/krs/";
	
    public String ingest() throws IOException {
        
		Path ingestionPath = ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();

        HttpURLConnection connection = 
                (HttpURLConnection)new URL(urlBase).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/text");
        
        try (
            OutputStream output = connection.getOutputStream();
        ) {
            output.write(ingestionPath.toAbsolutePath().toString().getBytes("UTF-8"));
            output.flush();
        }

        try{
            final int responseCode = connection.getResponseCode();
            
            if(responseCode != 200){
                throw new RuntimeException(
                    "response was " + responseCode + 
                    " but expected 200, with message " + connection.getResponseMessage()
                    );
            }
        } catch(IOException e){
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            try(InputStream errStream = connection.getErrorStream()){
                if(errStream != null){
                    IOUtils.copy(errStream, err);
                    
                    throw new IOException(new String(err.toByteArray()));
                }
            }
            
            throw e;
        }
        
        ByteArrayOutputStream readFromServer = new ByteArrayOutputStream();
        try(InputStream input = connection.getInputStream();){
            IOUtils.copy(input, readFromServer);
        }
        
        return new String(readFromServer.toByteArray());
    }
}
