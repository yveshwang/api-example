package com.macyves.integration.db;

import com.macyves.injection.guice.DAOModule;
import com.macyves.injection.guice.FacadeModule;
import com.macyves.injection.guice.ServiceInjector;
import com.macyves.shared.TestBase;
import com.macyves.shared.TestDBModule;

public class IntegrationTestBase extends TestBase {
    protected static final int API_PORT = 8888;
    protected static final String API_HOST = "127.0.0.1";
    protected static final String DEFAULT_USER = "admin";
    protected static final String DEFAULT_PASSWORD = "adminadmin";
    static {
        // override the default set of modules
        ServiceInjector.loadModules(new TestDBModule(), new DAOModule(), new FacadeModule());
    }
}
