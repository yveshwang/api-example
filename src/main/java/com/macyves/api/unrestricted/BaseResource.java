package com.macyves.api.unrestricted;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.macyves.injection.guice.ServiceInjector;

/**
 * Unrestricted base class for API.
 * 
 * @author yves
 * 
 */
public abstract class BaseResource {

    protected static final String MESSAGE_KEY = "message";

    protected static final Response DB_EXCEPTION = buildJSONResponse(500, "Failed to perform operation on mongodb.");

    protected static final Response ID_REQUIRED = buildJSONResponse(400, "Id field, _id , is missing in the JSON model.");

    protected static final Response INVALID_ID = buildJSONResponse(400, "Id field is invalid or missing on the url.");

    protected static final Response ID_DOES_NOT_EXIST = buildJSONResponse(404, "Id referred to by the object does not exist.");

    protected static final Response INVALID_URL_INPUT = buildJSONResponse(400, "Invalid url input.");

    protected static final Response INVALID_JSON = buildJSONResponse(400, "Invalid json input.");

    protected static final Response INVALID_JSON_SERVERSIDE = buildJSONResponse(500, "Server failed to parse JSON");

    protected static String buildJSONMessage(String message) {
        try {
            JSONObject obj = new JSONObject();
            obj.put(MESSAGE_KEY, message);
            return obj.toString();
        } catch (JSONException e) {
            return "{ \"" + MESSAGE_KEY + "\" : \"Unable to buildJSONMessage.\"}";
        }
    }

    protected static Response buildJSONResponse(int status, String message) {
        final StringBuilder builder = new StringBuilder();
        builder.append(buildJSONMessage(message));
        return Response.status(status).entity(builder.toString()).build();
    }

    @PostConstruct
    public void init() {
        ServiceInjector.getInjector().injectMembers(this);
    }

    protected Object verifyAndGetJSONString(String key, String data, String errorMessage) {
        JSONObject obj = null;
        String value = null;
        try {
            obj = new JSONObject(data);
        } catch (JSONException e) {
            return INVALID_JSON;
        }
        if (!obj.has(key)) {
            return buildJSONResponse(400, errorMessage);
        }
        try {
            value = obj.getString(key);
            return value;
        } catch (JSONException e) {
            return INVALID_JSON;
        }
    }
}
