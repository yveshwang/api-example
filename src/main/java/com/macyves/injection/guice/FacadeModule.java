package com.macyves.injection.guice;

import static com.google.inject.Scopes.SINGLETON;

import com.google.inject.AbstractModule;
import com.macyves.facade.CustomerFacade;
import com.macyves.facade.VersionFacade;
import com.macyves.facade.impl.VersionFacadeImpl;
import com.macyves.facade.mongo.CustomerFacadeImpl;

/**
 * Facade module
 * 
 * @author yves
 * 
 */
public class FacadeModule extends AbstractModule {

    @Override
    protected void configure() {
        // inject verison facade
        bind(CustomerFacade.class).to(CustomerFacadeImpl.class).in(SINGLETON);
        bind(VersionFacade.class).to(VersionFacadeImpl.class).in(SINGLETON);
    }
}
