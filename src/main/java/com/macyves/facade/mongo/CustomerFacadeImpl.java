package com.macyves.facade.mongo;

import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.inject.Inject;
import com.macyves.dao.BaseDAOInterface;
import com.macyves.dao.CustomerDAOInterface;
import com.macyves.entities.Customer;
import com.macyves.facade.CustomerFacade;
import com.macyves.pojo.exception.DBException;
import com.mongodb.WriteResult;

/**
 * Customer facade impl using mongo
 * 
 * @author yves
 * 
 */
public class CustomerFacadeImpl extends BaseMongoFacade<Query<Customer>, Key<Customer>, WriteResult, Customer, QueryResults<Customer>> implements CustomerFacade {

    @Inject
    private CustomerDAOInterface dao;

    @Override
    public void something() {
        System.out.println("something");
    }

    @Override
    protected BaseDAOInterface<Query<Customer>, Key<Customer>, WriteResult, Customer, QueryResults<Customer>> getDAO() {
        return dao;
    }

    @Override
    protected void saveEagerRelations(Customer entity) throws DBException {
        // no eager relationship here
    }

    @Override
    protected void removeEagerRelations(Customer entity) throws DBException {
        // no eager relationship here
    }
}
