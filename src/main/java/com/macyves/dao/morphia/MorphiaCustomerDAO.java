package com.macyves.dao.morphia;

import com.google.code.morphia.Morphia;
import com.google.inject.Inject;
import com.macyves.dao.CustomerDAOInterface;
import com.macyves.entities.Customer;
import com.macyves.injection.guice.DBName;
import com.mongodb.Mongo;

/**
 * Morphia implementation of the customer DAO.
 * 
 * @author yves
 * 
 */
public class MorphiaCustomerDAO extends MorphiaBasicDAO<Customer, String> implements CustomerDAOInterface {
    @Inject
    public MorphiaCustomerDAO(Mongo mongo, Morphia morphia, @DBName String db) {
        super(mongo, morphia, db);
    }
}
