package com.securboration.ftpc;

import java.io.File;
import java.io.IOException;


public class FtpClientUploader {
	
	private String ftpHost;

	private String ftpRemoteFileName;
	
	private String ftpUser;
	
	private char[] ftpPassword;
	
	public FtpClientUploader(String ftpHost, String ftpRemoteFileName, String ftpUser, char[] ftpPassword) {
		super();
		this.ftpHost = ftpHost;
		this.ftpRemoteFileName = ftpRemoteFileName;
		this.ftpUser = ftpUser;
		this.ftpPassword = ftpPassword;
	}
	
	public void upload(final File input) throws IOException {
		FtpClient.ftpUploadLocalFileToRemote(input, ftpHost, ftpRemoteFileName, ftpUser, ftpPassword);
	}

}
