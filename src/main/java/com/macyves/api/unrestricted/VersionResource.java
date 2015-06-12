package com.macyves.api.unrestricted;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.macyves.facade.VersionFacade;

/**
 * The root url for the unrestricted API
 * 
 * @author yves
 * 
 */
@Path("/")
public class VersionResource extends BaseResource {

    @Inject
    private VersionFacade facade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("versionjson")
    public Response version_json() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("version", facade.getAppVersion());
            obj.put("api_version", facade.getAPIVersion());
            return Response.status(200).entity(obj.toString()).build();
        } catch (JSONException ex) {
            return INVALID_JSON_SERVERSIDE;
        } catch (IOException ex) {
            return buildJSONResponse(500, "IO error. Missing property files? " + ex.getMessage());
        } catch (Exception ex) {
            return buildJSONResponse(500, ex.getMessage());
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("version")
    public Response version() {
        final StringBuilder builder = new StringBuilder();
        try {
            builder.append("version=" + facade.getAppVersion() + ", ");
            builder.append("api_version=" + facade.getAPIVersion());
            return Response.status(200).entity(builder.toString()).build();
        } catch (IOException ex) {
            return buildJSONResponse(500, "IO error. Missing property files? " + ex.getMessage());
        } catch (Exception ex) {
            return buildJSONResponse(500, ex.getMessage());
        }
    }
}
