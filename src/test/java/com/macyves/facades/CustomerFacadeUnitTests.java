package com.macyves.facades;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.Inject;
import com.macyves.dao.CustomerDAOInterface;
import com.macyves.facade.CustomerFacade;
import com.macyves.pojo.exception.DBException;
import com.macyves.unit.UnitTestBase;

public class CustomerFacadeUnitTests extends UnitTestBase {
    @Inject
    private CustomerDAOInterface customerDAO;

    @Inject
    private CustomerFacade facade;

    @Before
    public void reset() {
        Mockito.reset(customerDAO);
    }

    @Test
    public void simpleTest1() throws DBException {
        // give the dao says the count is 1.
        given(customerDAO.count()).willReturn(1L);
        // when the facade tries to figure out the cound
        int count = facade.count();
        // then the value returned here should also be one
        assertEquals(1, count);
    }

    @Test
    public void simpleTest2() throws DBException {
        // give the dao says the count is 1.
        given(customerDAO.count()).willReturn(2L);
        // when the facade tries to figure out the cound
        int count = facade.count();
        // then the value returned here should also be one
        assertEquals(2, count);
    }
}
