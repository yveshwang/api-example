package com.macyves.facade;

import com.macyves.entities.Customer;

/**
 * Facade for Customer entity
 * 
 * @author yves
 * 
 */
public interface CustomerFacade extends BaseDBFacadeInterface<Customer> {
    public void something();
}
