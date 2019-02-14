package com.securboration.immortals.service.config;

import org.springframework.beans.factory.annotation.Value;

import com.securboration.immortals.service.api.types.ImmortalsServiceType;

/**
 * Properties needed by IMMoRTALS services.
 * 
 * @see <a href=
 *      "https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html">
 *      spring property configuration documentation</a>
 * 
 * @author jstaples
 *
 */
public class ImmortalsServiceProperties extends ImmortalsServiceType {
    
    public ImmortalsServiceProperties() {
        super();
    }

    public ImmortalsServiceProperties(String s) {
        super(s);
    }

    // keys //
    public static final String FUSEKI_ENDPOINT_PROPERTY_KEY = 
            "fusekiUrl";
    public static final String IMMORTALS_VERSION_PROPERTY_KEY = 
            "immortalsVersion";
    public static final String IMMORTALS_NS_KEY = 
            "immortalsNs";
    

    // values //
    @Value("${" + FUSEKI_ENDPOINT_PROPERTY_KEY
            + ":" 
            + "http://localhost:3030/ds"
            + "}")
    private String fusekiEndpointUrl;
    
    @Value("${" + IMMORTALS_VERSION_PROPERTY_KEY
            + ":" 
            + "r2.0.0"
            + "}")
    private String immortalsVersion;
    
    @Value("${" + IMMORTALS_NS_KEY
        + ":" 
        + "http://darpa.mil/immortals/ontology"
        + "}")
    private String immortalsNs;
    
    private int localServerPort = 
            System.getProperty("server.port") == null ? 
                    8080 
                    : 
                    Integer.parseInt(System.getProperty("server.port"));

    
    public String getFusekiEndpointUrl() {
        return fusekiEndpointUrl;
    }

    public String getImmortalsVersion() {
        return immortalsVersion;
    }

    
    public String getImmortalsNs() {
        return immortalsNs;
    }

    
    public int getLocalServerPort() {
        return localServerPort;
    }
}
