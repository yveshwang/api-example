package com.macyves.dao;

import com.google.code.morphia.query.Query;

/**
 * The most basic DAO in the api-example project must provide the following. These
 * basic functionalities are provided by default from all dbs. This interface
 * outlines the few methods we care about.
 * 
 * Q is query object type.
 * K is key object type.
 * W is some kind of write result object type.
 * E is the entity type that this dao caters for.
 * X is the query result object type.
 * 
 * @author yves
 */
public interface BaseDAOInterface<Q, K, W, E, X> {
    public Q createQuery();

    public K save(E entity);

    public W delete(E entity);

    public W deleteByQuery(Query<E> query);

    public X find();

    public long count();

    public X find(Query<E> q);

    public void dropCollection();
}
