package com.macyves.injection.guice;

import java.net.UnknownHostException;

import com.google.code.morphia.Morphia;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * AbstractDB module.
 * 
 * @author yves
 * 
 */
public class AbstractDBModule extends AbstractModule {

    private static String MONGO_HOST_DEFAULT = "127.0.0.1";
    private static String MONGO_PORT_DEFAULT = "27017";
    private static final String MONGO_DB_DEFAULT = "api_default";

    private static final String PROPERTY_MONGO_HOST = "com.macyves.mongo.host";
    private static final String PROPERTY_MONGO_PORT = "com.macyves.mongo.port";
    private static final String PROPERTY_MONGO_DB = "com.macyves.mongo.db";

    private static String mongo_db;
    private static String mongo_host;
    private static String mongo_port;

    static {
        mongo_db = System.getProperty(PROPERTY_MONGO_DB);
        mongo_host = System.getProperty(PROPERTY_MONGO_HOST);
        mongo_port = System.getProperty(PROPERTY_MONGO_PORT);
    }

    public String getMongoHostDefault() {
        return MONGO_HOST_DEFAULT;
    }

    public String getMongoPortDefault() {
        return MONGO_PORT_DEFAULT;
    }

    public String getMondoDBNameDefault() {
        return MONGO_DB_DEFAULT;
    }

    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(DBName.class)
                .toInstance(mongo_db != null ? mongo_db : getMondoDBNameDefault());
        bind(Mongo.class).toProvider(new Provider<Mongo>() {

            @Override
            public Mongo get() {
                try {
                    return new MongoClient(mongo_host != null ? mongo_host : getMongoHostDefault(),
                            mongo_port != null ? Integer.parseInt(mongo_port) : Integer.parseInt(getMongoPortDefault()));
                } catch (UnknownHostException ex) {
                    return null;
                } catch (MongoException ex) {
                    return null;
                }
            }

        }).in(Scopes.SINGLETON);

        bind(Morphia.class).toProvider(new Provider<Morphia>() {
            @Override
            public Morphia get() {
                return new Morphia();
            }
        }).in(Scopes.SINGLETON);
    }
}
