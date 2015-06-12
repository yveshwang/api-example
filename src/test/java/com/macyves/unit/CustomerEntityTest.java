package com.macyves.unit;

import com.macyves.entities.Customer;

public class CustomerEntityTest extends EntityUnitTestBase<Customer> {
    @Override
    protected Class<Customer> getCoreEntityClass() {
        return Customer.class;
    }
}
