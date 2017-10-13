package com.securboration.immortals.service.api.DatabaseObjects;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Created by CharlesEndicott on 7/13/2017.
 */
@Repository
@Component
public interface ContextRepository extends CrudRepository<ImmortalsContext, String> {
}
