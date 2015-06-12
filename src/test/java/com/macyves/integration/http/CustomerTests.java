package com.macyves.integration.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;
import com.macyves.entities.Customer;
import com.macyves.facade.CustomerFacade;
import com.macyves.http.Curl;
import com.macyves.integration.db.IntegrationTestBase;
import com.macyves.pojo.exception.DBException;

public class CustomerTests extends IntegrationTestBase {
    @Inject
    private CustomerFacade facade;

    @Before
    public void init() {
        facade.dropCollection();
    }

    @Test
    public void curlTest() throws ClientProtocolException, IOException {
        HttpHost target = new HttpHost(API_HOST, API_PORT);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(DEFAULT_USER, DEFAULT_PASSWORD));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        try {
            HttpGet httpget = new HttpGet("/api/v1/res/customer");

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(target, httpget, localContext);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                EntityUtils.consume(response.getEntity());
                assertEquals(200, response.getStatusLine().getStatusCode());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getCustomerList() throws DBException, JSONException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        assertEquals(0, facade.count());
        Customer cus = facade.save(new Customer());
        assertEquals(1, facade.count());
        Curl curl = new Curl(API_HOST, API_PORT, true, true);
        curl.setUsernamePassword(DEFAULT_USER, DEFAULT_PASSWORD);
        assertEquals(200, curl.issueRequestWithHeaders("GET", "/api/v1/res/customer", null));
        String responseBody = curl.getResponseBodyString();
        assertNotNull(responseBody);
        JSONObject obj = new JSONObject(responseBody);
        JSONArray array = obj.getJSONArray("list");
        assertEquals(1, array.length());
        Customer check = facade.serializeJSONToEntity(array.getJSONObject(0), Customer.class);
        System.out.println(obj.toString());
        assertEquals(cus.getId(), check.getId());
        assertEquals(200, curl.issueRequestWithHeaders("GET", "/api/v1/res/customer/count", null));
    }
}
