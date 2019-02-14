package com.securboration.immortals.service.eos.api.types;

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
public abstract class EosType {
    /**
     * All subclasses inherit access to a default constructor to encourage
     * dependency injection design patterns
     */
    public EosType() {
    }

    /**
     * All subclasses must implement this constructor, which creates an object
     * of this type from a JSON serialization.
     * 
     * @param json
     *            a JSON representation of this object
     */
    public EosType(String json) {
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
     * 
     * @return a copy of this artifact
     */
    public Object cp(){
        return fromJson(this.toString(),this.getClass());
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
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
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
    
    public static <T> T fromJson(String json, Class<T> type){
        try{
            return new ObjectMapper().readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
