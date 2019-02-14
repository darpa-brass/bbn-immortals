package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.resources.logical.DBSchema;

public class DataBase extends PlatformResource {
    
    private DBSchema[] dataBaseSchemas;

    public DBSchema[] getDataBaseSchemas() {
        return dataBaseSchemas;
    }

    public void setDataBaseSchemas(DBSchema[] dataBaseSchemas) {
        this.dataBaseSchemas = dataBaseSchemas;
    }
}
