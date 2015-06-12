package com.macyves.integration.db;

import static org.junit.Assert.assertEquals;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;
import com.macyves.entities.Customer;
import com.macyves.facade.BaseDBFacadeInterface;
import com.macyves.facade.CustomerFacade;
import com.macyves.pojo.exception.DBException;

/**
 * Customer integration test. writes to db. so make sure tear down occurs.
 * 
 * @author yves
 * 
 */
public class CustomerDBTest extends DBTestBase<Customer> {
    
    @Inject
    private CustomerFacade facade;
    
    @Before
    public void init() {
        facade.dropCollection();
    }
    
    @Test
    public void addNewCustomer() throws DBException {
        final String name = "mr andersen";
        assertEquals(0, facade.count());
        Customer cus = new Customer();
        cus.setName(name);
        facade.save(cus);
        assertEquals(1, facade.count());
        assertEquals(name, facade.findAll().get(0).getName());
    }

    @Override
    protected BaseDBFacadeInterface<Customer> getMainTestFacade() {
        return facade;
    }

    @Override
    protected Class<Customer> getCoreEntityClass() {
        return Customer.class;
    }

    @Override
    protected ObjectId getObjectId(Customer t) {
        return t.getId();
    }

    @Override
    protected String getOneFieldName() {
        return "name";
    }

    @Override
    protected String getOneSetterMethodName() {
        return "setName";
    }

    @Override
    protected String getOneMethodParamInString() {
        return "mr andersen";
    }

    @Override
    protected String getOneOrderCondition() {
        return "-name";
    }

    @Override
    protected String getOneFilterCondition() {
        return "name ==";
    }

    @Override
    protected Object getOneFilterValue() {
        return getOneMethodParamInString();
    }

    @Override
    public void eagerSaveTest() throws DBException {
        // no eager tests
    }

    @Override
    public void eagerRemoveTest() throws DBException {
        // no eager tests
    }
}
