/**
 * Simple curl-like syntax wrapper based on Apache Httpcomponent Httpclient.
 * 
 * Yves Hwang, 08.10.2014
 * http://macyves.wordpress.com
 * 
 */
package com.macyves.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class Curl {
    private static final Logger LOGGER = Logger.getLogger(Curl.class);

    private CloseableHttpClient client;
    private HttpHost configuration;
    private HttpResponse lastResponseHeader;
    private HttpEntity lastResponseBody;
    private String lastResponseBodyString = "";
    private HttpClientContext localcontext;
    private String lastRequestBodyString = "";
    private String username = "test";
    private String password = "vac123!";
    private boolean usebasic = false;
    private boolean verbose = false;

    public Curl(String hostname, int port, final boolean verbose, final boolean basicAuth) {
        this.usebasic = basicAuth;
        this.verbose = verbose;
        configuration = new HttpHost(hostname, port);
        localcontext = HttpClientContext.create();
    }

    public void clearCookies() {
        localcontext.getCookieStore().clear();
    }

    public void setUsernamePassword(String uname, String pwd) {
        this.username = uname;
        this.password = pwd;
    }

    private int executeRequest(HttpRequestBase method) {
        try {
            HttpResponse response;
            if (usebasic) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(null, -1),
                        new UsernamePasswordCredentials(username, password));
                // localcontext.setCredentialsProvider(credentialsProvider);
                client = HttpClients.custom()
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .build();

                // Create AuthCache instance
                AuthCache authCache = new BasicAuthCache();
                // Generate BASIC scheme object and add it to the local
                // auth cache
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(configuration, basicAuth);

                // Add AuthCache to the execution context
                localcontext.setAuthCache(authCache);
            } else {
                client = HttpClientBuilder.create().build();
            }
            response = client.execute(configuration, method, localcontext);
            // build the client, then execute it
            lastResponseHeader = response;
            // consume the response
            lastResponseBody = response.getEntity();
            final int returnValue = response.getStatusLine().getStatusCode();
            if (lastResponseBody != null) {
                lastResponseBodyString = EntityUtils.toString(lastResponseBody);
            } else {
                lastResponseBodyString = null;
            }
            EntityUtils.consume(lastResponseBody);
            if (verbose) {
                // print out request
                LOGGER.info("===== REQUEST =====");
                LOGGER.info(method.getRequestLine().toString());
                Header[] r_headers = method.getAllHeaders();
                for (Header header : r_headers) {
                    LOGGER.info(header.getName() + ": " + header.getValue());
                }
                LOGGER.info(lastRequestBodyString);
                // print out response
                LOGGER.info("===== RESPONSE =====");
                LOGGER.info(response.getStatusLine().toString());
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    LOGGER.info(header.getName() + ": " + header.getValue());
                }
                LOGGER.info(lastResponseBodyString);
            }
            return returnValue;
        } catch (IOException e) {
            // could be connection refused here: Caused by:
            // java.net.ConnectException: Connection refused
            return 598;
        } finally {
            if (method != null) {
                try {
                    method.releaseConnection();
                } catch (java.lang.IllegalStateException ex) {
                    LOGGER.warn("Exception caught releasing connection.", ex);
                }
            }
        }
    }

    private int createAndExecuteMethodWithHeaders(String type, String url, String data, boolean upload, boolean multipart, String... headers) {
        HttpRequestBase method = null;
        try {
            method = createMethod(type, url, data, upload, multipart);
            if (headers != null) {
                if (headers.length > 0) {
                    for (String header_string : headers) {
                        String[] split = header_string.split(":");
                        method.addHeader(split[0].trim(), split[1].trim());
                    }
                }
            }

        } catch (UnsupportedEncodingException e1) {
            return 596;
        }
        return executeRequest(method);
    }

    public int uploadWithHeaders(String type, String url, String data, boolean multipart, String... headers) {
        return createAndExecuteMethodWithHeaders(type, url, data, true, multipart, headers);
    }

    public int issueRequestWithHeaders(String type, String url, String data, String... headers) {
        return createAndExecuteMethodWithHeaders(type, url, data, false, false, headers);
    }

    public String getCookieValue() {
        if (lastResponseHeader.containsHeader("Set-Cookie")) {
            return lastResponseHeader.getFirstHeader("Set-Cookie").getValue().split(";")[0];
        } else {
            return null;
        }
    }

    public String getResponseBodyString() {
        return lastResponseBodyString;
    }

    public List<String> getResponseHeadersString() {
        Header[] headers = lastResponseHeader.getAllHeaders();
        if (headers == null || headers.length <= 0)
            return null;
        List<String> array = new ArrayList<String>();
        for (int i = 0; i < headers.length; i++) {
            array.add(headers[i].getName() + ": " + headers[i].getValue());
        }
        return array;
    }

    private HttpRequestBase createMethod(String type, String url, String data, boolean upload, boolean multipart) throws UnsupportedEncodingException {
        HttpRequestBase method;
        if (type.equals("PUT")) {
            method = new HttpPut(url);
            if (data != null) {
                ((HttpPut) method).setEntity(new StringEntity(data));
            }
        } else if (type.equals("POST")) {
            method = new HttpPost(url);
            if (data != null && !upload) {
                ((HttpPost) method).setEntity(new StringEntity(data));
                lastRequestBodyString = data;
            } else if (data != null && upload) {
                if (multipart) {
                    // read from file location and use multipart entity
                    File file = new File(data);
                    FileBody body = new FileBody(file);
                    HttpEntity entity = MultipartEntityBuilder.create()
                            .addPart("file", body)
                            .build();
                    ((HttpPost) method).setEntity(entity);
                    lastRequestBodyString = "binary";
                } else {
                    // File entity as octetstream
                    byte[] content = null;
                    try {
                        content = Base64.encodeBase64(readFileToBytes(new File(data)));
                    } catch (IOException e) {
                        throw new UnsupportedEncodingException("Unable to perform Base64.encode.");
                    }
                    ((HttpPost) method).setEntity(new ByteArrayEntity(content, ContentType.APPLICATION_OCTET_STREAM));
                    // ((HttpPost) method).setEntity(new FileEntity(new
                    // File(data), ContentType.APPLICATION_OCTET_STREAM));
                }
            }
        } else if (type.equals("DELETE")) {
            method = new HttpDelete(url);
        } else if (type.equals("PATCH")) {
            method = new HttpPatch(url);
            if (data != null) {
                ((HttpPatch) method).setEntity(new StringEntity(data));
            }
        } else {
            method = new HttpGet(url);
        }
        return method;
    }

    public void shutdown() throws IOException {
        client.close();
    }

    public static byte[] readFileToBytes(File file) throws IOException {
        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large!");
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        return bytes;
    }

}