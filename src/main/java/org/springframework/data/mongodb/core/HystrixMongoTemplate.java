package org.springframework.data.mongodb.core;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bson.Document;
import org.deer.hundred.hysteric.tugriks.hystrix.query.command.CompositeQueryRequestCollapser;
import org.deer.hundred.hysteric.tugriks.hystrix.query.command.FindByIdRequestCollapser;
import org.deer.hundred.hysteric.tugriks.hystrix.query.command.factory.QueryCommandFactory;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.CompositeQueryCommandParam;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.factory.QueryCommandParamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;

public class HystrixMongoTemplate extends MongoTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(HystrixMongoTemplate.class);

  private final HystrixRequestContext globalRequestContext;
  private final QueryCommandParamFactory commandParamFactory;
  private final QueryCommandFactory commandFactory;

  public HystrixMongoTemplate(MongoDbFactory mongoDbFactory,
      MongoConverter mongoConverter,
      HystrixRequestContext globalRequestContext) {
    super(mongoDbFactory, mongoConverter);
    this.globalRequestContext = globalRequestContext;
    this.commandParamFactory = new QueryCommandParamFactory();
    this.commandFactory = new QueryCommandFactory(this);
  }

  @Override
  public <T> T findById(Object id, Class<T> entityClass,
      String collectionName) {
    synchronized (this) {
      if (!HystrixRequestContext.isCurrentThreadInitialized()) {
        HystrixRequestContext.setContextOnCurrentThread(globalRequestContext);
      }
    }
    try {
      return (T) new FindByIdRequestCollapser(this, (Serializable) id, entityClass, collectionName)
          .queue()
          .get(5000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  <S, T> List<T> doFind(String collectionName, Document query, Document fields,
      Class<S> sourceClass, Class<T> targetClass, CursorPreparer preparer) {

    //don't want to do this for projections for now
    if (sourceClass.equals(targetClass)) {

      final CompositeQueryCommandParam<S> param =
          commandParamFactory.createQueryCommandParam(sourceClass, query);

      //not a registered command, so fallback on default query method
      if (param != null) {
        final CompositeQueryRequestCollapser<S> command = commandFactory.createCommand(param);

        if (command != null) {
          try {
            return (List<T>) command.queue().get(20000, TimeUnit.MILLISECONDS);
          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
          }
        }
      }
    }
    return super.doFind(collectionName, query, fields, sourceClass, targetClass, preparer);
  }

  @Override
  public <T> T findById(Object id, Class<T> entityClass) {
    return this.findById(id, entityClass, null);
  }

  @Override
  public <T> List<T> findAll(Class<T> entityClass, String collectionName) {
    LOG.debug("findAll invoked");
    return super.findAll(entityClass, collectionName);
  }

  @Override
  public <T> List<T> findAll(Class<T> entityClass) {
    LOG.debug("findAll invoked");
    return super.findAll(entityClass);
  }
}
