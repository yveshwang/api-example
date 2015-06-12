package com.macyves.unit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.AbstractModule;
import com.macyves.dao.CustomerDAOInterface;

public class UnitTesDAOModule extends AbstractModule {

    @Mock
    private CustomerDAOInterface customerDao;

    @Override
    protected void configure() {
        MockitoAnnotations.initMocks(this);
        bind(CustomerDAOInterface.class).toInstance(this.customerDao);
    }
}
