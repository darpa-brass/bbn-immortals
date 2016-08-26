To run the service:
	java -Dserver.port=9999 -jar target/immortals-repository-service-boot.war
	
To test the service:
	Visit http://localhost:9999/immortalsRepositoryService/ping in your browser
	(an epoch timestamp should be returned)

For API details, see com.securboration.immortals.service.api.ImmortalsRepositoryService
