package org.deer.hundred.hysteric.tugriks.hystrix.query.command;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixCollapserProperties;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.SerializationUtils;
import org.deer.hundred.hysteric.tugriks.dto.MatchableById;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class FindByIdRequestCollapser<T extends MatchableById<ID>, ID extends Serializable> extends
    HystrixCollapser<Iterable<T>, T, ID> {

  private static final Logger LOG = LoggerFactory.getLogger(FindByIdRequestCollapser.class);

  private final MongoTemplate template;
  private final ID idArgument;
  private final Class<T> entityClass;

  private final String collectionName;
  private final HystrixCommand.Setter commandSetter;

  public FindByIdRequestCollapser(MongoTemplate template, ID idArgument, Class<T> entityClass,
      String collectionName) {
    super(HystrixCollapser.Setter.withCollapserKey(
        HystrixCollapserKey.Factory.asKey(collectionName + "_findById_" + collectionName))
        .andScope(Scope.GLOBAL)
        .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter()
            .withMaxRequestsInBatch(50)));
    this.template = template;
    this.idArgument = idArgument;
    this.entityClass = entityClass;
    this.collectionName = collectionName;
    this.commandSetter = HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(collectionName))
        .andCommandKey(HystrixCommandKey.Factory.asKey("findById_" + collectionName))
        .andCommandPropertiesDefaults(
            HystrixCommandProperties.Setter()
                .withExecutionIsolationSemaphoreMaxConcurrentRequests(10000)
                .withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)
                .withExecutionTimeoutEnabled(false)
                .withRequestLogEnabled(false));
  }

  @Override
  public ID getRequestArgument() {
    return idArgument;
  }

  @Override
  protected HystrixCommand<Iterable<T>> createCommand(
      Collection<CollapsedRequest<T, ID>> collection) {
    return new HystrixCommand<Iterable<T>>(commandSetter) {
      @Override
      protected Iterable<T> run() {
        final Set<ID> ids = collection.stream()
            .map(CollapsedRequest::getArgument)
            .collect(Collectors.toSet());
        //LOG.debug("Executing command for ids {}", ids);
        if (collectionName != null) {
          return template
              .find(Query.query(Criteria.where("_id").in(ids))
                      .with(Sort.by(Direction.ASC, "_id")),
                  entityClass, collectionName);
        } else {
          return template.find(Query.query(Criteria.where("_id").in(ids))
              .with(Sort.by(Direction.ASC, "_id")), entityClass);
        }
      }
    };
  }

  @Override
  protected void mapResponseToRequests(Iterable<T> results,
      Collection<CollapsedRequest<T, ID>> requests) {

    final List<CollapsedRequest<T, ID>> requestQueue = new LinkedList<>(requests);

    for (T result : results) {
      CollapsedRequest<T, ID> matched = null;
      for (CollapsedRequest<T, ID> request : requestQueue) {
        if (result.match(request.getArgument())) {
          matched = request;
          request.setResponse(result);
          break;//we can break here as request cache makes sure there are not duplicate requests
        }
      }
      if (matched != null) {
        requestQueue.remove(matched);
      }
    }

    requestQueue.forEach(unsucessfull -> unsucessfull.setResponse(null));
  }
}
