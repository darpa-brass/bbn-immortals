package com.securboration.immortals.service.api.DatabaseObjects;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A challenge problem context, which comprises a set of graphs and metadata
 * about their use in the context.
 *
 * @author jstaples
 *
 */
@Entity
public class ImmortalsContext {

    @Id
    private String name;
    
    private String description;
    
    public ImmortalsContext() {}
    
    public ImmortalsContext(String _name) {
        name = _name;
    }
    
    public String getName() { return name; }
    
    public String getDescription() { return description; }
    public void setDescription(String _description) { description = _description; }
    
    @Override
    public String toString() {
        return name;
    }

}
