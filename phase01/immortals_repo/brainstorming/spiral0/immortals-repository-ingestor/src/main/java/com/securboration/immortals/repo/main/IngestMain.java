package com.securboration.immortals.repo.main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.securboration.immortals.repo.api.RepositoryConfiguration;
import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.repo.model.build.JarIngestor;

/**
 * Main entrypoint for ingesting the models contained in a JAR into a fuseki
 * graph
 * 
 * @author jstaples
 *
 */
public class IngestMain {

    /**
     * Main entrypoint
     * 
     * @param args
     *            args[0] is the name of a graph to create, args[1] is the path
     *            to a jar file to ingest into the named graph, args[2...] are
     *            valid suffixes to use for ingesting models
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        final String graphName = args[0];
        final String jarPath = args[1];
        final String[] suffixes = new String[args.length-2];
        System.arraycopy(args, 2, suffixes, 0, suffixes.length);
        
        RepositoryConfiguration repoConfig = 
                new RepositoryConfiguration();
        RepositoryUnsafe repo = 
                new RepositoryUnsafe(repoConfig);
        
        
        System.out.printf(
                "using repository @ %s\n",
                repoConfig.getRepositoryBaseUrl());
        
        
        JarIngestor.ingest(
                repo,
                FileUtils.readFileToByteArray(new File(jarPath)), 
                graphName, 
                suffixes
                );

    }

}
