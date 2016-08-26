package com.securboration.immortals.service.api.types;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * All types used in the service API should be subclasses of this class. They
 * must include a constructor that accepts a JSON serialized form. This
 * constructor uses Jackson to deserialize the object instance from json.
 * 
 * See:
 * <a href="https://jersey.java.net/documentation/latest/jaxrs-resources.html" >
 * jaxrs-resources.html</a>
 * 
 * 
 * @author jstaples
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS, 
        include = JsonTypeInfo.As.PROPERTY, 
        property = "type"
        )
public abstract class ImmortalsServiceType {
    /**
     * All subclasses inherit access to a default constructor to encourage
     * dependency injection design patterns
     */
    public ImmortalsServiceType() {
    }

    /**
     * All subclasses must implement this constructor, which creates an object
     * of this type from a JSON serialization.
     * 
     * @param json
     *            a JSON representation of this object
     */
    public ImmortalsServiceType(String json) {
        if (json == null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readerForUpdating(this).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A convenience method for retrieving the JSON form of this object (e.g.,
     * this may be useful for debugging)
     * 
     * @return a JSON representation of this object
     */
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is something of a hack to get CXF to work with HTTP GET requests
     * 
     * @return a JSON representation of this object
     */
    @Override
    public final String toString() {
        return toJson();
    }
}
