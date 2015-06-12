package com.macyves.dao;

import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.macyves.entities.Customer;
import com.mongodb.WriteResult;

/**
 * Q, K, W, E, X in this case would be, if we are to use Morphia.
 * Q - Query<Customer>
 * Key - Key<Customer>
 * W - WriteResult
 * E - Customer
 * X - QueryResult<Customer>
 * 
 * @author yves
 * 
 */
public interface CustomerDAOInterface extends BaseDAOInterface<Query<Customer>, Key<Customer>, WriteResult, Customer, QueryResults<Customer>> {
}