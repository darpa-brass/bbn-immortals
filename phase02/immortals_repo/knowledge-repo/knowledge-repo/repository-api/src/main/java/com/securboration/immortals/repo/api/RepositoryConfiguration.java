package com.securboration.immortals.repo.api;

/**
 * Client-side configuration for talking to a possibly remote Fuseki repository
 * 
 * @author jstaples
 *
 */
public class RepositoryConfiguration {

    /**
     * The base URL for the fuseki instance. Default is http://localhost:3030/ds
     */
    private String repositoryBaseUrl = "http://localhost:3030/ds";

    public String getRepositoryBaseUrl() {
        return repositoryBaseUrl;
    }

    public void setRepositoryBaseUrl(String repositoryBaseUrl) {
        this.repositoryBaseUrl = repositoryBaseUrl;
    }

}
