package com.macyves.shared;

import com.macyves.injection.guice.AbstractDBModule;

/**
 * Test configuration for integration test db.
 * 
 * @author yves
 * 
 */
public class TestDBModule extends AbstractDBModule {
    @Override
    public String getMondoDBNameDefault() {
        return "api_test";
    }
}