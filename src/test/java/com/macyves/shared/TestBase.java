package com.macyves.shared;

import org.junit.Before;

import com.macyves.injection.guice.ServiceInjector;

/**
 * Base unit test class. All unit test should inherit this.
 * 
 * @author yves
 * 
 */
public class TestBase {

    @Before
    public void _init() {
        ServiceInjector.getInjector().injectMembers(this);
    }
}
