package com.macyves.injection.guice;

import com.google.inject.AbstractModule;
import com.macyves.dao.CustomerDAOInterface;
import com.macyves.dao.morphia.MorphiaCustomerDAO;

public class DAOModule extends AbstractModule {
    @Override
    protected void configure() {
        // inject verison facade
        bind(CustomerDAOInterface.class).to(MorphiaCustomerDAO.class);
    }
}
