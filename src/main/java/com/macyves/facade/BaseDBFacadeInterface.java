package com.macyves.facade;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.macyves.entities.EntityObject;
import com.macyves.pojo.exception.DBException;

/**
 * Base facade for all entities
 * 
 * @author yves
 * 
 */
public interface BaseDBFacadeInterface<T extends EntityObject> {

    /**
     * Serialize entity to json
     * 
     * @param entity
     * @return
     */
    JSONObject serializeEntityToJSON(T entity) throws JSONException;

    /**
     * Serialize json to entity.
     * 
     * @param obj
     * @return
     */
    T serializeJSONToEntity(JSONObject obj, Class<T> clazz) throws JSONException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException;

    /**
     * Save entity based on Type T.
     * 
     * @param entity
     * @return
     * @throws DBException
     */
    T save(T entity) throws DBException;

    /**
     * Find a single object based on id. Id can either be String or ObjectId.
     * 
     * @param id
     * @return
     * @throws DBException
     */
    T find(Object id) throws DBException;

    /**
     * Find a single object based on fieldname name and a the matching value.
     * 
     * @param fieldname
     * @param value
     * @param exactmatch
     * @return
     * @throws DBException
     */
    T findOneBasedOnField(String fieldname, Object value, boolean exactmatch) throws DBException;

    /**
     * Find a list of object based on fieldname. Exactmatch must be true if
     * value is a POJO, else strange string matching occurs.
     * 
     * @param fieldname
     * @param value
     * @param exactmatch
     * @return
     * @throws DBException
     */
    List<T> findBasedOnField(String fieldname, Object value, boolean exactmatch) throws DBException;

    /**
     * Find a list of objects based on field name and a matchin value, maximum
     * number of object is limited by limit.
     * 
     * @param columnname
     * @param value
     * @param limit
     * @param exactmatch
     * @return
     * @throws DBException
     */
    List<T> findBasedOnFieldWithLimit(String fieldname, Object value, int limit, boolean exactmatch) throws DBException;

    /**
     * Find a list of object based on field name and order it by ascending or
     * descending.
     * 
     * @param fieldname
     * @param value
     * @param order
     * @param exactmatch
     * @return
     * @throws DBException
     */
    List<T> findBasedOnFieldWithOrder(String fieldname, Object value, String order, boolean exactmatch) throws DBException;

    /**
     * Find a list of object based on field name and matching value, with limits
     * and its ordered.
     * 
     * @param fieldname
     * @param value
     * @param limit
     * @param order
     * @param exactmatch
     * @return
     * @throws DBException
     */
    List<T> findBasedOnFieldWithLimitAndOrder(String fieldname, Object value, int limit, String order, boolean exactmatch) throws DBException;

    /**
     * A select * routine. Not recommended due to performance.
     * 
     * @return
     * @throws DBException
     */
    List<T> findAll() throws DBException;

    /**
     * Get an ordered list based on the column name and the order (ascending or
     * descending).
     * 
     * @param order
     * @return
     * @throws DBException
     */
    List<T> findOrderedList(String order) throws DBException;

    /**
     * findOrderedRange() returns an ordered/sorted list of objects based on the
     * criteria/condition "order". Order is based on a mongo property (defines
     * return order). Examples: order("age"), order("-age") (descending order)
     * order("age,date") order("age,-date") (age ascending, date descending)
     * 
     * Create a filter based on the specified condition and value. Note:
     * Property is in the form of "name op" ("age >"). Valid operators are ["=",
     * "==","!=", "<>", ">", "<", ">=", "<=", "in", "nin", "all", "size",
     * "exists"] Examples: filter("yearsOfOperation >", 5)
     * filter("rooms.maxBeds >=", 2) filter("rooms.bathrooms exists", 1)
     * filter("stars in", new Long[]{3,4}) //3 and 4 stars (midrange?)}
     * filter("age >=", age) filter("age =", age) filter("age", age) (if no
     * operator, = is assumed) filter("age !=", age) filter("age in", ageList)
     * filter("customers.loyaltyYears in", yearsList) You can filter on id
     * properties if this query is restricted to a Class.
     * 
     * @param pageNo
     * @param itemsPerPage
     * @param order
     * @param filter
     * @param filterValue
     * @return
     * @throws DBException
     */
    List<T> findOrderedRange(int pageNo, int itemsPerPage, String order,
            String filterCondition, Object filterValue) throws DBException;

    /**
     * Get an unordered list based on the range.
     * 
     * @param from
     * @param to
     * @return
     * @throws DBException
     */
    List<T> findRange(int from, int to) throws DBException;

    /**
     * Obtain a single object that matches the filter
     * 
     * @param filters
     * @return
     * @throws DBException
     */
    T findOneBasedOnFilters(Map<String, Object> filters) throws DBException;

    /**
     * Obtain a list of object that matches the filter
     * 
     * @param filters
     * @return
     * @throws DBException
     */
    List<T> findBasedOnFilters(Map<String, Object> filters) throws DBException;

    /**
     * Return count of table.
     * 
     * @return
     * @throws DBException
     */
    int count() throws DBException;

    /**
     * Remove single entity from table.
     * 
     * @param entity
     * @throws DBException
     */
    void remove(T entity) throws DBException;

    /**
     * Remove a batch of entity from table.
     * 
     * @param entities
     * @throws DBException
     */
    void remove(List<T> entities) throws DBException;

    /**
     * Delete object by query.
     * 
     * @param field
     * @param value
     * @throws DBException
     */
    void remove(String field, Object value) throws DBException;

    /**
     * Drop the entire table.
     */
    void dropCollection();

    /**
     * Escape metacharacters in regex, making them literal.
     * 
     * @param input
     * @return
     */
    String escapeRegexMetaChars(String input);
}
