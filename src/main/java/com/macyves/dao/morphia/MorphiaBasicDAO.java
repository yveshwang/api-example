package com.macyves.dao.morphia;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.Mongo;

/**
 * Extended BasicDAO.
 * 
 * @author yves
 * 
 * @param <T> entity type.
 * @param <K> key type.
 */
public class MorphiaBasicDAO<T, K> extends BasicDAO<T, K> {

    public MorphiaBasicDAO(Class<T> entityClass, Datastore ds) {
        super(entityClass, ds);
    }

    public MorphiaBasicDAO(Class<T> entityClass, Mongo mongo, Morphia morphia, String dbName) {
        super(entityClass, mongo, morphia, dbName);
    }

    public MorphiaBasicDAO(Datastore ds) {
        super(ds);
    }

    public MorphiaBasicDAO(Mongo mongo, Morphia morphia, String dbName) {
        super(mongo, morphia, dbName);
    }

    public void dropCollection() {
        getCollection().drop();
    }
}
