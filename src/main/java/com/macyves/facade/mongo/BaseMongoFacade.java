package com.macyves.facade.mongo;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.Key;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Reference;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.inject.Inject;
import com.macyves.dao.BaseDAOInterface;
import com.macyves.entities.Customer;
import com.macyves.entities.EntityObject;
import com.macyves.facade.BaseDBFacadeInterface;
import com.macyves.injection.guice.DBName;
import com.macyves.pojo.exception.DBException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * Common implementatino for all entitiy dbs
 * 
 * @author yves
 */
public abstract class BaseMongoFacade<Q extends Query<T>, K extends Key<T>, W extends WriteResult, T extends EntityObject, X extends QueryResults<T>> implements BaseDBFacadeInterface<T> {

    @Inject
    private Morphia morphia;

    @Inject
    private Mongo mongo;

    @Inject
    @DBName
    private String dbname;

    private static String getDBName(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation a : annotations) {
            if (a instanceof Entity) {
                return ((Entity) a).value();
            }
        }
        return null;
    }

    private static final Map<String, Class<?>> MODEL_MAP = new HashMap<String, Class<?>>();
    static {
        if (getDBName(Customer.class) != null)
            MODEL_MAP.put(getDBName(Customer.class), Customer.class);
    }

    protected abstract BaseDAOInterface<Q, K, W, T, X> getDAO();

    protected abstract void saveEagerRelations(T entity) throws DBException;

    protected abstract void removeEagerRelations(T entity) throws DBException;

    protected int calculateOffset(int from, int to) throws DBException {
        return (from < 0 || (to - from) < 0) ? count() : from;
    }

    protected int calculateLimit(int from, int to) {

        return (to > 0 && ((to - from) > 0)) ? (to - from) + 1 : 1;
    }

    public List<T> findBasedOnField(String fieldname, Object value, boolean exactmatch) throws DBException {
        try {
            Query<T> query;
            if (value instanceof String && !exactmatch) {
                query = getDAO().createQuery().field(fieldname).containsIgnoreCase((String) value);
            } else {
                query = getDAO().createQuery().field(fieldname).equal(value);
            }
            return query.asList();

        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findBasedOnField(" + fieldname + ", " + value + ", " + exactmatch + ") failed - unchecked exceptions.", "findBasedOnField()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findBasedOnField(" + fieldname + ", " + value + ", " + exactmatch + ") failed - checked exceptions.", "findBasedOnField()", ex);
        }
    }

    public T findOneBasedOnField(String fieldname, Object value, boolean exactmatch) throws DBException {
        try {
            Query<T> query;
            if (value instanceof String && !exactmatch) {
                query = getDAO().createQuery().field(fieldname).containsIgnoreCase((String) value);
            } else {
                query = getDAO().createQuery().field(fieldname).equal(value);
            }
            return query.get();

        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findOneBasedOnField(" + fieldname + ", " + value + ", " + exactmatch + ") failed - unchecked exceptions.", "findOneBasedOnField()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findOneBasedOnField(" + fieldname + ", " + value + ", " + exactmatch + ") failed - checked exceptions.", "findOneBasedOnField()", ex);
        }
    }

    public List<T> findBasedOnFieldWithLimit(String fieldname, Object value, int limit, boolean exactmatch) throws DBException {
        try {
            Query<T> query;
            if (value instanceof String && !exactmatch) {
                query = getDAO().createQuery().field(fieldname).containsIgnoreCase((String) value).limit(limit);
            } else {
                query = getDAO().createQuery().field(fieldname).equal(value).limit(limit);
            }
            return query.asList();

        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findBasedOnFieldWithLimit(" + fieldname + ", " + value + ", " + limit + ", " + exactmatch + ") failed - unchecked exceptions.", "findBasedOnFieldWithLimit()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findBasedOnFieldWithLimit(" + fieldname + ", " + value + ", " + limit + ", " + exactmatch + ") failed - checked exceptions.", "findBasedOnFieldWithLimit()", ex);
        }
    }

    public List<T> findBasedOnFieldWithOrder(String fieldname, Object value, String order, boolean exactmatch) throws DBException {
        try {
            Query<T> query;
            if (value instanceof String && !exactmatch) {
                query = getDAO().createQuery().field(fieldname).containsIgnoreCase((String) value).order(order);
            } else {
                query = getDAO().createQuery().field(fieldname).equal(value).order(order);
            }
            return query.asList();

        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findBasedOnFieldWithOrder(" + fieldname + ", " + value + ", " + order + ", " + exactmatch + ") failed - unchecked exceptions.", "findBasedOnFieldWithOrder()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findBasedOnFieldWithOrder(" + fieldname + ", " + value + ", " + order + ", " + exactmatch + ") failed - checked exceptions.", "findBasedOnFieldWithOrder()", ex);
        }
    }

    public List<T> findBasedOnFieldWithLimitAndOrder(String fieldname, Object value, int limit, String order, boolean exactmatch) throws DBException {
        try {
            Query<T> query;
            if (value instanceof String && !exactmatch) {
                query = getDAO().createQuery().field(fieldname).containsIgnoreCase((String) value).limit(limit).order(order);
            } else {
                query = getDAO().createQuery().field(fieldname).equal(value).limit(limit).order(order);
            }
            return query.asList();

        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findBasedOnFieldWithLimitAndOrder(" + fieldname + ", " + value + ", " + exactmatch + ") failed - unchecked exceptions.", "findBasedOnFieldWithLimitAndOrder()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findBasedOnFieldWithLimitAndOrder(" + fieldname + ", " + value + ", " + exactmatch + ") failed - checked exceptions.", "findBasedOnFieldWithLimitAndOrder()", ex);
        }
    }

    public T save(T entity) throws DBException {
        try {
            saveEagerRelations(entity);
            entity.setTimestamp(new Date(System.currentTimeMillis()));
            getDAO().save(entity);
            return entity;
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("save(" + entity.toString() + ") failed - unchecked exceptions.", "save()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("save(" + entity.toString() + ") failed - checked exceptions.", "save()", ex);
        }
    }

    public void remove(String field, Object value) throws DBException {
        try {
            getDAO().deleteByQuery(getDAO().createQuery().field(field).equal(value));
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("remove(" + field + ", " + value.toString() + ") failed - unchecked exceptions.", "remove()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("remove(" + field + ", " + value.toString() + ") failed - checked exceptions.", "remove()", ex);
        }
    }

    public void remove(T entity) throws DBException {
        try {
            getDAO().delete(entity);
            removeEagerRelations(entity);
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("remove(" + entity.toString() + ") failed - unchecked exceptions.", "remove()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("remove(" + entity.toString() + ") failed - checked exceptions.", "remove()", ex);
        }
    }

    public void remove(List<T> entities) throws DBException {
        for (T entity : entities) {
            remove(entity);
        }
    }

    public T find(Object id) throws DBException {
        try {
            Query<T> query = getDAO().createQuery().field("_id").equal(id instanceof String ? new ObjectId(id.toString()) : id);
            return query.get();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("find(" + "id" + ") failed - unchecked exceptions.", "find()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("find(" + id + ") failed - checked exceptions.", "find()", ex);
        }
    }

    public List<T> findAll() throws DBException {
        try {
            return getDAO().find().asList();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findAll() failed - unchecked exceptions.", "findAll()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findAll() failed - checked exceptions.", "findAll()", ex);
        }
    }

    public List<T> findOrderedList(String order) throws DBException {
        try {
            Query<T> query;

            if (order == null || order.isEmpty()) {
                return findAll();
            } else {
                query = getDAO().createQuery().order(order);
            }
            return query.asList();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findOrderedList(" + order + "+) failed - unchecked exceptions.", "findOrderedList()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findOrderedList(" + order + ") failed - checked exceptions.", "findOrderedList()", ex);
        }
    }

    public List<T> findOrderedRange(int pageNo, int itemsPerPage, String order, String filterCondition, Object filterValue) throws DBException {
        try {
            final int from = (pageNo - 1) * itemsPerPage;
            final int to = from + (itemsPerPage - 1);
            Query<T> query;

            if (filterCondition == null) {
                query = getDAO().createQuery()
                        .offset(calculateOffset(from, to))
                        .limit(calculateLimit(from, to))
                        .order(order);
            } else {
                query = getDAO().createQuery()
                        .offset(calculateOffset(from, to))
                        .limit(calculateLimit(from, to))
                        .filter(filterCondition, filterValue)
                        .order(order);
            }
            return query.asList();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findOrderedRange(" + pageNo + ", " + itemsPerPage + ", " + order + ", " + filterCondition + ", " + filterValue + ") failed - unchecked exceptions.", "findOrderedRange()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findOrderedRange(" + pageNo + ", " + itemsPerPage + ", " + order + ", " + filterCondition + ", " + filterValue + ") failed - checked exceptions.", "findOrderedRange()", ex);
        }
    }

    private DBException createDBException(String msg, String method, Throwable ex) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("[BaseMongoFacade." + method + "] " + msg + ", " + ex.getMessage() + "\n");
        return new DBException(buffer.toString(), ex);
    }

    private boolean isDBServerDown(RuntimeException ex) {
        if ((ex instanceof MongoInternalException || ex instanceof MongoException) &&
                (ex.getCause().getClass().toString().equals(IOException.class.toString()))) {
            return true;
        }

        return false;
    }

    public List<T> findRange(int from, int to) throws DBException {
        try {
            // int skip = (from < 0 ) || (to-from) < 0 ? count() : from - 1;
            // int limit = (to > 0 && ( (to - from) > 0 ) ) ? (to - from)+1 : 1;
            Query<T> query = getDAO().createQuery().offset(calculateOffset(from, to)).limit(calculateLimit(from, to));
            return query.asList();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findRange(" + from + ", " + to + ") failed - unchecked exceptions.", "findRange()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findRange(" + from + ", " + to + ") failed - checked exceptions.", "findRange()", ex);
        }
    }

    public T findOneBasedOnFilters(Map<String, Object> filters) throws DBException {
        try {
            Query<T> query = getDAO().createQuery();
            for (Entry<String, Object> condition : filters.entrySet()) {
                query.filter(condition.getKey(), filters.get(condition.getKey()));
            }
            return query.get();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findSingleBasedOnFilters(" + convertFilters(filters) + ") failed - unchecked exceptions.", "findSingleBasedOnFilters()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findSingleBasedOnFilters(" + convertFilters(filters) + ") failed - checked exceptions.", "findSingleBasedOnFilters()", ex);
        }
    }

    public List<T> findBasedOnFilters(Map<String, Object> filters) throws DBException {
        try {
            Query<T> query = getDAO().createQuery();
            for (Entry<String, Object> condition : filters.entrySet()) {
                query.filter(condition.getKey(), filters.get(condition.getKey()));
            }
            return query.asList();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("findBasedOnFilters(" + convertFilters(filters) + ") failed - unchecked exceptions.", "findBasedOnFilters()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("findBasedOnFilters(" + convertFilters(filters) + ") failed - checked exceptions.", "findBasedOnFilters()", ex);
        }
    }

    private String convertFilters(Map<String, Object> filters) {
        final StringBuilder builder = new StringBuilder();
        if (filters == null) {
            return null;
        }
        Iterator<Entry<String, Object>> it = filters.entrySet().iterator();
        while (it.hasNext()) {
            String key = it.next().getKey();
            Object value = filters.get(key);
            builder.append(key + value.toString());
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public int count() throws DBException {
        try {
            return (int) getDAO().count();
        } catch (RuntimeException ex) {
            if (isDBServerDown(ex)) {
                throw ex;
            } else {
                throw createDBException("count() failed - unchecked exceptions.", "count()", ex);
            }
        } catch (Exception ex) {
            throw createDBException("count() failed - checked exceptions.", "count()", ex);
        }
    }

    public void dropCollection() {
        getDAO().dropCollection();
    }

    public String escapeRegexMetaChars(String str) {
        return str.replaceAll("([\\[\\]\\-\\\\{\\}\\(\\)\\*\\+\\?\\.\\,\\^\\$\\|\\#\\s])", "\\\\$1");
    }

    public JSONObject serializeEntityToJSON(T entity) throws JSONException {
        return new JSONObject(morphia.toDBObject(entity).toString());
    }

    public T serializeJSONToEntity(JSONObject obj, Class<T> clazz) throws JSONException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Map<String, String> refArray = new HashMap<String, String>();
        Map<String, String> refSingle = new HashMap<String, String>();
        // 1. obtain a list of keys that is annotated with @Reference
        // 2. strip away all the keys with foreign references in json
        // 3. store the stripped keys in an array
        // 4. repopulate the fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            Annotation[] annos = f.getAnnotations();
            for (Annotation a : annos) {
                if (a instanceof Reference) {
                    // yep got a hit. check to see if this field is an array
                    final String name = f.getName();
                    String value;
                    try {
                        value = obj.getString(name);
                    } catch (JSONException e) {
                        // not found
                        value = null;
                    }
                    if (f.getType() == List.class) {
                        refArray.put(name, value);
                    } else {
                        refSingle.put(name, value);
                    }
                    obj.remove(name);
                }
            }
        }

        DBObject dbobj = (DBObject) JSON.parse(obj.toString());
        T t = morphia.fromDBObject(clazz, dbobj, morphia.getMapper().createEntityCache());
        DB db = mongo.getDB(dbname);
        Class<?> c = t.getClass();
        for (Entry<String, String> key : refSingle.entrySet()) {
            String value = refSingle.get(key.getKey());
            if (value != null) {
                // obtain the object value
                JSONObject inner = new JSONObject(value);
                String refid = inner.getJSONObject("$id").getString("$oid");
                String ref = inner.getString("$ref");

                BasicDBObject basicobj = (BasicDBObject) db.getCollection(ref).findOne(new BasicDBObject("_id", new ObjectId(refid)));
                if (basicobj != null) {
                    Class<?> refClazz = MODEL_MAP.get(ref);
                    Object refObj = morphia.fromDBObject(refClazz, basicobj, morphia.getMapper().createEntityCache());
                    Field f = c.getDeclaredField(key.getKey());
                    f.setAccessible(true);
                    f.set(t, refObj);
                }
            }
        }
        for (Entry<String, String> key : refArray.entrySet()) {
            String value = refArray.get(key.getKey());
            if (value != null) {
                List<Object> list = new ArrayList<Object>();
                JSONArray inner = new JSONArray(value);
                for (int i = 0; i < inner.length(); i++) {
                    JSONObject inner_obj = inner.getJSONObject(i);
                    String refid = inner_obj.getJSONObject("$id").getString("$oid");
                    String ref = inner_obj.getString("$ref");
                    BasicDBObject basicobj = (BasicDBObject) db.getCollection(ref).findOne(new BasicDBObject("_id", new ObjectId(refid)));
                    if (basicobj != null) {
                        Class<?> refClazz = MODEL_MAP.get(ref);
                        Object refObj = morphia.fromDBObject(refClazz, basicobj, morphia.getMapper().createEntityCache());
                        list.add(refObj);
                    }
                }
                Field f = c.getDeclaredField(key.getKey());
                f.setAccessible(true);
                f.set(t, list);
            }
        }
        return t;
    }
}
