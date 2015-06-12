package com.macyves.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.BDDMockito.given;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.Inject;
import com.macyves.dao.CustomerDAOInterface;
import com.macyves.entities.Customer;

/**
 * The DAO unit test for customers. Avoid injection, use mocking instead.
 * 
 * @author yves
 */
public class CustomerDAOTest extends UnitTestBase {

    @Inject
    private CustomerDAOInterface customerDAO;

    @Before
    public void reset() {
        Mockito.reset(customerDAO);
    }

    @Test
    public void reallyDumbTest() {
        // given
        given(customerDAO.count()).willReturn(1L);

        // when a new customer is created
        customerDAO.save(new Customer());

        // then dao should return a customer
        assertEquals(1, customerDAO.count());
    }

    @Test
    public void testEqual() {
        Customer cus1 = new Customer();
        cus1.setName("John Doe");
        Customer cus2 = new Customer();
        cus2.setName("Not John Doe");
        // no id set, so they should be equal
        assertEquals(cus1, cus2);

        // has id, no longer equal
        cus1.setId(ObjectId.get());
        cus2.setId(ObjectId.get());
        assertNotEquals(cus1, cus2);

    }

    public Class<?> safeCast(Class<?> intend, Class<?> original) {
        if (original == intend) {
            return intend;
        } else {
            return null;
        }
    }
}
