package com.macyves.facade;

import java.io.IOException;

public interface VersionFacade {
    public String getAppVersion() throws IOException;

    public String getAPIVersion() throws IOException;
}
