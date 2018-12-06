package org.deer.hundred.hysteric.tugriks.config;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.deer.hundred.hysteric.tugriks.dto.MatchableById;
import org.deer.hundred.hysteric.tugriks.hystrix.FindByIdRequestCollapser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

public class HystrixMongoTemplate extends MongoTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(HystrixMongoTemplate.class);
  private final HystrixRequestContext globalRequestContext;

  public HystrixMongoTemplate(MongoDbFactory mongoDbFactory,
      MongoConverter mongoConverter, HystrixRequestContext globalRequestContext) {
    super(mongoDbFactory, mongoConverter);
    this.globalRequestContext = globalRequestContext;
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
