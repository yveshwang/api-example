package com.macyves.api.restricted;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.macyves.api.unrestricted.BaseResource;
import com.macyves.entities.EntityObject;
import com.macyves.facade.BaseDBFacadeInterface;
import com.macyves.pojo.exception.DBException;

/**
 * Base resource for the CRUD facades
 * 
 * @author yves
 * 
 */
public abstract class CRUDBaseResource<T extends EntityObject> extends BaseResource {

    protected abstract Class<T> getEntityClass();

    protected abstract BaseDBFacadeInterface<T> getFacade();

    protected List<T> findAllModels(HttpServletRequest req) throws DBException {
        return getFacade().findAll();
    }

    protected Response saveModel(T entity, HttpServletRequest req, boolean isupdate) {
        try {
            T saved = getFacade().save(entity);
            return Response.ok().entity(getFacade().serializeEntityToJSON(saved).toString()).build();
        } catch (DBException e) {
            return DB_EXCEPTION;
        } catch (IllegalArgumentException e) {
            return buildJSONResponse(400, e.getMessage());
        } catch (JSONException e) {
            return buildJSONResponse(500, e.getMessage());
        }
    }

    protected Response removeModel(T t, HttpServletRequest req) {
        try {
            getFacade().remove(t);
        } catch (DBException e) {
            return DB_EXCEPTION;
        }
        return Response.ok().entity(buildJSONMessage("Entity with id=" + t.getId().toString() + " removed successfully.")).build();
    }

    protected Response findModel(String id, HttpServletRequest req) {
        try {
            T t = getFacade().find(id);
            if (t == null) {
                return ID_DOES_NOT_EXIST;
            } else {
                return Response.ok().entity(getFacade().serializeEntityToJSON(t).toString()).build();
            }
        } catch (DBException ex) {
            return DB_EXCEPTION;
        } catch (IllegalArgumentException ex) {
            return buildJSONResponse(400, ex.getMessage());
        } catch (JSONException e) {
            return buildJSONResponse(500, "Unable to serialize json string to entity. message=" + e.getMessage() + " class=" + getEntityClass().getSimpleName());
        }
    }

    private Response upsertJSON(String id, String data, boolean isupdate, HttpServletRequest req) {
        try {
            if (isupdate) {
                if (id == null) {
                    return ID_REQUIRED;
                }

                if (!ObjectId.isValid(id)) {
                    return INVALID_ID;
                }
                // check that id exists.
                if (getFacade().find(id) == null) {
                    return ID_DOES_NOT_EXIST;
                }
            }

            JSONObject obj = new JSONObject(data);
            T t = getFacade().serializeJSONToEntity(obj, getEntityClass());
            // merge it here if its an update
            if (isupdate) {
                T original = getFacade().find(id);
                original.merge(t);
                return saveModel(original, req, isupdate);
            } else {
                return saveModel(t, req, isupdate);
            }

        } catch (DBException ex) {
            return DB_EXCEPTION;
        } catch (IllegalArgumentException ex) {
            return buildJSONResponse(400, ex.getMessage());
        } catch (JSONException e) {
            return buildJSONResponse(400, "Unable to serialize json string to entity. message=" + e.getMessage() + " class=" + getEntityClass().getSimpleName() + " jsonstring=" + data);
        } catch (NoSuchFieldException e) {
            return buildJSONResponse(400, "Unable to serialize json string to entity. message=" + e.getMessage() + " class=" + getEntityClass().getSimpleName() + " jsonstring=" + data);
        } catch (IllegalAccessException e) {
            return buildJSONResponse(400, "Unable to serialize json string to entity. message=" + e.getMessage() + " class=" + getEntityClass().getSimpleName() + " jsonstring=" + data);
        } catch (RuntimeException ex) {
            return buildJSONResponse(400, "Unable to serialize json string to entity. message=" + ex.getMessage() + " class=" + getEntityClass().getSimpleName() + " jsonstring=" + data);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getList(@Context HttpServletRequest req) {
        try {
            // get the entities
            List<T> list = findAllModels(req);
            JSONObject obj = convertListToJSonArray(list, getFacade());
            return Response.ok().entity(obj.toString()).build();
        } catch (DBException e) {
            return DB_EXCEPTION;
        } catch (IllegalArgumentException ex) {
            return buildJSONResponse(400, ex.getMessage());
        } catch (JSONException e) {
            return buildJSONResponse(500, "Unable to serialize json string to entity. message=" + e.getMessage() + " class=" + getEntityClass().getSimpleName());
        }
    }

    @GET
    @Path("{entity_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("entity_id") String id, @Context HttpServletRequest req) {
        if (id == null || !ObjectId.isValid(id)) {
            return INVALID_ID;
        }
        return findModel(id, req);
    }

    @DELETE
    @Path("{entity_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeById(@PathParam("entity_id") String id, @Context HttpServletRequest req) {
        if (id == null || !ObjectId.isValid(id)) {
            return INVALID_ID;
        }
        try {
            T t = getFacade().find(id);
            if (t == null) {
                return ID_DOES_NOT_EXIST;
            } else {
                return removeModel(t, req);
            }
        } catch (DBException ex) {
            return DB_EXCEPTION;
        } catch (IllegalArgumentException ex) {
            return buildJSONResponse(400, ex.getMessage());
        }
    }

    @PUT
    @Path("{entity_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("entity_id") String id, String data, @Context HttpServletRequest req) {
        // add, create routine
        return upsertJSON(id, data, true, req);
    }

    @PATCH
    @Path("{entity_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update_patch(@PathParam("entity_id") String id, String data, @Context HttpServletRequest req) {
        // add, create routine
        return upsertJSON(id, data, true, req);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String data, @Context HttpServletRequest req) {
        // update routine.
        return upsertJSON(null, data, false, req);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{page_no}/{items_per_page}")
    public Response pagination(@PathParam("page_no") String page_no, @PathParam("items_per_page") String items_per_page, @Context HttpServletRequest req) {
        // verify the page number and items per page
        if (page_no == null && items_per_page == null) {
            return INVALID_URL_INPUT;
        }

        int pageNo = 0;
        int itemsPerPage = 0;
        try {
            pageNo = Integer.parseInt(page_no);
            itemsPerPage = Integer.parseInt(items_per_page);
        } catch (NumberFormatException ex) {
            return INVALID_URL_INPUT;
        }

        try {
            List<T> objs = getFacade().findOrderedRange(pageNo, itemsPerPage, "-timestamp", null, null);
            JSONObject obj = convertListToJSonArray(objs, getFacade());
            return Response.ok().entity(obj.toString()).build();
        } catch (DBException e) {
            return DB_EXCEPTION;
        } catch (JSONException e) {
            return buildJSONResponse(500, "Unable to serialize bans into json obj");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("count")
    public Response count() {
        try {
            final int count = getFacade().count();
            JSONObject obj = new JSONObject();
            obj.put("count", count);
            return Response.ok().entity(obj.toString()).build();
        } catch (DBException e) {
            return DB_EXCEPTION;
        } catch (JSONException e) {
            return INVALID_JSON_SERVERSIDE;
        }
    }

    protected JSONObject convertListToJSonArray(List<T> list, BaseDBFacadeInterface<T> facade) throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        if (list != null) {
            for (T t : list) {
                array.put(facade.serializeEntityToJSON(t));
            }
        } else {
            return null;
        }
        obj.put("list", array);
        return obj;
    }
}
