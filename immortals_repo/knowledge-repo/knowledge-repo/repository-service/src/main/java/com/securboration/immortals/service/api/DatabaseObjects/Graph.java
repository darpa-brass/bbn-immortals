package com.securboration.immortals.service.api.DatabaseObjects;

import javax.persistence.*;

/**
 * Created by CharlesEndicott on 6/22/2017.
 */
@Entity
public class Graph {
    
    @Id
    private String name;
    @Lob
    private String body;
    private String type;
    private String context;
    
    public Graph() {}
    
    public Graph(String _name) {
        name = _name;
    }

    public String getName() {
       return name;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String _body) {
        body = _body;
    }
    
    
    public String getType() { return type; }
    public void setType(String _type) { type = _type; }
    
    public String getContext() { return context; }
    public void setContext(String _context) { context = _context; }

    @Override
    public String toString() {

        return getName();
    }


}
