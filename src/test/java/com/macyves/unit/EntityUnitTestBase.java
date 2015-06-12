package com.macyves.unit;

import java.lang.reflect.Field;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import com.macyves.entities.EntityObject;

/**
 * 
 * Base object merging tests.
 * 
 * @author yves
 * 
 */
public abstract class EntityUnitTestBase<T extends EntityObject> {

    protected abstract Class<T> getCoreEntityClass();

    private static int unique_counter = 0;

    @Test
    public void printModelSchema() throws InstantiationException, IllegalAccessException, IllegalArgumentException, JSONException {
        T t1 = getCoreEntityClass().newInstance();
        System.out.println(t1.getModelSchema().toString());
    }

    @Test
    public void merge_successful() throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, NoSuchFieldException {
        T t1 = getCoreEntityClass().newInstance();
        T t2 = getCoreEntityClass().newInstance();
        fillStrings(t1, false);
        fillStrings(t2, false);
        t1.merge(t2);
        assertMerge(t1, t2, false);
    }

    @Test
    public void merge_null_fields() throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, NoSuchFieldException {
        T t1 = getCoreEntityClass().newInstance();
        T t2 = getCoreEntityClass().newInstance();
        fillStrings(t1, false);
        fillStrings(t2, true);
        t1.merge(t2);
        assertMerge(t1, t2, true);
    }

    private void fillStrings(T t, boolean setNull) {
        Field[] origin_fields = getCoreEntityClass().getDeclaredFields();
        for (Field f : origin_fields) {
            f.setAccessible(true);
            if (!t.skipModifer(f) && !t.skipAnnotatedFields(f)) {
                try {
                    if (setNull) {
                        f.set(t, null);
                    } else {
                        // force set the value.
                        f.set(t, String.valueOf(unique_counter));
                    }
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }
            unique_counter++;
        }
    }

    private void assertMerge(T t1, T t2, boolean t2containsnull) throws IllegalArgumentException, IllegalAccessException {
        Field[] origin_fields = getCoreEntityClass().getDeclaredFields();
        for (Field f : origin_fields) {
            f.setAccessible(true);
            Object originalValue = f.get(t1);
            Object newValue = f.get(t2);
            if (t1.skipAnnotatedFields(f) || t1.skipClasses(originalValue) || t1.skipModifer(f)) {
                continue;
            }
            if (t1.noMerge(f)) {
                Assert.assertNotSame(originalValue, newValue);
            } else {
                if (t1.nullAllowed(f) && t2containsnull) {
                    Assert.assertEquals(originalValue, newValue);
                } else if (t1.nullAllowed(f) && !t2containsnull) {
                    Assert.assertEquals(originalValue, newValue);
                } else if (!t1.nullAllowed(f) && t2containsnull) {
                    // enums will be null, and thus the same
                    if (f.getType() != String.class) {
                        // if its not a string, it might not be set probably. and thus the original value could pertain its default value or could also be null
                        // System.out.println( f.getName() + " not a string.");
                    } else {
                        // System.out.println(f.getType().getName()+", "+f.getDeclaringClass().getName()+", "+f.getName()+" original="+originalValue+" new="+newValue);
                        Assert.assertNotSame(originalValue, newValue);
                    }
                } else if (!t1.nullAllowed(f) && !t2containsnull) {
                    Assert.assertEquals(originalValue, newValue);
                }
            }
        }
    }
}
