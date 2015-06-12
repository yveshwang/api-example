package com.macyves.injection.guice;

/**
 * Configuration of the DB module
 * 
 * @author yves
 */
public class DBModule extends AbstractDBModule {
    @Override
    public String getMondoDBNameDefault() {
        return "api_default";
    }
}
