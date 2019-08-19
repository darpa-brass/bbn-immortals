package com.securboration.ftpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClient {
	
	public static void ftpUploadLocalFileToRemote(
			final File localFile,
			final String ftpHostAddress,
			final String ftpRemoteFileName,
			final String ftpUser, 
			final char[] ftpPassword
			) throws IOException {
		System.out.printf("about to FTP %s -> %s/%s\n", localFile.getCanonicalPath(), ftpHostAddress, ftpRemoteFileName);
		
		final FTPClient ftp = new FTPClient();
	    
	    ftp.configure(new FTPClientConfig());
	    
	    try {
	      int reply;
	      ftp.connect(ftpHostAddress);
	      System.out.println("Connected to " + ftpHostAddress + " " + ftp.getReplyString());
	      
	      reply = ftp.getReplyCode();
	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftp.disconnect();
	        System.err.println("FTP server refused connection.");
	        System.exit(1);
	      }
	      
	      final boolean wasSuccess = ftp.login(ftpUser, new String(ftpPassword));
	      if(!wasSuccess) {
	    	  throw new RuntimeException("unable to login as " + ftpUser + " with the provided password");
	      }
	      
	      try(final InputStream in = new FileInputStream(localFile);){
	    	  ftp.setFileType(FTP.BINARY_FILE_TYPE);
	    	  ftp.enterLocalPassiveMode();
	    	  if(!ftp.storeFile(ftpRemoteFileName, in)) {
	    		  throw new RuntimeException("unable to copy " + localFile.getAbsolutePath() + " to " + ftpRemoteFileName);
    		  }
	    	  System.out.println("Copy successful, response is " + ftp.getReplyString());
    	  }
	    } catch(IOException e) {
	      e.printStackTrace();
	    } finally {
	      if(ftp.isConnected()) {
	        try {
	          ftp.disconnect();
	        } catch(IOException ioe) {
	          throw ioe;
	        }
	      }
	    }
	}
	
	public static void main(String[] args) throws IOException {
		final File tmpDir = new File("./tmp/" + System.currentTimeMillis());
		final File testFile = new File(tmpDir,"test.dat");
		FileUtils.writeStringToFile(testFile, "test 1234 " + UUID.randomUUID().toString(), StandardCharsets.UTF_8);
		
		FtpClient.ftpUploadLocalFileToRemote(
				testFile, 
				"192.168.9.107", 
				"test.mdl", 
				"swri-tester", 
				"swri-t35t3r".toCharArray()
				);
	}

}
