package com.securboration.immortals.service.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.instantiation.bytecode.UriMappings;

/**
 * An API for mapping URIs in <a href=
 * "https://dsl-external.bbn.com/svn/immortals/trunk/shared/modules/core/src/main/java/mil/darpa/immortals/core/Semantics.java">
 * Semantics.java</a> to URIs in the ontology
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsUriMappingService")
public class UriMappingService {
    
    @Autowired(required = true)
    private UriMappings mappings;

    /**
     * Used to map URIs in <a href=
     * "https://dsl-external.bbn.com/svn/immortals/trunk/shared/modules/core/src/main/java/mil/darpa/immortals/core/Semantics.java">
     * Semantics.java</a> to URIs in the ontology
     * 
     * @param uri
     *            a URI defined by BBN
     * @return a URI in the ontology
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/getOntologyUri",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String getOntologyUri(
            @RequestParam("uri") 
            String uri
            ) {
        return mappings.map(uri);
    }

}
