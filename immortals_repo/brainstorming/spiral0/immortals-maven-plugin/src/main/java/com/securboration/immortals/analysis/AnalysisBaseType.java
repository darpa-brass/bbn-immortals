package com.securboration.immortals.analysis;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class AnalysisBaseType implements UniquelyIdentifiable {

    private Uuid uniqueIdentifier;

    @Override
    public Uuid getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(Uuid uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /**
     * All subclasses inherit access to a default constructor to encourage
     * dependency injection design patterns
     */
    public AnalysisBaseType() {
        super();
    }

    /**
     * All subclasses must implement this constructor, which creates an object
     * of this type from a JSON serialization.
     * 
     * @param json
     *            a JSON representation of this object
     */
    public AnalysisBaseType(String json) {
        super();
        
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
