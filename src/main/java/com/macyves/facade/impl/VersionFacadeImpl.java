package com.macyves.facade.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.macyves.facade.VersionFacade;

public class VersionFacadeImpl implements VersionFacade {

    @Override
    public String getAppVersion() throws IOException {
        return readPropertyFile().getProperty("app_version");
    }

    @Override
    public String getAPIVersion() throws IOException {
        return readPropertyFile().getProperty("api_version");
    }

    private Properties readPropertyFile() throws IOException, FileNotFoundException {
        Properties prop = new Properties();
        final String propFileName = "api.properties";
        InputStream is = getClass().getClassLoader().getResourceAsStream(propFileName);
        prop.load(is);
        if (is == null) {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        is.close();
        return prop;
    }
}
