package com.macyves.integration.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

import com.macyves.http.Curl;
import com.macyves.integration.db.IntegrationTestBase;

public class SanityTests extends IntegrationTestBase {

    @Test
    public void curl_sanity() {
        Curl curl = new Curl("www.google.no", 80, true, false);
        assertEquals(200, curl.issueRequestWithHeaders("GET", "/", null));
    }

    @Test
    public void curl_version() {
        Curl curl = new Curl(API_HOST, API_PORT, true, false);
        assertEquals(200, curl.issueRequestWithHeaders("GET", "/api/v1/unres/version", null));
        assertTrue(curl.getResponseBodyString().contains("api_version=1"));
        assertTrue(curl.getResponseBodyString().contains("version=0.0.1"));
    }

    @Test
    public void curl_json_version() {
        Curl curl = new Curl(API_HOST, API_PORT, true, false);
        assertEquals(200, curl.issueRequestWithHeaders("GET", "/api/v1/unres/versionjson", null));
        JSONObject json = new JSONObject(curl.getResponseBodyString());
        assertEquals("1", json.getString("api_version"));
        assertEquals("0.0.1", json.getString("version"));
    }
}