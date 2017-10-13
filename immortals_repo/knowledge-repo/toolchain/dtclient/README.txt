Securboration Data Transfer Client-

Things that need to be true:
	1) There is a Immortals Repo Service running at:
		http://localhost:8080/
	as this DTClient is currently hard coded to submit requests to http://localhost:8080/jarsubmit
	and there is a Fuseki server running that the Repo Service can talk to

	2) Jar files must have a pom file of the same name (except file extension) in the same directory.
		eg. for a C:\test-jar.jar there must be a C:\test-jar.pom in the same folder
	
How to use:
	Call the jar file with args[0] with the path to a Jar or a Directory.

What it does:
	If args[0] is a jar, it will transfer that jar to the Repo Service to be ingested
	If args[0] is a directory, the project will recursively traverse that folder and all children folders, finding jars and ingesting each one

	At the end, any jar files found that either resulted in Server Errors (jar ingestion failures by the Repo Service) or Pom Errors (missing or malformed Pom files) will be listed.