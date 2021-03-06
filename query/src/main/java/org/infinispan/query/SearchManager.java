package org.infinispan.query;

import org.apache.lucene.search.Query;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.EntityContext;
import org.hibernate.search.query.engine.spi.TimeoutExceptionFactory;
import org.infinispan.query.dsl.QueryFactory;

/**
 * The SearchManager is the entry point to create full text queries on top of a cache.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2011 Red Hat Inc.
 * @author Marko Luksa
 */
public interface SearchManager {

   QueryFactory getQueryFactory();

   /**
    * This is a simple method that will just return a {@link CacheQuery}, filtered according to a set of classes passed
    * in.  If no classes are passed in, it is assumed that no type filtering is performed and so all known types will
    * be searched.
    *
    * @param luceneQuery - {@link org.apache.lucene.search.Query}
    * @param classes - optionally only return results of type that matches this list of acceptable types
    * @return the CacheQuery object which can be used to iterate through results
    */
   CacheQuery getQuery(Query luceneQuery, Class<?>... classes);

   /**
    * Experimental.
    * Provides Hibernate Search DSL to build full text queries
    * @return
    */
   EntityContext buildQueryBuilderForClass(Class<?> entityType);

   /**
    * Experimental.
    * Access the SearchFactory
    */
   SearchFactory getSearchFactory();

   /**
    * Experimental!
    * Use it to try out the newly introduced distributed queries.
    *
    * @param luceneQuery
    * @param classes
    * @return
    */
   CacheQuery getClusteredQuery(Query luceneQuery, Class<?>... classes);

   /**
    * The MassIndexer can be used to rebuild the Lucene indexes from
    * the entries stored in Infinispan.
    * @return the MassIndexer component
    */
   MassIndexer getMassIndexer();

   /**
    * Registers a {@link org.infinispan.query.Transformer} for the supplied key class.
    * When storing keys in cache that are neither simple (String, int, ...) nor annotated with @Transformable,
    * Infinispan-Query will need to know what Transformer to use when transforming the keys to Strings. Clients
    * must specify what Transformer to use for a particular key class by registering it through this method.
    *
    * @param keyClass the key class for which the supplied transformerClass should be used
    * @param transformerClass the transformer class to use for the supplied key class
    */
   void registerKeyTransformer(Class<?> keyClass, Class<? extends Transformer> transformerClass);

   /**
    * Define the timeout exception factory to customize the exception thrown when the query timeout is exceeded.
    * @param timeoutExceptionFactory the timeout exception factory to use
    */
   void setTimeoutExceptionFactory(TimeoutExceptionFactory timeoutExceptionFactory);
}
