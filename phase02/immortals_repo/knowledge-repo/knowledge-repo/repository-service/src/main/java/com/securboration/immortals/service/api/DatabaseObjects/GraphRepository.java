package com.securboration.immortals.service.api.DatabaseObjects;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by CharlesEndicott on 7/12/2017.
 */
@Repository
public interface GraphRepository extends CrudRepository<Graph, String> {
    
    
    
}
