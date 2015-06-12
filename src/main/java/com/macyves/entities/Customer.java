package com.macyves.entities;

import com.google.code.morphia.annotations.Entity;

/**
 * Entity.
 * 
 * @author yves
 */
@Entity(value = "customers", noClassnameStored = true)
public class Customer extends EntityObject {

    private static final long serialVersionUID = 1L;

    private String name = "";

    public Customer() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
