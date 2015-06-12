package com.macyves.entities;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import com.macyves.annotations.Merge;

/**
 * Base, mergable entity object that can be transformed into json.
 * 
 * @author yves
 * 
 */
public class EntityObject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    protected ObjectId id;

    protected Date created;

    protected Date timestamp;

    public EntityObject() {
        final long now = System.currentTimeMillis();
        created = new Date(now);
        timestamp = new Date(now);
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getCreated() {
        return created != null ? new Date(created.getTime()) : null;
    }

    public void setCreated(Date created) {
        this.created = created != null ? new Date(created.getTime()) : null;
    }

    public Date getTimestamp() {
        return timestamp != null ? new Date(timestamp.getTime()) : null;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp != null ? new Date(timestamp.getTime()) : null;
    }

    public final boolean skipModifer(Field f) {
        final int modifier = f.getModifiers();
        if (Modifier.isFinal(modifier) || Modifier.isStatic(modifier)) {
            return true;
        } else {
            return false;
        }
    }

    public final boolean skipAnnotatedFields(Field f) {
        if (f.getAnnotations() == null) {
            return false;
        }

        for (Annotation a : f.getAnnotations()) {
            if ((a instanceof Reference) || (a instanceof Id)) {
                return true;
            }
        }
        return false;
    }

    public final boolean skipClasses(Object originalValue) {
        if ((originalValue instanceof List) || (originalValue instanceof Map) || (originalValue instanceof Date)) {
            return true;
        }
        return false;
    }

    public final boolean noMerge(Field f) {
        if (f.getAnnotations() == null) {
            return false;
        }

        for (Annotation a : f.getAnnotations()) {
            if (a instanceof Merge) {
                final Merge merge = (Merge) a;
                return merge.no_merge();
            }
        }
        return false;
    }

    public final boolean nullAllowed(Field f) {
        if (f.getAnnotations() == null) {
            return false;
        }

        for (Annotation a : f.getAnnotations()) {
            if (a instanceof Merge) {
                final Merge merge = (Merge) a;
                return merge.null_allowed();
            }
        }
        return false;
    }

    public final void merge(EntityObject changed) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        Field[] origin_fields = this.getClass().getDeclaredFields();
        for (Field f : origin_fields) {
            f.setAccessible(true);
            final String name = f.getName();
            Object originalValue = f.get(this);
            Field newField = changed.getClass().getDeclaredField(name);
            newField.setAccessible(true);
            Object newValue = newField.get(changed);

            if (!skipAnnotatedFields(f) && !skipClasses(originalValue) && !skipModifer(f)) {
                // we got a hit, check the merge rules for this field
                if (noMerge(f)) {
                    continue;
                }
                if (!nullAllowed(f) && newValue == null) {
                    continue;
                }
                f.set(this, newValue);
            }
        }
    }

    private String getEntityValue() {
        Class<?> clazz = this.getClass();
        Annotation[] annos = clazz.getAnnotations();
        for (Annotation a : annos) {
            if (a instanceof Entity) {
                return ((Entity) a).value();
            }
        }
        return "unspecified";
    }

    private JSONObject refMetaJSON(String refname) throws JSONException {
        // { "$ref" : "B", "$id" : { "$oid" : "5243dc4a94c006da73bcaa30" } }
        JSONObject objectid = new JSONObject();
        JSONObject ref = new JSONObject();
        ref.put("$ref", refname);
        objectid.put("no_merge", true);
        JSONObject oid = new JSONObject();
        oid.put("$oid", "string");
        ref.put("$id", oid);
        objectid.put("ObjectId", ref);
        return ref;
    }

    private final boolean isMergeAnnotated(Field f) {
        if (f.getAnnotations() == null) {
            return false;
        }

        for (Annotation a : f.getAnnotations()) {
            if (a instanceof Merge) {
                return true;
            }
        }
        return false;
    }

    private final boolean isNullAnnotated(Field f) {
        if (f.getAnnotations() == null) {
            return false;
        }

        for (Annotation a : f.getAnnotations()) {
            if (a instanceof Merge) {
                return true;
            }
        }
        return false;
    }

    private JSONObject getSchema(Field f, Object obj) throws IllegalArgumentException, IllegalAccessException, JSONException, InstantiationException {
        f.setAccessible(true);
        JSONObject field = new JSONObject();

        if (f.getType().getName().contains("com.macyves.enums")) {
            // enum
            field.put("type_enum", "string");
            List<?> constants = Arrays.asList(f.getType().getEnumConstants());
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < constants.size(); i++) {
                sb.append(constants.get(i).toString() + " ");
            }
            field.put("valid_strings", sb.toString());
        } else if (f.getType().getName().contains("com.macyves.entities")) {
            // reference
            EntityObject instance = (EntityObject) f.getType().newInstance();
            field.put("type", refMetaJSON(instance.getEntityValue()));
            field.put("no_merge", true);
        } else if (f.getType().getName().contains("com.macyves.dto")) {
            // embedded dto
            Object originalValue = f.get(obj);
            field.put(f.getType().getSimpleName(), getSchema(f.getType(), originalValue));
        } else if (f.getType().getName().contains("List") || f.getType().getName().contains("Map")) {
            // list of things
            JSONArray array = new JSONArray();
            JSONObject parameterised = new JSONObject();
            Class<?> clazz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
            Object originalValue = clazz.newInstance(); // this is still a list, so we should
            if (clazz.getName().contains("com.macyves.entities")) {
                EntityObject instance = (EntityObject) clazz.newInstance();
                parameterised = refMetaJSON(instance.getEntityValue());
                field.put("no_merge", true);
            } else {
                parameterised.put(clazz.getSimpleName(), getSchema(clazz, originalValue));
            }
            array.put(parameterised);
            field.put("type", array);
        } else {
            field.put("type", f.getType().getSimpleName().toLowerCase());
        }

        // determine if its mergable
        Object originalValue = null;
        if (obj != null) {
            originalValue = f.get(obj);
        }
        if (!skipAnnotatedFields(f) && !skipClasses(originalValue) && !skipModifer(f)) {
            // we got a hit, check the merge rules for this field
            if (isMergeAnnotated(f)) {
                field.put("no_merge", noMerge(f));
            }
        }

        if (isNullAnnotated(f)) {
            field.put("null_allowed", nullAllowed(f));
        }
        return field;
    }

    private JSONObject getSchema(Class<?> clazz, Object obj) throws JSONException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        Field[] origin_fields = clazz.getDeclaredFields();
        JSONObject schema = new JSONObject();
        for (Field f : origin_fields) {
            f.setAccessible(true);
            final String name = f.getName();
            if (!skipModifer(f)) {
                JSONObject field = getSchema(f, obj);
                schema.put(name, field);
            }
        }
        return schema;
    }

    public JSONObject getModelSchema() throws IllegalArgumentException, IllegalAccessException, JSONException, InstantiationException {
        JSONObject obj = new JSONObject();
        // get simpel class name
        // get entity value
        JSONObject schema = getSchema(this.getClass(), this);
        JSONObject oid = new JSONObject();
        oid.put("$oid", "string");
        oid.put("no_merge", true);
        JSONObject inner = new JSONObject();
        inner.put("no_merge", true);
        inner.put("$date", "long");
        schema.put("_id", oid);
        schema.put("timestamp", inner);
        schema.put("created", inner);
        obj.put(getEntityValue(), schema);
        return obj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityObject other = (EntityObject) obj;
        if (id != other.id && (id == null || !id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (id != null ? id.hashCode() : 0);
        return hash;
    }
}