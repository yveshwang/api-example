package com.macyves.unit;

import com.macyves.injection.guice.FacadeModule;
import com.macyves.injection.guice.ServiceInjector;
import com.macyves.shared.TestBase;
import com.macyves.shared.TestDBModule;

public class UnitTestBase extends TestBase {

    static {
        // override the default set of modules
        ServiceInjector.loadModules(new TestDBModule(), new UnitTesDAOModule(), new FacadeModule());
    }
}
