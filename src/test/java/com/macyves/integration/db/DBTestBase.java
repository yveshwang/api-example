package com.macyves.integration.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.macyves.entities.EntityObject;
import com.macyves.facade.BaseDBFacadeInterface;
import com.macyves.pojo.exception.DBException;

public abstract class DBTestBase<T extends EntityObject> extends IntegrationTestBase {

    protected static final int MAX_BATCH_SIZE = 30;
    protected static final int SUBSTRING_START = 1;
    protected static final int SUBSTRING_END = 5;
    protected static final int LIMIT = 3;
    protected static final String ID_FIELD = "_id";

    @Before
    public void init() {
        // start with a clean state
        getMainTestFacade().dropCollection();
    }

    @After
    public void tearDown() {
        // back to a clean state
        // getMainTestFacade().dropCollection();
    }

    protected abstract BaseDBFacadeInterface<T> getMainTestFacade();

    protected abstract Class<T> getCoreEntityClass();

    protected abstract ObjectId getObjectId(T t);

    protected abstract String getOneFieldName();

    protected abstract String getOneSetterMethodName();

    protected abstract String getOneMethodParamInString();

    protected abstract String getOneOrderCondition();

    protected abstract String getOneFilterCondition();

    protected abstract Object getOneFilterValue();

    @Test
    public abstract void eagerSaveTest() throws DBException;

    @Test
    public abstract void eagerRemoveTest() throws DBException;

