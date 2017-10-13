package com.securboration.immortals.ontology.functionality.datatype;

import java.util.Date;

/**
 * Representation of a point in time
 * 
 * @author Securboration
 *
 */
public class Time {
    
    /**
     * A point in time
     */
    private Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
