package org.deer.hundred.hysteric.tugriks.hystrix.query.command;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixCollapserProperties;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.CompositeQueryCommandParam;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public abstract class CompositeQueryRequestCollapser<T>
    extends HystrixCollapser<Iterable<T>, List<T>, CompositeQueryCommandParam<T>> {

  private final CompositeQueryCommandParam<T> param;
  private final MongoTemplate template;
  private final Class<T> entityClass;
  private final HystrixCommand.Setter commandSetter;
  private final BiFunction<CompositeQueryCommandParam<T>, T, Boolean> resultMatcher;

  protected CompositeQueryRequestCollapser(CompositeQueryCommandParam<T> param,
      MongoTemplate template,
      Class<T> entityClass,
      BiFunction<CompositeQueryCommandParam<T>, T, Boolean> resultMatcher) {
    super(HystrixCollapser.Setter.withCollapserKey(
        HystrixCollapserKey.Factory.asKey(param.toKey()))
        .andScope(Scope.GLOBAL)
        .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter()
            .withTimerDelayInMilliseconds(100)
            .withMaxRequestsInBatch(20)));
    this.param = param;
    this.template = template;
    this.entityClass = entityClass;
    this.resultMatcher = resultMatcher;
    final String key = param.toKey();
    this.commandSetter = HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(key))
        .andCommandKey(HystrixCommandKey.Factory.asKey(key))
        .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.defaultSetter()
            .withQueueSizeRejectionThreshold(1000)
            .withCoreSize(50)
            .withMaximumSize(100))
        .andCommandPropertiesDefaults(
            HystrixCommandProperties.Setter()
                .withExecutionTimeoutEnabled(false)
                .withRequestLogEnabled(false));
  }

  @Override
  public CompositeQueryCommandParam<T> getRequestArgument() {
    return param;
  }

  @Override
  protected HystrixCommand<Iterable<T>> createCommand(
      Collection<CollapsedRequest<List<T>, CompositeQueryCommandParam<T>>> collection) {
    return new HystrixCommand<Iterable<T>>(commandSetter) {
      @Override
      protected Iterable<T> run() throws Exception {
        final Query query = new Query();

        collection.stream()
            .map(CollapsedRequest::getArgument)
            .map(CompositeQueryCommandParam::getQuery)
            .map(BasicQuery::new)
            .map(partialQuery -> new Criteria().is(partialQuery))
            .forEach(query::addCriteria);

        return template.find(query, entityClass);
      }
    };
  }

  @Override
  protected void mapResponseToRequests(Iterable<T> results,
      Collection<CollapsedRequest<List<T>, CompositeQueryCommandParam<T>>> requests) {
    final List<CollapsedRequest<List<T>, CompositeQueryCommandParam<T>>> requestQueue =
        new LinkedList<>(requests);

    requests.forEach(request ->
        request.setResponse(StreamSupport.stream(results.spliterator(), false)
            .filter(result -> resultMatcher.apply(request.getArgument(), result))
            .collect(Collectors.toList())));
  }
}