    private static Method getSingleMethod(Object o, String methodName) {
        Class<?> c = o.getClass();
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            if (m.getName().startsWith(methodName)) {
                return m;
            }
        }
        return null;
    }

    protected List<T> createAndSaveBatches() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            T t = getCoreEntityClass().newInstance();
            Method m = getSingleMethod(t, getOneSetterMethodName());
            m.setAccessible(true);
            m.invoke(t, getOneMethodParamInString());
            list.add(getMainTestFacade().save(t));
        }
        Assert.assertEquals(MAX_BATCH_SIZE, list.size());
        return list;
    }

    @Test
    public void save() throws DBException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        Assert.assertTrue(getMainTestFacade().findAll().contains(t));
        // tear down
        getMainTestFacade().remove(t);
    }

    @Test
    public void find() throws DBException, InstantiationException, IllegalAccessException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        T _t = getMainTestFacade().find(getObjectId(t));
        Assert.assertEquals(t, _t);
        // tear down
        getMainTestFacade().remove(_t);
    }

    @Test
    public void findOneBaseOnField_exactMatchTrue() throws DBException, InstantiationException, IllegalAccessException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        T _t = getMainTestFacade().findOneBasedOnField(ID_FIELD, getObjectId(t), true);
        Assert.assertEquals(_t, t);
        // tear down
        getMainTestFacade().remove(_t);
    }

    @Test
    public void findOneBasedOnField_exactMatchFalse() throws DBException, InstantiationException, IllegalAccessException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        T _t = getMainTestFacade().findOneBasedOnField(ID_FIELD, getObjectId(t), false);
        Assert.assertEquals(_t, t);
        // tear down
        getMainTestFacade().remove(_t);
    }

    @Test
    public void findBasedOnField_exactMatchTrue() throws DBException, InstantiationException, IllegalAccessException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        List<T> _t = getMainTestFacade().findBasedOnField(ID_FIELD, getObjectId(t), true);
        Assert.assertTrue(_t.contains(t));
        // tear down
        getMainTestFacade().remove(_t);
    }

    @Test
    public void findBasedOnField_exactMatchFalse() throws DBException, InstantiationException, IllegalAccessException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        List<T> _t = getMainTestFacade().findBasedOnField(ID_FIELD, getObjectId(t), false);
        Assert.assertTrue(_t.contains(t));
        // tear down
        getMainTestFacade().remove(_t);
    }

    @Test
    public void findBasedOnFieldWithLimit_exactMatchTrue() throws InstantiationException, IllegalAccessException, DBException, IllegalArgumentException, InvocationTargetException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithLimit(getOneFieldName(), getOneMethodParamInString(), LIMIT, true);
        Assert.assertEquals(LIMIT, _list.size());
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithLimit_exactMatchFalse() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithLimit(getOneFieldName(), getOneMethodParamInString().substring(SUBSTRING_START, SUBSTRING_END), LIMIT, false);
        Assert.assertEquals(_list.size(), LIMIT);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithOrder_exactMatchTrue() throws InstantiationException, IllegalAccessException, DBException, IllegalArgumentException, InvocationTargetException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithOrder(getOneFieldName(), getOneMethodParamInString(), getOneOrderCondition(), true);
        Assert.assertEquals(_list.size(), MAX_BATCH_SIZE);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithOrder_exactMatchFalse() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithOrder(getOneFieldName(), getOneMethodParamInString().substring(SUBSTRING_START, SUBSTRING_END), getOneOrderCondition(), false);
        Assert.assertEquals(_list.size(), MAX_BATCH_SIZE);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithOrder_exactMatchFalse_withfullstring() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        // full string matches should work by default
        List<T> list = createAndSaveBatches();
        // since this is regex based. lets escape any meta characters.
        List<T> _list = getMainTestFacade().findBasedOnFieldWithOrder(getOneFieldName(), getMainTestFacade().escapeRegexMetaChars(getOneMethodParamInString()), getOneOrderCondition(), false);
        Assert.assertEquals(_list.size(), MAX_BATCH_SIZE);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithOrder_exactMatchFalse_withObjectId() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        // id field shoudl work.
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithOrder(ID_FIELD, getObjectId(list.get(0)), getOneOrderCondition(), false);
        Assert.assertEquals(_list.size(), 1);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithLimitAndOrder_exactMatchTrue() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithLimitAndOrder(getOneFieldName(), getOneMethodParamInString(), LIMIT, getOneOrderCondition(), true);
        Assert.assertEquals(_list.size(), LIMIT);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findBasedOnFieldWithLimitAndOrder_exactMatchFalse() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findBasedOnFieldWithLimitAndOrder(getOneFieldName(), getOneMethodParamInString().substring(SUBSTRING_START, SUBSTRING_END), LIMIT, getOneOrderCondition(), false);
        Assert.assertEquals(_list.size(), LIMIT);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findAll() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findAll();
        Assert.assertEquals(_list.size(), MAX_BATCH_SIZE);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findOrderedList() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, DBException, InstantiationException {
        List<T> list = createAndSaveBatches();
        List<T> _list = getMainTestFacade().findOrderedList(getOneOrderCondition());
        Assert.assertEquals(_list.size(), MAX_BATCH_SIZE);
        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findOrderRange() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();

        // lets say 7 items a page. that should give us 5 pages with the last
        // page having 2 items.
        List<T> _list = getMainTestFacade().findOrderedRange(5, 7, getOneOrderCondition(), getOneFilterCondition(), getOneFilterValue());
        Assert.assertEquals(2, _list.size());

        List<T> __list = getMainTestFacade().findOrderedRange(1, 7, getOneOrderCondition(), getOneFilterCondition(), getOneFilterValue());
        Assert.assertEquals(7, __list.size());

        // 0 is not a page number, so it should not have fetched anything.
        List<T> badPage = getMainTestFacade().findOrderedRange(0, 7, getOneOrderCondition(), getOneFilterCondition(), getOneFilterValue());
        Assert.assertEquals(0, badPage.size());

        // -1 is an even worse page number, so it should not have fetched
        // anything
        List<T> realyBadPage = getMainTestFacade().findOrderedRange(-1, 7, getOneOrderCondition(), getOneFilterCondition(), getOneFilterValue());
        Assert.assertEquals(0, realyBadPage.size());

        // 0 items on the page should give us 0 result also
        List<T> zeroItems = getMainTestFacade().findOrderedRange(1, 0, getOneOrderCondition(), getOneFilterCondition(), getOneFilterValue());
        Assert.assertEquals(0, zeroItems.size());

        // -1 items on the page should give us 0 result too
        List<T> badItems = getMainTestFacade().findOrderedRange(1, -1, getOneOrderCondition(), getOneFilterCondition(), getOneFilterValue());
        Assert.assertEquals(0, badItems.size());

        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void findRange() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();

        // 30 items, we can go from 0 to 29 items.
        List<T> _list = getMainTestFacade().findRange(0, 29);
        Assert.assertEquals(list, _list);
        Assert.assertEquals(list.size(), _list.size());

        // just get 10 items
        List<T> ten = getMainTestFacade().findRange(0, 9);
        Assert.assertEquals(10, ten.size());

        // go out of range by -1, return 0
        List<T> outOfRange = getMainTestFacade().findRange(-1, 9);
        Assert.assertEquals(0, outOfRange.size());

        // out of range completely
        List<T> _outOfRange = getMainTestFacade().findRange(50, 60);
        Assert.assertEquals(0, _outOfRange.size());

        // out of range by 10
        List<T> __outOfRange = getMainTestFacade().findRange(0, 39);
        Assert.assertEquals(MAX_BATCH_SIZE, __outOfRange.size());

        // from > end
        List<T> bad = getMainTestFacade().findRange(29, 0);
        Assert.assertEquals(0, bad.size());

        // just generally stupid
        List<T> _bad = getMainTestFacade().findRange(-29, 0);
        Assert.assertEquals(0, _bad.size());

        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void count() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        Assert.assertEquals(list.size(), getMainTestFacade().count());

        // tear down
        getMainTestFacade().remove(list);
    }

    @Test
    public void remove() throws DBException, InstantiationException, IllegalAccessException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        T _t = getMainTestFacade().find(getObjectId(t));
        Assert.assertEquals(t, _t);

        getMainTestFacade().remove(_t);
        T __t = getMainTestFacade().find(getObjectId(t));
        Assert.assertNull(__t);
    }

    @Test
    public void removeList() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DBException {
        List<T> list = createAndSaveBatches();
        getMainTestFacade().remove(list);
        List<T> _list = getMainTestFacade().findAll();
        Assert.assertEquals(0, _list.size());
    }

    @Test
    public void removeBasedOnFieldValue() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DBException {
        createAndSaveBatches();
        getMainTestFacade().remove(getOneFieldName(), getOneMethodParamInString());
        List<T> _list = getMainTestFacade().findAll();
        Assert.assertEquals(0, _list.size());
    }

    @Test
    public void dropCollection() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DBException {
        createAndSaveBatches();
        getMainTestFacade().dropCollection();
        List<T> _list = getMainTestFacade().findAll();
        Assert.assertEquals(0, _list.size());
    }

    @Test
    public void serializeToJSONAndPerformUpdate() throws DBException, InstantiationException, IllegalAccessException, JSONException, SecurityException, IllegalArgumentException, NoSuchFieldException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        Assert.assertTrue(getMainTestFacade().findAll().contains(t));
        // serialize to json
        JSONObject obj = getMainTestFacade().serializeEntityToJSON(t);
        Assert.assertNotNull(obj);
        final String id = obj.getJSONObject("_id").getString("$oid");
        Assert.assertEquals(t.getId().toString(), id);
        // serialize back to entity
        T check = getMainTestFacade().serializeJSONToEntity(obj, getCoreEntityClass());
        Assert.assertNotNull(check);
        T check2 = getMainTestFacade().save(check);
        Assert.assertEquals(1, getMainTestFacade().count());
        Assert.assertEquals(t, check2);
        // tear down
        getMainTestFacade().remove(t);
    }

    @Test
    public void serializeToJSONAndPerformInsert() throws DBException, SecurityException, IllegalArgumentException, JSONException, NoSuchFieldException, IllegalAccessException, InstantiationException {
        // build up
        T t = getMainTestFacade().save(getCoreEntityClass().newInstance());
        // test assertion
        Assert.assertTrue(getMainTestFacade().findAll().contains(t));
        // serialize to json
        JSONObject obj = getMainTestFacade().serializeEntityToJSON(t);
        Assert.assertNotNull(obj);
        final String id = obj.getJSONObject("_id").getString("$oid");
        Assert.assertEquals(t.getId().toString(), id);
        // serialize back to entity
        obj.remove("_id");
        T check = getMainTestFacade().serializeJSONToEntity(obj, getCoreEntityClass());
        Assert.assertNotNull(check);
        T check2 = getMainTestFacade().save(check);
        Assert.assertEquals(2, getMainTestFacade().count());
        // tear down
        getMainTestFacade().remove(t);
        getMainTestFacade().remove(check2);
    }
}
